package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.basic.Multilingual;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.proto.common.ProtoMultilingual;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProtoMultilingualConverter {

    public Multilingual createFromProto(ProtoMultilingual proto) {
        if (ProtoMultilingual.getDefaultInstance().equals(proto)) {
            return null;
        }

        Map<MultilingualLanguage, String> map = new HashMap<>();
        Map<String, String> protoMap = proto.getMapMap();

        if (protoMap != null) {
            protoMap.forEach((key, value) -> map.put(MultilingualLanguage.fromString(key), value));
        }
        return new Multilingual(map);
    }

    public ProtoMultilingual createProto(Multilingual multilingual) {
        if (multilingual == null) {
            return ProtoMultilingual.getDefaultInstance();
        }

        Map<String, String> map = new HashMap<>();
        multilingual.getMap()
                .forEach((key, value) -> map.put(key.getValue(), value));
        return ProtoMultilingual.newBuilder()
                .putAllMap(map)
                .build();
    }
}
