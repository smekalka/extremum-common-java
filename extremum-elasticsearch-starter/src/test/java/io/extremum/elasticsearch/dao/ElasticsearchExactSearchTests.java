package io.extremum.elasticsearch.dao;

import io.extremum.elasticsearch.model.TestElasticsearchModel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
class ElasticsearchExactSearchTests {
    private final ElasticsearchCommonDao<TestElasticsearchModel> dao;

    ElasticsearchExactSearchTests(ElasticsearchCommonDao<TestElasticsearchModel> dao) {
        this.dao = dao;
    }

    String generate1ModelWithExactNameAnd2ModelsWithReversedAndAmendedNamesAndReturnExactName() {
        String exactName = generateStringWith2UniqueComponentsSeparatedWithDash();
        TestElasticsearchModel exact = modelWithName(exactName);
        TestElasticsearchModel reversed = modelWithName(splitByDashesAndReverse(exactName));
        TestElasticsearchModel longer = modelWithName(exactName + "-abc");

        dao.saveAll(Arrays.asList(exact, reversed, longer));

        return exactName;
    }

    @NotNull
    private String generateStringWith2UniqueComponentsSeparatedWithDash() {
        String component1 = generateUniqueStringInOneToken();
        String component2 = generateUniqueStringInOneToken();
        return component1 + "-" + component2;
    }

    @NotNull
    private String generateUniqueStringInOneToken() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    @NotNull
    private TestElasticsearchModel modelWithName(String name) {
        TestElasticsearchModel model1 = new TestElasticsearchModel();
        model1.setName(name);
        return model1;
    }

    private String splitByDashesAndReverse(String uniqueName) {
        List<String> fragments = Arrays.stream(uniqueName.split("-"))
                .collect(Collectors.toList());
        Collections.reverse(fragments);
        return String.join("-", fragments);
    }

    void assertThatInexactSearchYields3Results(String exactName) {
        List<TestElasticsearchModel> resultsByNonExact = dao.search(exactName, SearchOptions.defaults());

        assertThat(resultsByNonExact, hasSize(3));
    }

    void assertThatExactSearchYields1Result(String exactName) {
        List<TestElasticsearchModel> resultsByExact = dao.search(exactName,
                SearchOptions.builder().exactFieldValueMatch(true).build());

        assertThat(resultsByExact, hasSize(1));
        assertThat(resultsByExact.get(0).getName(), is(exactName));
    }
}
