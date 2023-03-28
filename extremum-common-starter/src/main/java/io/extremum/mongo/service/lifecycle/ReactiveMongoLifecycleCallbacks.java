package io.extremum.mongo.service.lifecycle;

import io.extremum.common.facilities.ReactiveDescriptorFacilities;
import io.extremum.common.lifecycle.InternalIdAdapter;
import io.extremum.common.lifecycle.ReactiveCommonModelLifecycleSupport;
import io.extremum.sharedmodels.basic.HasUuid;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterSaveCallback;
import org.springframework.data.mongodb.core.mapping.event.ReactiveBeforeConvertCallback;

/**
 * @author rpuch
 */
public abstract class ReactiveMongoLifecycleCallbacks<T extends HasUuid> implements
        ReactiveBeforeConvertCallback<T>, ReactiveAfterSaveCallback<T>, ReactiveAfterConvertCallback<T> {
    private final ReactiveCommonModelLifecycleSupport<T> modelsLifecycleSupport;

    public ReactiveMongoLifecycleCallbacks(ReactiveDescriptorFacilities descriptorFacilities,
                                          InternalIdAdapter<? super T> adapter) {
        modelsLifecycleSupport = new ReactiveCommonModelLifecycleSupport<>(descriptorFacilities, adapter);
    }

    @Override
    public Publisher<T> onBeforeConvert(T entity, String collection) {
        return modelsLifecycleSupport.fillRequiredFields(entity).thenReturn(entity);
    }

    @Override
    public Publisher<T> onAfterSave(T entity, Document document, String collection) {
        return modelsLifecycleSupport.createDescriptorIfNeeded(entity).thenReturn(entity);
    }

    @Override
    public Publisher<T> onAfterConvert(T entity, Document document, String collection) {
        return modelsLifecycleSupport.fillDescriptorFromInternalId(entity).thenReturn(entity);
    }
}
