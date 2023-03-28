package io.extremum.common.response.advice;

import io.extremum.common.utils.StreamUtils;
import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
public abstract class ReactiveResponseDtoHandlingAspect {
    protected abstract Mono<?> applyToResponseDto(ResponseDto responseDto);

    @Around("isController() && (returnsMono() || returnsFlux())")
    public Object executeAroundController(ProceedingJoinPoint point) throws Throwable {
        Object invocationResult = point.proceed();

        if (invocationResult instanceof Mono) {
            return applyToMonoPayload((Mono<?>) invocationResult);
        }
        if (invocationResult instanceof Flux) {
            return applyToFluxPayload((Flux<?>) invocationResult);
        }

        return invocationResult;
    }

    private Mono<?> applyToMonoPayload(Mono<?> mono) {
        return mono.flatMap(this::possiblyApplyToResponseDtoInsideResponseOrSSE);
    }

    private Flux<?> applyToFluxPayload(Flux<?> flux) {
        return flux.concatMap(this::possiblyApplyToResponseDtoInsideResponseOrSSE);
    }

    private Mono<?> possiblyApplyToResponseDtoInsideResponseOrSSE(Object object) {
        if (object instanceof Response) {
            Response response = (Response) object;
            Object payload = response.getResult();
            if (payload instanceof ResponseDto) {
                return applyToResponseDtoThenReturnResponse((ResponseDto) payload, response);
            } else if (payload instanceof ResponseDto[]) {
                ResponseDto[] responseDtos = (ResponseDto[]) payload;
                return applyToResponseDtosInListAndReturnResponse(Arrays.asList(responseDtos), response);
            } else if (payload instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) payload;
                return applyToResponseDtosInIterableAndReturnResponse(iterable, response);
            }
        }
        if (object instanceof ServerSentEvent) {
            ServerSentEvent<?> sse = (ServerSentEvent<?>) object;
            if (sse.data() instanceof ResponseDto) {
                ResponseDto responseDto = (ResponseDto) sse.data();
                return applyToResponseDtoThenReturnResponse(responseDto, sse);
            }
        }

        return Mono.just(object);
    }

    private Mono<?> applyToResponseDtoThenReturnResponse(ResponseDto responseDto, Object response) {
        return applyToResponseDto(responseDto)
                .thenReturn(response);
    }

    private Mono<?> applyToResponseDtosInIterableAndReturnResponse(Iterable<?> iterable, Response response) {
        List<ResponseDto> responseDtos = StreamUtils.fromIterable(iterable)
                .filter(obj -> obj instanceof ResponseDto)
                .map(ResponseDto.class::cast)
                .collect(Collectors.toList());
        return applyToResponseDtosInListAndReturnResponse(responseDtos, response);
    }

    private Mono<?> applyToResponseDtosInListAndReturnResponse(List<ResponseDto> responseDtos,
                                                               Response response) {
        return Flux.fromIterable(responseDtos)
                .concatMap(this::applyToResponseDto)
                .then(Mono.just(response));
    }

    /**
     * This means 'method calls on instances of classes annotated with @Controller
     * directly or via a meta-annotated annotation (with one level of indirection at max).
     */
    @Pointcut("" +
            "within(@org.springframework.stereotype.Controller *) || " +
            "within(@(@org.springframework.stereotype.Controller *) *)")
    protected void isController() {
    }

    @Pointcut("execution(reactor.core.publisher.Mono *..*.*(..))")
    protected void returnsMono() {
    }

    @Pointcut("execution(reactor.core.publisher.Flux *..*.*(..))")
    protected void returnsFlux() {
    }
}