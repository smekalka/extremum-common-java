package io.extremum.sharedmodels.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@Getter
public class User implements Principal {
    private final String name;
    private final String email;
    private final List<String> roles;
}