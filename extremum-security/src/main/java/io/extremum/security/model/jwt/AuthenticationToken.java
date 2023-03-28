package io.extremum.security.model.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.security.Principal;
import java.util.Collection;


public class AuthenticationToken extends AbstractAuthenticationToken {

    private final Principal principal;
    private final Jwt token;

    public AuthenticationToken(Collection<? extends GrantedAuthority> authorities, Principal principal, Jwt token) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
