package io.extremum.mongo.model;

import io.extremum.common.model.NamedModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Getter
@Setter
public abstract class MongoNamedModel extends MongoCommonModel implements NamedModel<ObjectId> {

    private String slug;
    private StringOrMultilingual name;
    private StringOrMultilingual description;

    @Override
    public void copyServiceFieldsTo(Model to) {
        super.copyServiceFieldsTo(to);
    }

    @Override
    public boolean isNotDeleted() {
        return super.isNotDeleted();
    }
}
