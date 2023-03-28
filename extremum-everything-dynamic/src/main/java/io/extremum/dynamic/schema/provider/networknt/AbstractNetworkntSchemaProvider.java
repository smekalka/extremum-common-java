package io.extremum.dynamic.schema.provider.networknt;

import com.networknt.schema.*;
import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingException;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.extremum.dynamic.DynamicModelConstants.CYBERNATED_DATE_TIME_FORMAT_NAME;
import static io.extremum.dynamic.DynamicModelConstants.DATE_FORMAT_PATTERN;
import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final JsonSchemaType type;

    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaLoadingException {
        JsonSchemaFactory factory = createFactory(type, getUriFetchers());

        URI schemaUri = makeSchemaUri(schemaName);

        try (InputStream is = getResourceLoader().loadAsInputStream(schemaUri)) {
            JsonSchema schema = factory.getSchema(is);
            return new NetworkntSchema(schema);
        } catch (ResourceLoadingException e) {
            if (e instanceof ResourceNotFoundException) {
                log.error("Schema {} not found. Unable to load schema from uri {}", schemaName, schemaUri);
                throw new SchemaNotFoundException(schemaName, e);
            } else {
                return unableToLoadSchemaThrow(schemaName, schemaUri, e);
            }
        } catch (JsonSchemaException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SchemaNotFoundException) {
                log.error("Schema " + schemaName + " isn't found");
                throw new SchemaNotFoundException(schemaName, cause);
            } else {
                return unableToLoadSchemaThrow(schemaName, schemaUri, e);
            }
        } catch (IOException e) {
            return unableToLoadSchemaThrow(schemaName, schemaUri, e);
        }
    }

    protected NetworkntSchema unableToLoadSchemaThrow(String schemaName, URI schemaUri, Exception e) {
        String errMessage = format("Schema %s can't be provided. Unable to load schema by uri %s",
                schemaName, schemaUri);

        log.error(errMessage);

        throw new SchemaLoadingException(errMessage, e);
    }

    protected abstract ResourceLoader getResourceLoader();

    protected abstract List<NetworkntURIFetcher> getUriFetchers();

    protected abstract URI makeSchemaUri(String schemaName);

    private JsonSchemaFactory createFactory(JsonSchemaType type, List<NetworkntURIFetcher> uriFetchers) {
        if (JsonSchemaType.V2019_09.equals(type)) {
            JsonMetaSchema metaSchema = buildMetaSchema();

            JsonSchemaFactory.Builder builder = new JsonSchemaFactory.Builder()
                    .addMetaSchema(metaSchema)
                    .defaultMetaSchemaURI(metaSchema.getUri());

            uriFetchers.forEach(fetcher ->
                    builder.uriFetcher(fetcher, fetcher.getSupportedSchemas().toArray(new String[]{})));

            return builder.build();
        } else {
            throw new RuntimeException("Only " + JsonSchemaType.V2019_09 + " schema version is supported");
        }
    }

    private JsonMetaSchema buildMetaSchema() {
        return new JsonMetaSchema.Builder(JsonMetaSchema.getV201909().getUri())
                .idKeyword("$id")
                .addFormats(new ArrayList<>(JsonMetaSchema.COMMON_BUILTIN_FORMATS))
                .addFormat(new PatternFormat(CYBERNATED_DATE_TIME_FORMAT_NAME, DATE_FORMAT_PATTERN))
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V201909))
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("$id"),
                        new NonValidationKeyword("title"),
                        new NonValidationKeyword("description"),
                        new NonValidationKeyword("default"),
                        new NonValidationKeyword("definitions"),
                        new NonValidationKeyword("$defs")  // newly added in 2018-09 release.
                ))
                .build();
    }

    @Override
    public JsonSchemaType getSchemaType() {
        return type;
    }
}
