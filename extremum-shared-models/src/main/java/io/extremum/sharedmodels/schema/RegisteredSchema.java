package io.extremum.sharedmodels.schema;

import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public  class RegisteredSchema {
        private final String id;

        private final Set<RegisteredSchema> inheritors = new HashSet<>();

        public RegisteredSchema(String id) {
            this.id = id;
        }

        public void addInheritor(RegisteredSchema schema) {
            inheritors.add(schema);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegisteredSchema that = (RegisteredSchema) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }