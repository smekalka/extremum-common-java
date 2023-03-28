package common.dao.mongo.versioned;

import io.extremum.common.annotation.ModelName;
import io.extremum.mongo.model.MongoVersionedModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("versionedModel")
@ModelName("TestMongoVersionedModel")
@Getter
@Setter
@ToString
public class TestMongoVersionedModel extends MongoVersionedModel {
    private String name;

    public TestMongoVersionedModel() {
    }

    public TestMongoVersionedModel(String name) {
        this.name = name;
    }
}
