package io.extremum.graphql.model;

import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Transient;

import javax.persistence.Column;
import java.util.Arrays;
import java.util.Collections;

class PagingAndSoringRequestValidatorTest {

    @Test
    @DisplayName("Validates request properly")
    public void should_validate_properly() {
        PagingAndSoringRequestValidator validator = new PagingAndSoringRequestValidator();
        PagingAndSortingRequest request = new PagingAndSortingRequest();
        SortOrder existent = new SortOrder();
        existent.setProperty("extremum");

        SortOrder existentCamelCase = new SortOrder();
        existentCamelCase.setProperty("existent_camel_case");
        SortOrder non_existent = new SortOrder();
        non_existent.setProperty("non_existent");
        SortOrder _transient = new SortOrder();
        _transient.setProperty("_transient");

        SortOrder _transient2 = new SortOrder();
        _transient2.setProperty("_transient");
        request.setOrders(Arrays.asList(existent, non_existent, _transient, _transient2));

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> validator.validate(request, TestModel.class));
        Assertions.assertEquals("TestModel does not have \"non_existent,_transient,_transient\" field(s)", thrown.getMessage());

        request.setOrders(Collections.singletonList(existent));
        validator.validate(request, TestModel.class);

        request.setOrders(Collections.singletonList(existentCamelCase));
        validator.validate(request, TestModel.class);

        request.setOrders(Arrays.asList(_transient, _transient2));


        thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            validator.validate(request, TestModel.class);
        });
        Assertions.assertEquals("TestModel does not have \"_transient,_transient\" field(s)", thrown.getMessage());

    }

    @Data
    private static class TestModel {
        @Column(name = "extremum")
        private String existent;
        private String existentCamelCase;

        @Transient
        private String _transient;

        @javax.persistence.Transient
        private String _transient2;
    }
}