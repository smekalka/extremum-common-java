package io.extremum.dynamic.dao;

import com.mongodb.client.result.DeleteResult;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.*;

@RequiredArgsConstructor
public class HardDeleteRemoveStrategy implements DynamicModelRemoveStrategy {
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
                    Publisher<DeleteResult> publisher = collection.deleteOne(eq("_id", oId));
                    return Mono.from(publisher);
                })
                .then();
    }

}
