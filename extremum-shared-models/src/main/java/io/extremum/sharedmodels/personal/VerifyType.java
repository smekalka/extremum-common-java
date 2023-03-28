package io.extremum.sharedmodels.personal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum VerifyType {
    @JsonProperty("email")
    EMAIL,
    @JsonProperty("sms")
    SMS,
    @JsonProperty("email.verify")
    EMAIL_VERIFY,
    @JsonProperty("sms.verify")
    SMS_VERIFY,
    @JsonProperty("username")
    USERNAME(Group.PASSWORD_LOGIN),
    @JsonProperty("password")
    PASSWORD(Group.PASSWORD_LOGIN);

    private final Group group;

    VerifyType() {
        this(Group.NONE);
    }

    VerifyType(Group group) {
        this.group = group;
    }

    public static boolean justOneAuthAttempt(List<VerifyType> types) {
        if (types.size() < 2) {
            return true;
        }

        if (containsDuplicates(types)) {
            return false;
        }

        long grouplessCount = types.stream()
                .filter(type -> type.group == Group.NONE)
                .count();
        if (grouplessCount > 1) {
            return false;
        }

        Set<Group> allGroups = types.stream()
                .map(type -> type.group)
                .collect(Collectors.toSet());

        return allGroups.size() == 1;
    }

    private static boolean containsDuplicates(List<VerifyType> types) {
        return new HashSet<>(types).size() != types.size();
    }

    private enum Group {
        NONE, PASSWORD_LOGIN
    }
}
