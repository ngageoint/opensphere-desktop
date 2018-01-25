package io.opensphere.core.data.util;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.Utilities;

/**
 * Abstract implementation of a property value receiver.
 *
 * @param <T> The type of the expected property values.
 */
public abstract class AbstractPropertyValueReceiver<T> implements PropertyValueReceiver<T>
{
    /** The property descriptor. */
    private final PropertyDescriptor<T> myPropertyDescriptor;

    /** The optional value processor. */
    private final ValueProcessor<T> myValueProcessor;

    /**
     * Constructor.
     *
     * @param propertyDescriptor The property descriptor.
     */
    public AbstractPropertyValueReceiver(PropertyDescriptor<T> propertyDescriptor)
    {
        this(propertyDescriptor, (ValueProcessor<T>)null);
    }

    /**
     * Constructor.
     *
     * @param propertyDescriptor The property descriptor.
     * @param valueProcessor An optional processor to be called as properties
     *            are received.
     */
    public AbstractPropertyValueReceiver(PropertyDescriptor<T> propertyDescriptor, ValueProcessor<T> valueProcessor)
    {
        Utilities.checkNull(propertyDescriptor, "propertyDescriptor");
        myPropertyDescriptor = propertyDescriptor;
        myValueProcessor = valueProcessor;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        return myPropertyDescriptor.equals(((AbstractPropertyValueReceiver<?>)obj).myPropertyDescriptor);
    }

    @Override
    public PropertyDescriptor<T> getPropertyDescriptor()
    {
        return myPropertyDescriptor;
    }

    @Override
    public ValueProcessor<T> getValueProcessor()
    {
        return myValueProcessor;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myPropertyDescriptor == null ? 0 : myPropertyDescriptor.hashCode());
        return result;
    }
}
