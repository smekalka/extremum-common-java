package io.extremum.common.walk;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
class DeepContentsGraphWalkerTest {
    private final DeepContentsGraphWalker rootToLeavesWalker = new DeepContentsGraphWalker(
            VisitDirection.ROOT_TO_LEAVES, 10);
    private final ValueCollector collector = new ValueCollector();

    @Test
    void whenObjectHasNoIntanceField_thenNothingShouldBeVisited() {
        rootToLeavesWalker.walk(new Object(), collector);

        assertThat(collector.getValues(), hasSize(0));
    }

    @Test
    void whenObjectHasInstanceFields_thenAllOfThemShouldBeVisited() {
        rootToLeavesWalker.walk(new ShallowBean(), collector);

        assertThat(collector.getValues(), hasSize(3));
        assertThat(collector.getValues(), hasItems("abc", 10L, 20));
    }

    @Test
    void whenRootObjectIsNull_thenaNullPointerExceptionShouldBeThrown() {
        try {
            rootToLeavesWalker.walk(null, collector);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Root cannot be null"));
        }
    }

    @Test
    void whenObjectHasEmbeddedObjects_thenAllTheirFieldsShouldBeVisited() {
        DeepBean root = new DeepBean();
        rootToLeavesWalker.walk(root, collector);

        assertThat(collector.getValues(), hasSize(5));
        assertThat(collector.getValues(), hasItems("I'm deep", root.shallowBean, "abc", 10L, 20));
    }

    @Test
    void whenObjectHasEmbeddedObjectsViaIterable_thenAllTheirFieldsShouldBeVisited() {
        Object embedded1 = new Container("test1");
        Object embedded2 = new Container("test2");
        List<Object> list = Arrays.asList(embedded1, embedded2);

        rootToLeavesWalker.walk(new Container(list), collector);

        assertThat(collector.getValues(), hasSize(5));
        assertThat(collector.collectedSet(),
                is(equalTo(ImmutableSet.of(list, embedded1, embedded2, "test1", "test2"))));
    }

    @Test
    void whenObjectHasEmbeddedObjectsViaArray_thenAllTheirFieldsShouldBeVisited() {
        Object embedded1 = new Container("test1");
        Object embedded2 = new Container("test2");
        Object[] array = new Object[]{embedded1, embedded2};

        rootToLeavesWalker.walk(new Container(array), collector);

        assertThat(collector.getValues(), hasSize(5));
        assertThat(collector.collectedSet(),
                is(equalTo(ImmutableSet.of(array, embedded1, embedded2, "test1", "test2"))));
    }

    @Test
    void whenObjectHasRecursiveReferences_thenFieldsShouldBeVisitedNormally() {
        Container a = new Container();
        Container b = new Container(a);
        a.object = b;

        rootToLeavesWalker.walk(new Container(a), collector);

        assertThat(collector.getValues(), hasSize(2));
        assertThat(collector.collectedSet(), is(equalTo(ImmutableSet.of(a, b))));
    }
    
    @Test
    void whenObjectHasRecursiveReferencesVia_thenFieldsShouldBeVisitedNormally() {
        Container a = new Container();
        Container b = new Container(a);
        a.object = b;

        rootToLeavesWalker.walk(new Container(a), collector);

        assertThat(collector.getValues(), hasSize(2));
        assertThat(collector.collectedSet(), is(equalTo(ImmutableSet.of(a, b))));
    }

    @Test
    void givenDepthIsLimited_whenThereAreFieldsDeeperThanTheLimit_thenTheyShouldNotBeVisited() {
        Container c = new Container("test");
        Container b = new Container(c);
        Container a = new Container(b);

        DeepContentsGraphWalker limitedWalker = new DeepContentsGraphWalker(VisitDirection.ROOT_TO_LEAVES, 2);

        limitedWalker.walk(new Container(a), collector);

        assertThat(collector.getValues(), hasSize(2));
        assertThat(collector.collectedSet(), is(equalTo(ImmutableSet.of(a, b))));
    }

    @Test
    void givenPredicateDoesNotAllowToVisitAnything_whenThereAreFieldsToVisit_nothingShouldBeVisited() {
        DeepContentsGraphWalker dontGoDeeper = new DeepContentsGraphWalker(VisitDirection.ROOT_TO_LEAVES,
                10, object -> false);

        DeepBean root = new DeepBean();
        dontGoDeeper.walk(root, collector);

        assertThat(collector.getValues(), hasSize(2));
        assertThat(collector.getValues(), hasItems("I'm deep", root.shallowBean));
    }

    @Test
    void givenWalkerVisitsRootToLeaves_whenVisiting_thenRootShouldBeVisitedFirst() {
        Object leaf = "leaf";
        Container root = new Container(leaf);

        rootToLeavesWalker.walk(new Container(root), collector);

        assertThat(collector.getValues(), is(Arrays.asList(root, leaf)));
    }

    @Test
    void givenWalkerVisitsLeavesToRoot_whenVisiting_thenRootShouldBeVisitedFirst() {
        DeepContentsGraphWalker leavesToRootWalker = new DeepContentsGraphWalker(VisitDirection.LEAVES_TO_ROOT, 10);

        Object leaf = "leaf";
        Container root = new Container(leaf);

        leavesToRootWalker.walk(new Container(root), collector);

        assertThat(collector.getValues(), is(Arrays.asList(leaf, root)));
    }

    private static class DeepBean {
        @SuppressWarnings("unused")
        private final String name = "I'm deep";
        private final ShallowBean shallowBean = new ShallowBean();
    }

    private static class Container {
        @SuppressWarnings("unused")
        private Object object;

        private Container() {
        }

        private Container(Object object) {
            this.object = object;
        }
    }

    private static class ValueCollector implements ObjectVisitor {
        private final List<Object> values = new ArrayList<>();

        List<Object> getValues() {
            return values;
        }

        Set<Object> collectedSet() {
            return new HashSet<>(values);
        }

        @Override
        public void visit(Object object) {
            values.add(object);
        }
    }

}