package io.extremum.sharedmodels.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartStatusMultipartUpload {
    private String resultId;
    private Integer size;

    public PartStatusMultipartUpload() {
    }

    public PartStatusMultipartUpload(String resultId, Integer size) {
        this.resultId = resultId;
        this.size = size;
    }

    @Override
    public String toString() {
        return "PartStatusMultipartUpload{" +
                "resultId='" + resultId + '\'' +
                ", size=" + size +
                '}';
    }
}
