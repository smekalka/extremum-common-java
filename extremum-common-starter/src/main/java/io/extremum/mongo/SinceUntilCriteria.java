package io.extremum.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.ZonedDateTime;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class SinceUntilCriteria {
    public static With with(String key) {
        return new With(key);
    }

    @RequiredArgsConstructor
    public static class With {
        private final String key;

        public Since since(ZonedDateTime since) {
            return new Since(since);
        }

        @RequiredArgsConstructor
        public class Since {
            private final ZonedDateTime since;

            public Criteria until(ZonedDateTime until) {
                return new Criteria().andOperator(
                        where(key).gte(since),
                        where(key).lt(until)
                );
            }
        }
    }

    private SinceUntilCriteria() {
    }
}
