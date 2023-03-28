package io.extremum.sharedmodels.personal;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.StringOrObject;
import io.extremum.sharedmodels.content.Media;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import io.extremum.sharedmodels.spacetime.CategorizedAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DocumentationName("Person")
public class PersonResponseDto extends CommonResponseDto {
    public static final String MODEL_NAME = "Person";

    private StringOrObject<Name> name;
    private Gender gender;
    private int age;
    private Birth birth;
    private String nationality;
    private List<Language> languages;
    private String hometown;
    private List<CategorizedAddress> addresses;
    private List<Contact> contacts;
    private List<PersonPositionForResponseDto> positions;
    private String relationship;
    private List<Media> images;
    private List<Object> documents;

    @Override
    public String getModel() {
        return MODEL_NAME;
    }
}
