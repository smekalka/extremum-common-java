package io.extremum.dynamic.resources.github;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GithubResourceConfiguration {
    private final String owner;
    private final String repo;
    private final String schemaPath;
    private final String ref;

    private final String githubApiBase = "https://api.github.com/";
}
