package io.opensphere.core.util.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.opensphere.core.PluginProperty;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * The Class PluginPropertyUtils.
 */
public final class PluginPropertyUtils
{
    /**
     * Takes a collection of {@link PluginProperty} and converts it to a Map of
     * key to value. Assumes each property has a unique key.
     *
     * Expands all properties using: StringUtilities.expandProperties(value,
     * System.getProperties());
     *
     * @param propertyCollection - the property collection to convert into a
     *            map.
     * @return a {@link Map} of key to value.
     */
    public static Map<String, String> convertToKeyValueMap(Collection<PluginProperty> propertyCollection)
    {
        HashMap<String, String> resultMap = new HashMap<>();
        if (propertyCollection != null)
        {
            for (PluginProperty pp : propertyCollection)
            {
                resultMap.put(pp.getKey(), StringUtilities.expandProperties(pp.getValue(), System.getProperties()));
            }
        }
        return resultMap;
    }

    /**
     * Takes a collection of {@link PluginProperty} and converts it to a
     * {@link Properties}.
     *
     * Expands all property values using:
     * StringUtilities.expandProperties(value, System.getProperties());
     *
     * @param propertyCollection - the property collection to convert into a
     *            map.
     * @return a {@link Properties} of key to value.
     */
    public static Properties convertToProperties(Collection<PluginProperty> propertyCollection)
    {
        Properties result = new Properties();
        if (propertyCollection != null)
        {
            for (PluginProperty pp : propertyCollection)
            {
                result.put(pp.getKey(), StringUtilities.expandProperties(pp.getValue(), System.getProperties()));
            }
        }
        return result;
    }

    /**
     * Instantiates a new plugin property utils.
     */
    private PluginPropertyUtils()
    {
        // Utility class, no instances allowed.
    }
}
