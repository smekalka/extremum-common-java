package io.extremum.test.core;

import io.extremum.sharedmodels.dto.Response;
import io.extremum.sharedmodels.dto.ResponseStatusEnum;
import org.hamcrest.Matcher;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

public class ResponseMatchers {
    public static Matcher<Response> successful() {
        return allOf(
                notNullValue(),
                hasProperty("code", equalTo(200)),
                hasProperty("status", equalTo(ResponseStatusEnum.OK)),
                hasProperty("alerts", nullValue())
        );
    }

    public static Matcher<Response> notFound() {
        return allOf(
                notNullValue(),
                hasProperty("code", equalTo(404)),
                hasProperty("status", equalTo(ResponseStatusEnum.FAIL))
        );
    }
}
