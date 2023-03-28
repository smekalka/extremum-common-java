package io.extremum.common.utils;

import io.extremum.common.slug.DefaultSlugGenerationStrategy;
import io.extremum.common.slug.SlugGenerationStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

class DefaultSluGenerationStrategyTest {

    SlugGenerationStrategy slugGenerationStrategy = new DefaultSlugGenerationStrategy(100);

    @Test
    public void should_generate_slug_from_phrase() {
        List<String> generate = slugGenerationStrategy.generate("Season Beta Mission 5");
        for (String s : generate) {
            System.out.println(s);
        }
    }
}