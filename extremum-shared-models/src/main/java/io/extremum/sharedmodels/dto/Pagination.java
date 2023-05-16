package io.extremum.sharedmodels.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    public Pagination() {
    }

    public Pagination(int offset, int count, Long total, ZonedDateTime since, ZonedDateTime until) {
        this.offset = offset;
        this.count = count;
        this.total = total;
        this.since = since;
        this.until = until;
    }

    @Override
    public String toString() {
        return "Pagination{" +
                "offset=" + offset +
                ", count=" + count +
                ", total=" + total +
                ", since=" + since +
                ", until=" + until +
                '}';
    }
}
