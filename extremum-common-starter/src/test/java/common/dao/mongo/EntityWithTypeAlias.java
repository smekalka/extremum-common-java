package common.dao.mongo;

import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.annotation.ModelName;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static common.dao.mongo.EntityWithTypeAlias.COLLECTION;

/**
 * @author rpuch
 */
@Document(COLLECTION)
@ModelName(COLLECTION)
@TypeAlias("the-alias")
class EntityWithTypeAlias extends MongoCommonModel {
    static final String COLLECTION = "EntityWithTypeAlias";
}
