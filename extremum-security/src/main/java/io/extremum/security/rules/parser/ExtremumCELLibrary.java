package io.extremum.security.rules.parser;

import com.google.api.expr.v1alpha1.Expr;
import com.google.api.expr.v1alpha1.ParsedExpr;
import com.google.api.expr.v1alpha1.Type;
import io.extremum.security.rules.model.AllowScope;
import io.extremum.security.rules.model.SecurityRule;
import io.extremum.security.rules.parser.jackson.JacksonRegistry;
import io.extremum.security.rules.provider.InvalidSecurityRulesException;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import lombok.SneakyThrows;
import org.apache.commons.lang3.function.TriFunction;
import org.projectnessie.cel.CEL;
import org.projectnessie.cel.Env;
import org.projectnessie.cel.EnvOption;
import org.projectnessie.cel.Library;
import org.projectnessie.cel.ProgramOption;
import org.projectnessie.cel.checker.Decls;
import org.projectnessie.cel.common.operators.Operator;
import org.projectnessie.cel.common.types.BoolT;
import org.projectnessie.cel.common.types.Err;
import org.projectnessie.cel.common.types.Overloads;
import org.projectnessie.cel.interpreter.functions.BinaryOp;
import org.projectnessie.cel.interpreter.functions.FunctionOp;
import org.projectnessie.cel.interpreter.functions.Overload;
import org.projectnessie.cel.parser.Macro;
import org.projectnessie.cel.parser.MacroExpander;
import org.projectnessie.cel.tools.Script;
import org.projectnessie.cel.tools.ScriptCreateException;
import org.projectnessie.cel.tools.ScriptHost;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class ExtremumCELLibrary implements Library {
    public static final String EXPR = "expr";
    public static final String PATH = "path";
    public static final String ALLOW = "allow";
    public static final String CONTAINS = "contains";
    public static final String LIKE = "like";
    public static final String EQ = "eq";
    public static final String ANY = "any";
    public static final String OR = "or";
    Type ruleType = Decls.newObjectType("SecurityRule");

    @Override
    public List<EnvOption> getCompileOptions() {
        return Arrays.asList(
                EnvOption.declarations(
                        Decls.newVar("db", ruleType),
                        Decls.newVar("object", Decls.Dyn),
                        Decls.newVar("context", Decls.Dyn),

                        Decls.newVar("storage", Decls.Dyn),
                        Decls.newVar("functions", Decls.Dyn),
                        Decls.newVar("signals", Decls.Dyn),
                        Decls.newVar("messaging", Decls.Dyn),
                        Decls.newVar("management", Decls.Dyn),
                        Decls.newVar("iam", Decls.Dyn),
                        Decls.newFunction(PATH, Decls.newInstanceOverload(PATH, Arrays.asList(ruleType, Decls.String), ruleType)),
                        Decls.newFunction(ALLOW, Decls.newInstanceOverload(ALLOW, Arrays.asList(ruleType, Decls.newListType(Decls.String)), ruleType)),
                        Decls.newFunction(EXPR, Decls.newInstanceOverload(EXPR, Arrays.asList(ruleType, Decls.String), ruleType)),
                        Decls.newFunction(CONTAINS, Decls.newInstanceOverload(CONTAINS, Arrays.asList(Decls.newListType(Decls.String), Decls.String), Decls.Bool)),
                        Decls.newFunction(LIKE, Decls.newInstanceOverload(LIKE, Arrays.asList(Decls.newListType(Decls.String), Decls.String), Decls.Bool)),
                        Decls.newFunction(EQ, Decls.newInstanceOverload(EQ, Arrays.asList(Decls.newListType(Decls.String), Decls.String), Decls.Bool)),
                        Decls.newFunction(ANY, Decls.newInstanceOverload(ANY, Arrays.asList( Decls.Dyn, Decls.newListType(Decls.Dyn)), Decls.Bool)),
                        Decls.newFunction(OR, Decls.newOverload(Overloads.LogicalOr, asList(Decls.Bool, Decls.Bool), Decls.Bool))
                ),
                EnvOption.macros(getMacrosList())
        );
    }

    private ArrayList<Macro> getMacrosList() {
        ArrayList<Macro> macros = new ArrayList<>(Macro.AllMacros);
        macros.add(new Macro("when", true, false, 1, getExprMacroExpander()));

        return macros;
    }

    private MacroExpander getExprMacroExpander() {
        return (exprHelper, expr, args) -> exprHelper
                .receiverCall(
                        EXPR,
                        expr,
                        Collections.singletonList(exprHelper.literalString(getExpressionStringFromArg(args.get(0))))
                );
    }

    private String getExpressionStringFromArg(Expr arg) {
        return CEL.astToString(CEL.parsedExprToAst(ParsedExpr.newBuilder().setExpr(arg).build()));
    }

    @Override
    public List<ProgramOption> getProgramOptions() {
        return Arrays.asList(
                ProgramOption.functions(Overload.binary(PATH, callInSecurityRuleStrOutSecurityRule(this::setPath))),
                ProgramOption.functions(Overload.binary(ALLOW, callInSecurityRuleStrArrOutSecurityRule(this::setAllow))),
                ProgramOption.functions(Overload.binary(EXPR, callInSecurityRuleStrOutSecurityRule(this::setWhen))),
                ProgramOption.functions(Overload.binary(CONTAINS, callInStrArrayStrOutBoolean(ExtremumCELLibrary::contains))),
                ProgramOption.functions(Overload.function(LIKE, callIn3StrOutBoolean(ExtremumCELLibrary::like))),
                ProgramOption.functions(Overload.function(EQ, callIn3StrOutBoolean(ExtremumCELLibrary::eq))),
                ProgramOption.functions(Overload.binary(ANY, callInObjectOutBoolean(ExtremumCELLibrary::any)))
        );
    }

    private static Boolean any(Object o, Object[] objects) {

        return true;
    }

    public static final ScriptHost scriptHost = ScriptHost.newBuilder().registry(JacksonRegistry.newRegistry()).build();

    @SneakyThrows
    private SecurityRule setWhen(SecurityRule securityRule, String expression) {
        securityRule.setExpression(expression);
        securityRule.setAccessChecker(buildScript(expression));
        securityRule.setAst(Env.newCustomEnv(this.getCompileOptions().toArray(new EnvOption[0])).parse(expression).getAst());

        return securityRule;
    }

    private SecurityRule setAllow(SecurityRule rule, String[] allowArray) {
        rule.setAllow((Arrays.stream(allowArray).map(AllowScope::byStringValueIgnoringCase).collect(Collectors.toList())));
        return rule;
    }

    private SecurityRule setPath(SecurityRule rule, String path) {
        rule.setPath(path);
        rule.setPattern(FileSystems.getDefault().getPathMatcher("glob:" + path.toLowerCase()));

        return rule;
    }

    private static BinaryOp callInSecurityRuleStrOutSecurityRule(BiFunction<SecurityRule, String, SecurityRule> func) {
        return (lhs, rhs) -> {
            try {
                return (func.apply((SecurityRule) (lhs), (String) rhs.value()));
            } catch (RuntimeException e) {
                return Err.newErr(e, "%s", e.getMessage());
            }
        };
    }

    private static BinaryOp callInSecurityRuleStrArrOutSecurityRule(BiFunction<SecurityRule, String[], SecurityRule> func) {
        return (lhs, rhs) -> {
            try {
                Object[] values = (Object[]) rhs.value();
                return (func.apply((SecurityRule) (lhs), Arrays.copyOf(values, values.length, String[].class)));
            } catch (RuntimeException e) {
                return Err.newErr(e, "%s", e.getMessage());
            }
        };
    }


    private static BinaryOp callInStrArrayStrOutBoolean(BiFunction<String[], String, Boolean> func) {
        return (lhs, rhs) -> {
            try {
                Object[] objects = (Object[]) lhs.value();
                Boolean apply = func.apply(Arrays.copyOf(objects, objects.length, String[].class), (String) rhs.value());
                if (apply) {
                    return BoolT.True;
                }

                return BoolT.False;
            } catch (RuntimeException var4) {
                return Err.newErr(var4, "%s", var4.getMessage());
            }
        };
    }

    private static BinaryOp callInObjectOutBoolean(BiFunction<Object, Object[], Boolean> func) {
        return (lhs, rhs) -> {
            try {
                Boolean apply = func.apply(lhs.value(), (Object[]) rhs.value());
                if (apply) {
                    return BoolT.True;
                }

                return BoolT.False;
            } catch (RuntimeException var4) {
                return Err.newErr(var4, "%s", var4.getMessage());
            }
        };
    }


    private static FunctionOp callIn3StrOutBoolean(TriFunction<StringOrMultilingual, String, String, Boolean> func) {

        return (vals) -> {
            try {
                Boolean apply = func.apply((StringOrMultilingual) vals[0].value(), (String) vals[1].value(), (String) vals[2].value());
                if (apply) {
                    return BoolT.True;
                }

                return BoolT.False;
            } catch (RuntimeException var4) {
                return Err.newErr(var4, "%s", var4.getMessage());
            }
        };
    }


    private static boolean contains(String[] array, String string) {
        for (String s : array) {
            if (s.equals(string)) {
                return true;
            }
        }

        return false;
    }

    public static boolean eq(Object target, String string, String locale) {
        if(target instanceof StringOrMultilingual) {
            if (((StringOrMultilingual)target).isTextOnly()) {
                return ((StringOrMultilingual)target).getText().equals(string);
            }
            if (!Objects.equals(locale, "*")) {
                return ((StringOrMultilingual)target).getMultilingualContent().getMap().get(MultilingualLanguage.fromString(locale)).equals(string);
            }

            return ((StringOrMultilingual)target).getMultilingualContent().getMap().containsValue(string);
        }

        return target.equals(string);
    }


    public static boolean like(Object target, String string, String locale) {
        String regexFromGlob = createRegexFromGlob(string);
        if(target instanceof StringOrMultilingual) {
            if (((StringOrMultilingual)target).isTextOnly()) {
                return ((StringOrMultilingual)target).getText().matches(regexFromGlob);
            }
            if (!Objects.equals(locale, "*")) {
                return ((StringOrMultilingual)target).getMultilingualContent().getMap().get(MultilingualLanguage.fromString(locale)).matches(regexFromGlob);
            }

            return ((StringOrMultilingual)target).getMultilingualContent().getMap().values().stream().anyMatch(s -> s.matches(regexFromGlob));
        }

        return ((String)target).matches(regexFromGlob);
    }

    public static String createRegexFromGlob(String glob) {
        StringBuilder out = new StringBuilder("^");
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*':
                    out.append(".*");
                    break;
                case '?':
                    out.append('.');
                    break;
                case '.':
                    out.append("\\.");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                default:
                    out.append(c);
            }
        }
        out.append('$');
        return out.toString();
    }


    public Script buildScript(String expression) {
        try {
            return scriptHost.buildScript(expression).withLibraries(this).build();
        } catch (ScriptCreateException e) {
            throw new InvalidSecurityRulesException("Unable to apply security rules", e);
        }
    }
}