package io.extremum.common.descriptor.factory.impl;

import org.bson.types.ObjectId;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author rpuch
 */
public class StringifiedObjectIdMatcher extends TypeSafeMatcher<String> {
    public static StringifiedObjectIdMatcher objectId() {
        return new StringifiedObjectIdMatcher();
    }

    @Override
    protected boolean matchesSafely(String item) {
        try {
            new ObjectId(item);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("String representation of ObjectId");
    }
}
