package io.extremum.descriptors.common.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("descriptors")
public class DescriptorsProperties {
    private String descriptorsMapName;
    private String internalIdsMapName;
    private String collectionCoordinatesMapName = "collection_descriptors_coordinates_idx";
    private String ownedCoordinatesMapName = "owned_descriptors_coordinates_idx";
    private String iriMapName = "iris";
}
