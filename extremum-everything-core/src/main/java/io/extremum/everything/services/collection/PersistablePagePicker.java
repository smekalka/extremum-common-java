package io.extremum.everything.services.collection;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.exceptions.EverythingEverythingException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
final class PersistablePagePicker extends AbstractPagePicker<PersistableCommonModel> {
    @Override
    List<PersistableCommonModel> convertToModels(Collection<?> nonEmptyCollection, Model host, String hostAttributeName) {
        return nonEmptyCollection.stream()
                .map(element -> convertElementToPersistableModel(element, host, hostAttributeName))
                .collect(Collectors.toList());
    }

    @Override
    List<PersistableCommonModel> sortModelsIfPossible(List<PersistableCommonModel> fullList) {
        return DatePickers.sortModels(fullList, modelsComparator());
    }

    private PersistableCommonModel convertElementToPersistableModel(Object element, Model host,
            String hostAttributeName) {
        if (!(element instanceof PersistableCommonModel)) {
            String message = DatePickers.unsupportedCollectionElementClassMessage(element, PersistableCommonModel.class,
                    host, hostAttributeName);
            throw new EverythingEverythingException(message);
        }

        return (PersistableCommonModel) element;
    }

    private Comparator<PersistableCommonModel> modelsComparator() {
        Comparator<PersistableCommonModel> compareByCreated = Comparator.comparing(PersistableCommonModel::getCreated,
                Comparator.nullsFirst(Comparator.naturalOrder()));
        return compareByCreated
                .thenComparing(BasicModel::getId, Comparator.nullsFirst(new IdComparator()));
    }

    final List<Model> filterIsPossible(List<PersistableCommonModel> nonEmptyFullList, Projection projection) {
        return nonEmptyFullList.stream()
                .filter(projection::accepts)
                .filter(PersistableCommonModel::isNotDeleted)
                .collect(Collectors.toList());
    }
}
