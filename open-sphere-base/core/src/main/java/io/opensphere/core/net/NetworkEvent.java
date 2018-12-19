package io.opensphere.core.net;

import java.util.Date;

import io.opensphere.core.event.AbstractEvent;

/** An abstract base class from which all network events descend. */
public abstract class NetworkEvent extends AbstractEvent
{
    /** The time at which the event took place. */
    private final Date myEventTime;

    /**
     * The transaction ID used to relate multiple network events together. If
     * the event represents a single transaction with only one event, this is
     * optional. {@link #myTransactionId}
     */
    private String myTransactionId;

    /** The state of the event. */
    private State myState;

    /**
     * Creates a new network event with default values.
     */
    public NetworkEvent()
    {
        myEventTime = new Date();
        myState = State.COMPLETED;
    }

    /**
     * Creates a new network event with the supplied transaction ID.
     *
     * @param transactionId the unique identifier applied to the transaction.
     */
    public NetworkEvent(String transactionId)
    {
        this();
        myTransactionId = transactionId;
    }

    /**
     * Creates a new network event with the supplied parameters.
     *
     * @param transactionId the unique identifier applied to the transaction.
     * @param state The state of the event.
     */
    public NetworkEvent(String transactionId, State state)
    {
        this(transactionId);
        myState = state;
    }

    /**
     * Gets the value of the {@link #myTransactionId} field.
     *
     * @return the value of the myTransactionId field.
     */
    public String getTransactionId()
    {
        return myTransactionId;
    }

    /**
     * Stores the supplied value in the {@link #myTransactionId} field.
     *
     * @param transactionId the value to store in the transactionId field.
     */
    public void setTransactionId(String transactionId)
    {
        myTransactionId = transactionId;
    }

    /**
     * Gets the value of the {@link #myEventTime} field.
     *
     * @return the value of the myEventTime field.
     */
    public Date getEventTime()
    {
        return myEventTime;
    }

    @Override
    public String getDescription()
    {
        return "An event describing a netwrok transmission.";
    }

    @Override
    public State getState()
    {
        return myState;
    }
}
