package io.opensphere.core.util.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;

import org.apache.commons.lang.StringUtils;

/**
 * An implementation of the {@link ObservableButtonBar} in which custom labels may be applied to the buttons.
 */
public class LabeledObservableButtonBar extends ObservableButtonBar
{
    /**
     * The property used to track the disable property. This is bound to all buttons except cancel buttons.
     */
    private final BooleanProperty myDisableButtonsProperty;

    /**
     * Creates a new button bar, using the {@link ButtonBar#BUTTON_ORDER_WINDOWS} button order.
     *
     * @see javafx.scene.control.ButtonBar#BUTTON_ORDER_WINDOWS
     */
    public LabeledObservableButtonBar()
    {
        super(ButtonBar.BUTTON_ORDER_WINDOWS);

        myDisableButtonsProperty = new SimpleBooleanProperty(false);
    }

    /**
     * Creates a new button bar, using the supplied button order.
     *
     * @param pButtonOrder The button order to use in this button bar instance.
     * @see javafx.scene.control.ButtonBar#BUTTON_ORDER_WINDOWS
     * @see javafx.scene.control.ButtonBar#BUTTON_ORDER_LINUX
     * @see javafx.scene.control.ButtonBar#BUTTON_ORDER_MAC_OS
     * @see javafx.scene.control.ButtonBar#BUTTON_ORDER_NONE
     */
    public LabeledObservableButtonBar(String pButtonOrder)
    {
        super(pButtonOrder);

        myDisableButtonsProperty = new SimpleBooleanProperty(false);
    }

    /**
     * Adds a button to the button bar. The button will not contain an icon, and will use the value of
     * {@link javafx.scene.control.ButtonBar.ButtonData#toString()} from the <code>pButtonData</code> parameter as the text of the
     * button.
     *
     * @param pButtonData the button data from which the button will be created.
     * @return the current instance of the button bar, to allow call chaining.
     */
    public LabeledObservableButtonBar addButton(ButtonData pButtonData)
    {
        return addButton(null, pButtonData);
    }

    /**
     * Adds a button to the button bar. The button will not contain an icon, and will use the supplied text for the button's
     * content. The button will be bound to the supplied {@link javafx.scene.control.ButtonBar.ButtonData}.
     *
     * @param pButtonText the text of the button to create.
     * @param pButtonData the button data from which the button will be created.
     * @return the current instance of the button bar, to allow call chaining.
     */
    public LabeledObservableButtonBar addButton(String pButtonText, ButtonData pButtonData)
    {
        return addButton(pButtonText, null, pButtonData);
    }

    /**
     * Adds a button to the button bar. The button will not the supplied icon (if not null), and will use the supplied text for
     * the button's content. The button will be bound to the supplied {@link javafx.scene.control.ButtonBar.ButtonData}.
     *
     * @param pButtonText the text of the button to create.
     * @param pIcon the icon for the button.
     * @param pButtonData the button data from which the button will be created.
     * @return the current instance of the button bar, to allow call chaining.
     */
    public LabeledObservableButtonBar addButton(String pButtonText, Node pIcon, ButtonData pButtonData)
    {
        String text = pButtonText;
        if (StringUtils.isBlank(text))
        {
            text = pButtonData.toString();
        }

        Button button = new Button(text);
        if (pIcon != null)
        {
            button.setGraphic(pIcon);
        }

        setButtonData(button, pButtonData);

        // This fixes a weird issue where the buttons shrink on hover
        button.setMinHeight(26);
        button.setOnAction(e -> getButtonClickObservable().notifyObservers(pButtonData));
        button.defaultButtonProperty().bind(button.focusedProperty());

        getButtons().add(button);

        if (!pButtonData.isCancelButton())
        {
            button.disableProperty().bind(myDisableButtonsProperty);
        }

        return this;
    }

    /**
     * Gets the value of the {@link #myDisableButtonsProperty} field.
     *
     * @return the value stored in the {@link #myDisableButtonsProperty} field.
     */
    public BooleanProperty disableButtonsProperty()
    {
        return myDisableButtonsProperty;
    }
}
