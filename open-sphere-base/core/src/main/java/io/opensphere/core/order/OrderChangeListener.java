package io.opensphere.core.order;

/**
 * Interface for listeners for changes to order participant.
 */
@FunctionalInterface
public interface OrderChangeListener
{
    /**
     * Method called when a participant has changed.
     *
     * @param event The event which has occurred.
     */
    void orderChanged(ParticipantOrderChangeEvent event);
}
