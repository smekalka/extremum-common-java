package io.extremum.common.exceptions.end2end.springhandler;

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

@SpringBootTest(classes = {CommonConfiguration.class, SpringHandlerExceptionTestConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class SpringHandlerExtremumExceptionHandlerEndToEndTest extends TestWithServices {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenCommonExceptionIsThrown_itShouldBeHandledWithSpringExceptionHandler() throws Exception {
        mockMvc.perform(
                get("/exceptions/common-exception").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(405))
                .andExpect(content().string(assertError("SpringExceptionHandler: Common exception message", 405)))
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
