package integration.io.extremum.dynamic;

import configurations.FileSystemSchemaProviderConfiguration;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.everything.reactive.config.ReactiveEverythingConfiguration;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.security.RoleSecurity;
import io.extremum.starter.CommonConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        CommonConfiguration.class,
        ReactiveEverythingConfiguration.class,
        FileSystemSchemaProviderConfiguration.class,
        DynamicModuleAutoConfiguration.class
})
@MockBeans({
        @MockBean(RoleSecurity.class),
        @MockBean(DataSecurity.class),
        @MockBean(PrincipalSource.class)
})
@Configuration
public class DynamicModelHeaterTestConfiguration {
}
