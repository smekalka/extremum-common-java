package io.extremum.common.exceptions.end2end;

import io.extremum.common.test.TestWithServices;
import io.extremum.starter.CommonConfiguration;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {CommonConfiguration.class, ExceptionsTestConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class ExtremumExceptionHandlerEndToEndTest extends TestWithServices {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenCommonExceptionIsThrown_itShouldBeHandledWithDefaultExtremumExceptionHandler() throws Exception {
        mockMvc.perform(
                get("/exceptions/common-exception").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403))
                .andExpect(content().string(assertError("Common exception message", 403)))
                .andReturn();
    }

    private Matcher<String> assertError(String message, int code) {
        return allOf(
                containsString("\"message\":\"" + message + "\""),
                containsString("\"code\":" + code),
                containsString("\"status\":\"fail\"")
        );
    }

}
