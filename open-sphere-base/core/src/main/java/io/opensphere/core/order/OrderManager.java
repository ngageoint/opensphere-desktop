package io.opensphere.core.order;

import java.util.Collection;
import java.util.List;

import gnu.trove.map.TObjectIntMap;
import io.opensphere.core.util.Service;

/**
 * The interface for order managers. An order manager takes a set of participant
 * keys and assigns them unique integer orders starting from the minimum for the
 * category and increasing consecutively. Participants are treated equally with
 * regard to order regardless of whether they are active, so if a subset of the
 * participants are active, their orders may not be consecutive. Any changes to
 * participant orders (including addition and removal) may cause other
 * participants to be reordered as required to maintain consecutive order
 * numbers. Getting the order manager from {@link OrderManagerRegistry} will
 * cause it to be created.
 */
public interface OrderManager
{
    /**
     * Activate a participant in this order manager adding it if required.
     *
     * @param participant the participant to activate.
     * @return the order of the participant.
     */
    int activateParticipant(OrderParticipantKey participant);

    /**
     * Activate a collection of participant in this order manager adding them if
     * required.
     *
     * @param participants the participant to activate.
     */
    void activateParticipants(Collection<OrderParticipantKey> participants);

    /**
     * Add a participant to this order manager without making it active.
     *
     * @param participant the participant to add.
     * @return the new order of the participant.
     */
    int addParticipant(OrderParticipantKey participant);

    /**
     * Add a listener for changes to order participants. Only a weak reference
     * is held.
     *
     * @param listener the listener to add.
     */
    void addParticipantChangeListener(OrderChangeListener listener);

    /**
     * Add participants to this order manager.
     *
     * @param adds the participants to add.
     */
    void addParticipants(Collection<OrderParticipantKey> adds);

    /**
     * Deactivate a participant in this order manager.
     *
     * @param participant the participant to deactivate.
     * @return the order of the participant.
     */
    int deactivateParticipant(OrderParticipantKey participant);

    /**
     * Deactivate a collection of participant in this order manager.
     *
     * @param participants the participant to deactivate.
     */
    void deactivateParticipants(Collection<OrderParticipantKey> participants);

    /**
     * Remove a participant from this order manager and remove the persistent
     * order value, if the order value should be preserved for later use,
     * deactivate rather than remove.
     *
     * @param participant the participant to remove.
     * @return the order of the participant before removal.
     */
    int expungeParticipant(OrderParticipantKey participant);

    /**
     * Remove participants from this order manager.
     *
     * @param removes the participants to remove.
     */
    void expungeParticipants(Collection<OrderParticipantKey> removes);

    /**
     * Get the active participants currently managed by this manager in order.
     *
     * @return an ordered collection of the active participants currently
     *         managed by this manager.
     */
    List<OrderParticipantKey> getActiveParticipants();

    /**
     * Get the category.
     *
     * @return the category
     */
    OrderCategory getCategory();

    /**
     * Get the family.
     *
     * @return the family
     */
    String getFamily();

    /**
     * Get the order of the participant.
     *
     * @param participant the participant whose order is desired.
     * @return the order of the participant.
     */
    int getOrder(OrderParticipantKey participant);

    /**
     * Get the map of orders to participants currently managed by this manager.
     *
     * @return the map of orders to participants currently managed by this
     *         manager.
     */
    TObjectIntMap<OrderParticipantKey> getParticipantMap();

    /**
     * Tell whether this manager has an active participant with the given id.
     *
     * @param orderId the order id of the participant.
     * @return true when this manager has an active participant with the given
     *         id.
     */
    boolean hasActiveParticipant(String orderId);

    /**
     * Tell whether the participant is managed by this manager.
     *
     * @param participant the participant.
     * @return true when the participant is managed by this manager.
     */
    boolean isManaged(OrderParticipantKey participant);

    /**
     * Move the participant to be directly above the reference participant.
     *
     * @param participant the participant to move.
     * @param reference the participant at the reference location.
     * @return the order of the moved participant.
     */
    int moveAbove(OrderParticipantKey participant, OrderParticipantKey reference);

    /**
     * Move the participant to be directly below the reference participant.
     *
     * @param participant the participant to move.
     * @param reference the participant at the reference location.
     * @return the order of the moved participant.
     */
    int moveBelow(OrderParticipantKey participant, OrderParticipantKey reference);

    /**
     * Move a participant to the bottom.
     *
     * @param participant the participant to move.
     * @return the new order of the participant.
     */
    int moveToBottom(OrderParticipantKey participant);

    /**
     * Move a participant to the top.
     *
     * @param participant the participant to move.
     * @return the new order of the participant.
     */
    int moveToTop(OrderParticipantKey participant);

    /**
     * Remove a listener for changes to order participants.
     *
     * @param listener the listener to remove.
     */
    void removeParticipantChangeListener(OrderChangeListener listener);

    /**
     * Creates a service that can be used to add/remove the given listener.
     *
     * @param listener the listener
     * @return the service
     */
    default Service getParticipantChangeListenerService(final OrderChangeListener listener)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                addParticipantChangeListener(listener);
            }

            @Override
            public void close()
            {
                removeParticipantChangeListener(listener);
            }
        };
    }
}
