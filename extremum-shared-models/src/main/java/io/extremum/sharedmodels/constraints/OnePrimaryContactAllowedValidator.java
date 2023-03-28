package io.extremum.sharedmodels.constraints;

import io.extremum.sharedmodels.personal.Contact;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class OnePrimaryContactAllowedValidator implements ConstraintValidator<OnePrimaryContactAllowed, List<Contact>> {
    public void initialize(OnePrimaryContactAllowed constraint) {
    }

    public boolean isValid(List<Contact> contacts, ConstraintValidatorContext context) {
        if (contacts == null || contacts.isEmpty()) {
            return true;
        } else {
            long primaryCount = contacts.stream().filter(Contact::isPrimary).count();
            if (primaryCount > 1) {
                List<Contact> badContacts = new ArrayList<>();

                contacts.stream().filter(Contact::isPrimary).forEach(badContacts::add);

                List<String> badContactsAsString = badContacts.stream().map(Contact::toString).collect(Collectors.toList());


                context.buildConstraintViolationWithTemplate(format("Only one primary contact allowed. Model have a %d: %s",
                        badContacts.size(), String.join(", ", badContactsAsString)));

                return false;
            } else {
                return true;
            }
        }
    }
}
