package io.opensphere.mantle.iconproject.view;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.IconButton;
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
    public ButtonBuilder(String label)
    {
        super();
        myLabel = label;
        Button myButton = new Button();       
    }

    // IconButton exportButton = new IconButton("Export", new
    // GenericFontIcon(AwesomeIconSolid.DOWNLOAD, Color.YELLOW));

    public void setIcon(AwesomeIconSolid Icon ,Color color) {
        
        getStyleClass().remove("button");

    }

    public Button createButton()
    {

        Button myButton = new Button();

        myButton.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        myButton.setFont(new Font(14.0));
        myButton.setText(myLabel);

        return myButton;
    }

   
    
}