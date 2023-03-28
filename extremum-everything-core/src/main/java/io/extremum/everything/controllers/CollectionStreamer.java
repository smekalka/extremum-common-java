package io.extremum.everything.controllers;

import io.extremum.common.logging.InternalErrorLogger;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.services.management.EverythingCollectionManagementService;
import io.extremum.sharedmodels.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class CollectionStreamer {
    private final EverythingCollectionManagementService collectionManagementService;
    private final InternalErrorLogger errorLogger = new InternalErrorLogger(log);

    public Flux<ServerSentEvent<Object>> streamCollection(String id, Projection projection,
                                                          boolean expand) {
        return collectionManagementService.streamCollection(id, projection, expand)
                .map(this::dtoToSse)
                .onErrorResume(e -> Mono.just(throwableToSse(e)));
    }

    private ServerSentEvent<Object> dtoToSse(ResponseDto dto) {
        return ServerSentEvent.builder()
                .data(dto)
                .build();
    }

    private ServerSentEvent<Object> throwableToSse(Throwable e) {
        return ServerSentEvent.builder().event("internal-error")
                .data(errorLogger.logErrorAndReturnId(e))
                .build();
    }
}
