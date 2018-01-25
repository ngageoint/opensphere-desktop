Working with Events
-------------------
Many operations in the application are initiated through asynchronous events. To facilitate the creation and propagation of events, a central mechanism is provided as part of the Core. This mechanism, known as the Event Manager, is responsible for registration / de-registration of listeners, and sending events to registered listeners.  Events may occur immediately or be scheduled for a future time. They may also be instantaneous or have a duration. The Event Manager instance is referenced using the core system toolbox.

The Event manager makes use of custom Event and Event Listener implementations. As such, all Events must implement the `io.opensphere.core.event.Event` interface, and all Listener implementation must implement the `io.opensphere.core.event.EventListener`  interface. 

Eventing infrastructure is defined in the Core library, and to take advantage, your plugin project must depend on Core. 

### Custom Event Listeners

While it is possible to create a custom concrete event listener class, which implements `io.opensphere.core.event.EventListener` (or an intermediate sub-interface), it is highly recommended to avoid this approach. Instead, where ever possible, you can use a Java 8 Lambda reference to assign a method as an event listener, completely avoiding the need to create a new listener interface or class. 

    private final EventListener<FooEvent> myFooListener = this::handleEventFoo;
    
    /**
     * Handle foo events.
     *
     * @param pEvent the foo event, fired when fighting foo.
     */
    protected void handleEventFoo(FooEvent pEvent)
    {
        LOG.info("Event Received!");
    } 

### Accessing the Event Manager Instance

To access the event manager, you must have a reference to the system Toolbox. Using this reference, perform the following:

    EventManager eventManager = getToolbox().getEventManager();

### Registering a Listener with the Event Manager

After implementing a custom Event Listener (or using a method reference to act as a listener implementation), instances of the listener may be registered with the Event Manager to receive notification by use of the `EventManager.subscribe` method. Consider the following example:

    /**
     * An example class used to provide examples.
     */
    private class ExampleClass 
    { 
        /**
         * The event listener instance used to receive notification when foo is being fought.
         */
        private final EventListener<FooEvent> myFooListener = this::handleEventFoo;
        
        /**
         * Creates a new example class, using the supplied toolbox to communicate with the rest of 
         * the application.
         * 
         * @param pToolbox the toolbox through which event subscriptions are managed.
         */
        public ExampleClass(Toolbox pToolbox) 
        {
            EventManager eventManager = getToolbox().getEventManager();
            eventManager.subscribe(FooEvent.class, myFooListener);
        }
    
        /**
         * Handle foo events.
         *
         * @param pEvent the foo event, fired when fighting foo.
         */
        protected void handleEventFoo(FooEvent pEvent)
        {
            LOG.info("Event Received!");
        } 
    }

### De-registering a Listener from the Event Manager

When reacting to events is no longer necessary, the listener should be removed from the manager. This should be done as soon as possible, to avoid unnecessary performance penalties from unused events. To remove a registered listener, use the `EventManager.unsubscribe` method:

    /**
     * Clean up after fighting foo has completed.
     *
     * @param pToolbox the toolbox through which event subscriptions are managed.
     */
    public void cleanup(Toolbox pToolbox)
    { 
        EventManager eventManager = getToolbox().getEventManager();
        eventManager.unsubscribe(FooEvent.class, myFooListener);
    }

### Sending an Event

To inform registered listeners that a specific action has taken place, or a condition has been met, an event is fired through the `EventManager`, which allows for asynchronous notification and reaction to the event. To do this, use the `EventManager.publishEvent` method:

    /**
     * Notifies all registered listeners that foo has taken place.
     * 
     * @param pToolbox the toolbox through which event subscriptions are managed.
     * @param pFooEvent the event describing foo.
     */
    public void sendFooEvent(Toolbox pToolbox, FooEvent pFooEvent)
    {
        EventManager eventManager = getToolbox().getEventManager();
        eventManager.publishEvent(pFooEvent);
    }
