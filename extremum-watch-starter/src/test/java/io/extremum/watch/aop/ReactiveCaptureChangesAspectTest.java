package io.extremum.watch.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import io.extremum.common.iri.properties.IriProperties;
import io.extremum.common.iri.service.DefaultIriFacilities;
import io.extremum.common.service.ReactiveCommonService;
import io.extremum.everything.services.management.ReactivePatchFlow;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.mongo.service.impl.ReactiveMongoCommonServiceImpl;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.test.aop.AspectWrapping;
import io.extremum.watch.processor.*;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class ReactiveCaptureChangesAspectTest {
    @InjectMocks
    private ReactiveCaptureChangesAspect aspect;

    @Mock
    private ReactiveCommonServiceWatchProcessor commonServiceWatchProcessor;
    @Mock
    private ReactivePatchFlowWatchProcessor patchFlowWatchProcessor;
    @Mock
    private ReactiveMongoCommonDao<TestModel> dao;
    @Mock
    private ReactivePatchFlow originalPatchFlow;

    @Captor
    private ArgumentCaptor<Invocation> invocationCaptor;

    private ReactiveCommonService<TestModel> commonServiceProxy;
    private ReactivePatchFlow patchFlowProxy;

    @BeforeEach
    void createProxies() {
        commonServiceProxy = wrapWithAspect(new TestCommonService(dao));
        patchFlowProxy = wrapWithAspect(originalPatchFlow);
    }

    private <T> T wrapWithAspect(T proxiedObject) {
        return AspectWrapping.wrapInAspect(proxiedObject, aspect);
    }

    @Test
    void whenInvokingSaveMethodOnCommonService_thenCommonServiceWatchProcessorShouldBeTriggered() throws Exception {
        TestModel model = makeModel();

        when(dao.save(model)).thenReturn(Mono.just(model));
        when(commonServiceWatchProcessor.process(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(commonServiceProxy.save(model))
                .thenAwait(Duration.ofMillis(100))
                .assertNext(this::assertThatSaveInvocationIsInvokedWith)
                .verifyComplete();
    }

    @SneakyThrows
    private void assertThatSaveInvocationIsInvokedWith(TestModel model) {
        verify(commonServiceWatchProcessor).process(invocationCaptor.capture(), same(model));
        Invocation invocation = invocationCaptor.getValue();
        assertThat(invocation.methodName(), is("save"));
        assertThat(invocation.args().length, is(greaterThanOrEqualTo(1)));
        assertThat(invocation.args()[0], is(sameInstance(model)));
    }

    @Test
    void whenInvokingDeleteMethodOnCommonService_thenCommonServiceWatchProcessorShouldBeTriggered() throws Exception {
        TestModel model = makeModel();

        when(dao.deleteByIdAndReturn(any())).thenReturn(Mono.just(model));
        when(commonServiceWatchProcessor.process(any(), any())).thenReturn(Mono.empty());

        StepVerifier
                .create(commonServiceProxy.delete(model.getUuid().getInternalId()))
                .thenAwait(Duration.ofMillis(100))
                .assertNext(this::assertThatDeleteInvocationIsInvokedWith)
                .verifyComplete();
    }

    @SneakyThrows
    private void assertThatDeleteInvocationIsInvokedWith(TestModel model) {
        verify(commonServiceWatchProcessor).process(invocationCaptor.capture(), same(model));
        Invocation invocation = invocationCaptor.getValue();
        assertThat(invocation.methodName(), is("delete"));
        assertThat(invocation.args().length, is(greaterThanOrEqualTo(1)));
        assertThat(invocation.args()[0], is(equalTo(model.getUuid().getInternalId())));
    }

    @Test
    void whenInvokingPatchMethodOnPatchFlow_thenPatchFlowWatchProcessorShouldBeTriggered() throws Exception {
        Descriptor descriptor = new Descriptor("external-id");
        JsonPatch jsonPatch = aJsonPatch();

        TestModel model = makeModel();
        when(originalPatchFlow.patch(any(), any())).thenReturn(Mono.just(model));
        when(patchFlowWatchProcessor.process(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(patchFlowProxy.patch(descriptor, jsonPatch))
                .thenAwait(Duration.ofMillis(100))
                .assertNext(result -> {
                    try {
                        verify(patchFlowWatchProcessor).process(invocationCaptor.capture(), same(model));
                    } catch (JsonProcessingException e) {}
                    Invocation invocation = invocationCaptor.getValue();
                    assertThat(invocation.methodName(), is("patch"));
                    assertThat(invocation.args().length, is(2));
                    assertThat(invocation.args()[0], is(sameInstance(descriptor)));
                    assertThat(invocation.args()[1], is(sameInstance(jsonPatch)));
                })
                .verifyComplete();
    }

    @NotNull
    private JsonPatch aJsonPatch() {
        return new JsonPatch(Collections.emptyList());
    }

    public static <T> Mono<T> addContext(Mono<T> source){
        return source.subscriberContext(context -> context.put("key", "Hello"));
    }

    @Test
    void whenInvokingSaveMethodOnCommonServiceAndPatchingFlagIsSet_thenTheInvocationShouldBeIgnored() throws Exception {
        TestModel model = makeModel();
        when(dao.save(model)).thenReturn(Mono.just(model));

        Mono<TestModel> resultMono = Mono.just(model)
                .flatMap(commonServiceProxy::save)
                .subscriberContext(ctx -> ctx.put(ReactivePatchFlow.MODEL_BEING_PATCHED, model.getUuid().getInternalId()));

        StepVerifier
                .create(resultMono)
                .thenAwait(Duration.ofMillis(100))
                .assertNext(result -> {
                    try {
                        verify(commonServiceWatchProcessor, never()).process(any(), any());
                    } catch (JsonProcessingException e) {}
                })
                .verifyComplete();
    }

    private TestModel makeModel() {
        ObjectId internalId = new ObjectId();
        TestModel model = new TestModel();
        model.setUuid(Descriptor.builder().internalId(internalId.toString()).build());
        return model;
    }

    private static class TestModel extends MongoCommonModel {
    }

    private static class TestCommonService extends ReactiveMongoCommonServiceImpl<TestModel> {
        TestCommonService(ReactiveMongoCommonDao<TestModel> dao) {
            super(dao, new DefaultIriFacilities(new IriProperties()));
        }
    }
}