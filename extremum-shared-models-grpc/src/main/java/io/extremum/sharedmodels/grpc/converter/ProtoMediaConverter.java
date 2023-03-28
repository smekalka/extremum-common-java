package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Int32Value;
import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.content.Media;
import io.extremum.sharedmodels.content.MediaType;
import io.extremum.sharedmodels.proto.common.ProtoMedia;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProtoMediaConverter {

    public Media createFromProto(ProtoMedia proto) {
        if (proto.equals(ProtoMedia.getDefaultInstance())) {
            return null;
        }
        Media media = new Media();

        media.setUrl(proto.getUrl().equals("") ? null : proto.getUrl());
        media.setType(MediaType.fromString(proto.getType().name()));

        IntegerOrString duration = null;
        if (proto.hasIntegerDuration()) {
            duration = new IntegerOrString(proto.getIntegerDuration().getValue());
        } else if (!proto.getStringDuration().equals("")) {
            duration = new IntegerOrString(proto.getStringDuration());
        }
        media.setDuration(duration);

        List<Media> convertedThumbnails = proto.getThumbnailsList()
                .stream()
                .map(this::createFromProto)
                .collect(Collectors.toList());
        media.setThumbnails(convertedThumbnails);

        if (proto.hasDepth()) {
            media.setDepth(proto.getDepth().getValue());
        } else {
            media.setDepth(null);
        }
        if (proto.hasHeight()) {
            media.setHeight(proto.getHeight().getValue());
        } else {
            media.setHeight(null);
        }
        if (proto.hasWidth()) {
            media.setWidth(proto.getWidth().getValue());
        } else {
            media.setWidth(null);
        }

        return media;
    }

    public ProtoMedia createProto(Media media) {
        if (media == null) {
            return ProtoMedia.getDefaultInstance();
        }
        Integer depth = media.getDepth();
        Integer height = media.getHeight();
        Integer width = media.getWidth();

        ProtoMedia.Builder builder = ProtoMedia.newBuilder()
                .setUrl(media.getUrl() == null ? "" : media.getUrl());

        if (depth != null) {
            builder.setDepth(Int32Value.newBuilder().setValue(depth).build());
        }
        if (height != null) {
            builder.setHeight(Int32Value.newBuilder().setValue(height).build());
        }
        if (width != null) {
            builder.setWidth(Int32Value.newBuilder().setValue(width).build());
        }

        MediaType type = media.getType();
        if (type != null) {
            builder.setType(ProtoMedia.ProtoMediaType.valueOf(type.name()));
        }

        List<Media> thumbnails = media.getThumbnails();
        if (thumbnails != null) {
            List<ProtoMedia> protoThumbnails = thumbnails.stream()
                    .map(this::createProto)
                    .collect(Collectors.toList());
            builder.addAllThumbnails(protoThumbnails);
        }

        IntegerOrString duration = media.getDuration();
        if (duration != null) {
            if (duration.isInteger()) {
                builder.setIntegerDuration(Int32Value.newBuilder()
                        .setValue(duration.getIntegerValue())
                        .build());
            } else {
                builder.setStringDuration(duration.getStringValue());
            }
        }
        return builder.build();
    }
}
