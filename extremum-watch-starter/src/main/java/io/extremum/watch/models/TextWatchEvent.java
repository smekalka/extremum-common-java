package io.extremum.watch.models;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.watch.dto.TextWatchEventNotificationDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import static io.extremum.watch.models.TextWatchEvent.COLLECTION_NAME;


@Getter
@Setter
@Document(COLLECTION_NAME)
@Entity
public class TextWatchEvent {
    /**
     * Collection name of the all child's of this class
     */
    public static final String COLLECTION_NAME = "watch_events";

    private UUID jpaId = UUID.randomUUID();

    @Id
    @javax.persistence.Id
    private UUID id = UUID.randomUUID();

    @CreatedDate
    @Indexed
    private ZonedDateTime created;

    @Version
    private long version;
    @Column(columnDefinition="text")
    private String jsonPatch;
    @Column(columnDefinition="text")
    private String fullReplacePatch;
    private String modelId;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ModelMetadata modelMetadata;
    @ElementCollection
    private Set<String> subscribers;

    public TextWatchEvent() {
    }

    public TextWatchEvent(String jsonPatch, String fullReplacePatch, String modelId, Model targetModel) {
        this.jsonPatch = jsonPatch;
        this.fullReplacePatch = fullReplacePatch;
        this.modelId = modelId;
        modelMetadata = ModelMetadata.fromModel(targetModel);
    }

    public TextWatchEventNotificationDto toDto() {
        return new TextWatchEventNotificationDto(jsonPatch, fullReplacePatch, subscribers);
    }

    public void touchModelMotificationTime() {
        modelMetadata.setModified(ZonedDateTime.now());
    }
}
