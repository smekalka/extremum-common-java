package io.extremum.watch.models;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.basic.Model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ModelMetadata {
    @Id
    private String id;
    private String model;
    private ZonedDateTime created;
    private ZonedDateTime modified;
    private Long version;

    public static ModelMetadata fromModel(Model model) {
        if (model instanceof PersistableCommonModel) {
            return new ModelMetadata((PersistableCommonModel) model);
        }
        if (model instanceof BasicModel) {
            return new ModelMetadata((BasicModel) model);
        }
        throw new IllegalStateException(model.getClass() + " is not a BasicModel");
    }

    public ModelMetadata(PersistableCommonModel model) {
        this(model.getUuid().getExternalId(), ModelUtils.getModelName(model),
                model.getCreated(), model.getModified(), model.getVersion());
    }

    public ModelMetadata(BasicModel model) {
        this(model.getUuid().getExternalId(), ModelUtils.getModelName(model), null, null, null);
    }
}
