package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import configurations.FileSystemSchemaProviderConfiguration;
import io.extremum.dynamic.DefaultSchemaMetaService;
import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.services.DateTypesNormalizer;
import io.extremum.dynamic.services.DatesProcessor;
import io.extremum.dynamic.services.impl.DefaultDateTypesNormalizer;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.*;

@Configuration
@Import(FileSystemSchemaProviderConfiguration.class)
public class JsonBasedDynamicModelServiceTestConfiguration {
    @Bean
    public DateTypesNormalizer dateDocumentTypesNormalizer() {
        DateTypesNormalizer normalizer = mock(DefaultDateTypesNormalizer.class);

        doAnswer(invocation -> invocation.getArguments()[0]).when(normalizer).normalize(any(), anyCollection());

        return normalizer;
    }

    @Bean
    public DatesProcessor datesProcessor() {
        DatesProcessor processor = mock(DatesProcessor.class);

        doAnswer(new ReturnsArgumentAt(0))
                .when(processor).processDates(any());

        return processor;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SchemaMetaService schemaMetaService() {
        return new DefaultSchemaMetaService();
    }
}
