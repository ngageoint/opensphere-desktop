package io.opensphere.core.util.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

/**
 * A listener handler used to add multiple property change listeners to a single
 * lambda.
 */
public class LambdaMultiplePropertyChangeListenerHandler
{
    /** A map of property references. */
    private final Map<ObservableValue<?>, Consumer<ObservableValue<?>>> myPropertyReferenceMap;

    /** The change listener registered for notifications. */
    private final ChangeListener<Object> myPropertyChangedListener;

    /** A handle to the weak reference of the property change listener. */
    private final WeakChangeListener<Object> myWeakPropertyChangedListener;

    /** Default no-op consumer. */
    private static final Consumer<ObservableValue<?>> EMPTY_CONSUMER = e ->
    {
        /* intentionally blank */
    };

    /** Creates a new handler. */
    public LambdaMultiplePropertyChangeListenerHandler()
    {
        myPropertyReferenceMap = new HashMap<>();
        myPropertyChangedListener = (observable, oldValue, newValue) ->
        {
            // because all consumers are chained, this calls each consumer for
            // the given property in turn.
            myPropertyReferenceMap.getOrDefault(observable, EMPTY_CONSUMER).accept(observable);
        };
        myWeakPropertyChangedListener = new WeakChangeListener<>(myPropertyChangedListener);
    }

    /**
     * Subclasses can invoke this method to register that we want to listen to
     * property change events for the given property.
     *
     * @param property the property for which to register the listener.
     * @param consumer the consumer to register as the listener.
     */
    public final void registerChangeListener(final ObservableValue<?> property, final Consumer<ObservableValue<?>> consumer)
    {
        if (consumer == null)
        {
            return;
        }

        // we only add a listener if the myPropertyReferenceMap does not contain
        // the property (that is, we've added a consumer to this specific
        // property for the first time).
        if (!myPropertyReferenceMap.containsKey(property))
        {
            property.addListener(myWeakPropertyChangedListener);
        }

        myPropertyReferenceMap.merge(property, consumer, Consumer::andThen);
    }

    /**
     * Unregisters all listeners registered for notifications on the supplied
     * property.
     *
     * @param property the property for which to unregister listeners.
     * @return the listener unregistered from notifications of changes to the
     *         supplied property.
     */
    public final Consumer<ObservableValue<?>> unregisterChangeListeners(final ObservableValue<?> property)
    {
        // need to be careful here - removing all listeners on the specific
        // property!
        property.removeListener(myWeakPropertyChangedListener);
        return myPropertyReferenceMap.remove(property);
    }

    /**
     * Cleans up and prepares for garbage disposal.
     */
    public void dispose()
    {
        // unhook listeners
        for (final ObservableValue<?> value : myPropertyReferenceMap.keySet())
        {
            value.removeListener(myWeakPropertyChangedListener);
        }
        myPropertyReferenceMap.clear();
    }
}
