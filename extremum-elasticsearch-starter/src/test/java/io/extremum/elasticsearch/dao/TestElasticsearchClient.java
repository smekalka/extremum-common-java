package io.extremum.elasticsearch.dao;

import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
class TestElasticsearchClient {
    private final ElasticsearchProperties elasticsearchProperties;

    TestElasticsearchClient(ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    Optional<String> getAsJson(String indexName, String id) {
        try (RestHighLevelClient client = getClient()) {
            GetResponse response = client.get(
                    Requests.getRequest(indexName).id(id),
                    RequestOptions.DEFAULT
            );

            if (response.isExists()) {
                return Optional.of(response.getSourceAsString());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to get data by id " + id +
                    " from index " + indexName, e);
        }
    }

    private RestHighLevelClient getClient() {
        List<HttpHost> httpHosts = elasticsearchProperties.getHosts().stream()
                .map(h -> new HttpHost(h.getHost(), h.getPort(), h.getProtocol()))
                .collect(Collectors.toList());

        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[]{}));
        return new RestHighLevelClient(builder);
    }

    void refresh(String... indices) {
        try (RestHighLevelClient client = getClient()) {
            RefreshRequest request = Requests.refreshRequest(indices);
            client.indices().refresh(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Unable to refresh indices " + Arrays.asList(indices), e);
        }
    }
}
