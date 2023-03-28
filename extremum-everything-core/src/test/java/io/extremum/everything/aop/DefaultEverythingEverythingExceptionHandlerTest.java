package io.extremum.everything.aop;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.response.status.HeaderBasedResponseStatusCodeResolver;
import io.extremum.everything.controllers.EverythingExceptionHandlerTarget;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.exceptions.RequestDtoValidationException;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.descriptor.DescriptorNotReadyException;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.RequestHeaderMapMethodArgumentResolver;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import static io.extremum.common.response.status.HeaderBasedResponseStatusCodeResolver.ALWAYS_200;
import static io.extremum.common.response.status.HeaderBasedResponseStatusCodeResolver.STATUS_CODE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author rpuch
 */
class DefaultEverythingEverythingExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                 .standaloneSetup(new TestController())
                 .setControllerAdvice(new DefaultEverythingEverythingExceptionHandler(
                         new HeaderBasedResponseStatusCodeResolver()))
                 .setCustomArgumentResolvers(new RequestHeaderMapMethodArgumentResolver())
                 .build();
    }

    @Test
    void whenNothingIsThrown_thenOriginalResponseShouldBeReturned() throws Exception {
        MvcResult result = mockMvc.perform(get("/ok"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        JSONObject root = parseResponse(result);

        assertThat(root.getString("status"), is("ok"));
        assertThat(root.getInt("code"), is(200));
        assertThat(root.getString("result"), is("Success!"));
    }

    @Test
    void whenNothingIsThrownWithAlways200Header_thenOriginalResponseShouldBeReturnedAsResponseMessageCodeAttribute() throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/ok");

        assertThat(root.getString("status"), is("ok"));
        assertThat(root.getInt("code"), is(200));
        assertThat(root.getString("result"), is("Success!"));
    }

    @NotNull
    private JSONObject parseResponse(MvcResult result) throws UnsupportedEncodingException, JSONException {
        String content = result.getResponse().getContentAsString();
        return new JSONObject(content);
    }

    @Test
    void whenModelNotFoundExceptionIsThrown_thenProper404ResponseShouldBeReturned() throws Exception {
        MvcResult result = mockMvc.perform(get("/model-not-found"))
                .andExpect(status().isNotFound())
                .andReturn();
        JSONObject root = parseResponse(result);

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(404));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @Test
    void whenModelNotFoundExceptionIsThrownWithAlways200Header_thenProper404ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/model-not-found");

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(404));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @Test
    void whenEvrEvrExceptionIsThrown_thenFail500ResponseShouldBeReturned() throws Exception {
        MvcResult result = mockMvc.perform(get("/evr-evr-exception"))
                .andExpect(status().isInternalServerError())
                .andReturn();
        JSONObject root = parseResponse(result);

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(500));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @Test
    void whenEvrEvrExceptionIsThrownWithAlways200Header_thenFail500ResponseShouldBeReturnedAsResponseMessageCodeAttribute() throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/evr-evr-exception");

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(500));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @Test
    void whenRequestDtoValidationExceptionIsThrown_thenFail400ResponseShouldBeReturned() throws Exception {
        MvcResult result = mockMvc.perform(get("/validation-failure"))
                .andExpect(status().isBadRequest())
                .andReturn();
        JSONObject root = parseResponse(result);

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(400));
        assertThat(root.getString("result"), is("Unable to complete 'everything-everything' operation"));
    }

    @Test
    void whenRequestDtoValidationExceptionIsThrownWithAlways200Header_thenFail400ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/validation-failure");

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(400));
        assertThat(root.getString("result"), is("Unable to complete 'everything-everything' operation"));
    }

    @Test
    void whenDescriptorNotFoundExceptionIsThrown_thenProper404ResponseShouldBeReturned() throws Exception {
        MvcResult result = mockMvc.perform(get("/descriptor-not-found"))
                .andExpect(status().isNotFound())
                .andReturn();
        JSONObject root = parseResponse(result);

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(404));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @Test
    void whenDescriptorNotFoundExceptionIsThrownWithAlways200Header_thenProper404ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/descriptor-not-found");

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(404));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @Test
    void whenDescriptorNotReadyExceptionIsThrown_thenProper102ResponseShouldBeReturned() throws Exception {
        MvcResult result = mockMvc.perform(get("/descriptor-not-ready"))
                .andExpect(status().isProcessing())
                .andReturn();
        JSONObject root = parseResponse(result);

        assertThat(root.getString("status"), is("doing"));
        assertThat(root.getInt("code"), is(102));
        assertThat(root.getString("result"), is(nullValue()));
        JSONArray alerts = root.getJSONArray("alerts");
        assertThat(alerts.length(), is(1));
        JSONObject alert = alerts.getJSONObject(0);
        assertThat(alert.getString("level"), is("info"));
        assertThat(alert.getString("message"), is("Requested entity is still being processed, please retry later"));
    }

    @Test
    void whenDescriptorNotReadyExceptionIsThrownWithAlways200Header_thenProper102ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/descriptor-not-ready");

        assertThat(root.getString("status"), is("doing"));
        assertThat(root.getInt("code"), is(102));
        assertThat(root.getString("result"), is(nullValue()));
        JSONArray alerts = root.getJSONArray("alerts");
        assertThat(alerts.length(), is(1));
        JSONObject alert = alerts.getJSONObject(0);
        assertThat(alert.getString("level"), is("info"));
        assertThat(alert.getString("message"), is("Requested entity is still being processed, please retry later"));
    }

    @Test
    void whenExtremumAccessDeniedExceptionIsThrown_thenProper403ResponseShouldBeReturned() throws Exception {
        MvcResult result = mockMvc.perform(get("/extremum-access-denied-exception"))
                .andExpect(status().isForbidden())
                .andReturn();
        JSONObject root = parseResponse(result);

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(403));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @Test
    void whenExtremumAccessDeniedExceptionIsThrownWithAlways200Header_thenProper403ResponseShouldBeReturnedAsResponseMessageCodeAttribute()
            throws Exception {
        JSONObject root = getSuccessfullyAndParseResponse("/extremum-access-denied-exception");

        assertThat(root.getString("status"), is("fail"));
        assertThat(root.getInt("code"), is(403));
        assertThat(root.getString("result"), is(nullValue()));
    }

    @NotNull
    private JSONObject getSuccessfullyAndParseResponse(String uri) throws Exception {
        MvcResult result = mockMvc.perform(get(uri).header(STATUS_CODE, ALWAYS_200))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        return parseResponse(result);
    }

    @RestController
    @EverythingExceptionHandlerTarget
    private static class TestController {
        @RequestMapping("/ok")
        Response ok() {
            return Response.ok("Success!");
        }

        @RequestMapping("/model-not-found")
        Response modelNotFound() {
            throw new ModelNotFoundException("Did not find the model!");
        }

        @RequestMapping("/evr-evr-exception")
        Response evrEvrException() {
            throw new EverythingEverythingException("Everything-everything is lost!");
        }

        @RequestMapping("/validation-failure")
        Response validationFailure() {
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("someProperty");

            @SuppressWarnings("unchecked")
            ConstraintViolation<RequestDto> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Some failure");
            when(violation.getPropertyPath()).thenReturn(path);

            throw new RequestDtoValidationException(new TestRequestDto(), Collections.singleton(violation));
        }

        @RequestMapping("/descriptor-not-found")
        Response descriptorNotFound() {
            throw new DescriptorNotFoundException("Did not find anything", Descriptor.builder().externalId("descriptor-not-found").build());
        }

        @RequestMapping("/descriptor-not-ready")
        Response descriptorNotReady() {
            throw new DescriptorNotReadyException("Still not ready");
        }

        @RequestMapping("/extremum-access-denied-exception")
        Response everythingAccessDeniedException() {
            throw new ExtremumAccessDeniedException("Access denied");
        }
    }

    private static class TestRequestDto implements RequestDto {

    }
}