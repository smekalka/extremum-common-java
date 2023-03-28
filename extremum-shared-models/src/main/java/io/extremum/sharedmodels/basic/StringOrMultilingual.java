package io.extremum.sharedmodels.basic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Access(AccessType.FIELD)
@Table(indexes = {
        @Index(columnList = "text")}
)
public class StringOrMultilingual implements Serializable, Comparable<StringOrMultilingual> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    private Type type;
    @Column(columnDefinition = "text")
    private String text;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Multilingual multilingualContent;

    private StringOrMultilingual(Type type, String text, Map<MultilingualLanguage, String> map) {
        this.type = type;
        this.text = text;
        this.multilingualContent = new Multilingual(map);
    }

    public StringOrMultilingual() {
        this(Type.UNKNOWN, "", new HashMap<>());
    }

    public StringOrMultilingual(String text) {
        type = Type.TEXT;
        this.text = text;
    }

    public StringOrMultilingual(Map<MultilingualLanguage, String> map) {
        type = Type.MAP;
        this.multilingualContent = new Multilingual(map);
    }

    public StringOrMultilingual(Map<MultilingualLanguage, String> map, Locale locale) {
        type = Type.MAP;
        this.multilingualContent = new Multilingual(map);
        String textForLocale = multilingualContent.getMap().get(MultilingualLanguage.fromString(locale.toLanguageTag()));
        if (textForLocale == null) {
            throw new IllegalArgumentException("Value for default locale " + locale + " must be set");
        }
        this.text = textForLocale;
    }

    public boolean isTextOnly() {
        return type == Type.TEXT;
    }

    public boolean isMultilingual() {
        return type == Type.MAP;
    }

    public boolean isKnown() {
        return type != Type.UNKNOWN;
    }

    @Override
    public int compareTo(StringOrMultilingual other) {
        if (this.isTextOnly() && other.isTextOnly()) {
            return this.getText().compareTo(other.getText());
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringOrMultilingual that = (StringOrMultilingual) o;
        if (id != null && Objects.equals(id, that.id)) {
            return true;
        }
        return type == that.type && Objects.equals(text, that.text) && Objects.equals(multilingualContent, that.multilingualContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, text, multilingualContent);
    }

    public enum Type {
        @JsonProperty("unknown")
        UNKNOWN,
        @JsonProperty("text")
        TEXT,
        @JsonProperty("map")
        MAP
    }

    public enum FIELDS {
        type, text, map
    }
}
