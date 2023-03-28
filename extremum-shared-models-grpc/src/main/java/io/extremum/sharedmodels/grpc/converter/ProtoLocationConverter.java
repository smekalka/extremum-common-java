package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.proto.common.ProtoCoordinates;
import io.extremum.sharedmodels.proto.common.ProtoLocation;
import io.extremum.sharedmodels.spacetime.ComplexAddress;
import io.extremum.sharedmodels.spacetime.ComplexAddress.Type;
import io.extremum.sharedmodels.spacetime.Coordinates;
import io.extremum.sharedmodels.spacetime.LocationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProtoLocationConverter {
    private final ProtoMultilingualConverter multilingualConverter;
    private final ProtoCoordinatesConverter coordinatesConverter;
    private final ProtoAddressConverter addressConverter;

    public ProtoLocation createProto(LocationRequestDto dto) {
        List<ProtoCoordinates> boundary = dto.getBoundary().stream()
                .map(coordinatesConverter::createProto)
                .collect(Collectors.toList());

        ProtoLocation.Builder locationBuilder = ProtoLocation.newBuilder()
                .setType(dto.getKind())
                .setSlug(dto.getSlug())
                .setStatus(dto.getStatus())
                .setUri(dto.getUri())
                .setCoordinates(coordinatesConverter.createProto(dto.getCoordinates()))
                .addAllBoundary(boundary);

        StringOrMultilingual name = dto.getName();
        if (name.isTextOnly()) {
            locationBuilder.setStringName(name.getText());
        } else if (name.isMultilingual()) {
            locationBuilder.setMultilingualName(multilingualConverter.createProto(name.getMultilingualContent()));
        }

        StringOrMultilingual description = dto.getDescription();
        if (description.isTextOnly()) {
            locationBuilder.setStringDescription(description.getText());
        } else if (description.isMultilingual()) {
            locationBuilder.setMultilingualDescription(multilingualConverter.createProto(description.getMultilingualContent()));
        }

        ComplexAddress complexAddress = dto.getAddress();
        Type type = complexAddress.getType();
        if (type == Type.string) {
            locationBuilder.setStringValue(complexAddress.getString());
        } else if (type == Type.multilingual) {
            locationBuilder.setMultilingualValue(multilingualConverter.createProto(complexAddress.getMultilingual()));
        } else if (type == Type.addressObject) {
            locationBuilder.setObjectValue(addressConverter.createProto(complexAddress.getAddress()));
        }

        return locationBuilder.build();
    }

    public LocationRequestDto createFromProto(ProtoLocation proto) {
        LocationRequestDto dto = new LocationRequestDto();
        dto.setSlug(proto.getSlug());
        dto.setUri(proto.getUri());
        dto.setKind(proto.getType());
        dto.setStatus(proto.getStatus());
        dto.setCoordinates(coordinatesConverter.createFromProto(proto.getCoordinates()));

        StringOrMultilingual name;
        if (proto.hasMultilingualName()) {
            name = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getMultilingualName()).getMap());
        } else {
            name = new StringOrMultilingual(proto.getStringName());
        }
        dto.setName(name);

        StringOrMultilingual description;
        if (proto.hasMultilingualDescription()) {
            description = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getMultilingualDescription()).getMap());
        } else {
            description = new StringOrMultilingual(proto.getStringDescription());
        }
        dto.setDescription(description);

        List<Coordinates> boundary = proto.getBoundaryList().stream()
                .map(coordinatesConverter::createFromProto)
                .collect(Collectors.toList());
        dto.setBoundary(boundary);

        ComplexAddress complexAddress;
        if (proto.hasObjectValue()) {
            complexAddress = new ComplexAddress(addressConverter.createFromProto(proto.getObjectValue()));
        } else if (proto.hasMultilingualValue()) {
            complexAddress = new ComplexAddress(multilingualConverter.createFromProto(proto.getMultilingualValue()));
        } else {
            complexAddress = new ComplexAddress(proto.getStringValue());
        }
        dto.setAddress(complexAddress);

        return dto;
    }
}
