package io.opensphere.core.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.collections.New;

/**
 * Map used to contain property values for multiple properties.
 */
public class PropertyValueMap
{
    /** The wrapped map of property descriptors to lists of property values. */
    private final Map<PropertyDescriptor<?>, List<?>> myMap = new HashMap<>();

    /**
     * Add a result list to the map. The list will be constructed by this
     * method.
     *
     * @param <T> The type of the property values.
     * @param propertyDescriptor The descriptor of the property these results
     *            belong to.
     */
    public <T> void addResultList(PropertyDescriptor<T> propertyDescriptor)
    {
        addResultList(propertyDescriptor, New.<T>list());
    }

    /**
     * Add a result list to the map.
     *
     * @param <T> The type of the property values.
     * @param propertyDescriptor The descriptor of the property these results
     *            belong to.
     * @param length The initial capacity of the list.
     */
    public <T> void addResultList(PropertyDescriptor<T> propertyDescriptor, int length)
    {
        addResultList(propertyDescriptor, New.<T>list(length));
    }

    /**
     * Add a result list to the map.
     *
     * @param <T> The type of the property values.
     * @param propertyDescriptor The descriptor of the property these results
     *            belong to.
     * @param list The list to contain the results.
     */
    public <T> void addResultList(PropertyDescriptor<T> propertyDescriptor, List<T> list)
    {
        myMap.put(propertyDescriptor, list);
    }

    /**
     * Get the property descriptors for the current result lists.
     *
     * @return The property descriptors.
     */
    public Set<PropertyDescriptor<?>> getPropertyDescriptors()
    {
        return myMap.keySet();
    }

    /**
     * Get a result list from the map.
     *
     * @param <T> The type of the property values.
     * @param propertyDescriptor The descriptor of the property these results
     *            belong to.
     * @return The list to contain the results.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getResultList(PropertyDescriptor<T> propertyDescriptor)
    {
        return (List<T>)myMap.get(propertyDescriptor);
    }

    /**
     * Get if the map is empty.
     *
     * @return If the map is empty, {@code true}.
     */
    public boolean isEmpty()
    {
        return myMap.isEmpty();
    }

    /**
     * Get the number of property descriptors in the map.
     *
     * @return The size of the map.
     */
    public int size()
    {
        return myMap.size();
    }

    /**
     * Get the value lists.
     *
     * @return The value lists.
     */
    public Collection<? extends List<?>> values()
    {
        return myMap.values();
    }
}
