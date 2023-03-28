package io.extremum.dynamic.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties("dynamic-models")
public class DynamicModelProperties {
    @NotNull
    private Schema schema;
    private LocalSchemaServer localSchemaServer;
    private GithubWebhookListener githubWebHookListener;

    @Data
    public static class Schema {
        @NotNull
        private Location location;
        @NotNull
        private Pointer pointer;

        public enum Location {
            github, local
        }

        @Data
        public static class Pointer {
            @NotNull
            private String schemaPath;
            @NotNull
            private String schemaName;
            private int schemaVersion;
            private Local local;
            private Github github;

            @Data
            public static class Local {
                @NotNull
                private String baseDirectory;
            }

            @Data
            public static class Github {
                @NotNull
                private String owner;
                @NotNull
                private String ref;
                @NotNull
                private String repo;
                @NotNull
                private String token;
            }
        }
    }

    @Data
    public static class LocalSchemaServer {
        @NotNull
        private Integer port;
        @NotNull
        private String contextPath;
    }

    @Data
    public static class GithubWebhookListener {
        @NotNull
        private Integer port;
        @NotNull
        private String serverContext;
    }
}
