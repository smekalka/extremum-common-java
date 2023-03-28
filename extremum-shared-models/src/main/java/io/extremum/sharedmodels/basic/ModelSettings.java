package io.extremum.sharedmodels.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelSettings {
    private Properties properties;
    private Class<?> modelClass;


    @Data
    @NoArgsConstructor
    public static class Properties {
        private Set<String> visible = new HashSet<>();

        private Collection<String> all;

        public Properties(Set<String> visible) {
            this.visible = visible;
            this.all = visible;
        }

        public Set<String> getVisible() {
            visible.addAll(ALWAYS_VISIBLE);
            return visible;
        }

        private static List<String> ALWAYS_VISIBLE = Arrays.asList("uuid", "created", "modified");
    }
}
