package io.opensphere.core.data.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * A default property value receiver that adds the received properties to a
 * list.
 *
 * @param <T> The type of the received properties.
 */
public class DefaultPropertyValueReceiver<T> extends AbstractPropertyValueReceiver<T>
{
    /** The list of values received. */
    private volatile List<T> myValues = Collections.emptyList();

    /**
     * Constructor.
     *
     * @param propertyDescriptor The property descriptor.
     */
    public DefaultPropertyValueReceiver(PropertyDescriptor<T> propertyDescriptor)
    {
        super(propertyDescriptor);
    }

    /**
     * Constructor.
     *
     * @param propertyDescriptor The property descriptor.
     * @param valueProcessor An optional processor to be called as properties
     *            are received.
     */
    public DefaultPropertyValueReceiver(PropertyDescriptor<T> propertyDescriptor, ValueProcessor<T> valueProcessor)
    {
        super(propertyDescriptor, valueProcessor);
    }

    /**
     * Clear any values that have been retrieved so far.
     */
    public synchronized void clearValues()
    {
        myValues = Collections.emptyList();
    }

    /**
     * Get the values that have been retrieved so far. This will return an
     * unmodifiable, iteration-safe list. Subsequent calls may return different
     * list instances if values are retrieved in multiple batches, so this
     * method must be called again to get the new values.
     *
     * @return The values.
     */
    public List<T> getValues()
    {
        return myValues;
    }

    @Override
    public synchronized void receive(List<? extends T> values)
    {
        int addSize = values.size();
        if (addSize > 0)
        {
            int newSize = addSize + myValues.size();
            List<T> list;
            if (newSize > addSize)
            {
                list = new ArrayList<>(newSize);
                list.addAll(myValues);
                list.addAll(values);
            }
            else
            {
                list = new ArrayList<>(values);
            }
            myValues = Collections.unmodifiableList(list);
        }
    }
}
