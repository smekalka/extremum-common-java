package io.extremum.graphql.model.relay;

import graphql.annotations.connection.PaginatedData;
import graphql.relay.Connection;
import graphql.relay.ConnectionCursor;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultEdge;
import graphql.relay.DefaultPageInfo;
import graphql.relay.Edge;
import graphql.relay.PageInfo;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DefaultPaginatedDataConnection<T> implements Connection<T>, PaginatedData<T> {

    @Getter
    private final Page<T> dataPage;
    private final Pageable pageable;
    private final ConnectionCursor startCursor;
    private final ConnectionCursor endCursor;

    public DefaultPaginatedDataConnection(Page<T> dataPage) {
        this.dataPage = dataPage;
        this.pageable = dataPage.getPageable();
        this.startCursor = new DefaultConnectionCursor(String.valueOf(pageable.getOffset()));
        this.endCursor = new DefaultConnectionCursor(String.valueOf(pageable.getOffset() + dataPage.getContent().size() - 1));
    }

    @Override
    public List<Edge<T>> getEdges() {
        AtomicLong index = new AtomicLong(pageable.getOffset());

        return dataPage
                .getContent()
                .stream()
                .map(item -> new DefaultEdge<>(item, new DefaultConnectionCursor(String.valueOf(index.getAndIncrement()))))
                .collect(Collectors.toList());
    }

    @Override
    public PageInfo getPageInfo() {
        return new DefaultPageInfo(startCursor, endCursor, dataPage.hasPrevious(), dataPage.hasNext());
    }

    @Override
    public boolean hasNextPage() {
        return dataPage.hasNext();
    }

    @Override
    public boolean hasPreviousPage() {
        return dataPage.hasPrevious();
    }

    @Override
    public String getCursor(T entity) {
        return String.valueOf(dataPage.getContent().indexOf(entity) + pageable.getOffset());
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return dataPage.iterator();
    }
}
