package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.dto.Alert;
import io.extremum.sharedmodels.proto.common.ProtoAlert;
import io.extremum.sharedmodels.proto.common.ProtoAlert.ProtoAlertLevelEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProtoAlertConverter {
    private final ProtoZonedTimestampConverter timestampConverter;

    public ProtoAlert createProto(Alert alert) {
        if (alert == null) {
            return ProtoAlert.getDefaultInstance();
        }
        ProtoAlert.Builder alertBuilder = ProtoAlert.newBuilder()
                .setCode(Optional.ofNullable(alert.getCode()).orElse(""))
                .setElement(Optional.ofNullable(alert.getElement()).orElse(""))
                .setMessage(Optional.ofNullable(alert.getMessage()).orElse(""))
                .setLink(Optional.ofNullable(alert.getLink()).orElse(""))
                .setTraceId(Optional.ofNullable(alert.getTraceId()).orElse(""));

        if (alert.getTimestamp() != null) {
            alertBuilder.setTimestamp(timestampConverter.createProto(alert.getTimestamp()));
        }
        if (alert.getLevel() != null) {
            alertBuilder.setLevel(ProtoAlertLevelEnum.valueOf(alert.getLevel().name()));
        }
        return alertBuilder.build();
    }

    public Alert createFromProto(ProtoAlert proto) {
        if (proto.equals(ProtoAlert.getDefaultInstance()) ||
                proto.getLevel().equals(ProtoAlertLevelEnum.UNKNOWN)) {
            return null;
        }
        Alert.Builder alertBuilder = Alert.builder();
        switch (proto.getLevel()) {
            case INFO:
                alertBuilder.withInfoLevel();
                break;
            case WARNING:
                alertBuilder.withWarningLevel();
                break;
            case ERROR:
                alertBuilder.withErrorLevel();
                break;
        }
        return alertBuilder
                .withTimestamp(timestampConverter.createFromProto(proto.getTimestamp()))
                .withCode(proto.getCode().equals("") ? null : proto.getCode())
                .withElement(proto.getElement().equals("") ? null : proto.getElement())
                .withMessage(proto.getMessage().equals("") ? null : proto.getMessage())
                .withLink(proto.getLink())
                .withTraceId(proto.getTraceId())
                .build();
    }
}
