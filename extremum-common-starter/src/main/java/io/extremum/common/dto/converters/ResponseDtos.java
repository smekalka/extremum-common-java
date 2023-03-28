package io.extremum.common.dto.converters;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;

public class ResponseDtos {
    public static void fillFromModel(CommonResponseDto dto, Model model) {
        if (model instanceof BasicModel) {
            BasicModel<?> basicModel = (BasicModel<?>) model;
            dto.setId(basicModel.getUuid());
        }
        if (model instanceof PersistableCommonModel) {
            PersistableCommonModel<?> persistableModel = (PersistableCommonModel<?>) model;
            dto.setCreated(persistableModel.getCreated());
            dto.setModified(persistableModel.getModified());
            dto.setVersion(persistableModel.getVersion());
        }
    }

    private ResponseDtos() {}
}
