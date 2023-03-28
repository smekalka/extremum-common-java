package io.extremum.security.model.jwt;

import com.nimbusds.jose.shaded.json.JSONObject;
import io.extremum.sharedmodels.auth.User;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class OidcJwtTokenConverter implements Converter<Jwt, AuthenticationToken> {

    private final String appId;

    @Override
    @SneakyThrows
    public AuthenticationToken convert(Jwt jwt) {
        Map<String, Object> resourceAccessMap = jwt.getClaimAsMap("resource_access");
        if(resourceAccessMap==null){
            log.error("Invalid bearer token {}", jwt.getTokenValue());
            throw new InvalidBearerTokenException("Invalid token");
        }

        JSONObject resource_access = (JSONObject) jwt.getClaimAsMap("resource_access").get(appId);
        if(resource_access == null){
            log.error("Invalid bearer token {}", jwt.getTokenValue());
            throw new InvalidBearerTokenException("Invalid token");
        }

        List<?> roles = (List<?>) resource_access.get("roles");
        if(roles == null){
            log.error("Invalid bearer token {}", jwt.getTokenValue());
            throw new InvalidBearerTokenException("Invalid token");
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Object role : roles) {
            authorities.add(new SimpleGrantedAuthority((String) role));
        }

        return new AuthenticationToken(
                authorities,
                new User(
                        getUsernameFromToken(jwt),
                        getEmailFromToken(jwt),
                        authorities
                                .stream()
                                .map(SimpleGrantedAuthority::toString)
                                .collect(Collectors.toList())
                ), jwt
        );
    }

    private String getUsernameFromToken(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }

    private String getEmailFromToken(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }
}