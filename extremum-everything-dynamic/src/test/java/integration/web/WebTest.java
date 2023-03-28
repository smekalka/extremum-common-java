package integration.web;

import integration.SpringBootTestWithServices;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.JsonBasedDynamicModelService;
import io.extremum.sharedmodels.dto.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@AutoConfigureWebTestClient
@SpringBootTest(classes = WebTestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebTest extends SpringBootTestWithServices {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    JsonBasedDynamicModelService service;

    @Test
    void createValidModel_ok() {
        Response response = Response.ok();

        doReturn(Mono.just(response)).when(service).saveModel(any());

        String modelName = "SomeDynamicModel";

        webTestClient.post()
                .uri("/models/" + modelName)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just("{\"field1\": \"aaa\", \"field2\": 1}"), String.class)
                .exchange()
                .expectStatus().isOk().expectBody();
//                .expectBody(Response.class);

        ArgumentCaptor<JsonDynamicModel> captor = ArgumentCaptor.forClass(JsonDynamicModel.class);
        verify(service, times(1)).saveModel(captor.capture());

        Assertions.assertEquals(modelName, captor.getValue().getModelName());
    }

    @Test
    void createNotValidModel_returnsError() {

    }
}
