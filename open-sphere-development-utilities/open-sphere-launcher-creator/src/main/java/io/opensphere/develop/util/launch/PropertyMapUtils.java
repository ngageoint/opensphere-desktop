package io.opensphere.develop.util.launch;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A set of utility methods designed to simplify interaction with
 * {@link Properties} objects.
 */
public final class PropertyMapUtils
{
    /**
     * Private constructor to prevent instantiation.
     */
    private PropertyMapUtils()
    {
        throw new UnsupportedOperationException("Instantiating utility classes is not permitted.");
    }

    /**
     * Converts the supplied {@link Properties} object to a {@link String}
     * {@link Map}.
     *
     * @param properties the properties to convert.
     * @return a {@link Map} containing {@link String} keys and values.
     */
    public static Map<String, String> toMap(Properties properties)
    {
        Map<String, String> map = new HashMap<>();
        for (String property : properties.stringPropertyNames())
        {
            map.put(property, properties.getProperty(property));
        }
        return map;
    }
}
