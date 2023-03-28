package models;

import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.model.annotation.HardDelete;
import io.extremum.common.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import static models.HardDeleteMongoModel.COLLECTION;

@ModelName(COLLECTION)
@Document(COLLECTION)
@Getter @Setter
@HardDelete
public class HardDeleteMongoModel extends MongoCommonModel {

    static final String COLLECTION = "hardDelete";
    private String name;

    public enum FIELDS {
        name
    }
}
