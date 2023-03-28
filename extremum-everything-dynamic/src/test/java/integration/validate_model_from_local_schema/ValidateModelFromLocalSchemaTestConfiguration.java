package integration.validate_model_from_local_schema;

import io.extremum.authentication.api.SecurityProvider;
import io.extremum.dynamic.config.DynamicModelProperties;
import io.extremum.dynamic.server.handlers.security.SchemaHandlerSecurityManager;
import io.extremum.dynamic.server.supports.FilesSupportsService;
import io.extremum.dynamic.server.supports.impl.DefaultFilesSupportsService;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.security.RoleSecurity;
import io.extremum.watch.processor.ReactiveWatchEventConsumer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockBeans({
        @MockBean(DataSecurity.class),
        @MockBean(RoleSecurity.class),
        @MockBean(SecurityProvider.class),
        @MockBean(ReactiveWatchEventConsumer.class),
        @MockBean(ModelSaver.class),
        @MockBean(PrincipalSource.class)
})
@RequiredArgsConstructor
@EnableAutoConfiguration
public class ValidateModelFromLocalSchemaTestConfiguration {
    private final DynamicModelProperties props;

    @Bean
    @Primary
    public SchemaHandlerSecurityManager schemaHandlerSecurityManager() {
        SchemaHandlerSecurityManager manager = mock(SchemaHandlerSecurityManager.class);

        doReturn(true).when(manager).isAccessAllowed(any());

        return manager;
    }

    @Bean
    @Primary
    public FilesSupportsService filesSupportsService() throws IOException, URISyntaxException {
        DefaultFilesSupportsService filesSupportsService = mock(DefaultFilesSupportsService.class);

        Path firstLocalPath = Paths.get(props.getSchema().getPointer().getLocal().getBaseDirectory())
                .resolve("like-network-access-schema");
        Path secondLocalPath = Paths.get(props.getSchema().getPointer().getLocal().getBaseDirectory())
                .resolve("path/second-like-network-schema");

        Path firstSchemaResourcePath = Paths.get(this.getClass().getClassLoader().getResource("schemas/like-network-access-schema").toURI());
        Path secondSchemaResourcePath = Paths.get(this.getClass().getClassLoader().getResource("schemas/path/second-like-network-schema").toURI());

        doReturn(true).when(filesSupportsService).isRegularFile(any());
        doAnswer(invocation -> {
            Path requestPathToFile = invocation.getArgument(0);

            if (requestPathToFile.equals(firstLocalPath)) {
                Files.copy(firstSchemaResourcePath, invocation.getArgument(1));
            } else if (requestPathToFile.equals(secondLocalPath)) {
                Files.copy(secondSchemaResourcePath, invocation.getArgument(1));
            } else {
                Assertions.fail("Requested not expected path to local file. Expected one of [ " + filesSupportsService +
                        " / " + secondSchemaResourcePath + " ]); actual requested : " + requestPathToFile);
            }

            return null;
        }).when(filesSupportsService).copy(any(), any());

        return filesSupportsService;
    }
}
