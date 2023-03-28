package io.extremum.common.exceptions.end2end.fixture;

import io.extremum.common.exceptions.CommonException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exceptions")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ExceptionTestController {

    @RequestMapping("/common-exception")
    public ExceptionsTestModel commonException() {
        throw new CommonException("Common exception message", 403);
    }

}
