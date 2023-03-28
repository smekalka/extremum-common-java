package io.extremum.sharedmodels.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Формат для полей-списков в GraphQL моделях.
 * Для списочных полей в парадигме GraphQL нужно указывать
 * - пагинацию в запросах и
 * - вложенные поля в edges { node { поля }}
 * - в ответах приходят объекты в "edges" [{ "node": { объект } "node": { объект }...}]
 * За эти требования отвечает этот формат.
 */
public class GraphQlList<T> implements Serializable {
    private List<GraphQlListEdge<T>> edges = new ArrayList<>();

    public List<GraphQlListEdge<T>> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphQlListEdge<T>> edges) {
        this.edges = edges;
    }

    public List<T> getValues() {
        return edges.stream().map(GraphQlListEdge::getNode).collect(Collectors.toList());
    }

    public GraphQlList() {
    }

    public GraphQlList(List<T> values) {
        List<GraphQlListEdge<T>> newEdges = values.stream()
                .map(it -> {
                    GraphQlListEdge<T> edge = new GraphQlListEdge<>();
                    edge.setNode(it);
                    return edge;
                })
                .collect(Collectors.toList());
        setEdges(newEdges);
    }

    @Override
    public String toString() {
        return "GraphQlList{" +
                "edges=" + edges +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphQlList<?> that = (GraphQlList<?>) o;

        return Objects.equals(edges, that.edges);
    }

    @Override
    public int hashCode() {
        return edges != null ? edges.hashCode() : 0;
    }
}
