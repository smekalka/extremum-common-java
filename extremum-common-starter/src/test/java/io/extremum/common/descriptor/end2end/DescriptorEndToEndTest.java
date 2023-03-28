package io.extremum.common.descriptor.end2end;

import io.extremum.common.descriptor.end2end.fixture.ReactiveTestMongoModelDao;
import io.extremum.common.descriptor.end2end.fixture.TestMongoModel;
import io.extremum.common.descriptor.end2end.fixture.TestMongoModelConfiguration;
import io.extremum.common.test.TestWithServices;
import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.starter.CommonConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Random;

import static io.extremum.common.descriptor.end2end.fixture.ReactiveTestMongoModelController.ERROR_UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

@SpringBootTest(
        classes = {CommonConfiguration.class, TestMongoModelConfiguration.class},
        properties = {"spring.main.web-application-type=reactive"}
)
@TestInstance(Lifecycle.PER_CLASS)
public class DescriptorEndToEndTest extends TestWithServices {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ReactiveDescriptorDao descriptorDao;

    @Autowired
    private ReactiveTestMongoModelDao testMongoModelDao;

    private WebTestClient webTestClient;

    private final Random random = new Random();

    @BeforeAll
    void beforeAll() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Test
    public void whenModelWithoutDescriptorRequested_exceptionShouldBeThrownBeforeJsonProcessing() {
        TestMongoModel modelWithDescriptor = createTestMongoModel().block();
        TestMongoModel modelWithoutDescriptor = createModelWithoutDescriptor().block();

        Flux<String> response = webTestClient.get()
                .uri("/models")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(response)
                .assertNext(data -> assertModelDto(modelWithDescriptor, data))
                .assertNext(this::assertError)
                .expectComplete()
                .verify();
    }

    private void assertModelDto(TestMongoModel expected, String actual) {
        assertThat(actual)
                .withFailMessage("Expecting response to match <%s> but was <%s>.", expected, actual)
                .contains(String.format("\"@uuid\":\"%s\"", expected.getUuid().getExternalId()))
                .contains(String.format("\"number\":\"%s\"", expected.getNumber()));
    }

    private void assertError(String error) {
        assertThat(error)
                .withFailMessage("Expecting error message to be <%s> but was <%s>.", ERROR_UUID, error)
                .isEqualTo(ERROR_UUID);
    }

    private Mono<TestMongoModel> createModelWithoutDescriptor() {
        return createTestMongoModel()
                .flatMap(testMongoModel -> deleteDescriptor(testMongoModel.getUuid())
                        .thenReturn(testMongoModel));
    }

    private Mono<TestMongoModel> createTestMongoModel() {
        TestMongoModel testMongoModel = new TestMongoModel();
        testMongoModel.setNumber(Integer.toString(random.nextInt(10000)));
        return testMongoModelDao.save(testMongoModel);
    }

    private Mono<Void> deleteDescriptor(Descriptor descriptor) {
        return descriptorDao.destroy(descriptor.getExternalId());
    }

}
