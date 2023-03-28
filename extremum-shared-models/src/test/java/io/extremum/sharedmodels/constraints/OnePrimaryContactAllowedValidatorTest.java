package io.extremum.sharedmodels.constraints;

import io.extremum.sharedmodels.personal.Contact;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class OnePrimaryContactAllowedValidatorTest {
    @Test
    void ok_withoutPrimary() {
        Model model = new Model();
        model.contacts = Arrays.asList(
                new Contact("email", "a@mail.com", false),
                new Contact("email", "b@mail.com", false),
                new Contact("email", "c@mail.com", false)
        );

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        Set<ConstraintViolation<Model>> constraints = validator.validate(model);
        Assertions.assertTrue(constraints.isEmpty());
    }

    @Test
    void ok_withOnePrimary() {
        Model model = new Model();
        model.contacts = Arrays.asList(
                new Contact("email", "a@mail.com", true),
                new Contact("email", "b@mail.com", false),
                new Contact("email", "c@mail.com", false)
        );

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        Set<ConstraintViolation<Model>> constraints = validator.validate(model);
        Assertions.assertTrue(constraints.isEmpty());
    }

    @Test
    void fail_withMoreThenOnePrimary() {
        Model model = new Model();
        model.contacts = Arrays.asList(
                new Contact("email", "a@mail.com", true),
                new Contact("email", "b@mail.com", true),
                new Contact("email", "c@mail.com", false)
        );

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        Set<ConstraintViolation<Model>> constraints = validator.validate(model);
        Assertions.assertEquals(1, constraints.size());
    }

    private static class Model {
        @OnePrimaryContactAllowed
        public List<Contact> contacts;
    }
}
