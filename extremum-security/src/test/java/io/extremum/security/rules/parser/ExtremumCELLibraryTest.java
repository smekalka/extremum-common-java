package io.extremum.security.rules.parser;

import org.junit.jupiter.api.Test;

class ExtremumCELLibraryTest {

    @Test
    public void test(){
        boolean вася_пупкин = ExtremumCELLibrary.like("Вася пупкин", "*ася*", "");
        System.out.println(вася_пупкин);
    }

}