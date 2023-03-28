package io.extremum.watch.processor;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.mapper.MapperDependencies;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.common.support.ModelClasses;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static io.extremum.watch.processor.ProcessorTests.assertThatEventMetadataMatchesModelMetadataExceptModifiedField;
import static io.extremum.watch.processor.ProcessorTests.assertThatEventMetadataMatchesModelMetadataFully;
import static io.extremum.watch.processor.ProcessorTests.assertThatEventModelIdMatchesModelId;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class CommonServiceWatchProcessorTest {

    @InjectMocks
    private CommonServiceWatchProcessor processor;

    @Mock
    private DtoConversionService dtoConversionService;
    @Mock
    private WatchEventConsumer watchEventConsumer;
    @Spy
    private ObjectMapper mapper = new SystemJsonObjectMapper(mock(MapperDependencies.class));
    @Mock
    private DescriptorService descriptorService;
    @Spy
    private ModelClasses modelClasses = new TestModelClasses();

    @Captor
    private ArgumentCaptor<TextWatchEvent> watchEventCaptor;

    @Test
    void whenProcessingSaveInvocationOnWatchedModel_thenEventShouldBeCreatedWithReplacingJsonPatch() throws Exception {
        WatchedModel model = new WatchedModel();
        model.setName("the-model");
        when(dtoConversionService.convertUnknownToRequestDto(same(model), any()))
                .thenReturn(new WatchedModelRequestDto(model));

        processor.process(new TestInvocation("save", new Object[]{model}), model);

        verify(watchEventConsumer).consume(watchEventCaptor.capture());
        TextWatchEvent event = watchEventCaptor.getValue();
        assertThatEventModelIdMatchesModelId(model, event);
        assertThatPatchIsForFullReplaceWithName(event.getJsonPatch(), "the-model");
        assertThatEventMetadataMatchesModelMetadataFully(model, event);
    }

    @SuppressWarnings("SameParameterValue")
    private void assertThatPatchIsForFullReplaceWithName(String jsonPatchString, String expectedName)
            throws JSONException {
        JSONArray patch = new JSONArray(jsonPatchString);

        assertThat(patch.length(), is(1));
        JSONObject operation = patch.getJSONObject(0);

        assertThat(operation.get("op"), is("replace"));
        assertThat(operation.get("path"), is("/"));

        JSONObject value = operation.getJSONObject("value");
        assertThat("Only fields visible externally are expected", keySet(value), is(singleton("name")));
        assertThat(value.get("name"), is(equalTo(expectedName)));
    }

    private Set<Object> keySet(JSONObject object) {
        Set<Object> set = new HashSet<>();
        Iterator iterator = object.keys();
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }

    @Test
    void whenProcessingSaveInvocationOnNonWatchedModel_thenInvocationShouldBeIgnored() throws Exception {
        NonWatchedModel model = new NonWatchedModel();

        processor.process(new TestInvocation("save", new Object[]{model}), model);

        verify(watchEventConsumer, never()).consume(any());
    }

    @Test
    void whenProcessingDeleteInvocationOnWatchedModel_thenEventShouldBeCreatedWithRemovingJsonPatch() throws Exception {
        WatchedModel model = new WatchedModel();
        String modelInternalId = model.getId().toString();
        when(descriptorService.loadByInternalId(modelInternalId))
                .thenReturn(Optional.ofNullable(model.getUuid()));

        processor.process(new TestInvocation("delete", new Object[]{modelInternalId}), model);

        verify(watchEventConsumer).consume(watchEventCaptor.capture());
        TextWatchEvent event = watchEventCaptor.getValue();
        assertThatEventModelIdMatchesModelId(model, event);
        assertThatPatchIsForFullRemoval(event);
        assertThatEventMetadataMatchesModelMetadataExceptModifiedField(event, model);
    }

    private void assertThatPatchIsForFullRemoval(TextWatchEvent event) throws JSONException {
        JSONArray patch = new JSONArray(event.getJsonPatch());

        assertThat(patch.length(), is(1));
        JSONObject operation = patch.getJSONObject(0);

        assertThat(operation.get("op"), is("remove"));
        assertThat(operation.get("path"), is("/"));

        assertThat(operation.opt("value"), is(nullValue()));
    }

    @Test
    void whenProcessingDeleteInvocationOnNonWatchedModel_thenInvocationShouldBeIgnored() throws Exception {
        NonWatchedModel model = new NonWatchedModel();
        String modelInternalId = model.getId().toString();
        when(descriptorService.loadByInternalId(modelInternalId))
                .thenReturn(Optional.ofNullable(model.getUuid()));

        processor.process(new TestInvocation("delete", new Object[]{modelInternalId}), model);

        verify(watchEventConsumer, never()).consume(any());
    }

    @Test
    void whenProcessingUnknownInvocation_thenInvocationShouldBeIgnored() throws Exception {
        NonWatchedModel model = new NonWatchedModel();

        processor.process(new TestInvocation("method-we-are-not-interested-in", new Object[]{model}), model);

        verify(watchEventConsumer, never()).consume(any());
    }

    @Getter
    private static class WatchedModelRequestDto implements RequestDto {
        @JsonProperty
        private final String name;

        WatchedModelRequestDto(WatchedModel model) {
            name = model.getName();
        }
    }

}