package io.extremum.common.descriptorpool;

import java.util.List;

public interface Allocator<T> {
    List<T> allocate(int quantityToAllocate);
}
