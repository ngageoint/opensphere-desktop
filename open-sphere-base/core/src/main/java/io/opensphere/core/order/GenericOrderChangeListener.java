package io.opensphere.core.order;

import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.core.util.ToBinaryObjectIntFunction;

/**
 * A change listener implementation used to react to changes in z-order.
 */
public class GenericOrderChangeListener implements OrderChangeListener
{
    /**
     * The function to call when the event is received.
     */
    private final ToBinaryObjectIntFunction<OrderParticipantKey> myFunction;

    /**
     * Creates a new order change listener, configured to call the supplied
     * function when the order changes.
     *
     * @param pFunction the function to call when the event is received.
     */
    public GenericOrderChangeListener(ToBinaryObjectIntFunction<OrderParticipantKey> pFunction)
    {
        myFunction = pFunction;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.order.OrderChangeListener#orderChanged(io.opensphere.core.order.ParticipantOrderChangeEvent)
     */
    @Override
    public void orderChanged(ParticipantOrderChangeEvent event)
    {
        if (event.getChangeType() == ParticipantChangeType.ORDER_CHANGED)
        {
            event.getChangedParticipants().forEachEntry((participant, order) -> myFunction.apply(participant, order));
        }
    }
}
