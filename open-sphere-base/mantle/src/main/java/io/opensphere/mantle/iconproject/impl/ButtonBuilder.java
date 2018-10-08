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
     * @param iconchoice the (optional) icon to display with the menu item.
     */
    public ButtonBuilder(String label, boolean iconchoice)
    {
        super();
        myLabel = label;
        setText(myLabel);
        setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);

        if (iconchoice)
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
        Button theButton = inputButton;
        AnchorPane.setLeftAnchor(theButton, 0.);
        AnchorPane.setRightAnchor(theButton, 0.);
    }
}
