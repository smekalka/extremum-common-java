package io.extremum.everything.services.collection;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.exceptions.EverythingEverythingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
final class BasicPagePicker extends AbstractPagePicker<BasicModel> {
    @Override
    List<BasicModel> convertToModels(Collection<?> nonEmptyCollection, Model host, String hostAttributeName) {
        return nonEmptyCollection.stream()
                .map(element -> convertElementToBasicModel(element, host, hostAttributeName))
                .collect(Collectors.toList());
    }

    @Override
    List<BasicModel> sortModelsIfPossible(List<BasicModel> fullList) {
        return DatePickers.sortModels(fullList, modelsComparator());
    }

    private BasicModel convertElementToBasicModel(Object element, Model host, String hostAttributeName) {
        if (!(element instanceof BasicModel)) {
            String message = DatePickers.unsupportedCollectionElementClassMessage(element, BasicModel.class, host,
                    hostAttributeName);
            throw new EverythingEverythingException(message);
        }

        return (BasicModel) element;
    }

    private Comparator<BasicModel> modelsComparator() {
        return Comparator.comparing(BasicModel::getId, Comparator.nullsFirst(new IdComparator()));
    }

    final List<Model> filterIsPossible(List<BasicModel> nonEmptyFullList, Projection projection) {
        if (projection.definesFilteringOnCreationDate()) {
            throw new EverythingEverythingException(
                    "Cannot filter BasicModel instances on creation date; please do not specify since/until");
        }

        return new ArrayList<>(nonEmptyFullList);
    }
}
