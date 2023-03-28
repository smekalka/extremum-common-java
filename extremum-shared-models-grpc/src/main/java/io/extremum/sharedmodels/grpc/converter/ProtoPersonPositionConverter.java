package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.basic.IdOrObject;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.personal.PersonPositionForRequestDto;
import io.extremum.sharedmodels.proto.common.ProtoPersonPosition;
import io.extremum.sharedmodels.spacetime.LocationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoPersonPositionConverter {
    private final ProtoLocationConverter locationConverter;
    private final ProtoTimeFrameConverter timeFrameConverter;
    private final ProtoMultilingualConverter multilingualConverter;

    public PersonPositionForRequestDto createFromProto(ProtoPersonPosition proto) {
        PersonPositionForRequestDto position = new PersonPositionForRequestDto();
        position.setTimeframe(timeFrameConverter.createFromProto(proto.getTimeframe()));

        StringOrMultilingual company;
        if (proto.hasMultilingualCompany()) {
            company = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getMultilingualCompany()).getMap());
        } else {
            company = new StringOrMultilingual(proto.getStringCompany());
        }
        position.setCompany(company);

        StringOrMultilingual description;
        if (proto.hasMultilingualDescription()) {
            description = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getMultilingualDescription()).getMap());
        } else {
            description = new StringOrMultilingual(proto.getStringDescription());
        }
        position.setDescription(description);

        StringOrMultilingual title;
        if (proto.hasMultilingualTitle()) {
            title = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getMultilingualTitle()).getMap());
        } else {
            title = new StringOrMultilingual(proto.getStringTitle());
        }
        position.setTitle(title);

        IdOrObject<String, LocationRequestDto> location;
        if (proto.hasObjectValue()) {
            location = new IdOrObject<String, LocationRequestDto>(locationConverter.createFromProto(proto.getObjectValue()));
        } else {
            location = new IdOrObject<>(proto.getStringValue());
        }
        position.setLocation(location);

        return position;
    }

    public ProtoPersonPosition createProto(PersonPositionForRequestDto dto) {
        ProtoPersonPosition.Builder positionBuilder = ProtoPersonPosition.newBuilder()
                .setTimeframe(timeFrameConverter.createProto(dto.getTimeframe()));

        StringOrMultilingual company = dto.getCompany();
        if (company.isMultilingual()) {
            positionBuilder.setMultilingualCompany(multilingualConverter.createProto(company.getMultilingualContent()));
        } else if (company.isTextOnly()) {
            positionBuilder.setStringCompany(company.getText());
        }

        StringOrMultilingual description = dto.getDescription();
        if (description.isMultilingual()) {
            positionBuilder.setMultilingualDescription(multilingualConverter.createProto(description.getMultilingualContent()));
        } else if (description.isTextOnly()) {
            positionBuilder.setStringDescription(description.getText());
        }

        StringOrMultilingual title = dto.getTitle();
        if (title.isMultilingual()) {
            positionBuilder.setMultilingualTitle(multilingualConverter.createProto(title.getMultilingualContent()));
        } else if (title.isTextOnly()) {
            positionBuilder.setStringTitle(title.getText());
        }

        IdOrObject<String, LocationRequestDto> location = dto.getLocation();
        if (location.isComplex()) {
            positionBuilder.setObjectValue(locationConverter.createProto(location.getObject()));
        } else if (location.isSimple()) {
            positionBuilder.setStringValue(location.getId());
        }
        return positionBuilder.build();
    }
}
