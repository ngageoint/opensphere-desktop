package io.opensphere.mantle.iconproject.impl;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/** Creates customized buttons for the icon manager. */
public class ButtonBuilder extends Button
{
    /** The button label. */
    private final String myLabel;

    /**
     * Creates a new button with the supplied parameters.
     *
     * @param label the text to display inside the button.
     * @param useIcon the (optional) icon to display with the menu item.
     */
    public ButtonBuilder(String label, boolean useIcon)
    {
        super();
        myLabel = label;
        setText(myLabel);
        setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);

        if (useIcon)
        {
            getStyleClass().remove("button");
            setGraphic(new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("alert.png"))));
        }
    }

    /**
     * Locks the icon buttons position.
     *
     * @param inputButton the button being locked.
     */
    public void lockButton(Button inputButton)
    {
        AnchorPane.setLeftAnchor(inputButton, 0.);
        AnchorPane.setRightAnchor(inputButton, 0.);
    }
}
