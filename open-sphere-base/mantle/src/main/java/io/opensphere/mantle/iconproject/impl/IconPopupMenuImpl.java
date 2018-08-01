package io.opensphere.mantle.iconproject.impl;

import java.net.URL;

import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.model.PanelModel;

/**
 * The Class IconPopupMenuImpl.
 */

public class IconPopupMenuImpl
{
    /** The selected icon. */
    IconRecord selectedIcon;
    private final PanelModel myPanelModel;
    /**
     * The constructor for IconPopupMenuImpl.
     *
     * @param choice the chosen Icon
     */

    public IconPopupMenuImpl(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        selectedIcon = myPanelModel.getIconRecord();
    }

    /**
     * Adds the selected icon to favorites.
     */
    public void addToFav(PanelModel myPanel)
    {
        System.out.println("Adding: " + selectedIcon + " to favorites");
        //        selectedIcon
        URL imageURL = selectedIcon.getImageURL();
        IconProvider provider = new DefaultIconProvider(imageURL, IconRecord.FAVORITES_COLLECTION, null, "User");
        myPanel.getMyIconRegistry().addIcon(provider, this);
    }

    /**
     * Rotates the selected icon.
     */
    public void rotate()
    {
        System.out.println("Rotating: " + selectedIcon);
    }

    /**
     * Deletes the selected icon.
     */
    public void delete()
    {
        System.out.println("Deleting: " + selectedIcon);
    }
}
