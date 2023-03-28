package io.extremum.sharedmodels.signal;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class Peer {
    public Peer(@NonNull String uuid) {
        this.uuid = uuid;
    }

    public Peer(@NonNull String uuid, String xid) {
        this.uuid = uuid;
        this.xid = xid;
    }

    private final String uuid;
    private String xid;
}
