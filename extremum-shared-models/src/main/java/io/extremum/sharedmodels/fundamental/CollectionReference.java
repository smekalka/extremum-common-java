package io.extremum.sharedmodels.fundamental;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.extremum.sharedmodels.annotation.DocumentationName;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

/**
 * @author rpuch
 */
@Getter
@DocumentationName("Collection")
public class CollectionReference<T> {
    /**
     * The UUID of the collection, which can be used for fetching its data.
     */
    @JsonProperty("@uuid")
    private String id;

    /**
     * The unique IRI of the collection, which can be used for fetching its data.
     */
    @JsonldId
    private String iri;

    /**
     * The count of elements in the collection (if it's countable).
     */
    private Long count;

    /**
     * The list of top N elements in the collection (if provided).
     */
    private List<T> top;

    public static <T> CollectionReference<T> forUnknownTotalSize(List<T> top) {
        return new CollectionReference<>(top, null);
    }

    public static <T> CollectionReference<T> uninitialized() {
        return new CollectionReference<>(null, null);
    }

    public static <T> CollectionReference<T> withTotal(long total) {
        return new CollectionReference<>(null, (Long) total);
    }

    public CollectionReference() {
        this(emptyList());
    }

    public CollectionReference(List<T> list) {
        this(list, list.size());
    }

    public CollectionReference(List<T> top, long total) {
        this(Objects.requireNonNull(top, "top cannot be null"), (Long) total);
    }

    private CollectionReference(List<T> top, Long total) {
        this.count = total;
        this.top = top;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public void setTop(List<T> top) {
        Objects.requireNonNull(top, "top cannot be null");
        this.top = top;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
