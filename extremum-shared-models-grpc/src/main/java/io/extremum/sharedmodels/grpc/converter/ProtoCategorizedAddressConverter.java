package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.proto.common.ProtoCategorizedAddress;
import io.extremum.sharedmodels.spacetime.CategorizedAddress;
import io.extremum.sharedmodels.spacetime.ComplexAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoCategorizedAddressConverter {
    private final ProtoMultilingualConverter multilingualConverter;
    private final ProtoAddressConverter addressConverter;

    public CategorizedAddress createFromProto(ProtoCategorizedAddress proto) {
        CategorizedAddress address = new CategorizedAddress();
        address.setCaption(proto.getCaption());
        address.setCategory(proto.getCategory());

        ComplexAddress complexAddress;
        if (proto.hasMultilingualValue()) {
            complexAddress = new ComplexAddress(multilingualConverter.createFromProto(proto.getMultilingualValue()));
        } else if (proto.hasObjectValue()) {
            complexAddress = new ComplexAddress(addressConverter.createFromProto(proto.getObjectValue()));
        } else {
            complexAddress = new ComplexAddress(proto.getStringValue());
        }
        address.setAddress(complexAddress);
        return address;
    }

    public ProtoCategorizedAddress createProto(CategorizedAddress address) {
        ProtoCategorizedAddress.Builder addressBuilder = ProtoCategorizedAddress.newBuilder()
                .setCaption(address.getCaption())
                .setCategory(address.getCategory());

        ComplexAddress complexAddress = address.getAddress();
        if (complexAddress.getType() == ComplexAddress.Type.multilingual) {
            addressBuilder.setMultilingualValue(multilingualConverter.createProto(complexAddress.getMultilingual()));
        } else if (complexAddress.getType() == ComplexAddress.Type.string) {
            addressBuilder.setStringValue(complexAddress.getString());
        } else if (complexAddress.getType() == ComplexAddress.Type.addressObject) {
            addressBuilder.setObjectValue(addressConverter.createProto(complexAddress.getAddress()));
        }
        return addressBuilder.build();
    }
}
