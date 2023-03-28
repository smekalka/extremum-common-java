package io.extremum.common.walk;

import io.extremum.common.attribute.Attribute;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
class ShallowFieldGraphWalkerTest {
    private final ShallowFieldGraphWalker walker = new ShallowFieldGraphWalker();
    private final ValueCollector collector = new ValueCollector();

    @Test
    void whenObjectHasNoIntanceField_thenNothingShouldBeVisited() {
        walker.walk(new Object(), collector);

        assertThat(collector.getValues(), hasSize(0));
    }

    @Test
    void whenObjectHasInstanceFields_thenAllOfThemShouldBeVisited() {
        walker.walk(new ShallowBean(), collector);

        assertThat(collector.getValues(), hasSize(4));
        assertThat(collector.getValues(), hasItems("abc", 10L, 20, null));
    }

    @Test
    void whenRootObjectIsNull_thenaNullPointerExceptionShouldBeThrown() {
        try {
            walker.walk(null, collector);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Root cannot be null"));
        }
    }

    private static class ValueCollector implements AttributeVisitor {
        private final List<Object> values = new ArrayList<>();

        List<Object> getValues() {
            return values;
        }

        Set<Object> collectedSet() {
            return new HashSet<>(values);
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            values.add(attribute.value());
        }
    }

}