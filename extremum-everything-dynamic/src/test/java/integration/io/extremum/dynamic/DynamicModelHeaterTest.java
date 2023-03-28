package integration.io.extremum.dynamic;

import integration.SpringBootTestWithServices;
import io.extremum.dynamic.ReactiveDescriptorDeterminator;
import io.extremum.dynamic.metadata.impl.DefaultDynamicModelMetadataProviderService;
import io.extremum.everything.services.management.ModelSaver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MockBeans({
        @MockBean(DefaultDynamicModelMetadataProviderService.class),
        @MockBean(ModelSaver.class)
})
@SpringBootTest(classes = {DynamicModelHeaterTestConfiguration.class})
public class DynamicModelHeaterTest extends SpringBootTestWithServices {
    @Autowired
    ReactiveDescriptorDeterminator descriptorDeterminator;

    @Test
    void heaterLoadSchemaAndRegisterModelInADescriptorDeterminator() {
        Set<String> registeredModelNames = descriptorDeterminator.getRegisteredModelNames();

        assertEquals(1, registeredModelNames.size());
        assertTrue(registeredModelNames.contains("TestDynamicModel"));
    }
}
