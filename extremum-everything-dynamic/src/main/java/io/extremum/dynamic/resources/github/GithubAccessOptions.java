package io.extremum.dynamic.resources.github;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GithubAccessOptions {
    private final String authToken;

    public String getAuthToken() {
        return authToken;
    }
}
