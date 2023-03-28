package integration.validate_model_from_local_schema;

import integration.SpringBootTestWithServices;
import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("load-schema-from-local-server-test")
@SpringBootTest(classes = ValidateModelFromLocalSchemaTestConfiguration.class)
public class LocalSchemaLocationTest extends SpringBootTestWithServices {
    @Autowired
    NetworkntSchemaProvider schemaProvider;

    @Autowired
    SchemaMetaService schemaMetaService;

    final static String SCHEMA_NAME = "like-network-access-schema";
    final static String MODEL_NAME = "TestDynamicModelFrom-an_id_for_complex_schema_from-network";
    final static int SCHEMA_VERSION = 1;

    @Test
    void schemaMetaServiceKnowsAboutModel() {
        boolean schemaMetaServiceContainsAModel = schemaMetaService.getModelNames().contains(MODEL_NAME);
        String registeredSchemaName = schemaMetaService.getSchemaName(MODEL_NAME, SCHEMA_VERSION);

        assertTrue(schemaMetaServiceContainsAModel);
        assertEquals(SCHEMA_NAME, registeredSchemaName);
    }

    @Test
    void localSchemaFromLocalLocationTest() {
        NetworkntSchema loaded = schemaProvider.loadSchema(SCHEMA_NAME);
        assertNotNull(loaded);
    }
}
