package io.extremum.everything.services.management;


import org.junit.jupiter.api.*;

class VocabularyBasedModelCollectionNameResolverTest {

    @Test
    public void should_return_model_name_by_collection_name() {
        DefaultModelCollectionNameResolver defaultModelCollectionNameResolver = new DefaultModelCollectionNameResolver();
        Assertions.assertEquals("cat", defaultModelCollectionNameResolver.resolveModelName("cats"));
        Assertions.assertEquals("movie", defaultModelCollectionNameResolver.resolveModelName("movies"));
        Assertions.assertEquals("axis", defaultModelCollectionNameResolver.resolveModelName("axes"));
        Assertions.assertEquals("tooth", defaultModelCollectionNameResolver.resolveModelName("teeth"));
        Assertions.assertEquals("fish", defaultModelCollectionNameResolver.resolveModelName("fish"));
        Assertions.assertEquals("person", defaultModelCollectionNameResolver.resolveModelName("persons"));

        defaultModelCollectionNameResolver.register("person", "swarm");
        Assertions.assertEquals("person", defaultModelCollectionNameResolver.resolveModelName("swarm"));
    }
}