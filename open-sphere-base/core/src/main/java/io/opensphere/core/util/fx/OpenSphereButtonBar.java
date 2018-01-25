package io.opensphere.core.util.fx;

import java.util.Map;

import javafx.scene.control.Button;

import io.opensphere.core.util.collections.New;

/** OpenSphere button bar. */
public class OpenSphereButtonBar extends ObservableButtonBar
{
    /** Map of button data to text. */
    private static final Map<ButtonData, String> BUTTON_TEXT_MAP = New.map();

    static
    {
        BUTTON_TEXT_MAP.put(ButtonData.OK_DONE, "OK");
        BUTTON_TEXT_MAP.put(ButtonData.CANCEL_CLOSE, "Cancel");
    }

    /**
     * Constructor.
     */
    public OpenSphereButtonBar()
    {
        this(ButtonData.OK_DONE, ButtonData.CANCEL_CLOSE);
    }

    /**
     * Factory method for instances that show only the "OK" button.
     * @return a button bar
     */
    public static OpenSphereButtonBar okay()
    {
        return new OpenSphereButtonBar(ButtonData.OK_DONE);
    }

    /**
     * Factory method for instances that show the "OK" and "Cancel" buttons.
     * @return a button bar
     */
    public static OpenSphereButtonBar okayCancel()
    {
        return new OpenSphereButtonBar();
    }

    /**
     * Constructor.
     *
     * @param buttonData the button data
     */
    public OpenSphereButtonBar(ButtonData... buttonData)
    {
        super(BUTTON_ORDER_WINDOWS);
        for (ButtonData data : buttonData)
        {
            getButtons().add(newButton(data));
        }
    }

    /**
     * Creates a new button for the button data.
     *
     * @param data the button data
     * @return the button
     */
    private Button newButton(ButtonData data)
    {
        String text = BUTTON_TEXT_MAP.getOrDefault(data, data.toString());
        Button button = new Button(text);
        setButtonData(button, data);
        // This fixes a weird issue where the buttons shrink on hover
        button.setMinHeight(26);
        button.setOnAction(e -> getButtonClickObservable().notifyObservers(data));
        button.defaultButtonProperty().bind(button.focusedProperty());
        return button;
    }
}
