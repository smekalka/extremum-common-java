package configurations;

import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.nio.file.Paths;

@Configuration
public class FileSystemSchemaProviderConfiguration {
    @Bean
    @SneakyThrows
    public NetworkntSchemaProvider networkntSchemaProvider() {
        URL url = this.getClass().getClassLoader().getResource("test.file.txt");

        return new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(
                        Paths.get(url.toURI()).getParent().toString(),
                        "/schemas/"
                ));
    }
}
