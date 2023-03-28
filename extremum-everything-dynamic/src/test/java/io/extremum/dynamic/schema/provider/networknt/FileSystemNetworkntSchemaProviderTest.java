package io.extremum.dynamic.schema.provider.networknt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemNetworkntSchemaProviderTest {
    static FileSystemNetworkntSchemaProvider provider;

    @BeforeAll
    static void before() {
        String pathToFile = FileSystemNetworkntSchemaProviderTest.class.getClassLoader()
                .getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        provider = new FileSystemNetworkntSchemaProvider(JsonSchemaType.V2019_09,
                Paths.get(base, "schemas"));
    }

    @Test
    void schemaLoadedFromLocalFileSystemOkTest() {
        assertDoesNotThrow(() -> provider.loadSchema("simple.schema.json"));
    }

    @Test
    void loadedSchemaContainsAJsonSchemaDataTest() {
        NetworkntSchema schema = provider.loadSchema("simple.schema.json");
        assertEquals("an_id_for_simple_schema_json",
                schema.getSchema().getSchemaNode().get("$id").textValue());
    }

    @Test
    void loadsSchemaWithRefsToAnotherSchemaInLocalFileSystemOkTest() throws IOException {
        NetworkntSchema schema = provider.loadSchema("complex.schema.json");

        assertEquals("an_id_for_complex_schema_json",
                schema.getSchema().getSchemaNode().get("$id").textValue());

        ObjectMapper mapper = new ObjectMapper();

        String modelData = "{\"field1\":\"sss\", \"field2\":33.2, \"field3\":23}";
        JsonNode jsonNode = mapper.readValue(modelData, JsonNode.class);
        Set<ValidationMessage> violations = schema.getSchema().validate(jsonNode);

        assertFalse(violations.isEmpty());
    }

    @Test
    void schemaNotFoundException_fileBySchemaPath_isNotFound() {
        URL url = this.getClass().getClassLoader().getResource("test.file.txt");

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(JsonSchemaType.V2019_09,
                Paths.get(url.getPath()));

        assertThrows(SchemaNotFoundException.class, () -> provider.loadSchema("unknownSchema"));
    }

    @Test
    void schemaNotFoundException_fileByRefInSchema_isNotFound() {
        URL url = this.getClass().getClassLoader().getResource("schemas");

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(JsonSchemaType.V2019_09,
                Paths.get(url.getPath()));

        assertThrows(SchemaNotFoundException.class,
                () -> provider.loadSchema("complex_with_bad_ref.schema.json"));
    }
}
