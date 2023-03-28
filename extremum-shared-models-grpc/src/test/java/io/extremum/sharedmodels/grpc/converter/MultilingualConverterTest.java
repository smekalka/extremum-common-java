package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.basic.Multilingual;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.proto.common.ProtoMultilingual;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultilingualConverterTest {
    private final ProtoMultilingualConverter multilingualConverter = new ProtoMultilingualConverter();

    @Test
    void testSuccessful_CreateFromProto() {
        ProtoMultilingual proto = ProtoMultilingual.newBuilder().putAllMap(getProtoMultilingualMap()).build();
        Multilingual fromProto = multilingualConverter.createFromProto(proto);
        Map<MultilingualLanguage, String> multiMap = fromProto.getMap();
        assertThat(multiMap.size(), is(1));
        assertThat(multiMap.containsKey(MultilingualLanguage.en), is(true));
    }

    @Test
    void testBrokeNull_CreateFromProto() {
        assertThat(multilingualConverter
                .createFromProto(ProtoMultilingual.getDefaultInstance()), equalTo(null));

        ProtoMultilingual proto = ProtoMultilingual.newBuilder().build();
        assertNotNull(proto.getMapMap());
        assertThat(proto, equalTo(ProtoMultilingual.getDefaultInstance()));

        Multilingual fromProto = multilingualConverter.createFromProto(proto);
        assertNull(fromProto);
    }

    @Test
    void testBroke_CreateFromProto() {
        assertThrows(NullPointerException.class, () -> ProtoMultilingual.newBuilder().putAllMap(null).build());

        Map<String, String> multilingualMap = getProtoMultilingualMap();
        multilingualMap.put("test", "1");

        ProtoMultilingual proto = ProtoMultilingual.newBuilder().putAllMap(multilingualMap).build();
        Map<MultilingualLanguage, String> map = multilingualConverter.createFromProto(proto).getMap();

        assertThat(map.size(), is(2));
        assertThat(map.containsKey(null), is(true));
        assertThat(map.get(null), equalTo("1"));
    }

    @Test
    void testSuccessful_CreateProto() {
        Multilingual multilingual = new Multilingual(getMultilingualMap());
        Map<String, String> protoMap = multilingualConverter.createProto(multilingual).getMapMap();

        assertThat(protoMap.size(), is(1));
        assertThat(protoMap.get(MultilingualLanguage.ru.getValue()), is("1212"));
    }

    @Test
    void testBrokeNull_CreateProto() {
        ProtoMultilingual proto = multilingualConverter.createProto(null);
        assertThat(proto, is(ProtoMultilingual.getDefaultInstance()));
    }

    private Map<String, String> getProtoMultilingualMap() {
        Map<String, String> map = new HashMap<>();
        map.put(MultilingualLanguage.en.getValue(), "test");
        return map;
    }

    private Map<MultilingualLanguage, String> getMultilingualMap() {
        Map<MultilingualLanguage, String> map = new HashMap<>();
        map.put(MultilingualLanguage.ru, "1212");
        return map;
    }
}