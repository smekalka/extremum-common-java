package io.extremum.graphql.dao;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.model.CollectionFilter;
import io.extremum.graphql.model.PagingAndSortingRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdvancedCommonDao {

    <Folder extends BasicModel<?>, Item> List<Item> getNestedCollection(Folder model, Class<Item> nestedClass, String collectionName, PagingAndSortingRequest paging);

    <Folder extends BasicModel<?>, Item> List<Item> getNestedCollection(Folder model, Class<Item> nestedClass, String collectionName, PagingAndSortingRequest paging, String joinTable, String reverseId);

    <Folder extends BasicModel<?>, Item> Page<Item> getNestedCollection(Folder model, CollectionFilter filter, String collectionName, PagingAndSortingRequest paging);

    <Folder extends BasicModel<?>, Item> void addToNestedCollection(Folder model, List<Item> input, String collectionName);

    <Folder extends BasicModel<?>, Item> void removeFromNestedCollection(Folder model, List<Item> input, String collectionName);

    <Item extends BasicModel<?>> Page<Item> findAll(String modelName, PagingAndSortingRequest paging);

    <Item extends BasicModel<?>> Page<Item> findAll(String modelName, CollectionFilter filter, PagingAndSortingRequest paging);

    <Item extends BasicModel<?>> Item get(String id);
}
