package io.extremum.sharedmodels.grpc.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Timestamp;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Pagination;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.proto.common.ProtoAlert;
import io.extremum.sharedmodels.proto.common.ProtoPagination;
import io.extremum.sharedmodels.proto.common.ProtoResponse;
import io.extremum.sharedmodels.proto.common.ProtoResponse.ProtoResponseStatusEnum;
import io.extremum.sharedmodels.proto.common.ProtoZonedTimestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class ResponseConverterTest {
    private ProtoZonedTimestampConverter zonedTimestampConverter = new ProtoZonedTimestampConverter();
    private ProtoAlertConverter alertConverter = new ProtoAlertConverter(zonedTimestampConverter);
    private ObjectMapper mapper = new ObjectMapper();
    private ProtoPaginationConverter paginationConverter = new ProtoPaginationConverter(zonedTimestampConverter);
    private ProtoResponseConverter responseConverter = new ProtoResponseConverter(
            zonedTimestampConverter, mapper, alertConverter, paginationConverter
    );

    @Test
    void testWithoutNulls_createFromProto() {
        ZonedDateTime fromZoned = ZonedDateTime.now();
        Instant from = fromZoned.toInstant();

        ProtoResponse proto = ProtoResponse.newBuilder()
                .setStatus(ProtoResponseStatusEnum.OK)
                .setCode(Int32Value.newBuilder().setValue(100).build())
                .setTimestamp(createProtoTimestamp(fromZoned, from))
                .setRequestId("qweqwe")
                .setLocale("ru_RU")
                .setResult(ByteString.EMPTY)
                .setPagination(ProtoPagination.newBuilder().setCount(100).build())
                .addAlerts(ProtoAlert.newBuilder().setLevelValue(100).build())
                .build();

        Response response = responseConverter.createFromProto(proto);

        assertNotNull(response.getStatus(), "status can't be null");
        assertNotNull(response.getCode(), "code can't be null");
        assertNotNull(response.getTimestamp(), "timestamp can't be null");
        assertNotNull(response.getRequestId(), "requestId can't be null");
        assertNotNull(response.getLocale(), "locale can't be null");
        assertNotNull(response.getAlerts(), "alerts can't be null");
        assertNotNull(response.getPagination(), "pagination can't be null");
    }

    @Test
    void testWithoutNulls_createProto() {
        Response response = Response.builder()
                .withDoingStatus()
                .withNowTimestamp()
                .withRequestId("ss")
                .withLocale("ru_RU")
                .withAlert(Alert.errorAlert("sadasd"))
                .withResult("sd")
                .withPagination(Pagination.builder().build())
                .build();

        ProtoResponse protoResponse = responseConverter.createProto(response);

        assertNotNull(protoResponse.getStatus());
        assertNotNull(protoResponse.getCode());
        assertNotNull(protoResponse.getTimestamp());
        assertNotNull(protoResponse.getRequestId());
        assertNotNull(protoResponse.getLocale());
        assertNotNull(protoResponse.getAlertsList());
        assertNotNull(protoResponse.getResult());
        assertNotNull(protoResponse.getPagination());
    }

    @Test
    void testWithNulls_createProto() {
        Response response = Response.builder()
                .withDoingStatus()
                .withRequestId(null)
                .withLocale(null)
                .withAlert(null)
                .withResult(null)
                .withPagination(null)
                .build();

        ProtoResponse protoResponse = responseConverter.createProto(response);

        assertNotNull(protoResponse.getStatus());
        assertNotNull(protoResponse.getCode());
        assertNotNull(protoResponse.getTimestamp());
        assertNotNull(protoResponse.getRequestId());
        assertNotNull(protoResponse.getLocale());
        assertNotNull(protoResponse.getAlertsList());
        assertNotNull(protoResponse.getResult());
        assertNotNull(protoResponse.getPagination());
    }

    @Test
    void testResponseResultConversion() throws JsonProcessingException {
        ProtoResponse dtoProto = ProtoResponse.newBuilder()
                .setStatus(ProtoResponseStatusEnum.DOING)
                .setResult(ByteString.copyFrom(mapper.writeValueAsBytes(new StringDto("test"))))
                .build();
        Response dtoResponse = responseConverter.createFromProto(dtoProto);

        String dtoJsonResult = mapper.writeValueAsString(dtoResponse.getResult());
        assertThat(dtoJsonResult, not(containsString("\\\"")));
        log.info(dtoJsonResult);

        ProtoResponse rawJsonProto = ProtoResponse.newBuilder()
                .setStatus(ProtoResponseStatusEnum.FAIL)
                .setResult(ByteString.copyFromUtf8("{\"name\":\"ben\",\"age\":23}"))
                .build();
        Response rawJsonResponse = responseConverter.createFromProto(rawJsonProto);

        String rawJsonResult = mapper.writeValueAsString(rawJsonResponse.getResult());
        assertThat(rawJsonResult, not(containsString("\\\"")));
        log.info(rawJsonResult);
    }

    private ProtoZonedTimestamp createProtoTimestamp(ZonedDateTime fromZoned, Instant from) {
        return ProtoZonedTimestamp.newBuilder()
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(from.getEpochSecond())
                        .setNanos(from.getNano())
                        .build())
                .setZoneId(fromZoned.getZone().toString())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class StringDto {
        private String content;
    }
}
