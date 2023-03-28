package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.basic.Multilingual;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.proto.common.ProtoLocator;
import io.extremum.sharedmodels.proto.common.ProtoLocator.ProtoType;
import io.extremum.sharedmodels.spacetime.Locator;
import io.extremum.sharedmodels.spacetime.Locator.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoLocatorConverter {
    private final ProtoMultilingualConverter multilingualConverter;

    public Locator createFromProto(ProtoLocator proto) {
        Locator locator = new Locator();
        locator.setCode(proto.getCode());
        locator.setType(Type.valueOf(proto.getType().name()));

        StringOrMultilingual name;
        if (proto.hasMultilingualValue()) {
            Multilingual multilingual = multilingualConverter.createFromProto(proto.getMultilingualValue());
            name = new StringOrMultilingual(multilingual.getMap());
        } else {
            name = new StringOrMultilingual(proto.getStringValue());
        }
        locator.setName(name);

        return locator;
    }

    public ProtoLocator createProto(Locator locator) {
        ProtoLocator.Builder locatorBuilder = ProtoLocator.newBuilder()
                .setCode(locator.getCode())
                .setType(ProtoType.valueOf(locator.getType().name()));

        StringOrMultilingual name = locator.getName();
        if (name.isMultilingual()) {
            locatorBuilder.setMultilingualValue(multilingualConverter.createProto(name.getMultilingualContent()));
        } else if (name.isTextOnly()) {
            locatorBuilder.setStringValue(name.getText());
        }
        return locatorBuilder.build();
    }
}
