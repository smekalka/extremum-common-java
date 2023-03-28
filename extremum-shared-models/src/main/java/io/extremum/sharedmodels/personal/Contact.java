package io.extremum.sharedmodels.personal;

import io.extremum.sharedmodels.annotation.DocumentationName;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@DocumentationName("Contact")
public class Contact {
    private String type;
    private String contact;
    private boolean primary;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact1 = (Contact) o;
        return primary == contact1.primary &&
                Objects.equals(type, contact1.type) &&
                Objects.equals(contact, contact1.contact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, contact, primary);
    }
}
