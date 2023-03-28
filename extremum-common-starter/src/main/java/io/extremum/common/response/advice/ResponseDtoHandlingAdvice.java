package io.extremum.common.response.advice;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author rpuch
 */
public abstract class ResponseDtoHandlingAdvice implements ResponseBodyAdvice<Response> {
    @Override
    public final boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType() == Response.class;
    }

    @Override
    public final Response beforeBodyWrite(Response body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        applyIfNeeded(body);
        return body;
    }

    private void applyIfNeeded(Response response) {
        if (response == null) {
            return;
        }

        applyMakeupToPayloadIfNeeded(response.getResult());
    }

    private void applyMakeupToPayloadIfNeeded(Object result) {
        if (result == null) {
            return;
        }

        if (result instanceof ResponseDto) {
            applyToResponseDto((ResponseDto) result);
        }

        if (result instanceof ResponseDto[]) {
            applyToArrayOfDto((ResponseDto[]) result);
        }

        if (result instanceof Iterable) {
            applyToIterable((Iterable<?>) result);
        }
    }

    private void applyToArrayOfDto(ResponseDto[] array) {
        for (ResponseDto dto : array) {
            applyToResponseDto(dto);
        }
    }

    private void applyToIterable(Iterable<?> iterable) {
        iterable.forEach(element -> {
            if (element instanceof ResponseDto) {
                applyToResponseDto((ResponseDto) element);
            }
        });
    }

    protected abstract void applyToResponseDto(ResponseDto responseDto);
}
