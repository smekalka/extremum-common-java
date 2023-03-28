package io.extremum.watch.services;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.sharedmodels.auth.User;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.watch.ModelSignal;
import io.extremum.sharedmodels.watch.ModelSignalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultModelSignalProviderTest {

    @Mock
    private DataSecurity dataSecurity;

    @Mock
    private ModelRetriever modelRetriever;

    @Mock
    private PrincipalSource principalSource;

    private DefaultModelSignalProvider provider;

    @BeforeEach
    public void setUp() {
        provider = new DefaultModelSignalProvider(dataSecurity, modelRetriever, principalSource);
    }

    @Test
    void subscribe_to_model_signal_by_id() {
        TestModel model = new TestModel();
        when(principalSource.getPrincipal()).thenReturn(Optional.of(new User("user", "", Collections.emptyList())));
        when(modelRetriever.retrieveModel(any())).thenReturn(model);
        Publisher<Model> subscribe = provider.subscribe("does-not-matter", ModelSignalType.CREATED);
        verify(dataSecurity, times(1)).checkWatchAllowed(any(), any());

    }

    @Test
    void subscribe_to_model_signal_by_class() {
        when(principalSource.getPrincipal()).thenReturn(Optional.of(new User("user", "", Collections.emptyList())));
        Publisher<Model> subscribe = provider.subscribe(TestModel.class, ModelSignalType.CREATED);
    }

    @Test
    void publish_signal_when_subscribed_to_id() {
        when(principalSource.getPrincipal()).thenReturn(Optional.of(new User("user", "", Collections.emptyList())));
        provider.subscribe("externalId", ModelSignalType.CREATED);
        TestModel testModel = new TestModel();
        testModel.setUuid(Descriptor.builder().externalId("externalId").build());
        provider.publish(new ModelSignal.Created(testModel));

        provider.subscribe(TestModel.class, ModelSignalType.CREATED);

    }

    @Test
    void publish_signal_when_subscribed_to_class() {
        when(principalSource.getPrincipal()).thenReturn(Optional.of(new User("user", "", Collections.emptyList())));
        provider.subscribe(TestModel.class, ModelSignalType.CREATED);
        TestModel testModel = new TestModel();
        testModel.setUuid(Descriptor.builder().externalId("externalId").build());
        provider.publish(new ModelSignal.Created(testModel));
    }

    private static class TestModel implements BasicModel<UUID> {

        private Descriptor uuid;

        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public void setId(UUID uuid) {

        }

        @Override
        public Descriptor getUuid() {
            return uuid;
        }

        @Override
        public void setUuid(Descriptor uuid) {
            this.uuid = uuid;
        }

        @Override
        public String getIri() {
            return null;
        }

        @Override
        public void setIri(String iri) {

        }
    }
}