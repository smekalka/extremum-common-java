package io.extremum.everything.regular.config;

import io.extremum.security.config.JwtTokenConfiguration;
import io.extremum.security.config.SecurityConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import({SecurityConfiguration.class, JwtTokenConfiguration.class})
@AutoConfigureAfter(EverythingEverythingConfiguration.class)
public class EverythingSecurityConfiguration {
}