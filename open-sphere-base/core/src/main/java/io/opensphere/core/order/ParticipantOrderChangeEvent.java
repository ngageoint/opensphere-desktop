package io.opensphere.core.order;

import gnu.trove.map.TObjectIntMap;

/**
 * Event which contains the details of a participant change.
 */
public class ParticipantOrderChangeEvent
{
    /** The participants which have been changed. */
    private final TObjectIntMap<OrderParticipantKey> myChangedParticipants;

    /** The type of change which has occurred. */
    private final ParticipantChangeType myChangeType;

    /**
     * Constructor.
     *
     * @param changedParticipants The participants which have been changed.
     * @param type The type of change which has occurred.
     */
    public ParticipantOrderChangeEvent(TObjectIntMap<OrderParticipantKey> changedParticipants, ParticipantChangeType type)
    {
        myChangedParticipants = changedParticipants;
        myChangeType = type;
    }

    /**
     * Get the changed participants.
     *
     * @return A map of the newly assigned order to the participant whose order
     *         has changed.
     */
    public TObjectIntMap<OrderParticipantKey> getChangedParticipants()
    {
        return myChangedParticipants;
    }

    /**
     * Get the change type.
     *
     * @return The change type.
     */
    public ParticipantChangeType getChangeType()
    {
        return myChangeType;
    }

    /** The type of change to the set of participants. */
    public enum ParticipantChangeType
    {
        /** An existing participant has been made active. */
        ACTIVATED,

        /** An existing participant has been made inactive. */
        DEACTIVATED,

        /**
         * A participant's order number has been changed. The participants
         * relative order to other participants may be the same even when its
         * order number has changed.
         */
        ORDER_CHANGED,

        ;
    }
}
