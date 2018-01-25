package io.opensphere.core.util.fx;

import java.util.Observable;
import java.util.function.Consumer;

import javafx.scene.control.ButtonBar;

import io.opensphere.core.util.NonSuckingObservable;

/**
 * An abstract extension of the {@link ButtonBar} class, in which a click observer is provided for handling custom click events.
 */
public class ObservableButtonBar extends ButtonBar
{
    /**
     * The observable instance used to watch for button clicks.
     */
    private final Observable myButtonClickObservable = new NonSuckingObservable();

    /**
     * Creates a new button bar, using the supplied button order.
     *
     * @param pButtonOrder The button order to use in this button bar instance.
     * @see javafx.scene.control.ButtonBar#BUTTON_ORDER_WINDOWS
     * @see javafx.scene.control.ButtonBar#BUTTON_ORDER_LINUX
     * @see javafx.scene.control.ButtonBar#BUTTON_ORDER_MAC_OS
     * @see javafx.scene.control.ButtonBar#BUTTON_ORDER_NONE
     */
    public ObservableButtonBar(String pButtonOrder)
    {
        super(pButtonOrder);
    }

    /**
     * Adds a listener for button clicks.
     *
     * @param listener the listener
     */
    public void addButtonClickListener(Consumer<ButtonData> listener)
    {
        myButtonClickObservable.addObserver((Observable o, Object arg) -> listener.accept((ButtonData)arg));
    }

    /**
     * Gets the value of the {@link #myButtonClickObservable} field.
     *
     * @return the value stored in the {@link #myButtonClickObservable} field.
     */
    public Observable getButtonClickObservable()
    {
        return myButtonClickObservable;
    }
}
