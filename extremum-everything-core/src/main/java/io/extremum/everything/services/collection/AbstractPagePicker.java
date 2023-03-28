package io.extremum.everything.services.collection;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;

import java.util.Collection;
import java.util.List;

/**
 * @author rpuch
 */
abstract class AbstractPagePicker<T extends Model> implements PagePicker {
    @Override
    public final CollectionFragment<Model> getModelsFromModelsCollection(Collection<?> nonEmptyCollection,
            Projection projection, Model host, String hostAttributeName) {
        List<T> fullList = convertToModels(nonEmptyCollection, host, hostAttributeName);
        List<T> sortedFullList = sortModelsIfPossible(fullList);
        return filterAndProject(sortedFullList, projection);
    }

    abstract List<T> convertToModels(Collection<?> nonEmptyCollection, Model host, String hostAttributeName);

    abstract List<T> sortModelsIfPossible(List<T> fullList);

    private CollectionFragment<Model> filterAndProject(List<T> nonEmptyFullList, Projection projection) {
        List<Model> filteredList = filterIsPossible(nonEmptyFullList, projection);
        List<Model> fragmentElements = projection.cut(filteredList);
        return CollectionFragment.forFragment(fragmentElements, filteredList.size());
    }

    abstract List<Model> filterIsPossible(List<T> nonEmptyFullList, Projection projection);
}
