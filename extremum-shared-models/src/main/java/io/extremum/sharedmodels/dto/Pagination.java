package io.extremum.sharedmodels.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class Pagination {
    private int offset;
    private int count;
    private Long total;
    private ZonedDateTime since;
    private ZonedDateTime until;

    static Pagination singlePage(int total) {
        return Pagination.builder()
                .count(total)
                .total((long) total)
                .build();
    }
}
