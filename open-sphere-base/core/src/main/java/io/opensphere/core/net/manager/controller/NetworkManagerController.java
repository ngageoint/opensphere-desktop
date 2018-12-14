package io.opensphere.core.net.manager.controller;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.net.NetworkReceiveEvent;
import io.opensphere.core.net.NetworkTransmitEvent;
import io.opensphere.core.net.manager.model.NetworkTransaction;
import io.opensphere.core.net.manager.model.NetworkTransactionModel;

/** A controller used to orchestrate the network manager. */
public class NetworkManagerController
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(NetworkManagerController.class);

    /** The model managed by this controller. */
    private final NetworkTransactionModel myModel;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /** The event handler used to process transmission events. */
    private EventListener<? super NetworkTransmitEvent> myTransmitSubscriber;

    /** The event handler used to process receive events. */
    private EventListener<? super NetworkReceiveEvent> myReceiveSubscriber;

    /**
     * Creates a new network manager controller, bound to the supplied toolbox.
     *
     * @param toolbox the toolbox through which application state is accessed.
     */
    public NetworkManagerController(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myModel = new NetworkTransactionModel();
        myTransmitSubscriber = this::dataSent;
        myReceiveSubscriber = this::dataReceived;
    }

    /**
     * Prepares the controller for execution, subscribing it to all relevant
     * events.
     */
    public void open()
    {
        myToolbox.getEventManager().subscribe(NetworkTransmitEvent.class, myTransmitSubscriber);
        myToolbox.getEventManager().subscribe(NetworkReceiveEvent.class, myReceiveSubscriber);
    }

    /**
     * Prepares the controller for shutdown, unsubscribing it from all relevant
     * events.
     */
    public void close()
    {
        myToolbox.getEventManager().unsubscribe(NetworkTransmitEvent.class, myTransmitSubscriber);
        myToolbox.getEventManager().unsubscribe(NetworkReceiveEvent.class, myReceiveSubscriber);
    }

    /**
     * Gets the value of the {@link #myModel} field.
     *
     * @return the value stored in the {@link #myModel} field.
     */
    public NetworkTransactionModel getModel()
    {
        return myModel;
    }

    /**
     * Handles the processing of network transmission events. New events are
     * mapped to transactions and populated into the model.
     *
     * @param event the event to process.
     */
    private void dataSent(NetworkTransmitEvent event)
    {
        NetworkTransaction transaction = new NetworkTransaction(event.getTransactionId());
        transaction.sendEventProperty().set(event);
        // transaction.updateValues(event);
        myModel.getTransactions().add(transaction);
    }

    /**
     * Handles the processing of network received events. Events are mapped to
     * transactions and populated into the model.
     *
     * @param event the event to process.
     */
    private void dataReceived(NetworkReceiveEvent event)
    {
        NetworkTransaction transaction = myModel.getTransaction(event.getTransactionId());
        if (transaction == null)
        {
            LOG.warn("Attempted to process a network received event with no submit event.");
            return;
        }
        transaction.receiveEventProperty().set(event);
        // transaction.updateValues(event);
    }
}
