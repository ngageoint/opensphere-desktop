package io.opensphere.core.common.filter.dto;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines the some of the methods from the Java <code>Map</code>
 * interface for familiarity. From a filtering perspective, only read-only
 * access is necessary so any methods that return collections may return
 * immutable collections.
 */
public interface FilterDTO
{
    /**
     * Returns the unique identifier for this DTO.
     *
     * @return the unique identifier.
     */
    public Object getId();

    /**
     * Indicates if the DTO contains the given field name.
     *
     * @param name the field name.
     * @return <code>true</code> if the DTO contains the given field name.
     */
    public boolean containsKey(String name);

    /**
     * Indicates if the DTO contains the given field value.
     *
     * @param value the field value.
     * @return <code>true</code> if the DTO contains the given field value.
     */
    public boolean containsValue(Object value);

    /**
     * Returns the field names and values as a <code>Set</code>.
     *
     * @return the DTO's field names and values.
     */
    public Set<Map.Entry<String, Object>> entrySet();

    /**
     * Returns the value for the given field name or <code>null</code> if the
     * DTO contains no mapping for the name.
     *
     * @param key the field name.
     * @return the associated value or <code>null</code>.
     */
    public Object get(String key);

    /**
     * Returns the field names.
     *
     * @return the field names.
     */
    public Set<String> keySet();

    /**
     * Returns the field values.
     *
     * @return the field values.
     */
    public Collection<Object> values();
}
