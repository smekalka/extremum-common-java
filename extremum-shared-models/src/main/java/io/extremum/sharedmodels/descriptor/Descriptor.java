package io.extremum.sharedmodels.descriptor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.extremum.common.annotation.FromStorageString;
import io.extremum.common.annotation.ToStorageString;
import io.extremum.sharedmodels.annotation.UsesStaticDependencies;
import io.extremum.sharedmodels.content.Display;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;
import reactor.core.publisher.Mono;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@TypeDef(name = "jsonb", typeClass = JsonType.class)
public class Descriptor implements Serializable {
    public static final String COLLECTION = "descriptor-identifiers";

    @JsonProperty("externalId")
    @Id
    private String externalId;

    @JsonProperty("iri")
    private String iri;

    @JsonProperty("type")
    private Type type;
    @JsonProperty("readiness")
    private Readiness readiness = Readiness.READY;

    @JsonProperty("internalId")
    private String internalId;
    @JsonProperty("modelType")
    private String modelType;
    @JsonProperty("storageType")
    private String storageType;

    @JsonProperty("collection")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CollectionDescriptor collection;

    @JsonProperty("owned")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OwnedModelDescriptor owned;

    @JsonProperty("created")
    private ZonedDateTime created;
    @JsonProperty("modified")
    private ZonedDateTime modified;
    @JsonProperty("version")
    private Long version;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("display")
    private Display display;

    @org.hibernate.annotations.Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> stump = new HashMap<>();

    public Descriptor() {
    }

    public Descriptor(String externalId) {
        this.externalId = externalId;
    }

    public static Descriptor forCollection(String externalId, CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = new Descriptor(externalId);
        descriptor.type = Type.COLLECTION;
        descriptor.collection = collectionDescriptor;
        descriptor.readiness = Readiness.READY;
        return descriptor;
    }


    public static Descriptor forOwnedModel(String externalId, OwnedModelDescriptor ownedModelDescriptor) {
        Descriptor descriptor = new Descriptor(externalId);
        descriptor.type = Type.OWNED;
        descriptor.owned = ownedModelDescriptor;
        descriptor.readiness = Readiness.READY;
        descriptor.iri = ownedModelDescriptor.getCoordinates().getOwnedCoordinates().getHostId().iri + "/" + ownedModelDescriptor.getCoordinates().getOwnedCoordinates().getHostAttributeName();
        return descriptor;
    }


    public String getExternalId() {
        if (this.externalId == null) {
            fillByInternalId();
        }
        return this.externalId;
    }

    public boolean hasExternalId() {
        return externalId != null;
    }

    public Type effectiveType() {
        if (type != null) {
            return type;
        }
        if (internalId == null) {
            fillByExternalIdAndValidateAccordingToType();
        }
        return type;
    }

    @UsesStaticDependencies
    private void fillByExternalIdAndValidateAccordingToType() {
        //noinspection deprecation
        StaticDescriptorLoaderAccessor.getDescriptorLoader().loadByExternalId(this.externalId)
                .map(this::copyFieldsFromAnotherDescriptor)
                .map(descriptor -> {
                    descriptor.validateFilled();
                    return descriptor;
                })
                .orElseThrow(this::newDescriptorNotFoundByExternalIdException);
    }

    private void validateFilled() {
        if (type == Type.COLLECTION) {
            if (collection == null) {
                throw new IllegalStateException(
                        String.format("No collection in descriptor with external ID '%s'", externalId));
            }
        } else {
            if (internalId == null) {
                throw newDescriptorNotFoundByExternalIdException();
            }
        }
    }

    public String getInternalId() {
        if (this.internalId == null) {
            fillSingleByExternalId();
        }
        return this.internalId;
    }

    public boolean hasInternalId() {
        return internalId != null;
    }

    public boolean hasIri() {
        return iri != null;
    }

    @JsonIgnore
    @UsesStaticDependencies
    public Mono<String> getExternalIdReactively() {
        if (externalId != null) {
            return Mono.just(externalId);
        }
        if (internalId == null) {
            throw new IllegalStateException("Both internalId and externalId are null");
        }
        return loadByInternalIdReactively()
                .then(Mono.defer(() -> Mono.just(externalId)));
    }

    private Mono<Descriptor> loadByInternalIdReactively() {
        return StaticDescriptorLoaderAccessor.getDescriptorLoader()
                .loadByInternalIdReactively(internalId)
                .doOnNext(this::copyFieldsFromAnotherDescriptor)
                .switchIfEmpty(Mono.error(newDescriptorNotFoundByInternalIdException()));
    }

    @JsonIgnore
    @UsesStaticDependencies
    public Mono<String> getInternalIdReactively() {
        if (internalId != null) {
            return Mono.just(internalId);
        }
        if (externalId == null) {
            throw new IllegalStateException("Both internalId and externalId are null");
        }
        return loadByExternalIdReactively()
                .then(Mono.defer(() -> Mono.just(internalId)));
    }

    private Mono<Descriptor> loadByExternalIdReactively() {
        return StaticDescriptorLoaderAccessor.getDescriptorLoader()
                .loadByExternalIdReactively(externalId)
                .doOnNext(this::copyFieldsFromAnotherDescriptor)
                .switchIfEmpty(Mono.error(newDescriptorNotFoundByExternalIdException()));
    }

    public String getStorageType() {
        if (this.storageType == null) {
            fillByIds();
            if (this.storageType == null) {
                throw new IllegalStateException(
                        String.format("Storage type can't be resolved by both id's: internalId - %s; descriptorId %s",
                                internalId, externalId)
                );
            }
        }
        return this.storageType;
    }

    @JsonIgnore
    @UsesStaticDependencies
    public Mono<String> getStorageTypeReactively() {
        if (this.storageType != null) {
            return Mono.just(storageType);
        }

        if (this.externalId != null) {
            return loadByExternalIdReactively()
                    .then(Mono.defer(() -> Mono.just(storageType)));
        } else if (this.internalId != null) {
            return loadByInternalIdReactively()
                    .then(Mono.defer(() -> Mono.just(storageType)));
        } else {
            throw new IllegalStateException("Both externalId and internalId are null");
        }
    }

    public String getModelType() {
        if (this.modelType == null) {
            fillByIds();
        }
        return this.modelType;
    }

    public Mono<String> getModelTypeReactively() {
        if (this.modelType == null) {
            return fillByIdsReactively().map(Descriptor::getModelType);
        } else {
            return Mono.just(this.modelType);
        }
    }

    private void fillByIds() {
        if (this.internalId != null) {
            fillByInternalId();
        } else if (this.externalId != null) {
            fillSingleByExternalId();
        }
    }

    private Mono<Descriptor> fillByIdsReactively() {
        if (this.internalId != null) {
            return fillByInternalIdReactively();
        } else if (this.externalId != null) {
            return fillSingleByExternalIdReactively();
        } else {
            return Mono.error(new RuntimeException("Unable to fill ids: internalId && externalId are null"));
        }
    }

    @UsesStaticDependencies
    private void fillByInternalId() {
        //noinspection deprecation
        StaticDescriptorLoaderAccessor.getDescriptorLoader().loadByInternalId(internalId)
                .map(this::copyFieldsFromAnotherDescriptor)
                .filter(d -> d.externalId != null)
                .orElseThrow(this::newDescriptorNotFoundByInternalIdException);
    }

    @UsesStaticDependencies
    private Mono<Descriptor> fillByInternalIdReactively() {
        //noinspection deprecation
        return StaticDescriptorLoaderAccessor.getDescriptorLoader().loadByInternalIdReactively(internalId)
                .map(this::copyFieldsFromAnotherDescriptor)
                .filter(d -> d.externalId != null)
                .switchIfEmpty(Mono.error(newDescriptorNotFoundByInternalIdException()));
    }


    private DescriptorNotFoundException newDescriptorNotFoundByInternalIdException() {
        return new DescriptorNotFoundException(
                String.format("Internal id %s without corresponding descriptor", internalId), this
        );
    }

    @UsesStaticDependencies
    private void fillSingleByExternalId() {
        //noinspection deprecation
        StaticDescriptorLoaderAccessor.getDescriptorLoader().loadByExternalId(this.externalId)
                .map(this::copyFieldsFromAnotherDescriptor)
                .filter(d -> d.internalId != null)
                .orElseThrow(this::newDescriptorNotFoundByExternalIdException);
    }

    @UsesStaticDependencies
    private Mono<Descriptor> fillSingleByExternalIdReactively() {
        //noinspection deprecation
        return StaticDescriptorLoaderAccessor.getDescriptorLoader().loadByExternalIdReactively(this.externalId)
                .map(this::copyFieldsFromAnotherDescriptor)
                .filter(d -> d.internalId != null)
                .switchIfEmpty(Mono.error(newDescriptorNotFoundByExternalIdException()));
    }

    private DescriptorNotFoundException newDescriptorNotFoundByExternalIdException() {
        return new DescriptorNotFoundException("Internal ID was not found for external ID " + this.externalId, this);
    }

    public Descriptor copyFieldsFrom(Descriptor d) {
        this.externalId = d.externalId;
        this.type = d.type;
        this.iri = d.iri;
        this.readiness = d.readiness;
        this.internalId = d.internalId;
        this.modelType = d.modelType;
        this.storageType = d.storageType;
        this.collection = d.collection;
        this.created = d.created;
        this.modified = d.modified;
        this.version = d.version;
        this.deleted = d.deleted;
        this.display = d.display;

        return this;
    }

    private Descriptor copyFieldsFromAnotherDescriptor(Descriptor d) {
        this.externalId = d.externalId;
        this.iri = d.iri;
        this.type = d.type;
        this.readiness = d.readiness;
        this.internalId = d.internalId;
        this.modelType = d.modelType;
        this.storageType = d.storageType;
        this.collection = d.collection;
        this.created = d.created;
        this.modified = d.modified;
        this.version = d.version;
        this.deleted = d.deleted;
        this.display = d.display;

        return d;
    }

    @JsonIgnore
    @UsesStaticDependencies
    public Mono<Type> effectiveTypeReactively() {
        if (this.type != null) {
            return Mono.just(type);
        }

        if (this.externalId != null) {
            return loadByExternalIdReactively()
                    .then(Mono.defer(() -> Mono.just(effectiveType())));
        } else if (this.internalId != null) {
            return loadByInternalIdReactively()
                    .then(Mono.defer(() -> Mono.just(effectiveType())));
        } else {
            throw new IllegalStateException("Both externalId and internalId are null");
        }
    }

    @Override
    public String toString() {
        if (hasExternalId()) {
            return "externalId: " + externalId;
        }
        if (hasInternalId()) {
            return "internalId: " + internalId;
        }
        return "No external/internal ID, " + super.toString();
    }

    @JsonIgnore
    public boolean isSingle() {
        return effectiveType() == Type.SINGLE;
    }

    @JsonIgnore
    public boolean isCollection() {
        return effectiveType() == Type.COLLECTION;
    }

    @JsonIgnore
    public boolean isOwned() {
        return effectiveType() == Type.OWNED;
    }

    public enum Type {
        SINGLE("single"),
        COLLECTION("collection"),
        OWNED("owned");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        @JsonValue
        @ToStorageString
        public String getValue() {
            return value;
        }

        @JsonCreator
        @FromStorageString
        public static Type fromString(String value) {
            if (value == null) {
                return SINGLE;
            }

            for (Type type : Type.values()) {
                if (type.getValue().equalsIgnoreCase(value)) {
                    return type;
                }
            }

            throw new IllegalArgumentException(String.format("'%s' is not a known descriptor type", value));
        }
    }

    public enum Readiness {
        BLANK("blank"), READY("ready");

        private final String value;

        Readiness(String value) {
            this.value = value;
        }

        @JsonValue
        @ToStorageString
        public String getValue() {
            return value;
        }

        @JsonCreator
        @FromStorageString
        public static Readiness fromString(String value) {
            if (value != null) {
                for (Readiness readiness : Readiness.values()) {
                    if (readiness.getValue().equalsIgnoreCase(value)) {
                        return readiness;
                    }
                }
            }

            throw new IllegalStateException(String.format("Unsupported Readiness: '%s'", value));
        }
    }

    public enum FIELDS {
        externalId, internalId, modelType, storageType, collection, created, modified, version, deleted, display, iri, owned
    }

    public static class DescriptorBuilder {
        public DescriptorBuilder storageType(StorageType storageType) {
            this.storageType = storageType.getStorageValue();
            return this;
        }
    }
}


