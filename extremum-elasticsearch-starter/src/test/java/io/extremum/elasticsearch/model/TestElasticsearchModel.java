package io.extremum.elasticsearch.model;

import io.extremum.common.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

@ModelName("TestElasticsearchModel")
@Document(indexName = TestElasticsearchModel.INDEX, type = TestElasticsearchModel.TYPE)
@Getter
@Setter
public class TestElasticsearchModel extends ElasticsearchCommonModel {

    public static final String INDEX = "test_entities";
    public static final String TYPE = "_doc";

    private String name;

    public enum FIELDS {
        name
    }
}
