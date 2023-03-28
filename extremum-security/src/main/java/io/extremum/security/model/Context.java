package io.extremum.security.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

@AllArgsConstructor
@Getter
public class Context {
    private final Principal user;
}
