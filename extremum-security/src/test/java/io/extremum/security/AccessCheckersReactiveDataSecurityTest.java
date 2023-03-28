package io.extremum.security;

import io.extremum.common.annotation.ModelName;
import io.extremum.mongo.model.MongoCommonModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class AccessCheckersReactiveDataSecurityTest {
    private static final String ROLE_PRIVILEGED = "ROLE_PRIVILEGED";

    private AccessCheckersReactiveDataSecurity security;

    @Mock
    private ReactiveRoleChecker roleChecker;
    @Mock
    private ReactivePrincipalSource principalSource;

    @BeforeEach
    void createSecurityInstance() {
        security = new AccessCheckersReactiveDataSecurity(Arrays.asList(new AllowEverything(), new DenyEverything(),
                new CheckerForModelWithCheckerAndWithNoDataSecurityAnnotation(),
                new CheckerForModelWithRoleChecksInContext(),
                new CheckerForModelWithPrincipalChecksInContext()),
                roleChecker, principalSource);
    }

    @Test
    void givenCheckerExistsAndAllows_whenCheckGet_thenAccessShouldBeAllowed() {
        security.checkGetAllowed(new ModelWithAllowingChecker()).block();
    }

    @Test
    void givenCheckerExistsAndDenies_whenCheckGet_thenAccessShouldBeDenied() {
        try {
            security.checkGetAllowed(new ModelWithDenyingChecker()).block();
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenNoCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckGet_thenAccessShouldBeAllowed() {
        security.checkGetAllowed(new ModelWithoutCheckerButWithNoDataSecurityAnnotation()).block();
    }

    @Test
    void givenNoCheckerExistAndNotAnnotatedWithNoDataSecurity_whenCheckGet_thenAnExceptionShouldBeThrown() {
        try {
            security.checkGetAllowed(new ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation()).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("No DataAccessChecker was found and no @NoDataSecurity annotation exists" +
                    " on 'ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenBothCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckGet_thenAnExceptionShouldBeThrown() {
        try {
            security.checkGetAllowed(new ModelWithCheckerAndWithNoDataSecurityAnnotation()).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Both DataAccessChecker was found and @NoDataSecurity annotation exists" +
                    " on 'ModelWithCheckerAndWithNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenModelIsNull_whenCheckGet_thenShouldBeAllowed() {
        security.checkGetAllowed(null).block();
    }

    @Test
    void givenCheckerExistsAndAllows_whenCheckPatch_thenAccessShouldBeAllowed() {
        security.checkPatchAllowed(new ModelWithAllowingChecker()).block();
    }

    @Test
    void givenCheckerExistsAndDenies_whenCheckPatch_thenAccessShouldBeDenied() {
        try {
            security.checkPatchAllowed(new ModelWithDenyingChecker()).block();
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenNoCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckPatch_thenAccessShouldBeAllowed() {
        security.checkPatchAllowed(new ModelWithoutCheckerButWithNoDataSecurityAnnotation()).block();
    }

    @Test
    void givenNoCheckerExistAndNotAnnotatedWithNoDataSecurity_whenCheckPatch_thenAnExceptionShouldBeThrown() {
        try {
            security.checkPatchAllowed(new ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation()).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("No DataAccessChecker was found and no @NoDataSecurity annotation exists" +
                    " on 'ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenBothCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckPatch_thenAnExceptionShouldBeThrown() {
        try {
            security.checkPatchAllowed(new ModelWithCheckerAndWithNoDataSecurityAnnotation()).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Both DataAccessChecker was found and @NoDataSecurity annotation exists" +
                    " on 'ModelWithCheckerAndWithNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenModelIsNull_whenCheckPatch_thenShouldBeAllowed() {
        security.checkPatchAllowed(null).block();
    }

    @Test
    void givenCheckerExistsAndAllows_whenCheckRemove_thenAccessShouldBeAllowed() {
        security.checkRemovalAllowed(new ModelWithAllowingChecker()).block();
    }

    @Test
    void givenCheckerExistsAndDenies_whenCheckRemove_thenAccessShouldBeDenied() {
        try {
            security.checkRemovalAllowed(new ModelWithDenyingChecker()).block();
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenNoCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckRemove_thenAccessShouldBeAllowed() {
        security.checkRemovalAllowed(new ModelWithoutCheckerButWithNoDataSecurityAnnotation()).block();
    }

    @Test
    void givenNoCheckerExistAndNotAnnotatedWithNoDataSecurity_whenCheckRemove_thenAnExceptionShouldBeThrown() {
        try {
            security.checkRemovalAllowed(new ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation()).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("No DataAccessChecker was found and no @NoDataSecurity annotation exists" +
                    " on 'ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenBothCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckRemove_thenAnExceptionShouldBeThrown() {
        try {
            security.checkRemovalAllowed(new ModelWithCheckerAndWithNoDataSecurityAnnotation()).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Both DataAccessChecker was found and @NoDataSecurity annotation exists" +
                    " on 'ModelWithCheckerAndWithNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenModelIsNull_whenCheckRemove_thenShouldBeAllowed() {
        security.checkRemovalAllowed(null).block();
    }

    @Test
    void givenRoleCheckerAllowsPrivilegedRole_whenCheckerChecksForPrivilegedRoleViaContext_thenItShouldBeAllowed() {
        when(roleChecker.currentUserHasOneRoleOf(ROLE_PRIVILEGED)).thenReturn(Mono.just(true));

        security.checkGetAllowed(new ModelWithRoleChecksInContext()).block();
    }

    @Test
    void givenRoleCheckerDeniesPrivilegedRole_whenCheckerChecksForPrivilegedRoleViaContext_thenItShouldBeDenied() {
        when(roleChecker.currentUserHasOneRoleOf(ROLE_PRIVILEGED)).thenReturn(Mono.just(false));

        assertThrows(ExtremumAccessDeniedException.class,
                () -> security.checkGetAllowed(new ModelWithRoleChecksInContext()).block());
    }

    @Test
    void givenModelOwnerIsAlexAndCurrentPrincipalIsAlex_whenCheckerChecksOwnerMatchesPrincipal_thenItShouldBeAllowed() {
        when(principalSource.getPrincipal()).thenReturn(Mono.just(()->"Alex"));

        security.checkGetAllowed(new ModelWithPrincipalChecksInContext("Alex")).block();
    }

    @Test
    void givenModelOwnerIsAlexAndCurrentPrincipalIsBen_whenCheckerChecksOwnerMatchesPrincipal_thenItShouldBeDenied() {
        when(principalSource.getPrincipal()).thenReturn(Mono.just(()->"Ben"));

        assertThrows(ExtremumAccessDeniedException.class,
                () -> security.checkGetAllowed(new ModelWithPrincipalChecksInContext("Alex")).block());
    }

    @Test
    void givenCheckerExistsAndAllows_whenCheckWatch_thenAccessShouldBeAllowed() {
        security.checkWatchAllowed(new ModelWithAllowingChecker()).block();
    }

    @Test
    void givenCheckerExistsAndDenies_whenCheckWatch_thenAccessShouldBeDenied() {
        try {
            security.checkWatchAllowed(new ModelWithDenyingChecker()).block();
            fail("An exception should be thrown");
        } catch (ExtremumAccessDeniedException e) {
            assertThat(e.getMessage(), is("Access denied"));
        }
    }

    @Test
    void givenNoCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckWatch_thenAccessShouldBeAllowed() {
        security.checkWatchAllowed(new ModelWithoutCheckerButWithNoDataSecurityAnnotation()).block();
    }

    @Test
    void givenNoCheckerExistAndNotAnnotatedWithNoDataSecurity_whenCheckWatch_thenAnExceptionShouldBeThrown() {
        try {
            security.checkWatchAllowed(new ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation()).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("No DataAccessChecker was found and no @NoDataSecurity annotation exists" +
                    " on 'ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenBothCheckerExistAndAnnotatedWithNoDataSecurity_whenCheckWatch_thenAnExceptionShouldBeThrown() {
        try {
            security.checkWatchAllowed(new ModelWithCheckerAndWithNoDataSecurityAnnotation()).block();
            fail("An exception should be thrown");
        } catch (ExtremumSecurityException e) {
            assertThat(e.getMessage(), is("Both DataAccessChecker was found and @NoDataSecurity annotation exists" +
                    " on 'ModelWithCheckerAndWithNoDataSecurityAnnotation'"));
        }
    }

    @Test
    void givenModelIsNull_whenCheckWatch_thenShouldBeAllowed() {
        security.checkWatchAllowed(null).block();
    }

    private static abstract class BaseModel extends MongoCommonModel {
    }

    @ModelName("ModelWithAllowingChecker")
    private static class ModelWithAllowingChecker extends BaseModel {
    }

    @ModelName("ModelWithDenyingChecker")
    private static class ModelWithDenyingChecker extends BaseModel {
    }

    @ModelName("ModelWithoutCheckerButWithNoDataSecurityAnnotation")
    @NoDataSecurity
    private static class ModelWithoutCheckerButWithNoDataSecurityAnnotation extends BaseModel {
    }

    @ModelName("ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation")
    private static class ModelWithoutCheckerAndWithoutNoDataSecurityAnnotation extends BaseModel {
    }

    @ModelName("ModelWithCheckerAndWithNoDataSecurityAnnotation")
    @NoDataSecurity
    private static class ModelWithCheckerAndWithNoDataSecurityAnnotation extends BaseModel {
    }

    @ModelName("ModelWithRoleChecksInContext")
    private static class ModelWithRoleChecksInContext extends BaseModel {
    }

    @ModelName("ModelWithPrincipalChecksInContext")
    private static class ModelWithPrincipalChecksInContext extends BaseModel {
        private final String owner;

        private ModelWithPrincipalChecksInContext(String owner) {
            this.owner = owner;
        }
    }

    private static class AllowEverything extends ConstantReactiveChecker<ModelWithAllowingChecker> {
        AllowEverything() {
            super(true);
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithAllowingChecker";
        }
    }

    private static class DenyEverything extends ConstantReactiveChecker<ModelWithDenyingChecker> {
        DenyEverything() {
            super(false);
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithDenyingChecker";
        }
    }

    private static class CheckerForModelWithCheckerAndWithNoDataSecurityAnnotation
            extends ConstantReactiveChecker<ModelWithCheckerAndWithNoDataSecurityAnnotation> {

        CheckerForModelWithCheckerAndWithNoDataSecurityAnnotation() {
            super(true);
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithCheckerAndWithNoDataSecurityAnnotation";
        }
    }

    private static class CheckerForModelWithRoleChecksInContext
            extends SamePolicyReactiveChecker<ModelWithRoleChecksInContext> {

        @Override
        Mono<Boolean> allowed(ModelWithRoleChecksInContext model, ReactiveCheckerContext context) {
            return context.currentUserHasOneOf(ROLE_PRIVILEGED);
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithRoleChecksInContext";
        }
    }

    private static class CheckerForModelWithPrincipalChecksInContext
            extends SamePolicyReactiveChecker<ModelWithPrincipalChecksInContext> {

        @Override
        Mono<Boolean> allowed(ModelWithPrincipalChecksInContext model, ReactiveCheckerContext context) {
            return context.getCurrentPrincipal()
                    .map(principal -> Objects.equals(model.owner, principal.getName()));
        }

        @Override
        public String getSupportedModel() {
            return "ModelWithPrincipalChecksInContext";
        }
    }
}