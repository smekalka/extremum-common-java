package io.extremum.elasticsearch.model;

import io.extremum.common.model.annotation.HardDelete;
import io.extremum.common.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@ModelName("HardDeleteElasticsearchModel")
@Document(indexName = HardDeleteElasticsearchModel.INDEX)
@Getter
@Setter
@HardDelete
public class HardDeleteElasticsearchModel extends ElasticsearchCommonModel {

    public static final String INDEX = "hard_delete_test_entities";

    @Field(type = FieldType.Text)
    private String name;

    public enum FIELDS {
        name
    }
}
