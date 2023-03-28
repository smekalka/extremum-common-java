package io.extremum.watch.controller;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.dto.TextWatchEventResponseDto;
import io.extremum.watch.dto.converter.TextWatchEventConverter;
import io.extremum.watch.exception.WatchException;
import io.extremum.watch.models.TextWatchEvent;
import io.extremum.watch.services.WatchEventService;
import io.extremum.watch.services.WatchSubscriberIdProvider;
import io.extremum.watch.services.WatchSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


/**
 * This controller is responsible for searching appropriate watch events.
 */
@RestController
@RequestMapping("/watch")
@RequiredArgsConstructor
@ConditionalOnBean(BlockingWatchConfiguration.class)
public class WatchController {
    private final WatchEventService watchEventService;
    private final WatchSubscriberIdProvider watchSubscriberIdProvider;
    private final WatchSubscriptionService watchSubscriptionService;
    private final TextWatchEventConverter textWatchEventConverter;
    private final DescriptorFactory descriptorFactory;

    @GetMapping
    public Response getEvents(GetEventsRequest request) {
        List<TextWatchEvent> eventsAfter = watchEventService.findEvents(getSubscriber(),
                request.getSince(), request.getUntil(), request.getLimit());
        return Response.ok(convertToResponseDtos(eventsAfter));
    }

    private List<TextWatchEventResponseDto> convertToResponseDtos(List<TextWatchEvent> eventsAfter) {
        return eventsAfter.stream()
                .map(textWatchEventConverter::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response subscribe(@RequestBody List<String> idsToWatch) {
        List<Descriptor> descriptors = externalIdsToDescriptors(idsToWatch);

        watchSubscriptionService.subscribe(descriptors, getSubscriber());

        return Response.ok();
    }

    private List<Descriptor> externalIdsToDescriptors(List<String> externalIds) {
        return externalIds.stream()
                .map(descriptorFactory::fromExternalId)
                .collect(Collectors.toList());
    }

    private String getSubscriber() {
        return watchSubscriberIdProvider.getSubscriberId()
                .orElseThrow(() -> new WatchException("Cannot find subscriber ID"));
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response deleteSubscription(@RequestBody List<String> idsToStopWatching) {
        List<Descriptor> descriptors = externalIdsToDescriptors(idsToStopWatching);

        watchSubscriptionService.unsubscribe(descriptors, getSubscriber());

        return Response.ok();
    }

}

