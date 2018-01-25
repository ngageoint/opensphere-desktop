package io.opensphere.core.data.util;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * An order specification.
 */
public interface OrderSpecifier
{
    /**
     * Get the order for this property.
     *
     * @return The order.
     */
    OrderSpecifier.Order getOrder();

    /**
     * Get the ordered property description.
     *
     * @return The property descriptor.
     */
    PropertyDescriptor<?> getPropertyDescriptor();

    /** Enumeration of order constants. */
    enum Order
    {
        /** Constant indicating ascending order. */
        ASCENDING,

        /** Constant indicating descending order. */
        DESCENDING,
    }
}
