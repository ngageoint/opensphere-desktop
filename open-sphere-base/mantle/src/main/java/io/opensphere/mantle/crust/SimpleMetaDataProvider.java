package io.opensphere.mantle.crust;

import java.util.Map;
import java.util.Set;

/**
 * This class completes the process of reducing the MetaDataProvider interface
 * to what it really is: a glorified Map of String to Object. This class embeds
 * just such a thing and its "getValue" method delegates to the "get" method of
 * the embedded Map.
 */
public class SimpleMetaDataProvider extends MetaDataProviderAdapter
{
    /** A mapping of keys (String) to values (Object). */
    private final Map<String, Object> myValueMap;

    /**
     * Create a new provider populated with the supplied values.
     *
     * @param valueMap the contained field mapping.
     */
    public SimpleMetaDataProvider(Map<String, Object> valueMap)
    {
        myValueMap = valueMap;
        setFieldNames(valueMap.keySet());
    }

    /**
     * Get all of the columns even if some values are null.
     *
     * @param values the contained field mapping
     * @param columns exhaustive list of fields, some of which may map to null
     */
    public SimpleMetaDataProvider(Map<String, Object> values, Set<String> columns)
    {
        myValueMap = values;
        setFieldNames(columns);
    }

    @Override
    public Object getValue(String key)
    {
        return myValueMap.get(key);
    }
}