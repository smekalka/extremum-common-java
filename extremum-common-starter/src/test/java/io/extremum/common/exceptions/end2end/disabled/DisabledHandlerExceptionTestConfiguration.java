package io.extremum.common.exceptions.end2end.disabled;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan({"io.extremum.common.exceptions.end2end.fixture",
        "io.extremum.common.exceptions.end2end.disabled.handler"})
public class DisabledHandlerExceptionTestConfiguration {
}
