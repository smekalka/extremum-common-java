package io.extremum.elasticsearch.dao;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClassicTestElasticsearchModelDao extends DefaultElasticsearchCommonDao<TestElasticsearchModel> {
    public ClassicTestElasticsearchModelDao(
            ElasticsearchProperties elasticsearchProperties,
            DescriptorService descriptorService,
            ElasticsearchDescriptorFacilities descriptorFacilities,
            ObjectMapper mapper) {
        super(elasticsearchProperties, descriptorService, descriptorFacilities, mapper,
                TestElasticsearchModel.INDEX, "_doc");
    }
}
