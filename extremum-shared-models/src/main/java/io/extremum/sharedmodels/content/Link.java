package io.extremum.sharedmodels.content;

import com.github.jsonldjava.core.RDFDataset;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Link implements Serializable {
    private String name;
    private Media icon;
    private String url;
}
