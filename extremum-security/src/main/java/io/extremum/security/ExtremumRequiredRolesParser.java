package io.extremum.security;

/**
 * @author rpuch
 */
public class ExtremumRequiredRolesParser {
    public ExtremumRequiredRolesConfig parse(ExtremumRequiredRoles extremumRequiredRoles) {
        return new ExtremumRequiredRolesConfig(
                extremumRequiredRoles.defaultAccess(),
                extremumRequiredRoles.get(),
                extremumRequiredRoles.patch(),
                extremumRequiredRoles.remove(),
                extremumRequiredRoles.watch()
        );
    }
}
