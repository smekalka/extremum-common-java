package io.extremum.watch.end2end;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import com.jayway.jsonpath.JsonPath;
import io.extremum.security.DataSecurity;
import io.extremum.security.ExtremumAccessDeniedException;
import io.extremum.security.RoleSecurity;
import io.extremum.test.core.StringResponseMatchers;
import io.extremum.test.poll.Poller;
import io.extremum.watch.config.TestWithServices;
import io.extremum.watch.config.WatchTestConfiguration;
import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.config.conditional.WebSocketConfiguration;
import io.extremum.watch.end2end.fixture.WatchedModel;
import io.extremum.watch.end2end.fixture.WatchedModelService;
import io.extremum.watch.services.WatchSubscriberIdProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.extremum.common.response.status.HeaderBasedResponseStatusCodeResolver.ALWAYS_200;
import static io.extremum.common.response.status.HeaderBasedResponseStatusCodeResolver.STATUS_CODE;
import static io.extremum.test.core.StringResponseMatchers.responseThat;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {WatchTestConfiguration.class, BlockingWatchConfiguration.class, WebSocketConfiguration.class})
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class WatchEndToEndTest extends TestWithServices {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WatchedModelService watchedModelService;

    @MockBean
    private WatchSubscriberIdProvider subscriberIdProvider;
    @SpyBean
    private RoleSecurity roleSecurity;
    @SpyBean
    private DataSecurity dataSecurity;

    private WatchedModel model;

    @BeforeEach
    void init() {
        plugInAFreshSubscriberId();
        saveAFreshModel();
    }

    private void plugInAFreshSubscriberId() {
        String subscriberId = UUID.randomUUID().toString();
        when(subscriberIdProvider.getSubscriberId()).thenReturn(Optional.of(subscriberId));
    }

    private void saveAFreshModel() {
        WatchedModel modelToSave = new WatchedModel();
        modelToSave.setName("old name");
        model = watchedModelService.create(modelToSave);
    }

    @Test
    void givenCurrentUserIsSubscribedToAModelAndTheModelIsPatched_whenGettingWatchEvents_thenOnePatchEventShouldBeReturned()
            throws Exception {
        subscribeToTheModel();
        patchToChangeNameTo("new name");

        List<Map<String, Object>> events = getNonZeroEventsForCurrentPrincipal();

        assertThatThereIsOneEventForPatchingNameProperty(events);
    }

    private void subscribeToTheModel() throws Exception {
        mockMvc.perform(
                        put("/watch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[\"" + getModelExternalId() + "\"]")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(StringResponseMatchers.successfulResponse()))
                .andReturn();
    }

    private String getModelExternalId() {
        return model.getUuid().getExternalId();
    }

    @SuppressWarnings("SameParameterValue")
    private void patchToChangeNameTo(String newName) throws Exception {
        JsonPatch jsonPatch = new JsonPatch(singletonList(
                new ReplaceOperation(new JsonPointer("/name"), new TextNode(newName))
        ));

        mockMvc.perform(
                        patch("/" + getModelExternalId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(jsonPatch)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(StringResponseMatchers.successfulResponse()))
                .andReturn();
    }

    private List<Map<String, Object>> getNonZeroEventsForCurrentPrincipal() throws InterruptedException {
        Poller poller = new Poller(Duration.ofSeconds(10));
        return poller.poll(this::getWatchEventsForCurrentUser, events -> events.size() > 0);
    }

    private List<Map<String, Object>> getWatchEventsForCurrentUser() {
        try {
            MvcResult mvcResult = mockMvc.perform(get("/watch")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().string(StringResponseMatchers.successfulResponse()))
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            return parseEvents(contentAsString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> parseEvents(String response) {
        return JsonPath.parse(response).read("$.result");
    }

    private void assertThatThereIsOneEventForPatchingNameProperty(List<Map<String, Object>> events) {
        assertThat(events, hasSize(1));
        Map<String, Object> event = events.get(0);

        assertThatEventObjectMetadataIsCorrect(event, getModelExternalId());
        Map<String, Object> operation = getSingleOperation(event);

        assertThat(operation, hasEntry(is("op"), is("replace")));
        assertThat(operation, hasEntry(is("path"), is("/name")));
        assertThat(operation, hasEntry(is("value"), is("new name")));
    }

    private void assertThatEventObjectMetadataIsCorrect(Map<String, Object> event, String externalId) {
        assertThat(event.get("object"), is(notNullValue()));
        assertThat(event.get("object"), is(instanceOf(Map.class)));
        @SuppressWarnings("unchecked")
        Map<String, Object> object = (Map<String, Object>) event.get("object");
        assertThat(object, hasEntry(is("id"), equalTo(externalId)));
        assertThat(object, hasEntry(is("model"), is("E2EWatchedModel")));
        assertThat(object, hasKey("created"));
        assertThat(object, hasKey("modified"));
        assertThat(object, hasKey("version"));
    }

    private Map<String, Object> getSingleOperation(Map<String, Object> event) {
        assertThat(event.get("patch"), is(notNullValue()));
        assertThat(event.get("patch"), is(instanceOf(List.class)));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> operations = (List<Map<String, Object>>) event.get("patch");
        assertThat(operations, hasSize(1));

        return operations.get(0);
    }

    @Test
    void givenCurrentUserIsSubscribedToAModelAndTheModelIsSaved_whenGettingWatchEvents_thenOneSaveEventShouldBeReturned()
            throws Exception {
        subscribeToTheModel();
        saveToChangeNameTo("new name");

        List<Map<String, Object>> events = getNonZeroEventsForCurrentPrincipal();

        assertThatThereIsOneEventForSaving(events);
    }

    @SuppressWarnings("SameParameterValue")
    private void saveToChangeNameTo(String newName) {
        model.setName(newName);
        watchedModelService.save(model);
    }

    private void assertThatThereIsOneEventForSaving(List<Map<String, Object>> events) {
        assertThat(events, hasSize(1));
        Map<String, Object> event = events.get(0);

        assertThatEventObjectMetadataIsCorrect(event, getModelExternalId());
        Map<String, Object> operation = getSingleOperation(event);

        assertThat(operation, hasEntry(is("op"), is("replace")));
        assertThat(operation, hasEntry(is("path"), is("/")));
        assertThat(operation, hasEntry(is("value"), is(singletonMap("name", "new name"))));
    }

    @Test
    void givenCurrentUserIsSubscribedToAModelAndTheModelIsDeleted_whenGettingWatchEvents_thenOneDeletionEventShouldBeReturned()
            throws Exception {
        subscribeToTheModel();
        deleteTheModel();

        List<Map<String, Object>> events = getNonZeroEventsForCurrentPrincipal();

        assertThatThereIsOneEventForDeletion(events);
    }

    private void deleteTheModel() {
        watchedModelService.delete(model.getId().toString());
    }

    private void assertThatThereIsOneEventForDeletion(List<Map<String, Object>> events) {
        assertThat(events, hasSize(1));
        Map<String, Object> event = events.get(0);

        assertThatEventObjectMetadataIsCorrect(event, getModelExternalId());
        Map<String, Object> operation = getSingleOperation(event);

        assertThat(operation, hasEntry(is("op"), is("remove")));
        assertThat(operation, hasEntry(is("path"), is("/")));
        assertThat(operation, not(hasKey("value")));
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleRequiredToWatch_whenSubscribing_thenADeniedExceptionShouldBeThrown()
            throws Exception {
        doThrow(new ExtremumAccessDeniedException("Not allowed to watch")).when(roleSecurity).checkWatchAllowed(any());

        subscribeToCurrentModelAndGet403();
    }

    private void subscribeToCurrentModelAndGet403() throws Exception {
        mockMvc.perform(
                        put("/watch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[\"" + getModelExternalId() + "\"]")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(responseThat(hasProperty("code", is(403)))))
                .andReturn();
    }

    @Test
    void givenCurrentUserDoesNotHaveRoleRequiredToWatch_withAlways200Header_whenSubscribing_thenADeniedExceptionShouldBeThrown()
            throws Exception {
        doThrow(new ExtremumAccessDeniedException("Not allowed to watch")).when(roleSecurity).checkWatchAllowed(any());

        subscribeToCurrentModel_withAlways200Header_AndGet403();
    }

    private void subscribeToCurrentModel_withAlways200Header_AndGet403() throws Exception {
        mockMvc.perform(
                        put("/watch")
                                .header(STATUS_CODE, ALWAYS_200)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[\"" + getModelExternalId() + "\"]")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(responseThat(hasProperty("code", is(403)))))
                .andReturn();
    }

    @Test
    void givenDataSecurityDoesNotAllowCurrentUserToWatch_whenSubscribing_thenADeniedExceptionShouldBeThrown()
            throws Exception {
        doThrow(new ExtremumAccessDeniedException("Not allowed to watch")).when(dataSecurity).checkWatchAllowed(any());

        subscribeToCurrentModelAndGet403();
    }

    @Test
    void givenDataSecurityDoesNotAllowCurrentUserToWatch_withAlways200Header_whenSubscribing_thenADeniedExceptionShouldBeThrown()
            throws Exception {
        doThrow(new ExtremumAccessDeniedException("Not allowed to watch")).when(dataSecurity).checkWatchAllowed(any());

        subscribeToCurrentModel_withAlways200Header_AndGet403();
    }
}