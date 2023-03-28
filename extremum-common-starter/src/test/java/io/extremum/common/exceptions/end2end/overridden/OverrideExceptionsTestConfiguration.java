package io.extremum.common.exceptions.end2end.overridden;

import io.extremum.common.exceptions.CommonException;
import io.extremum.common.exceptions.ExceptionResponse;
import io.extremum.common.exceptions.ExtremumExceptionHandlers;
import io.extremum.common.exceptions.handler.annotation.ExtremumExceptionHandler;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
@EnableAutoConfiguration
@ComponentScan("io.extremum.common.exceptions.end2end.fixture")
public class OverrideExceptionsTestConfiguration {

    @Bean
    public ExtremumExceptionHandlers newHandlers() {
        return new UserHandlers();
    }

    public static class UserHandlers implements ExtremumExceptionHandlers {

        @ExtremumExceptionHandler
        public ExceptionResponse handle(CommonException e) {
            return ExceptionResponse.withMessageAndHttpStatus(
                    "Overridden common exception handler, message: " + e.getMessage(),
                    HttpStatus.resolve(e.getCode()));
        }

    }

}
