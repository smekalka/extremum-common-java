package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Int32Value;
import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.content.Media;
import io.extremum.sharedmodels.content.MediaType;
import io.extremum.sharedmodels.proto.common.ProtoMedia;
import io.extremum.sharedmodels.proto.common.ProtoMedia.ProtoMediaType;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MediaConverterTest {
    private final ProtoMediaConverter mediaConverter = new ProtoMediaConverter();

    @Test
    void testSuccessfully_CreateFromProto() {
        ProtoMedia thumbnail = ProtoMedia.newBuilder()
                .setUrl("thumbnail_url")
                .build();

        ProtoMedia fullProtoMedia = ProtoMedia.newBuilder()
                .setUrl("test_url")
                .setType(ProtoMediaType.APPLICATION)
                .setIntegerDuration(Int32Value.newBuilder().setValue(1212).build())
                .setDepth(Int32Value.newBuilder().setValue(1).build())
                .setHeight(Int32Value.newBuilder().setValue(2).build())
                .setWidth(Int32Value.newBuilder().setValue(3).build())
                .addThumbnails(thumbnail)
                .build();
        Media fullMedia = mediaConverter.createFromProto(fullProtoMedia);

        assertAll(
                () -> assertThat(fullMedia.getUrl(), is("test_url")),
                () -> assertThat(fullMedia.getType(), is(MediaType.APPLICATION)),
                () -> assertThat(fullMedia.getDepth(), is(1)),
                () -> assertThat(fullMedia.getHeight(), is(2)),
                () -> assertThat(fullMedia.getWidth(), is(3)),
                () -> assertThat(fullMedia.getDuration().isInteger(), is(true)),
                () -> assertThat(fullMedia.getThumbnails().size(), is(1)),
                () -> assertThat(fullMedia.getThumbnails().get(0).getUrl(), equalTo("thumbnail_url"))
        );
    }

    @Test
    void testSuccessfulEmpty_CreateFromProto() {
        ProtoMedia defaultProtoMedia = ProtoMedia.getDefaultInstance();
        Media defaultMedia = mediaConverter.createFromProto(defaultProtoMedia);

        assertThat(defaultMedia, nullValue());
    }

    @Test
    void testSuccessful_CreateProto() {
        Media thumbnail = new Media();
        thumbnail.setType(MediaType.AUDIO);

        Media fullMedia = new Media();
        fullMedia.setDuration(new IntegerOrString(4));
        fullMedia.setType(MediaType.IMAGE);
        fullMedia.setUrl("test_url");
        fullMedia.setHeight(1);
        fullMedia.setWidth(2);
        fullMedia.setDepth(3);
        fullMedia.setThumbnails(Collections.singletonList(thumbnail));

        ProtoMedia fullProto = mediaConverter.createProto(fullMedia);
        assertAll(
                () -> assertThat(fullProto.getType(), is(ProtoMediaType.IMAGE)),
                () -> assertThat(fullProto.getDepth().getValue(), is(3)),
                () -> assertThat(fullProto.getWidth().getValue(), is(2)),
                () -> assertThat(fullProto.getHeight().getValue(), is(1)),
                () -> assertThat(fullProto.getUrl(), is("test_url")),
                () -> assertThat(fullProto.getThumbnailsList().size(), is(1)),
                () -> assertThat(fullProto.getThumbnails(0).getType(), is(ProtoMediaType.AUDIO)),
                () -> assertThat(fullProto.hasIntegerDuration(), is(true)),
                () -> assertThat(fullProto.getIntegerDuration().getValue(), is(4))
        );
    }

    @Test
    void testBroke_CreateProto() {
        ProtoMedia nullProto = mediaConverter.createProto(null);
        assertThat(nullProto, is(ProtoMedia.getDefaultInstance()));

        Media mediaWithNullFields = new Media();
        mediaWithNullFields.setDuration(null);
        mediaWithNullFields.setType(null);
        mediaWithNullFields.setDepth(null);
        mediaWithNullFields.setWidth(null);
        mediaWithNullFields.setHeight(null);
        mediaWithNullFields.setThumbnails(null);
        mediaWithNullFields.setUrl(null);

        ProtoMedia protoWithNullFields = mediaConverter.createProto(mediaWithNullFields);
        assertAll(
                () -> assertThat(protoWithNullFields.getType(), is(ProtoMediaType.UNKNOWN)),
                () -> assertThat(protoWithNullFields.hasDepth(), is(false)),
                () -> assertThat(protoWithNullFields.hasHeight(), is(false)),
                () -> assertThat(protoWithNullFields.hasWidth(), is(false)),
                () -> assertThat(protoWithNullFields.getUrl(), is("")),
                () -> assertThat(protoWithNullFields.getThumbnailsList().size(), is(0))
        );
    }
}