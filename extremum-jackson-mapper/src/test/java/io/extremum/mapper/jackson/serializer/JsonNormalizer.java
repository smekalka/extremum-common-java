package io.extremum.mapper.jackson.serializer;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author rpuch
 */
class JsonNormalizer {
    public String normalizeJson(String json) {
        try {
            return new JSONObject(json).toString();
        } catch (JSONException e) {
            throw new RuntimeException(String.format("Cannot parse '%s' as json", json), e);
        }
    }
}
