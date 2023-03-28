package io.extremum.sharedmodels.basic;

import java.io.Serializable;
import java.util.Objects;

public class GraphQlListEdge<T> implements Serializable {
    private T node;

    public T getNode() {
        return node;
    }

    public void setNode(T node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "GraphQlListEdges{" +
                "node=" + node +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphQlListEdge<?> that = (GraphQlListEdge<?>) o;

        return Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return node != null ? node.hashCode() : 0;
    }
}
