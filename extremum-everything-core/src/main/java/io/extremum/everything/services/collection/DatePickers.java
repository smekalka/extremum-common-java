package io.extremum.everything.services.collection;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.utils.ModelUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author rpuch
 */
class DatePickers {
    static <T extends Model> List<T> sortModels(List<T> fullList, Comparator<T> comparator) {
        List<T> sortedFullList = new ArrayList<>(fullList);
        sortedFullList.sort(comparator);
        return sortedFullList;
    }

    static String unsupportedCollectionElementClassMessage(Object collectionElement,
            Class<? extends Model> expectedModelClassBound, Model host,
            String hostAttributeName) {
        String name = ModelUtils.getModelName(host);
        return String.format("For entity '%s', field name '%s', collection elements must be String," +
                        " ObjectId, or %s instances, but encountered '%s'", name, hostAttributeName,
                expectedModelClassBound.getSimpleName(), collectionElement.getClass());
    }
}
