package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.basic.Multilingual;
import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.content.Display.Type;
import io.extremum.sharedmodels.proto.common.ProtoDisplay;
import io.extremum.sharedmodels.proto.common.ProtoDisplay.ProtoType;
import io.extremum.sharedmodels.proto.common.ProtoMedia;
import io.extremum.sharedmodels.proto.common.ProtoMultilingual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisplayConverterTest {
    @InjectMocks
    private ProtoDisplayConverter displayConverter;
    @Mock
    private ProtoMediaConverter mediaConverter;
    @Mock
    private ProtoMultilingualConverter multilingualConverter;

    @Test
    void testSuccessful_CreateFromProto() {
        Multilingual mockMultilingual = new Multilingual(new HashMap<>());

        when(mediaConverter.createFromProto(any()))
                .thenReturn(null);
        when(multilingualConverter.createFromProto(any()))
                .thenReturn(mockMultilingual);

        ProtoDisplay fullStringProto = ProtoDisplay.newBuilder()
                .setType(ProtoType.STRING)
                .setStringValue("display_text")
                .build();
        ProtoDisplay fullObjectTextProto = ProtoDisplay.newBuilder()
                .setType(ProtoType.OBJECT)
                .setTextCaption("object_text")
                .setIcon(ProtoMedia.newBuilder().setUrl("icon_url").build())
                .setSplash(ProtoMedia.newBuilder().setUrl("splash_url").build())
                .build();
        ProtoDisplay fullObjectMultilingualProto = ProtoDisplay.newBuilder()
                .setType(ProtoType.OBJECT)
                .setMultilingualCaption(ProtoMultilingual.newBuilder().build())
                .build();

        Display fullStringDisplay = displayConverter.createFromProto(fullStringProto);
        Display objectTextDisplay = displayConverter.createFromProto(fullObjectTextProto);
        Display objectMultilingualDisplay = displayConverter.createFromProto(fullObjectMultilingualProto);

        assertAll(
                () -> assertThat(fullStringDisplay.getType(), is(Type.STRING)),
                () -> assertThat(fullStringDisplay.getStringValue(), is("display_text")),
                () -> assertThat(fullStringDisplay.getCaption(), nullValue()),

                () -> assertThat(objectTextDisplay.getType(), is(Type.OBJECT)),
                () -> assertThat(objectTextDisplay.getCaption().isTextOnly(), is(true)),
                () -> assertThat(objectTextDisplay.getCaption().getText(), is("object_text")),
                () -> assertThat(objectTextDisplay.getIcon(), nullValue()),
                () -> assertThat(objectTextDisplay.getSplash(), nullValue()),

                () -> assertThat(objectMultilingualDisplay.getType(), is(Type.OBJECT)),
                () -> assertThat(objectMultilingualDisplay.getCaption().isMultilingual(), is(true)),
                () -> assertThat(objectMultilingualDisplay.getCaption().getMultilingualContent().getMap(), equalTo(mockMultilingual.getMap())),
                () -> assertThat(objectMultilingualDisplay.getIcon(), nullValue()),
                () -> assertThat(objectMultilingualDisplay.getSplash(), nullValue())
        );
    }

//    TODO create other tests for Display
}