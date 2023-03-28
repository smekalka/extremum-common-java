package io.extremum.sharedmodels.content;

import io.extremum.sharedmodels.basic.IntegerOrString;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class Media implements Serializable {
    public String url;
    public MediaType type;
    public Integer width;
    public Integer height;
    public Integer depth;
    public IntegerOrString duration;
    public List<Media> thumbnails;

    public enum FIELDS {
        url, type, width, height, depth, duration, thumbnails
    }
}
