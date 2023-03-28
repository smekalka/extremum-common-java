package io.extremum.watch.controller;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.dto.TextWatchEventResponseDto;
import io.extremum.watch.dto.converter.TextWatchEventConverter;
import io.extremum.watch.exception.WatchException;
import io.extremum.watch.services.ReactiveWatchEventService;
import io.extremum.watch.services.ReactiveWatchSubscriberIdProvider;
import io.extremum.watch.services.ReactiveWatchSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This controller is responsible for searching appropriate watch events.
 */
@RestController
@RequestMapping("/watch")
@RequiredArgsConstructor
@ConditionalOnBean(ReactiveWatchConfiguration.class)
@Slf4j
public class ReactiveWatchController {
    private final ReactiveWatchEventService watchEventService;
    private final ReactiveWatchSubscriberIdProvider subscriberIdProvider;
    private final ReactiveWatchSubscriptionService watchSubscriptionService;
    private final TextWatchEventConverter textWatchEventConverter;
    private final DescriptorFactory descriptorFactory;

    @GetMapping
    public Flux<TextWatchEventResponseDto> getEvents(GetEventsRequest request) {
        return getSubscriber().flatMapMany(subscriber -> watchEventService.findEvents(subscriber,
                request.getSince(), request.getUntil(), request.getLimit()))
                .map(textWatchEventConverter::convertToResponseDto);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Response> subscribe(@RequestBody List<String> idsToWatch) {
        return getSubscriber()
                .flatMap(subscriber -> {
                    List<Descriptor> descriptors = externalIdsToDescriptors(idsToWatch);
                    return watchSubscriptionService.subscribe(descriptors, subscriber);
                })
                .thenReturn(Response.ok());
    }

    private List<Descriptor> externalIdsToDescriptors(List<String> externalIds) {
        return externalIds.stream()
                .map(descriptorFactory::fromExternalId)
                .collect(Collectors.toList());
    }

    private Mono<String> getSubscriber() {
        return subscriberIdProvider.getSubscriberId()
                .onErrorMap(e -> {
                    log.error("Cannot find subscriber ID", e);
                    return new WatchException("Cannot find subscriber ID");
                });
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Response> deleteSubscription(@RequestBody List<String> idsToStopWatching) {
            return getSubscriber().flatMap(subscriber -> {
                List<Descriptor> descriptors = externalIdsToDescriptors(idsToStopWatching);
                return watchSubscriptionService.unsubscribe(descriptors, subscriber);
            })
            .thenReturn(Response.ok());
    }
}

