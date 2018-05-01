package io.opensphere.wfs.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;

/**
 * Utilities for handling XML documents.
 */
public final class JsonUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(JsonUtilities.class);

    /**
     * Format JSON class into a JSON-formatted string.
     *
     * @param jsonObj the Java JSON class.
     * @return The re-formatted JSON string.
     */
    public static String format(Object jsonObj)
    {
        ObjectMapper mapper = getDefaultObjectMapper(false);
        String jsonText = null;
        try
        {
            jsonText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to transform JSON: " + e, e);
        }
        return jsonText;
    }

    /**
     * Format JSON string into a pretty-printed string.
     *
     * @param jsonString the unformatted JSON text string.
     * @return The re-formatted JSON string.
     */
    public static String format(String jsonString)
    {
        ObjectMapper mapper = getDefaultObjectMapper(false);
        try
        {
            JsonNode node = mapper.readTree(jsonString);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to pretty print JSON string [" + jsonString + "]: " + e, e);
            return jsonString;
        }
    }

    /**
     * Gets a default object mapper that is setup to handle the serialization
     * and de-serialization of JSON classes.
     *
     * @param failOnUnknownField fail if an unknown field is encountered during
     *            deserialization
     * @return the default object mapper
     */
    public static ObjectMapper getDefaultObjectMapper(boolean failOnUnknownField)
    {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig().with(Feature.READ_ENUMS_USING_TO_STRING);
        if (!failOnUnknownField)
        {
            config = config.without(Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
        mapper.setDeserializationConfig(config);
        return mapper;
    }

    /**
     * Read a JSON string from an {@link InputStream} as one of a series of
     * specified classes. The first class in the argument list that has all of
     * the fields found in the top-level JSON node will be used to build the
     * object.
     *
     * @param is the input stream from which to read the JSON-formatted string
     * @param failOnUnknownField fail if an unknown field is encountered during
     *            deserialization
     * @param classes the list of classes to which the input stream could map
     * @return the java object representation of the JSON string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JsonProcessingException If the JSON could not be formatted into
     *             any of the specified object types.
     */
    public static Object read(InputStream is, boolean failOnUnknownField, Class<?>... classes)
        throws IOException, JsonProcessingException
    {
        Class<?> declaredClass = Object.class;
        Object toReturn = null;
        if (is != null)
        {
            ObjectMapper mapper = JsonUtilities.getDefaultObjectMapper(failOnUnknownField);
            JsonNode node = mapper.readTree(is);

            // Pull the field names from the top-level JSON node
            List<String> nodeFields = new ArrayList<>();
            for (Iterator<String> iter = node.getFieldNames(); iter.hasNext();)
            {
                nodeFields.add(iter.next());
            }

            // Find the first class whose fields match the fields in the
            // top-level JSON node
            for (Class<?> classOption : classes)
            {
                Collection<String> jsonProperties = getJsonProperties(classOption);
                if (jsonProperties.containsAll(nodeFields))
                {
                    declaredClass = classOption;
                    break;
                }
            }

            // Cast the JSON node to the class that matches its format
            toReturn = mapper.readValue(node, declaredClass);
        }
        return toReturn;
    }

    /**
     * Read a JSON string from an {@link InputStream} as one of a series of
     * specified classes. The first class in the argument list that has all of
     * the fields found in the top-level JSON node will be used to build the
     * object.
     *
     * @param is the input stream from which to read the JSON-formatted string
     * @param classes the list of classes to which the input stream could map
     * @return the java object representation of the JSON string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JsonProcessingException If the JSON could not be formatted into
     *             any of the specified object types.
     */
    public static Object read(InputStream is, Class<?>... classes) throws IOException, JsonProcessingException
    {
        return read(is, false, classes);
    }

    /**
     * Read a JSON object from an InputStream. Use the ObjectMapper to convert
     * it to the target class type.
     *
     * @param <T> the type of object being read
     * @param src the InputStream to read from
     * @param target the type of object to read
     * @return the transformed object that was read
     * @throws JsonProcessingException If the JSON could not be formatted into
     *             the specified object type.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static <T> T read(InputStream src, Class<T> target) throws JsonProcessingException, IOException
    {
        return read(src, target, false);
    }

    /**
     * Read a JSON object from an InputStream. Use the ObjectMapper to convert
     * it to the target class type.
     *
     * @param <T> the type of object being read
     * @param src the InputStream to read from
     * @param target the type of object to read
     * @param failOnUnknownField fail if an unknown field is encountered during
     *            deserialization
     * @return the transformed object that was read
     * @throws JsonProcessingException If the JSON could not be formatted into
     *             the specified object type.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static <T> T read(InputStream src, Class<T> target, boolean failOnUnknownField)
        throws JsonProcessingException, IOException
    {
        ObjectMapper mapper = getDefaultObjectMapper(failOnUnknownField);
        ObjectReader reader = mapper.reader(target);
        return reader.readValue(src);
    }

    /**
     * Gets the fields from the specified class that are tagged as
     * {@link JsonProperty}s. This will only retrieve the properties from the
     * declared class, not any parent (super) or child classes.
     *
     * @param toAnalyze the class to analyze
     * @return the fields that are tagged as {@link JsonProperty}s
     */
    private static Collection<String> getJsonProperties(Class<?> toAnalyze)
    {
        List<String> jsonAnnotations = new ArrayList<>();
        for (Field field : toAnalyze.getDeclaredFields())
        {
            for (Annotation annotation : field.getDeclaredAnnotations())
            {
                if (annotation instanceof JsonProperty)
                {
                    jsonAnnotations.add(((JsonProperty)annotation).value());
                }
            }
        }
        return jsonAnnotations;
    }

    /** Disallow instantiation. */
    private JsonUtilities()
    {
    }
}
