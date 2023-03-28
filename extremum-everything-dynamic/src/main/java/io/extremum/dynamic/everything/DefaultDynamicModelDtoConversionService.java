package io.extremum.dynamic.everything;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.common.dto.converters.services.DynamicModelDtoConversionService;
import io.extremum.common.model.VersionedModel;
import io.extremum.dynamic.everything.dto.JsonDynamicModelResponseDto;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class DefaultDynamicModelDtoConversionService implements DynamicModelDtoConversionService {
    @Override
    public Mono<ResponseDto> convertToResponseDtoReactively(Model model, ConversionConfig config) {
        if (model instanceof JsonDynamicModel) {
            return Mono.fromSupplier(() -> {
                JsonDynamicModel dModel = (JsonDynamicModel) model;

                JsonDynamicModelResponseDto dto = new JsonDynamicModelResponseDto();
                Map<String, Object> data = new HashMap<>(dModel.getModelData());
                removeTechnicalData(data);
                dto.setData(data);
                dto.setId(dModel.getId());

                return dto;
            });
        } else {
            return Mono.error(new IllegalArgumentException("Only JsonDynamicModel supported"));
        }
    }

    private void removeTechnicalData(Map<String, Object> data) {
        data.remove("_id");
        data.remove(VersionedModel.FIELDS.lineageId.name());
        data.remove(VersionedModel.FIELDS.currentSnapshot.name());
        data.remove(VersionedModel.FIELDS.deleted.name());
        data.remove(VersionedModel.FIELDS.start.name());
        data.remove(VersionedModel.FIELDS.end.name());
        data.remove(VersionedModel.FIELDS.uuid.name());
        data.remove(VersionedModel.FIELDS.id.name());
    }
}
