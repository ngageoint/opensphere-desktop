package io.opensphere.mantle.iconproject.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.opensphere.core.util.AwesomeIconSolid;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class ButtonBuilder extends Button

{
    /** The Label. */
    private final String myLabel;

    /** The group an Item belongs to. */
    private String myGroup;

    /** Indicates an icon was chosen for the button. */
    private boolean myIconChosen;

    /** AwesomeFont Icons */
    private AwesomeIconSolid myIcon;

    private boolean myToggle;

    /**
     * Creates a new button with the supplied parameters.
     *
     * @param label the text to display inside the button.
     * @param group the group to which the menu item belongs.
     * @param icon the (optional) icon to display with the menu item.
     * @param handler the consumer called when the menu item is activated.
     */
    public ButtonBuilder(String label, boolean iconChoice, AwesomeIconSolid icon)
    {
        super();
        myLabel = label;
        myIconChosen = iconChoice;
        myIcon = icon;
    }

    public Button createButton() throws FileNotFoundException
    {

        Button myButton = new Button();

        if (myIconChosen)
        {
            myButton.getStyleClass().remove("button");
            Image myGridIcon = new Image(new FileInputStream("src/main/resources/images/file.png"));
            myButton.setGraphic(new ImageView(myGridIcon));
        }

        else
        {
            myButton.setText(myLabel);
        }

        myButton.setOnAction(null);
        myButton.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        myButton.setFont(new Font(14.0));

        return myButton;
    }

}