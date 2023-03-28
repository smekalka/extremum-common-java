package io.extremum.common.exceptions.handler;

import io.extremum.common.exceptions.CommonException;
import io.extremum.common.exceptions.DefaultExtremumExceptionHandlers;
import io.extremum.common.exceptions.handler.annotation.AnnotationBasedExtremumExceptionResolver;
import io.extremum.sharedmodels.dto.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefaultExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new DefaultExceptionHandler(new AnnotationBasedExtremumExceptionResolver(singletonList(
                        new DefaultExtremumExceptionHandlers()))))
                .build();
    }

    @Test
    public void whenNothingIsThrown_thenOriginalResponseShouldBeReturned() throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/ok");

        assertThat(root.getString("status"), is("ok"));
        assertThat(root.getInt("code"), is(200));
        assertThat(root.getString("result"), is("Success!"));
    }

    @Test
    public void whenCommonExceptionIsThrown_thenCodeAndMessageFromCommonExceptionShouldBeReturnedInBody() throws Exception {
        MvcResult result = mockMvc.perform(get("/common-exception"))
                .andExpect(status().isForbidden())
                .andReturn();

        JSONObject root = parseResponse(result);
        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(403));

        JSONArray alerts = root.getJSONArray("alerts");
        JSONObject alert = alerts.getJSONObject(0);
        assertThat(alert.getString("level"), is("error"));
        assertThat(alert.getString("code"), is("403"));
        assertThat(alert.getString("message"), is("Common exception message"));
    }

    @Test
    public void whenCommonExceptionWithIncorrectCodeIsThrown_thenDefaultCodeAndMessageFromCommonExceptionShouldBeReturnedInBody() throws Exception {
        MvcResult result = mockMvc.perform(get("/common-exception-incorrect-code"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        JSONObject root = parseResponse(result);
        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(500));

        JSONArray alerts = root.getJSONArray("alerts");
        JSONObject alert = alerts.getJSONObject(0);
        assertThat(alert.getString("level"), is("error"));
        assertThat(alert.getString("code"), is("500"));
        assertThat(alert.getString("message"), is("Common exception incorrect code message"));
    }

    private JSONObject getSuccessfullyAndParseResponse(String uri) throws Exception {
        MvcResult result = mockMvc.perform(get(uri))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        return parseResponse(result);
    }

    private JSONObject parseResponse(MvcResult result) throws UnsupportedEncodingException, JSONException {
        String content = result.getResponse().getContentAsString();
        return new JSONObject(content);
    }

    @RestController
    private static class TestController {
        @RequestMapping("/ok")
        Response ok() {
            return Response.ok("Success!");
        }

        @RequestMapping("/common-exception")
        Response commonException() {
            throw new CommonException("Common exception message", 403);
        }

        @RequestMapping("/common-exception-incorrect-code")
        Response commonExceptionIncorrectCode() {
            throw new CommonException("Common exception incorrect code message", 0);
        }
    }

}