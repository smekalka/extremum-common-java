package io.extremum.common.walk;

import com.google.common.collect.ImmutableList;
import io.extremum.common.attribute.Attribute;
import io.extremum.common.attribute.InstanceAttributes;
import io.extremum.common.exceptions.ProgrammingErrorException;

import java.util.*;
import java.util.function.Predicate;

/**
 * Deep attribute graph walker. Considers both fields and properties,
 * proceeds to their attributes as well. The maximum depth is controllable
 * via constructor parameters, as well as a rule that defines whether
 * the walker should go 'inside' current object.
 *
 * @author rpuch
 */
public class DeepContentsGraphWalker implements ObjectContentsGraphWalker {
    private static final List<String> PREFIXES_TO_IGNORE = ImmutableList.of("java", "sun.");
    private static final int INITIAL_DEPTH = 1;

    private final VisitDirection visitDirection;
    private final int maxLevel;
    private final Predicate<Object> shouldGoDeeperPredicate;

    public DeepContentsGraphWalker(VisitDirection visitDirection, int maxLevel) {
        this(visitDirection, maxLevel, object -> true);
    }

    public DeepContentsGraphWalker(VisitDirection visitDirection, int maxLevel,
                                   Predicate<Object> shouldGoDeeperPredicate) {
        this.visitDirection = visitDirection;
        this.maxLevel = maxLevel;
        this.shouldGoDeeperPredicate = shouldGoDeeperPredicate;
    }

    @Override
    public void walk(Object root, ObjectVisitor visitor) {
        Objects.requireNonNull(root, "Root cannot be null");
        Objects.requireNonNull(visitor, "Visitor cannot be null");

        walkContentsRecursively(root, new Context(visitor), INITIAL_DEPTH);
    }

    private void walkContentsRecursively(Object currentTarget, Context context, int currentDepth) {
        new InstanceAttributes(currentTarget).stream()
                .forEach(attribute -> introspectAttribute(attribute, context, currentDepth));
    }

    private void introspectAttribute(Attribute attribute, Context context, int currentDepth) {
        Object attributeValue = attribute.value();
        if (attributeValue == null) {
            return;
        }

        if (context.alreadySeen(attributeValue)) {
            return;
        }
        context.rememberAsSeen(attributeValue);

        visitWithAttribute(attribute.value(), context, currentDepth);
    }

    private void visitWithAttribute(Object object, Context context, int currentDepth) {
        if (visitDirection == VisitDirection.ROOT_TO_LEAVES) {
            context.visitObject(object);
            goDeeperIfNeeded(object, context, currentDepth);
        } else if (visitDirection == VisitDirection.LEAVES_TO_ROOT) {
            goDeeperIfNeeded(object, context, currentDepth);
            context.visitObject(object);
        } else {
            throw new ProgrammingErrorException("Unsupported visit direction " + visitDirection);
        }
    }

    private void goDeeperIfNeeded(Object nextValue, Context context, int currentDepth) {
        if (nextValue instanceof Iterable) {
            @SuppressWarnings("unchecked") Iterable<Object> iterable = (Iterable<Object>) nextValue;
            goDeeperThroughIterable(iterable, context, currentDepth);
        } else if (nextValue instanceof Object[]) {
            Object[] array = (Object[]) nextValue;
            goDeeperThroughIterable(Arrays.asList(array), context, currentDepth);
        } else if (shouldGoDeeper(nextValue, currentDepth)) {
            walkContentsRecursively(nextValue, context, currentDepth + 1);
        }
    }

    private void goDeeperThroughIterable(Iterable<Object> iterable,
            Context context, int currentDepth) {
        iterable.forEach(element -> {
            if (element != null) {
                visitAndGoDeeperThroughIterableElement(element, context, currentDepth);
            }
        });
    }

    private void visitAndGoDeeperThroughIterableElement(Object element, Context context, int currentDepth) {
        visitWithAttribute(element, context, currentDepth);
        if (shouldGoDeeper(element, currentDepth)) {
            walkContentsRecursively(element, context, currentDepth + 1);
        }
    }

    private boolean shouldGoDeeper(Object nextValue, int currentDepth) {
        if (currentDepth >= maxLevel) {
            return false;
        }

        if (nextValue == null) {
            return false;
        }

        Class<?> nextClass = nextValue.getClass();

        if (nextClass.getPackage() == null) {
            // something like an array class
            return false;
        }
        if (isJavaSystemClass(nextClass)) {
            return false;
        }

        if (!shouldGoDeeperPredicate.test(nextValue)) {
            return false;
        }
        
        return true;
    }

    private boolean isJavaSystemClass(Class<?> nextClass) {
        return PREFIXES_TO_IGNORE.stream().anyMatch(prefix -> nextClass.getPackage().getName().startsWith(prefix));
    }

    private static class Context {
        private final ObjectVisitor visitor;
        private final Map<Object, Object> visitedObjects = new IdentityHashMap<>();

        private Context(ObjectVisitor visitor) {
            this.visitor = visitor;
        }

        boolean alreadySeen(Object object) {
            return visitedObjects.containsKey(object);
        }

        void rememberAsSeen(Object object) {
            if (object != null) {
                visitedObjects.put(object, object);
            }
        }

        void visitObject(Object object) {
            visitor.visit(object);
        }
    }
}
