package io.extremum.everything.services.collection;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares IDs: if they are Comparable, uses natural order comparison,
 * otherwise compares by IDs key representation obtained via toString().
 *
 * @author rpuch
 */
final class IdComparator implements Comparator<Serializable> {
    private Class<?> elementClass;

    @Override
    public int compare(Serializable o1, Serializable o2) {
        // just in case...
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }

        rememberOrCheckElementClass(o1);
        rememberOrCheckElementClass(o2);

        if (o1 instanceof Comparable) {
            @SuppressWarnings("unchecked") int comparisonResult = ((Comparable) o1).compareTo(o2);
            return comparisonResult;
        }

        return o1.toString().compareTo(o2.toString());
    }

    private void rememberOrCheckElementClass(Object obj) {
        if (obj == null) {
            return;
        }
        Class<?> objClass = obj.getClass();
        if (elementClass == null) {
            elementClass = objClass;
        } else {
            if (objClass != elementClass) {
                String message = String.format("This comparator only supports comparing elements of the same " +
                        "type, but it was given '%s' and '%s'", elementClass, objClass);
                throw new IllegalStateException(message);
            }
        }
    }
}
