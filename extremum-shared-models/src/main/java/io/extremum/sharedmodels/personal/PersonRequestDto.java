package io.extremum.sharedmodels.personal;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.StringOrObject;
import io.extremum.sharedmodels.constraints.OnePrimaryContactAllowed;
import io.extremum.sharedmodels.content.Media;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.spacetime.CategorizedAddress;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@DocumentationName("Person")
public class PersonRequestDto implements RequestDto {
    private StringOrObject<Name> name;
    private Gender gender;
    private int age;
    private Birth birth;
    private String nationality;
    private List<Language> languages;
    private String hometown;
    private List<CategorizedAddress> addresses;
    @OnePrimaryContactAllowed
    private List<Contact> contacts;
    private List<PersonPositionForRequestDto> positions;
    private String relationship;
    private List<Media> images;
    private List<Object> documents;
}
