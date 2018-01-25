package io.opensphere.server.util;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Utility class for marshalling json objects.
 */
public final class JsonUtils
{
    /**
     * Creates the object mapper used to deserialize a json string into json
     * objects.
     *
     * @return The object mapper.
     */
    public static ObjectMapper createMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getDeserializationConfig().set(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        return mapper;
    }

    /**
     * Not constructible.
     */
    private JsonUtils()
    {
    }
}
