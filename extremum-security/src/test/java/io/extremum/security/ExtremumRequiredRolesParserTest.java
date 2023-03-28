package io.extremum.security;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class ExtremumRequiredRolesParserTest {
    private ExtremumRequiredRolesParser parser = new ExtremumRequiredRolesParser();

    @Test
    void givenIndividualAccessIsSpecified_whenGettingAccess_thenIndividualAccessShouldBeReturned() {
        ExtremumRequiredRoles extremumRequiredRoles = Individual.class.getAnnotation(ExtremumRequiredRoles.class);
        ExtremumRequiredRolesConfig config = parser.parse(extremumRequiredRoles);

        assertThat(config.rolesForGet(), equalTo(new String[]{"GET"}));
        assertThat(config.rolesForPatch(), equalTo(new String[]{"PATCH"}));
        assertThat(config.rolesForRemove(), equalTo(new String[]{"REMOVE"}));
        assertThat(config.rolesForWatch(), equalTo(new String[]{"WATCH"}));
    }

    @Test
    void givenDefaultAccessIsSpecified_whenGettingAccess_thenDefaultAccessShouldBeReturned() {
        ExtremumRequiredRoles extremumRequiredRoles = WithDefault.class.getAnnotation(ExtremumRequiredRoles.class);
        ExtremumRequiredRolesConfig config = parser.parse(extremumRequiredRoles);

        assertThat(config.rolesForGet(), equalTo(new String[]{"DEFAULT"}));
        assertThat(config.rolesForPatch(), equalTo(new String[]{"DEFAULT"}));
        assertThat(config.rolesForRemove(), equalTo(new String[]{"DEFAULT"}));
        assertThat(config.rolesForWatch(), equalTo(new String[]{"DEFAULT"}));
    }

    @ExtremumRequiredRoles(
            defaultAccess = "DEFAULT",
            get = "GET",
            patch = "PATCH",
            remove = "REMOVE",
            watch = "WATCH"
    )
    private static class Individual {
    }

    @ExtremumRequiredRoles(
            defaultAccess = "DEFAULT"
    )
    private static class WithDefault {
    }
}