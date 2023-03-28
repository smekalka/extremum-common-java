package io.extremum.dynamic.resources.github;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GithubResourceConfigurationTest {
    @Test
    void getPropertiesTest() {
        String owner = "owner";
        String repo = "repo";
        String path = "/path/to/schema/file.schema.json";
        String ref = "master";

        GithubResourceConfiguration opts = new GithubResourceConfiguration(
                owner,
                repo,
                path,
                ref
        );

        assertEquals(owner, opts.getOwner());
        assertEquals(repo, opts.getRepo());
        assertEquals(path, opts.getSchemaPath());
        assertEquals(ref, opts.getRef());
    }
}