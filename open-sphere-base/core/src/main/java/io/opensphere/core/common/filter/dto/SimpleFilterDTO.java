package io.opensphere.core.common.filter.dto;

import java.util.LinkedHashMap;

public class SimpleFilterDTO extends LinkedHashMap<String, Object> implements FilterDTO
{
    /**
     * The default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Returns <code>null</code>.
     *
     * @see io.opensphere.core.common.filter.dto.FilterDTO#getId()
     */
    @Override
    public Object getId()
    {
        return null;
    }

    /**
     * @see io.opensphere.core.common.filter.dto.FilterDTO#containsKey(java.lang.String)
     */
    @Override
    public boolean containsKey(String name)
    {
        return super.containsKey(name);
    }

    /**
     * @see io.opensphere.core.common.filter.dto.FilterDTO#get(java.lang.String)
     */
    @Override
    public Object get(String key)
    {
        return super.get(key);
    }

    /**
     * @see io.opensphere.core.common.filter.dto.FilterDTO#containsValue(java.lang.Comparable)
     */
    @Override
    public boolean containsValue(Object value)
    {
        return super.containsValue(value);
    }
}
