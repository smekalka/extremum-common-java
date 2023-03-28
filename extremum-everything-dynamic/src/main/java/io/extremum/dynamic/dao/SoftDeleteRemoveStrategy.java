package io.extremum.dynamic.dao;

import com.mongodb.client.result.UpdateResult;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static io.extremum.sharedmodels.basic.Model.FIELDS.deleted;

@RequiredArgsConstructor
public class SoftDeleteRemoveStrategy implements DynamicModelRemoveStrategy {
    private final ReactiveMongoOperations mongoOperations;

    @Override
    public Mono<Void> remove(Descriptor id, String collectionName) {
        return id.getInternalIdReactively()
                .map(ObjectId::new)
                .flatMap(oId -> doRemove(oId, collectionName));
    }

    private Mono<Void> doRemove(ObjectId oId, String collectionName) {

        return mongoOperations.getCollection(collectionName)
                .flatMap(collection -> {
                    Publisher<UpdateResult> publisher1 = collection.updateOne(
                            and(
                                    eq("_id", oId),
                                    or(
                                            eq(deleted.name(), false),
                                            exists(deleted.name(), false)
                                    )
                            ),
                            set(deleted.name(), true)
                    );
                    return Mono.from(publisher1);
                })
                .then();
    }
}
