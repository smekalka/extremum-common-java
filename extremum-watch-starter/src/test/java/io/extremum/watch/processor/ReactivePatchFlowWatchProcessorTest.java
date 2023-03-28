package io.extremum.watch.processor;

import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.mapper.MapperDependencies;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.common.support.ModelClasses;
import io.extremum.watch.end2end.fixture.WatchedModelRequestDto;
import io.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import lombok.SneakyThrows;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;

import static io.extremum.watch.processor.ProcessorTests.assertThatEventMetadataMatchesModelMetadataFully;
import static io.extremum.watch.processor.ProcessorTests.assertThatEventModelIdMatchesModelId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactivePatchFlowWatchProcessorTest {
    @InjectMocks
    private ReactivePatchFlowWatchProcessor processor;

    @Mock
    private ReactiveWatchEventConsumer watchEventConsumer;
    @Mock
    private DtoConversionService dtoConversionService;
    @Spy
    private ModelClasses modelClasses = new TestModelClasses();
    @Spy
    private ObjectMapper mapper = new SystemJsonObjectMapper(mock(MapperDependencies.class));

    @Captor
    private ArgumentCaptor<TextWatchEvent> watchEventCaptor;

    @Test
    void whenProcessingPatchInvocationInWatchedModel_thenEventShouldBeCreated() throws Exception {
        WatchedModel model = new WatchedModel();
        model.setName("old-name");
        JsonPatch jsonPatch = replaceNameWithNewName();

        WatchedModelRequestDto dto = new WatchedModelRequestDto();
        dto.setName("new-name");

        when(dtoConversionService.convertUnknownToRequestDtoReactively(any(), any())).thenReturn(Mono.just(dto));
        when(watchEventConsumer.consume(any())).thenReturn(Mono.empty());

        StepVerifier.create(processor.process(new TestInvocation("patch", new Object[]{model.getUuid(), jsonPatch}), model))
                .thenAwait(Duration.ofMillis(100))
                .then(() -> {
                    verify(watchEventConsumer).consume(watchEventCaptor.capture());
                    TextWatchEvent event = watchEventCaptor.getValue();
                    assertThatEventModelIdMatchesModelId(model, event);
                    assertThatPatchReplacesNameWith(event.getJsonPatch(), "new-name");
                    assertThatEventMetadataMatchesModelMetadataFully(model, event);
                })
                .verifyComplete();
    }

    @NotNull
    private JsonPatch replaceNameWithNewName() throws JsonPointerException {
        ReplaceOperation operation = new ReplaceOperation(new JsonPointer("/name"), new TextNode("new-name"));
        return new JsonPatch(Collections.singletonList(operation));
    }

    @SuppressWarnings("SameParameterValue")
    @SneakyThrows
    private void assertThatPatchReplacesNameWith(String jsonPatchString, String expectedName) {
        JSONArray patch = new JSONArray(jsonPatchString);

        assertThat(patch.length(), is(1));
        JSONObject operation = patch.getJSONObject(0);

        assertThat(operation.get("op"), is("replace"));
        assertThat(operation.get("path"), is("/name"));

        assertThat(operation.getString("value"), is(equalTo(expectedName)));
    }

    @Test
    void whenProcessingPatchInvocationInNonWatchedModel_thenEventShouldNotBeCreated() throws Exception {
        NonWatchedModel model = new NonWatchedModel();
        JsonPatch jsonPatch = replaceNameWithNewName();

        StepVerifier.create(processor.process(new TestInvocation("patch", new Object[]{model.getUuid(), jsonPatch}), model))
                .thenAwait(Duration.ofMillis(100))
                .then(() -> {
                    verify(watchEventConsumer, never()).consume(watchEventCaptor.capture());
                })
                .verifyComplete();
    }
}