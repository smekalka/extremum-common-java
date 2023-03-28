package io.extremum.sharedmodels.personal;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * @author rpuch
 */
class CredentialTest {
    @Test
    void whenToString_thenValueShouldNotBeOutput() {
        Credential credential = new Credential();
        credential.setSystem("test-system");
        credential.setType(VerifyType.EMAIL);
        credential.setValue("secret");

        assertThat(credential.toString(), not(containsString("secret")));
    }
}