package io.extremum.everything.dao;

import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.annotation.ModelName;
import io.extremum.everything.collection.CollectionElementType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import static io.extremum.everything.dao.Street.MODEL_NAME;

/**
 * @author rpuch
 */
@Document(MODEL_NAME)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ModelName(MODEL_NAME)
public class Street extends MongoCommonModel {
    public static final String MODEL_NAME = "Street";

    private String name;
    @CollectionElementType(House.class)
    private List<String> houses = new ArrayList<>();

    public enum FIELDS {
        name, houses
    }
}
