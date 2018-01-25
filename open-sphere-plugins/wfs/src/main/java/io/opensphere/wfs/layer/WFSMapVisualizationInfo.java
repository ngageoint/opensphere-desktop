package io.opensphere.wfs.layer;

import gnu.trove.procedure.TObjectIntProcedure;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent;
import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;

/**
 * The Class WFSMapVisualizationInfo.
 */
public class WFSMapVisualizationInfo extends DefaultMapFeatureVisualizationInfo
{
    /** Flag indicating whether the Z-Order has been set. */
    private boolean myIsOrderSet;

    /** Listener for changes to the order of my data type. */
    private OrderChangeListener myOrderChangeListener;

    /** My Z-Order manager. */
    private final transient OrderManager myZOrderManager;

    /**
     * Instantiates a new WFS map visualization info.
     *
     * @param visType the visualization type
     * @param orderManager the Z-Order manager
     */
    public WFSMapVisualizationInfo(MapVisualizationType visType, OrderManager orderManager)
    {
        super(visType);
        myZOrderManager = orderManager;
    }

    @Override
    public int getZOrder()
    {
        // The myIsOrderSet flag is used so the Z-Order is not assigned until
        // the first time it is actually used. This prevents unused layers
        // (which
        // could number in the thousands) from getting assigned too early, which
        // could
        // order them below previously queried layers. It also prevents unused
        // layers
        // from cluttering up the saved z-order configuration.
        if (!myIsOrderSet)
        {
            myOrderChangeListener = new OrderChangeListener()
            {
                @Override
                public void orderChanged(ParticipantOrderChangeEvent event)
                {
                    if (event.getChangeType() == ParticipantChangeType.ORDER_CHANGED)
                    {
                        event.getChangedParticipants().forEachEntry(new TObjectIntProcedure<OrderParticipantKey>()
                        {
                            @Override
                            public boolean execute(OrderParticipantKey participant, int order)
                            {
                                if (participant.equals(getDataTypeInfo().getOrderKey())
                                        && getDataTypeInfo().getMapVisualizationInfo() != null)
                                {
                                    getDataTypeInfo().getMapVisualizationInfo().setZOrder(order, null);
                                }
                                return true;
                            }
                        });
                    }
                }
            };
            myZOrderManager.addParticipantChangeListener(myOrderChangeListener);
            setZOrder(myZOrderManager.activateParticipant(getDataTypeInfo().getOrderKey()), this);
            myIsOrderSet = true;
        }
        return super.getZOrder();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(super.toString());
        sb.append("  IsOrderSet          :").append(myIsOrderSet).append('\n');
        return sb.toString();
    }
}
