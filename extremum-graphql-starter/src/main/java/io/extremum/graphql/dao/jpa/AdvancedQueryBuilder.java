package io.extremum.graphql.dao.jpa;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.data.domain.Pageable;

import javax.persistence.Query;

public interface AdvancedQueryBuilder {
    <Folder extends BasicModel<?>, Item> Query composeQueryForCollectionIds(Folder folder, Class<Item> nestedClass, String collectionName, Pageable paging);

    <Folder extends BasicModel<?>, Item> Query composeQueryForCollectionIds(Folder folder, Class<Item> nestedClass, String collectionName, Pageable paging, String joinTable, String reverseId);

    <Folder, Item> Query deleteNestedElementCollectionQuery(String collectionName, Folder model, Item item);

    <Folder, Item extends BasicModel<?>> Query deleteNestedModelCollectionQuery(String collectionName, Folder model, Item item);

    <Folder extends BasicModel<?>> Query insertToNestedModelCollectionQuery(Folder model, String collectionName, Descriptor d);

    <Folder extends BasicModel<?>, Item> Query insertToNestedElementCollectionQuery(Folder model, String collectionName, Item item);
}
