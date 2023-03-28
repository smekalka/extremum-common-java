package io.extremum.watch.dto.converter;

import io.extremum.watch.dto.ModelMetadataResponseDto;
import io.extremum.watch.dto.TextWatchEventResponseDto;
import io.extremum.watch.models.ModelMetadata;
import io.extremum.watch.models.TextWatchEvent;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Service
public class TextWatchEventConverter {

    public TextWatchEventResponseDto convertToResponseDto(TextWatchEvent event) {
        ModelMetadata metadata = event.getModelMetadata();
        ModelMetadataResponseDto metadataDto = new ModelMetadataResponseDto(
                metadata.getId(),
                metadata.getModel(),
                metadata.getCreated(),
                metadata.getModified(),
                metadata.getVersion()
        );
        return new TextWatchEventResponseDto(metadataDto, event.getJsonPatch());
    }
}
