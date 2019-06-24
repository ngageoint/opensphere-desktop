package io.opensphere.mantle.icon.chooser.model;

import io.opensphere.mantle.icon.IconRecord;
import javafx.scene.control.TreeItem;

/**
 * Contains the users preferences within each session. When opening icon manager
 * it will use these values.
 */
public class IconManagerPrefs
{
    /** The value used for the icon width. */
    private int myIconWidth = 0;

    /** The icon last selected in the icon manager. */
    private IconRecord myLastSelectedIcon = null;

    /** The tree which will be selected on start up. */
    private TreeItem<String> myTreeSelection = new TreeItem<>("temp");

    /**
     * Gets the icon width for displaying icons.
     *
     * @return the icon width.
     */
    public int getIconWidth()
    {
        return myIconWidth;
    }

    /**
     * Sets the icon width for displaying icons.
     *
     * @param width the icon width
     */
    public void setIconWidth(int width)
    {
        myIconWidth = width;
    }

    /**
     * Gets the icon that was last selected in the icon dialog.
     *
     * @return the last selected icon
     */
    public IconRecord getLastSelectedIcon()
    {
        return myLastSelectedIcon;
    }

    /**
     * Sets the icon that was last selected in the icon dialog.
     *
     * @param iconRecord the last selected icon
     */
    public void setLastSelectedIcon(IconRecord iconRecord)
    {
        myLastSelectedIcon = iconRecord;
    }

    /**
     * Gets which tree is being selected.
     *
     * @return the currently selected tree
     */
    public TreeItem<String> getTreeSelection()
    {
        return myTreeSelection;
    }

    /**
     * Sets which tree is being selected.
     *
     * @param selection the tree being selected
     */
    public void setTreeSelection(TreeItem<String> selection)
    {
        myTreeSelection = selection;
    }
}
