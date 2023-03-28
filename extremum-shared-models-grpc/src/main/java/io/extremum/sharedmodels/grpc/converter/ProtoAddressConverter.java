package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.basic.Multilingual;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.proto.common.ProtoAddress;
import io.extremum.sharedmodels.proto.common.ProtoLocator;
import io.extremum.sharedmodels.spacetime.Address;
import io.extremum.sharedmodels.spacetime.Locator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProtoAddressConverter {
    private final ProtoMultilingualConverter multilingualConverter;
    private final ProtoLocatorConverter locatorConverter;

    public Address createFromProto(ProtoAddress proto) {
        Address address = new Address();

        StringOrMultilingual name;
        if (proto.hasMultilingualValue()) {
            Multilingual multilingual = multilingualConverter.createFromProto(proto.getMultilingualValue());
            name = new StringOrMultilingual(multilingual.getMap());
        } else {
            name = new StringOrMultilingual(proto.getStringValue());
        }
        address.setName(name);

        List<Locator> locality = proto.getLocalityList().stream()
                .map(locatorConverter::createFromProto)
                .collect(Collectors.toList());
        address.setLocality(locality);

        return address;
    }

    public ProtoAddress createProto(Address address) {
        List<ProtoLocator> locality = address.getLocality().stream()
                .map(locatorConverter::createProto)
                .collect(Collectors.toList());

        ProtoAddress.Builder addressBuilder = ProtoAddress.newBuilder()
                .addAllLocality(locality);

        StringOrMultilingual name = address.getName();
        if (name.isTextOnly()) {
            addressBuilder.setStringValue(name.getText());
        } else if (name.isMultilingual()) {
            addressBuilder.setMultilingualValue(multilingualConverter.createProto(name.getMultilingualContent()));
        }
        return addressBuilder.build();
    }
}
