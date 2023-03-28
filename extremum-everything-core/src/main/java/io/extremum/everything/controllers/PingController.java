package io.extremum.everything.controllers;

import io.extremum.sharedmodels.dto.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Ping")
@CrossOrigin
@RestController
public class PingController {
    @ApiOperation(value = "Check the health status of the service")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @GetMapping(value = "/ping")
    public Response ping() {
        return Response.ok();
    }
}
