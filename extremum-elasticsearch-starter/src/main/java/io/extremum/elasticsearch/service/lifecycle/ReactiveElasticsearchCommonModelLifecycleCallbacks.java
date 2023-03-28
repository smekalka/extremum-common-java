package io.extremum.elasticsearch.service.lifecycle;

import io.extremum.common.lifecycle.ReactiveCommonModelLifecycleSupport;
import io.extremum.elasticsearch.facilities.ReactiveElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.reactivestreams.Publisher;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.event.ReactiveAfterConvertCallback;
import org.springframework.data.elasticsearch.core.event.ReactiveAfterSaveCallback;
import org.springframework.data.elasticsearch.core.event.ReactiveBeforeConvertCallback;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

public final class ReactiveElasticsearchCommonModelLifecycleCallbacks implements
        ReactiveBeforeConvertCallback<ElasticsearchCommonModel>,
        ReactiveAfterSaveCallback<ElasticsearchCommonModel>,
        ReactiveAfterConvertCallback<ElasticsearchCommonModel> {
    private final ReactiveCommonModelLifecycleSupport<ElasticsearchCommonModel> modelsLifecycleSupport;

    public ReactiveElasticsearchCommonModelLifecycleCallbacks(
            ReactiveElasticsearchDescriptorFacilities descriptorFacilities) {
        modelsLifecycleSupport = new ReactiveCommonModelLifecycleSupport<>(descriptorFacilities,
                new ElasticsearchInternalIdAdapter());
    }

    @Override
    public Publisher<ElasticsearchCommonModel> onBeforeConvert(ElasticsearchCommonModel entity,
            IndexCoordinates index) {
        return modelsLifecycleSupport.fillRequiredFields(entity).thenReturn(entity);
    }

    @Override
    public Publisher<ElasticsearchCommonModel> onAfterSave(ElasticsearchCommonModel entity, IndexCoordinates index) {
        return modelsLifecycleSupport.createDescriptorIfNeeded(entity).thenReturn(entity);
    }

    @Override
    public Publisher<ElasticsearchCommonModel> onAfterConvert(ElasticsearchCommonModel entity, Document document,
            IndexCoordinates indexCoordinates) {
        return modelsLifecycleSupport.fillDescriptorFromInternalId(entity).thenReturn(entity);
    }
}
