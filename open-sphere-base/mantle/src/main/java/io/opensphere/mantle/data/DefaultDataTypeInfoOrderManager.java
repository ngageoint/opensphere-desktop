package io.opensphere.mantle.data;

import java.util.Collections;
import java.util.Map;

import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Helper method for managing the Z-order of a {@link DataTypeInfo}.
 */
public class DefaultDataTypeInfoOrderManager implements DataTypeInfoOrderManager
{
    /** Listener for changes to the order. */
    private final OrderChangeListener myOrderChangeListener = event ->
    {
        if (event.getChangeType() == ParticipantChangeType.ORDER_CHANGED)
        {
            event.getChangedParticipants().forEachEntry((participant, order) ->
            {
                DataTypeInfo dti = myOrderKeyMap.get(participant);
                if (dti != null)
                {
                    dti.getMapVisualizationInfo().setZOrder(order, DefaultDataTypeInfoOrderManager.this);
                }
                return true;
            });
        }
    };

    /** A map of the order key to the data type info being ordered. */
    private final Map<OrderParticipantKey, DataTypeInfo> myOrderKeyMap = Collections
            .synchronizedMap(New.<OrderParticipantKey, DataTypeInfo>weakMap());

    /** Manager which determines the z-order of the CPTC results. */
    private final OrderManager myOrderManager;

    /**
     * Constructor.
     *
     * @param orderManager The order manager.
     */
    public DefaultDataTypeInfoOrderManager(OrderManager orderManager)
    {
        myOrderManager = Utilities.checkNull(orderManager, "orderManager");
    }

    /**
     * Constructor that uses the default order manager from the order manager
     * registry.
     *
     * @param orderManagerRegistry The order manager registry.
     */
    public DefaultDataTypeInfoOrderManager(OrderManagerRegistry orderManagerRegistry)
    {
        this(Utilities.checkNull(orderManagerRegistry, "orderManagerRegistry")
                .getOrderManager(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, DefaultOrderCategory.FEATURE_CATEGORY));
    }

    @Override
    public void activateParticipant(DataTypeInfo type)
    {
        if (type.getMapVisualizationInfo() != null)
        {
            type.getMapVisualizationInfo().setZOrder(myOrderManager.activateParticipant(addDataType(type)), this);
        }
    }

    @Override
    public void deactivateParticipant(DataTypeInfo type)
    {
        if (myOrderKeyMap.remove(type.getOrderKey()) != null)
        {
            myOrderManager.deactivateParticipant(type.getOrderKey());
        }
    }

    @Override
    public void close()
    {
        synchronized (myOrderKeyMap)
        {
            for (OrderParticipantKey orderParticipantKey : myOrderKeyMap.keySet())
            {
                myOrderManager.deactivateParticipant(orderParticipantKey);
            }
            myOrderKeyMap.clear();
        }
        myOrderManager.removeParticipantChangeListener(myOrderChangeListener);
    }

    @Override
    public void expungeDataType(DataTypeInfo type)
    {
        myOrderManager.expungeParticipant(type.getOrderKey());
        myOrderKeyMap.remove(type.getOrderKey());
    }

    @Override
    public void open()
    {
        myOrderManager.addParticipantChangeListener(myOrderChangeListener);
    }

    /**
     * Create an order key for a data type and add the data type info and key to
     * my map.
     *
     * @param type The data type info.
     * @return The order key.
     */
    protected OrderParticipantKey addDataType(DataTypeInfo type)
    {
        OrderParticipantKey orderKey = new DefaultOrderParticipantKey(myOrderManager.getFamily(), myOrderManager.getCategory(),
                type.getTypeKey());
        type.setOrderKey(orderKey);
        myOrderKeyMap.put(orderKey, type);
        return orderKey;
    }
}
