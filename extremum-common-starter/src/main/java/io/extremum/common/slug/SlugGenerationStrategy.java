package io.extremum.common.slug;

import java.util.List;

public interface SlugGenerationStrategy {
    List<String> generate(String name);
}
