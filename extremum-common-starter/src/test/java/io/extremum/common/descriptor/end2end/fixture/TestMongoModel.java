package io.extremum.common.descriptor.end2end.fixture;

import io.extremum.common.annotation.ModelName;
import io.extremum.mongo.model.MongoCommonModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static io.extremum.common.descriptor.end2end.fixture.TestMongoModel.MODEL_NAME;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ModelName(MODEL_NAME)
public class TestMongoModel extends MongoCommonModel {

    public static final String MODEL_NAME = "TestMongoModel";

    private String number;

}
