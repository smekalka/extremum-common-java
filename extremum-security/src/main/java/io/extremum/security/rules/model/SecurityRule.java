package io.extremum.security.rules.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.NotImplementedException;
import org.projectnessie.cel.Ast;
import org.projectnessie.cel.common.types.BoolT;
import org.projectnessie.cel.common.types.TypeT;
import org.projectnessie.cel.common.types.ref.Type;
import org.projectnessie.cel.common.types.ref.Val;
import org.projectnessie.cel.tools.Script;

import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@ToString
public class SecurityRule implements Val {

    private ServiceType service;
    private String path;
    private PathMatcher pattern;
    private int numRestrictedPathSegments;
    private String expression;
    private List<AllowScope> allow;
    private Script accessChecker;
    private Ast ast;

    public static SecurityRule DB() {
        return new SecurityRule(ServiceType.DB);
    }

    public static SecurityRule STORAGE() {
        return new SecurityRule(ServiceType.STORAGE);
    }

    public static SecurityRule IAM() {
        return new SecurityRule(ServiceType.IAM);
    }

    public static SecurityRule MANAGEMENT() {
        return new SecurityRule(ServiceType.MANAGEMENT);
    }

    public static SecurityRule FUNCTIONS() {
        return new SecurityRule(ServiceType.FUNCTIONS);
    }

    public static SecurityRule MESSAGING() {
        return new SecurityRule(ServiceType.MESSAGING);
    }

    public static SecurityRule SIGNALS() {
        return new SecurityRule(ServiceType.SIGNALS);
    }


    private SecurityRule(ServiceType service) {
        this.service = service;
    }

    @Override
    public <T> T convertToNative(Class<T> typeDesc) {
        return (T) this;
    }

    @Override
    public Val convertToType(Type typeValue) {
        throw new NotImplementedException();
    }

    @Override
    public Val equal(Val other) {
        if (other.type() == this.type() && this.equals(other)) {
            return BoolT.True;
        }

        return BoolT.False;
    }

    private final Type type = TypeT.newObjectTypeValue(this.getClass().getName());

    @Override
    public Type type() {
        return type;
    }

    @Override
    public Object value() {
        return this;
    }

    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
    public long intValue() {
        return 0;
    }

    public void setAccessChecker(Script accessChecker) {
        this.accessChecker = accessChecker;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityRule that = (SecurityRule) o;
        return numRestrictedPathSegments == that.numRestrictedPathSegments && service == that.service && path.equals(that.path) && pattern.equals(that.pattern) && expression.equals(that.expression) && allow.equals(that.allow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, path, pattern, numRestrictedPathSegments, expression, allow);
    }
}