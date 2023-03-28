package io.extremum.sharedmodels.spacetime;

import lombok.*;

/**
 * @author rpuch
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CategorizedAddress {
    private String category;
    private String caption;
    private ComplexAddress address;
}
