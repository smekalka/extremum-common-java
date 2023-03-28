package io.extremum.mongo;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class SinceUntilCriteriaTest {
    @Test
    void constructsCorrectSinceUntilCriteria() {
        ZonedDateTime since = ZonedDateTime.now().minusDays(1);
        ZonedDateTime until = ZonedDateTime.now().plusDays(1);

        Criteria criteria = SinceUntilCriteria.with("created").since(since).until(until);

        Document criteriaDocument = criteria.getCriteriaObject();

        assertThat(criteriaDocument, equalTo(doc(
                "$and", ImmutableList.of(
                        doc("created", doc("$gte", since)),
                        doc("created", doc("$lt", until))
                )
        )));
    }

    private Document doc(String key, Object value) {
        return new Document(ImmutableMap.of(key, value));
    }
}