package io.extremum.elasticsearch;

import io.extremum.common.model.PersistableCommonModel;
import io.extremum.elasticsearch.springdata.repository.CriteriaQueryProcessor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.query.Criteria;

import static org.springframework.data.elasticsearch.core.query.Criteria.where;

/**
 * @author rpuch
 */
public class SoftDeletion {
    private static final String DELETED = PersistableCommonModel.FIELDS.deleted.name();

    public Criteria notDeleted() {
        return where(DELETED).not().is(true);
    }

    public QueryBuilder amendQueryBuilderWithNotDeletedCondition(QueryBuilder query) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.must(query);
        QueryBuilder notDeletedQuery = new CriteriaQueryProcessor().createQueryFromCriteria(notDeleted());
        boolQueryBuilder.must(notDeletedQuery);

        return boolQueryBuilder;
    }
}
