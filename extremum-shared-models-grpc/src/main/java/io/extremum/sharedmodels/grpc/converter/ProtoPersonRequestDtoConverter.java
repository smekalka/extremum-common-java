package io.extremum.sharedmodels.grpc.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.extremum.sharedmodels.basic.StringOrObject;
import io.extremum.sharedmodels.content.Media;
import io.extremum.sharedmodels.personal.*;
import io.extremum.sharedmodels.proto.common.*;
import io.extremum.sharedmodels.spacetime.CategorizedAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProtoPersonRequestDtoConverter {
    private final ObjectMapper mapper;
    private final ProtoMediaConverter mediaConverter;
    private final ProtoBirthConverter birthConverter;
    private final ProtoCategorizedAddressConverter categorizedAddressConverter;
    private final ProtoContactConverter contactConverter;
    private final ProtoLanguageConverter languageConverter;
    private final ProtoNameConverter nameConverter;
    private final ProtoPersonPositionConverter personPositionConverter;

    public PersonRequestDto createFromProto(ProtoPersonRequestDto proto) {
        if (proto.equals(ProtoPersonRequestDto.getDefaultInstance())) {
            return null;
        }

        PersonRequestDto dto = new PersonRequestDto();
        dto.setAge(proto.getAge());
        dto.setGender(Gender.valueOf(proto.getGender().name()));
        dto.setRelationship(proto.getRelationship());
        dto.setHometown(proto.getHometown());
        dto.setNationality(proto.getNationality());
        dto.setBirth(birthConverter.createFromProto(proto.getBirth()));

        List<Media> images = proto.getImagesList().stream()
                .map(mediaConverter::createFromProto)
                .collect(Collectors.toList());
        dto.setImages(images);

        List<CategorizedAddress> addresses = proto.getAddressesList().stream()
                .map(categorizedAddressConverter::createFromProto)
                .collect(Collectors.toList());
        dto.setAddresses(addresses);

        List<Contact> contacts = proto.getContactsList().stream()
                .map(contactConverter::createFromProto)
                .collect(Collectors.toList());
        dto.setContacts(contacts);

        List<Language> languages = proto.getLanguagesList().stream()
                .map(languageConverter::createFromProto)
                .collect(Collectors.toList());
        dto.setLanguages(languages);

        List<Object> documents = proto.getDocumentsList().stream()
                .map(ByteString::toByteArray)
                .map(this::mapBytesToObject)
                .collect(Collectors.toList());
        dto.setDocuments(documents);

        List<PersonPositionForRequestDto> positions = proto.getPositionsList().stream()
                .map(personPositionConverter::createFromProto)
                .collect(Collectors.toList());
        dto.setPositions(positions);

        StringOrObject<Name> name;
        if (proto.hasObjectValue()) {
            name = new StringOrObject<>(nameConverter.createFromProto(proto.getObjectValue()));
        } else {
            name = new StringOrObject<>(proto.getStringValue());
        }
        dto.setName(name);

        return dto;
    }

    public ProtoPersonRequestDto createProto(PersonRequestDto dto) {
        if (dto == null) {
            return ProtoPersonRequestDto.getDefaultInstance();
        }
        List<ProtoMedia> images = dto.getImages().stream()
                .map(mediaConverter::createProto)
                .collect(Collectors.toList());
        List<ProtoCategorizedAddress> categorizedAddresses = dto.getAddresses().stream()
                .map(categorizedAddressConverter::createProto)
                .collect(Collectors.toList());
        List<ProtoContact> contacts = dto.getContacts().stream()
                .map(contactConverter::createProto)
                .collect(Collectors.toList());
        List<ByteString> documents = dto.getDocuments().stream()
                .map(this::writeObjectAsBytes)
                .filter(Objects::nonNull)
                .map(ByteString::copyFrom)
                .collect(Collectors.toList());
        List<ProtoLanguage> languages = dto.getLanguages().stream()
                .map(languageConverter::createProto)
                .collect(Collectors.toList());
        List<ProtoPersonPosition> positions = dto.getPositions().stream()
                .map(personPositionConverter::createProto)
                .collect(Collectors.toList());

        ProtoPersonRequestDto.Builder personBuilder = ProtoPersonRequestDto.newBuilder()
                .setAge(dto.getAge())
                .setRelationship(dto.getRelationship())
                .setHometown(dto.getHometown())
                .setNationality(dto.getNationality())
                .setGender(ProtoGender.valueOf(dto.getGender().name()))
                .setBirth(birthConverter.createProto(dto.getBirth()))
                .addAllImages(images)
                .addAllAddresses(categorizedAddresses)
                .addAllContacts(contacts)
                .addAllDocuments(documents)
                .addAllLanguages(languages)
                .addAllPositions(positions);

        StringOrObject<Name> name = dto.getName();
        if (name.isComplex()) {
            personBuilder.setObjectValue(nameConverter.createProto(name.getObject()));
        } else if (name.isSimple()) {
            personBuilder.setStringValue(name.getString());
        }

        return personBuilder.build();
    }

    private Object mapBytesToObject(byte[] object) {
        try {
            return mapper.readValue(object, Object.class);
        } catch (IOException e) {
            log.error("Exception on mapBytesToObject() : ", e);
            return null;
        }
    }

    private byte[] writeObjectAsBytes(Object object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("Exception on writeObjectAsBytes() : ", e);
            return null;
        }
    }
}
