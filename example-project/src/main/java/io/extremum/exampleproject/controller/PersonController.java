package io.extremum.exampleproject.controller;

import io.extremum.exampleproject.converter.PersonConverter;
import io.extremum.exampleproject.converter.dto.PersonRequestDto;
import io.extremum.exampleproject.service.PersonService;
import io.extremum.sharedmodels.dto.Response;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("person")
@AllArgsConstructor
public class PersonController {

    private final PersonService service;
    private final PersonConverter converter;

    @PostMapping
    public Mono<Response> createPerson(@RequestBody PersonRequestDto dto) {
        return service.save(converter.convertFromRequest(dto)).map(
                person -> Response.ok(person.getUuid())
        );
    }
}