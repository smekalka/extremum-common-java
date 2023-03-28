package io.extremum.sharedmodels.basic;

import io.extremum.sharedmodels.annotation.DocumentationName;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


@Getter
@ToString
@DocumentationName("Multilingual")
@Entity
public class Multilingual {
    @Id
    private UUID id = UUID.randomUUID();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "multilingual_item_mapping",
            joinColumns = {@JoinColumn(name = "multilingual_id", referencedColumnName = "id")})
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "content", columnDefinition = "text")
    private Map<MultilingualLanguage, String> map = new HashMap<>();

    public Multilingual() {

    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Multilingual(Map<MultilingualLanguage, String> map) {
        this.map = Collections.unmodifiableMap(new HashMap<>(map));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Multilingual that = (Multilingual) o;
        if (id.equals(that.id)) {
            return true;
        }

        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}
