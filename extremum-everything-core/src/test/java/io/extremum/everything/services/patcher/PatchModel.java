package io.extremum.everything.services.patcher;

import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.annotation.ModelName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ModelName("patchModel")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
class PatchModel extends MongoCommonModel {
    private String name;
}
