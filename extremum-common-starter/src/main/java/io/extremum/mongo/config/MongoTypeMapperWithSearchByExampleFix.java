package io.extremum.mongo.config;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.convert.MappingContextTypeInformationMapper;
import org.springframework.data.mapping.Alias;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.Set;

/**
 * This fixes the following discrepancy. When searching by example, a type restriction may
 * be applied, that is, 'give me documents for which properties are same as in this object,
 * AND having the same type'. Type is recorded is '_class' attribute in Mongo; this attribute
 * is filled by Spring Data Mongo's mapper with a type alias.
 * Sometimes, we do not want to save that '_class' attribute. When we don't, search-by-example
 * queries' type restriction condition is turned into something like "_class $in []", so
 * no document is found.
 * We fix this by removing the type restriction for the cases where we get that empty $in argument.
 *
 * @author rpuch
 */
class MongoTypeMapperWithSearchByExampleFix extends DefaultMongoTypeMapper {
    private final String typeKey;

    public MongoTypeMapperWithSearchByExampleFix(MappingContextTypeInformationMapper typeInformationMapper) {
        super(DefaultMongoTypeMapper.DEFAULT_TYPE_KEY, Collections.singletonList(typeInformationMapper));
        typeKey = DefaultMongoTypeMapper.DEFAULT_TYPE_KEY;
    }

    @Override
    public void writeTypeRestrictions(Document result, Set<Class<?>> restrictedTypes) {
        if (ObjectUtils.isEmpty(restrictedTypes)) {
            return;
        }

        BasicDBList restrictedMappedTypes = new BasicDBList();

        for (Class<?> restrictedType : restrictedTypes) {

            Alias typeAlias = getAliasFor(ClassTypeInformation.from(restrictedType));

            if (!ObjectUtils.nullSafeEquals(Alias.NONE, typeAlias) && typeAlias.isPresent()) {
                restrictedMappedTypes.add(typeAlias.getValue());
            }
        }

        // extremum patch start
        if (restrictedMappedTypes.isEmpty()) {
            return;
        }
        // extremum patch end

        writeTypeTo(result, new Document("$in", restrictedMappedTypes));
    }

    private void writeTypeTo(Bson sink, Object alias) {

        if (typeKey != null) {

            if (sink instanceof Document) {
                ((Document) sink).put(typeKey, alias);
            } else if (sink instanceof DBObject) {
                ((DBObject) sink).put(typeKey, alias);
            }
        }
    }
}
