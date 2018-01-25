package io.opensphere.mantle.data.event;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Events from changes to DataTypeInfo.
 */
public class DataTypeInfoZOrderChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param order - the new z-order
     * @param source - the source of the event.
     */
    public DataTypeInfoZOrderChangeEvent(DataTypeInfo dti, int order, Object source)
    {
        super(dti, Type.Z_ORDER_CHANGED, Integer.valueOf(order), source);
    }

    /**
     * Gets the new z-order value.
     *
     * @return the new order.
     */
    public int getOrder()
    {
        return ((Integer)getValue()).intValue();
    }
}
