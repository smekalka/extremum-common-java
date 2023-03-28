package io.extremum.sharedmodels.grpc.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int32Value;
import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.grpc.exception.ProtoConversionException;
import io.extremum.sharedmodels.proto.common.ProtoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProtoResponseConverter {
    private final ProtoZonedTimestampConverter zonedTimestampConverter;
    private final ObjectMapper objectMapper;
    private final ProtoAlertConverter alertConverter;
    private final ProtoPaginationConverter paginationConverter;

    public Response createFromProto(ProtoResponse protoResponse) {
        Response.Builder builder = Response.builder();

        byte[] resultBytes = protoResponse.getResult().toByteArray();
        if (resultBytes.length != 0) {
            try {
                Object result = objectMapper.readValue(resultBytes, Object.class);
                builder.withResult(result);
            } catch (IOException e) {
                throw new ProtoConversionException(e);
            }
        }

        builder.withRequestId(protoResponse.getRequestId().equals("") ? null : protoResponse.getRequestId());
        builder.withLocale(protoResponse.getLocale().equals("") ? null : protoResponse.getLocale());
        setResponseStatus(builder, protoResponse);

        if (protoResponse.getAlertsCount() > 0) {
            List<Alert> alerts = protoResponse.getAlertsList().stream()
                    .map(alertConverter::createFromProto)
                    .collect(Collectors.toList());
            builder.withAlerts(alerts);
        }

        if (protoResponse.hasPagination()) {
            builder.withPagination(paginationConverter.createFromProto(protoResponse.getPagination()));
        }

        return builder.build();
    }

    public ProtoResponse createProto(Response response) {
        ProtoResponse.Builder builder = ProtoResponse.newBuilder()
                .setStatus(ProtoResponse.ProtoResponseStatusEnum.valueOf(response.getStatus().name()))
                .setCode(Int32Value.newBuilder().setValue(response.getCode()).build())
                .setTimestamp(zonedTimestampConverter.createProto(response.getTimestamp()))
                .setRequestId(Optional.ofNullable(response.getRequestId()).orElse(""))
                .setLocale(Optional.ofNullable(response.getLocale()).orElse(""));

        Optional.ofNullable(response.getAlerts())
                .map(alerts -> alerts.stream()
                        .map(alertConverter::createProto)
                        .collect(Collectors.toList()))
                .map(builder::addAllAlerts);

        if (response.getResult() != null) {
            try {
                byte[] result = objectMapper.writeValueAsBytes(response.getResult());
                builder.setResult(ByteString.copyFrom(result));
            } catch (JsonProcessingException e) {
                log.warn("Unable to map data {} to bytes: ", response.getResult(), e);
            }
        }

        if (response.getPagination() != null) {
            builder.setPagination(paginationConverter.createProto(response.getPagination()));
        }

        return builder.build();
    }

    private void setResponseStatus(Response.Builder builder, ProtoResponse protoResponse) {
        ProtoResponse.ProtoResponseStatusEnum status = protoResponse.getStatus();
        switch (status) {
            case OK:
                builder.withOkStatus();
                break;
            case FAIL:
                builder.withFailStatus(protoResponse.getCode().getValue());
                break;
            case DOING:
                builder.withDoingStatus();
                break;
            case WARNING:
                builder.withWarningStatus(protoResponse.getCode().getValue());
                break;
        }
    }
}
