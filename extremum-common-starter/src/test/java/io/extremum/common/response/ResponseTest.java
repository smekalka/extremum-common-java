package io.extremum.common.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.mapper.MockedMapperDependencies;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseStatusEnum;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rpuch
 */
class ResponseTest {
    private ObjectMapper mapper = new SystemJsonObjectMapper(new MockedMapperDependencies());

    @Test
    void builderCreatesResponse() {
        String rqid = UUID.randomUUID().toString();

        Response response = createDefaultResponseBuilder(rqid).build();

        Assertions.assertEquals(response.getStatus(), ResponseStatusEnum.OK);
        assertEquals(response.getCode().intValue(), 200);
        assertEquals(response.getLocale(), "en_US");
        assertEquals(response.getRequestId(), rqid);
    }

    @Test
    void builderCreatesRightStatuses() {
        Response responseOk = createDefaultResponseBuilder(UUID.randomUUID().toString())
                .withOkStatus().build();
        Assertions.assertEquals(ResponseStatusEnum.OK, responseOk.getStatus());

        Response responseDoing = createDefaultResponseBuilder(UUID.randomUUID().toString())
                .withDoingStatus().build();
        assertEquals(ResponseStatusEnum.DOING, responseDoing.getStatus());

        Response responseWarning = createDefaultResponseBuilder(UUID.randomUUID().toString())
                .withWarningStatus(200).build();
        assertEquals(ResponseStatusEnum.WARNING, responseWarning.getStatus());

        Response responseFail = createDefaultResponseBuilder(UUID.randomUUID().toString())
                .withFailStatus(400).build();
        assertEquals(ResponseStatusEnum.FAIL, responseFail.getStatus());
    }

    @Test
    void toJsonOnlyRequiredFieldsPresented() throws JSONException {
        String rqid = UUID.randomUUID().toString();
        Response response = createDefaultResponseBuilder(rqid).build();
        String jsonString = toJson(response);

        JSONObject jsonObject = new JSONObject(jsonString);

        List<String> requiredFields = Arrays.asList("status", "code", "timestamp", "rqid", "locale");
        for (String fieldName : requiredFields) {
            assertTrue(jsonObject.has(fieldName), fieldName + " must be presented in json");
        }

        List<String> notExistsFields = Arrays.asList("alerts", "result");
        for (String fieldName : notExistsFields) {
            assertFalse(jsonObject.has(fieldName), fieldName + " must not be presented in json");
        }
    }

    @Test
    void toJsonAllFieldsPresented() throws JSONException {
        String rqid = UUID.randomUUID().toString();

        Response response = createDefaultResponseBuilder(rqid)
                .withAlert(Alert.infoAlert("message", "code"))
                .withResult("result")
                .build();

        String jsonString = toJson(response);

        JSONObject jsonObject = new JSONObject(jsonString);

        List<String> requiredFields = Arrays.asList("status", "code", "timestamp", "rqid", "locale", "alerts", "result");
        for (String fieldName : requiredFields) {
            assertTrue(jsonObject.has(fieldName), fieldName + " must be presented in json");
        }
    }

    @Test
    void toJsonContainsCorrectValues() throws JSONException, ParseException {
        String rqid = UUID.randomUUID().toString();
        Response response = createDefaultResponseBuilder(rqid).build();
        String jsonString = toJson(response);

        JSONObject jsonObject = new JSONObject(jsonString);

        Assertions.assertTrue(ResponseStatusEnum.OK.toString().equalsIgnoreCase(jsonObject.getString("status")));
        assertEquals(200, jsonObject.getInt("code"));
        assertEquals(rqid, jsonObject.getString("rqid"));
        assertEquals("en_US", jsonObject.getString("locale"));
        new SimpleDateFormat("uuuu-MM-dd'T'HH:mm:ss.SSSSSSX").parse(jsonObject.getString("timestamp"));
    }
    
    private Response.Builder createDefaultResponseBuilder(String rqid) {
        return Response.builder()
                .withOkStatus()
                .withLocale("en_US")
                .withRequestId(rqid);
    }

    private String toJson(Response response) {
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenCopyingWithARequestId_thenRequestIdShouldChangeButEverythingElseRemainTheSame() {
        Response response = Response.builder()
                .withFailStatus(400)
                .withRequestId("old-id")
                .withResult("the result")
                .build();

        Response copy = response.withRequestId("new-id");

        assertThat(copy.getStatus(), is(ResponseStatusEnum.FAIL));
        assertThat(copy.getCode(), is(400));
        assertThat(copy.getResult(), is("the result"));

        assertThat(copy.getRequestId(), is("new-id"));
    }
}