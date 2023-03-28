package io.extremum.common.descriptorpool;

import java.util.List;

public interface BatchDestroyer<T> {
    void destroy(List<T> batch);
}
