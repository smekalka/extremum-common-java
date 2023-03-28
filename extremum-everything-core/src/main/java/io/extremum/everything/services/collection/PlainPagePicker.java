package io.extremum.everything.services.collection;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.exceptions.EverythingEverythingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
final class PlainPagePicker extends AbstractPagePicker<Model> {
    @Override
    List<Model> convertToModels(Collection<?> nonEmptyCollection, Model host, String hostAttributeName) {
        return nonEmptyCollection.stream()
                .map(element -> convertElementToModel(element, host, hostAttributeName))
                .collect(Collectors.toList());
    }

    @Override
    List<Model> sortModelsIfPossible(List<Model> fullList) {
        return fullList;
    }

    private Model convertElementToModel(Object element, Model host, String hostAttributeName) {
        if (!(element instanceof Model)) {
            String message = DatePickers.unsupportedCollectionElementClassMessage(element, Model.class,
                    host, hostAttributeName);
            throw new EverythingEverythingException(message);
        }

        return (Model) element;
    }

    final List<Model> filterIsPossible(List<Model> nonEmptyFullList, Projection projection) {
        if (projection.definesFilteringOnCreationDate()) {
            throw new EverythingEverythingException(
                    "Cannot filter plain Model instances on creation date; please do not specify since/until");
        }

        return new ArrayList<>(nonEmptyFullList);
    }
}
