package io.extremum.exampleproject.converter;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.FromRequestDtoConverter;
import io.extremum.common.dto.converters.ReactiveToRequestDtoConverter;
import io.extremum.common.dto.converters.ReactiveToResponseDtoConverter;
import io.extremum.exampleproject.converter.dto.PersonRequestDto;
import io.extremum.exampleproject.converter.dto.PersonResponseDto;
import io.extremum.exampleproject.model.Person;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PersonConverter implements FromRequestDtoConverter<Person, PersonRequestDto>,
        ReactiveToRequestDtoConverter<Person, PersonRequestDto>,
        ReactiveToResponseDtoConverter<Person, PersonResponseDto> {

    @Override
    public String getSupportedModel() {
        return "Person";
    }

    @Override
    public Person convertFromRequest(PersonRequestDto dto) {
        Person person = new Person();
        person.setAddress(dto.getAddress());
        person.setName(dto.getName());
        person.setSlug(dto.getSlug());

        return person;
    }

    @Override
    public Mono<PersonRequestDto> convertToRequestReactively(Person model, ConversionConfig config) {
        PersonRequestDto dto = new PersonRequestDto();
        dto.setAddress(model.getAddress());

        return Mono.just(dto);
    }

    @Override
    public Class<? extends PersonRequestDto> getRequestDtoType() {
        return PersonRequestDto.class;
    }

    @Override
    public Mono<PersonResponseDto> convertToResponseReactively(Person model, ConversionConfig config) {
        PersonResponseDto dto = new PersonResponseDto();
        dto.setAddress(model.getAddress());
        dto.setId(model.getUuid());
        dto.setSlug(model.getSlug());

        return Mono.just(dto);
    }

    @Override
    public Class<? extends PersonResponseDto> getResponseDtoType() {
        return PersonResponseDto.class;
    }
}
