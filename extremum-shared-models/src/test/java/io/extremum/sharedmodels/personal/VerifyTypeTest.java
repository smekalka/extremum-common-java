package io.extremum.sharedmodels.personal;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author rpuch
 */
class VerifyTypeTest {
    private final List<VerifyType> grouplessVerifyTypes = Arrays.asList(
            VerifyType.EMAIL, VerifyType.EMAIL_VERIFY, VerifyType.SMS, VerifyType.SMS_VERIFY);
    
    @Test
    void emptyListIsOneAuthAttempt() {
        assertTrue(VerifyType.justOneAuthAttempt(emptyList()));
    }

    @Test
    void singletonIsOneAuthAttempt() {
        assertTrue(VerifyType.justOneAuthAttempt(singletonList(VerifyType.EMAIL)));
    }

    @Test
    void duplicatesAreNotOneAuthAttempt() {
        assertFalse(VerifyType.justOneAuthAttempt(asList(VerifyType.USERNAME, VerifyType.USERNAME)));
    }

    @Test
    void usernameAndPasswordShouldBeOneAuthAttempt() {
        assertTrue(VerifyType.justOneAuthAttempt(asList(VerifyType.USERNAME, VerifyType.PASSWORD)));
    }

    @Test
    void usernameIsNotOneAuthAttemptWithAnythingButPassword() {
        for (VerifyType otherType : grouplessVerifyTypes) {
            assertFalse(VerifyType.justOneAuthAttempt(asList(VerifyType.USERNAME, otherType)), "USERNAME+" + otherType);
        }
    }

    @Test
    void passwordIsNotOneAuthAttemptWithAnythingButUsername() {
        for (VerifyType otherType : grouplessVerifyTypes) {
            assertFalse(VerifyType.justOneAuthAttempt(asList(VerifyType.PASSWORD, otherType)), "PASSWORD+" + otherType);
        }
    }

    @Test
    void nonUsernameIsNotOneAuthAttemptWithNonPassword() {
        for (VerifyType firstType : grouplessVerifyTypes) {
            for (VerifyType secondType : grouplessVerifyTypes) {
                assertFalse(VerifyType.justOneAuthAttempt(asList(firstType, secondType)),
                        firstType + "+" + secondType);
            }
        }
    }
}