package io.opensphere.mantle.iconproject.impl;

import io.opensphere.mantle.icon.IconRecord;

/**
 * The Class IconPopupMenuImpl.
 */
public class IconPopupMenuImpl
{
    /** The selected icon. */
    IconRecord selectedIcon;
    /**
     * The constructor for IconPopupMenuImpl.
     *
     * @param choice the chosen Icon
     */
    public IconPopupMenuImpl(IconRecord choice)
    {
        selectedIcon = choice;
    }

    /**
     * Adds the selected icon to favorites.
     */
    public void addToFav()
    {
        System.out.println("Adding: " + selectedIcon + " to favorites");
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
