package io.opensphere.icon.manager;

import io.opensphere.core.util.AwesomeIconSolid;
import javafx.application.Application;

public abstract class ButtonBuilder extends Application 
{
    /** The Label. */
    private final String myLabel;

    /** The group an Item belongs to. */
    private String myGroup;

    /** Indicates an icon was chosen for the button. */
    private boolean myIconChosen;

    private AwesomeIconSolid myIcon;

    /**
     * Creates a new menu action with the supplied parameters.
     *
     * @param label the label to display on the menu item.
     * @param group the group to which the menu item belongs.
     * @param icon the (optional) icon to display with the menu item.
     * @param handler the consumer called when the menu item is activated.
     */
    public ButtonBuilder(String label, String group,boolean iconChoice,AwesomeIconSolid icon)
    {
        super();
        myLabel = label;
        myGroup = group;
        myIconChosen = iconChoice;
        myIcon = icon;
        
    }
    
    
    
    
    
    
    
    
}
