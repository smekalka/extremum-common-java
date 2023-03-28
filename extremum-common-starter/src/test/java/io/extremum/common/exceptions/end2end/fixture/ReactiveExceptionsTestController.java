package io.extremum.common.exceptions.end2end.fixture;

import io.extremum.common.exceptions.CommonException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/reactive-exceptions")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveExceptionsTestController {

    public final static List<ExceptionsTestModel> DATA = Arrays.asList(
            new ExceptionsTestModel("name", "Bob"),
            new ExceptionsTestModel("age", "25"),
            new ExceptionsTestModel("job", "Developer"),
            new ExceptionsTestModel("city", "Moscow"),
            new ExceptionsTestModel("email", "example@example.example")
    );

    @RequestMapping("/flux-with-data-and-exception")
    public Flux<ExceptionsTestModel> fluxWithDataAndException() {
        return Flux.fromIterable(DATA)
                .map(d -> {
                    if (d.getKey().equals("email")) {
                        throw new CommonException("Common exception message", 403);
                    }
                    return d;
                });
    }

    @RequestMapping("/flux-with-exception")
    public Flux<ExceptionsTestModel> fluxWithException() {
        return Flux.error(new CommonException("Common exception message", 403));
    }

}
