package io.extremum.sharedmodels.signal;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.spacetime.Timepoint;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@DocumentationName("Signal")
public class Signal {

    public static final String MODEL_NAME = "Signal";
    public Signal(@NonNull SignalKind kind, @NonNull String exchange, @NonNull String source) {
        this.kind = kind;
        this.exchange = exchange;
        this.source = source;
    }

    @NonNull
    private SignalKind kind;

    @NonNull
    private String exchange;

    @NonNull
    private String source;

    private String destination;

    private String xid;

    private Timepoint sent;

    private Object data;

    private Peer peer;
}
