package io.opensphere.core.util.fx;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

/**
 *
 */
public class LambdaMultiplePropertyChangeListenerHandler
{
    private final Map<ObservableValue<?>, Consumer<ObservableValue<?>>> propertyReferenceMap;

    private final ChangeListener<Object> propertyChangedListener;

    private final WeakChangeListener<Object> weakPropertyChangedListener;

    private static final Consumer<ObservableValue<?>> EMPTY_CONSUMER = e ->
    {
    };

    public LambdaMultiplePropertyChangeListenerHandler()
    {
        this.propertyReferenceMap = new HashMap<>();
        this.propertyChangedListener = (observable, oldValue, newValue) ->
        {
            // because all consumers are chained, this calls each consumer for
            // the given property
            // in turn.
            propertyReferenceMap.getOrDefault(observable, EMPTY_CONSUMER).accept(observable);
        };
        this.weakPropertyChangedListener = new WeakChangeListener<>(propertyChangedListener);
    }

    /**
     * Subclasses can invoke this method to register that we want to listen to
     * property change events for the given property.
     *
     * @param property
     */
    public final void registerChangeListener(ObservableValue<?> property, Consumer<ObservableValue<?>> consumer)
    {
        if (consumer == null)
        {
            return;
        }

        // we only add a listener if the propertyReferenceMap does not contain
        // the property
        // (that is, we've added a consumer to this specific property for the
        // first
        // time).
        if (!propertyReferenceMap.containsKey(property))
        {
            property.addListener(weakPropertyChangedListener);
        }

        propertyReferenceMap.merge(property, consumer, Consumer::andThen);
    }

    // need to be careful here - removing all listeners on the specific
    // property!
    public final Consumer<ObservableValue<?>> unregisterChangeListeners(ObservableValue<?> property)
    {
        property.removeListener(weakPropertyChangedListener);
        return propertyReferenceMap.remove(property);
    }

    public void dispose()
    {
        // unhook listeners
        for (ObservableValue<?> value : propertyReferenceMap.keySet())
        {
            value.removeListener(weakPropertyChangedListener);
        }
        propertyReferenceMap.clear();
    }
}
