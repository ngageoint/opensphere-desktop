package io.opensphere.core.util.javafx.input.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

/**
 * A handler designed as a clearing house for change events. Classes may register properties with this handler, which binds to the
 * property's {@link ObservableValue} reference with a String event name. When the property changes, a single event is propagated
 * to the registering class, allowing the class to react to the change.
 */
public class MultiplePropertyChangeListenerHandler
{
    /**
     * The callback through which property changes are handled.
     */
    private final Consumer<String> myPropertyChangedHandler;

    /**
     * A change listener used to register for notifications of changes to the bound properties.
     */
    private final ChangeListener<Object> myPropertyChangedListener;

    /**
     * A weak change listener wrapping the {@link #myPropertyChangedListener} field.
     */
    private final WeakChangeListener<Object> myWeakPropertyChangedListener;

    /**
     * A reference map in which properties are bound to observable values.
     */
    private final Map<ObservableValue<?>, String> myPropertyReferenceMap = new HashMap<>();

    /**
     * Creates a new property change listener, configured with the supplied callback.
     *
     * @param pPropertyChangedHandler The callback through which property changes are handled.
     */
    public MultiplePropertyChangeListenerHandler(Consumer<String> pPropertyChangedHandler)
    {
        myPropertyChangedHandler = pPropertyChangedHandler;
        myPropertyChangedListener = (property, oldValue, newValue) -> myPropertyChangedHandler
                .accept(myPropertyReferenceMap.get(property));
        myWeakPropertyChangedListener = new WeakChangeListener<>(myPropertyChangedListener);
    }

    /**
     * Subclasses can invoke this method to register that we want to listen to property change events for the given property.
     *
     * @param pProperty the property to which to bind the listener handler.
     * @param pChangeEventName the name of the event that will be propagated when the property changes.
     */
    public final void registerChangeListener(ObservableValue<?> pProperty, String pChangeEventName)
    {
        if (!myPropertyReferenceMap.containsKey(pProperty))
        {
            myPropertyReferenceMap.put(pProperty, pChangeEventName);
            pProperty.addListener(myWeakPropertyChangedListener);
        }
    }

    /**
     * Removes the registered property from the listener.
     *
     * @param pProperty the property to remove.
     */
    public final void unregisterChangeListener(ObservableValue<?> pProperty)
    {
        if (myPropertyReferenceMap.containsKey(pProperty))
        {
            myPropertyReferenceMap.remove(pProperty);
            pProperty.removeListener(myWeakPropertyChangedListener);
        }
    }

    /**
     * Cleans up all registered properties.
     */
    public void dispose()
    {
        // unhook listeners
        for (ObservableValue<?> value : myPropertyReferenceMap.keySet())
        {
            value.removeListener(myWeakPropertyChangedListener);
        }
        myPropertyReferenceMap.clear();
    }
    //
    //    /**
    //     * The callback through which property changes are handled.
    //     */
    //    private final Callback<String, Void> myPropertyChangedHandler;
    //
    //    /**
    //     * A change listener used to register for notifications of changes to the bound properties.
    //     */
    //    private final ChangeListener<Object> myPropertyChangedListener;
    //
    //    /**
    //     * A weak change listener wrapping the {@link #myPropertyChangedListener} field.
    //     */
    //    private final WeakChangeListener<Object> myWeakPropertyChangedListener;
    //
    //    /**
    //     * A reference map in which properties are bound to observable values.
    //     */
    //    private final Map<ObservableValue<?>, String> myPropertyReferenceMap = new HashMap<>();
    //
    //    /**
    //     * Creates a new property change listener, configured with the supplied callback.
    //     *
    //     * @param pPropertyChangedHandler The callback through which property changes are handled.
    //     */
    //    public MultiplePropertyChangeListenerHandler(Callback<String, Void> pPropertyChangedHandler)
    //    {
    //        myPropertyChangedHandler = pPropertyChangedHandler;
    //        myPropertyChangedListener = (property, oldValue, newValue) -> myPropertyChangedHandler
    //                .call(myPropertyReferenceMap.get(property));
    //        myWeakPropertyChangedListener = new WeakChangeListener<>(myPropertyChangedListener);
    //    }
    //
    //    /**
    //     * Subclasses can invoke this method to register that we want to listen to property change events for the given property.
    //     *
    //     * @param pProperty the property to which to bind the listener handler.
    //     * @param pChangeEventName the name of the event that will be propagated when the property changes.
    //     */
    //    public final void registerChangeListener(ObservableValue<?> pProperty, String pChangeEventName)
    //    {
    //        if (!myPropertyReferenceMap.containsKey(pProperty))
    //        {
    //            myPropertyReferenceMap.put(pProperty, pChangeEventName);
    //            pProperty.addListener(myWeakPropertyChangedListener);
    //        }
    //    }
    //
    //    /**
    //     * Removes the registered property from the listener.
    //     *
    //     * @param pProperty the property to remove.
    //     */
    //    public final void unregisterChangeListener(ObservableValue<?> pProperty)
    //    {
    //        if (myPropertyReferenceMap.containsKey(pProperty))
    //        {
    //            myPropertyReferenceMap.remove(pProperty);
    //            pProperty.removeListener(myWeakPropertyChangedListener);
    //        }
    //    }
    //
    //    /**
    //     * Cleans up all registered properties.
    //     */
    //    public void dispose()
    //    {
    //        // unhook listeners
    //        for (ObservableValue<?> value : myPropertyReferenceMap.keySet())
    //        {
    //            value.removeListener(myWeakPropertyChangedListener);
    //        }
    //        myPropertyReferenceMap.clear();
    //    }
}
