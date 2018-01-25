package io.opensphere.core.data.util;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Default order specifier.
 */
public class DefaultOrderSpecifier implements OrderSpecifier
{
    /** The order type. */
    private final Order myOrder;

    /** The property descriptor. */
    private final PropertyDescriptor<?> myPropertyDescriptor;

    /**
     * Construct the order specifier.
     *
     * @param order The order.
     * @param propertyDescriptor The property descriptor.
     */
    public DefaultOrderSpecifier(Order order, PropertyDescriptor<?> propertyDescriptor)
    {
        myOrder = order;
        myPropertyDescriptor = propertyDescriptor;
    }

    @Override
    public Order getOrder()
    {
        return myOrder;
    }

    @Override
    public PropertyDescriptor<?> getPropertyDescriptor()
    {
        return myPropertyDescriptor;
    }
}
