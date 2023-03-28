package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Int32Value;
import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.proto.common.ProtoTimeframe;
import io.extremum.sharedmodels.spacetime.TimeFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoTimeFrameConverter {
    private final ProtoZonedTimestampConverter zonedTimestampConverter;

    public TimeFrame createFromProto(ProtoTimeframe proto) {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setStart(zonedTimestampConverter.createFromProto(proto.getStart()));
        timeFrame.setEnd(zonedTimestampConverter.createFromProto(proto.getEnd()));

        IntegerOrString duration;
        if (proto.hasIntValue()) {
            duration = new IntegerOrString(proto.getIntValue().getValue());
        } else {
            duration = new IntegerOrString(proto.getStringValue());
        }
        timeFrame.setDuration(duration);
        return timeFrame;
    }

    public ProtoTimeframe createProto(TimeFrame timeFrame) {
        ProtoTimeframe.Builder timeframeBuilder = ProtoTimeframe.newBuilder()
                .setStart(zonedTimestampConverter.createProto(timeFrame.getStart()))
                .setEnd(zonedTimestampConverter.createProto(timeFrame.getEnd()));

        IntegerOrString duration = timeFrame.getDuration();
        if (duration.isInteger()) {
            timeframeBuilder.setIntValue(Int32Value.newBuilder().setValue(duration.getIntegerValue()).build());
        } else if (duration.isString()) {
            timeframeBuilder.setStringValue(duration.getStringValue());
        }
        return timeframeBuilder.build();
    }
}
