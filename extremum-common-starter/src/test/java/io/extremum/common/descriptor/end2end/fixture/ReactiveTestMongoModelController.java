package io.extremum.common.descriptor.end2end.fixture;

import io.extremum.sharedmodels.dto.ResponseDto;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/models")
public class ReactiveTestMongoModelController {

    private final ReactiveTestMongoService testMongoService;
    private final TestMongoModelConverter testMongoModelConverter;

    public static final String ERROR_UUID = UUID.randomUUID().toString();

    public ReactiveTestMongoModelController(ReactiveTestMongoService testMongoService,
            TestMongoModelConverter testMongoModelConverter) {
        this.testMongoService = testMongoService;
        this.testMongoModelConverter = testMongoModelConverter;
    }

    @GetMapping(produces = {"text/event-stream"})
    public Flux<ServerSentEvent<Object>> getTestDtos() {
        return testMongoService.getModels()
                .flatMap(testMongoModelConverter::convertToResponseReactively)
                .map(this::dtoToSse)
                .onErrorResume((e) -> Mono.just(this.throwableToSse(e)));
    }

    private ServerSentEvent<Object> dtoToSse(ResponseDto dto) {
        return ServerSentEvent.builder().data(dto).build();
    }

    private ServerSentEvent<Object> throwableToSse(Throwable e) {
        return ServerSentEvent.builder().event("internal-error").data(ERROR_UUID).build();
    }

}
