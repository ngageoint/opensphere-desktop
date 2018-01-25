package io.opensphere.kml.tree.model;

import javax.swing.tree.TreePath;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Helper class to manage selection state of TreePaths.
 */
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class SelectedTreePath extends TreePath
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Whether the path is selected. */
    private boolean myIsSelected;

    /**
     * Constructor.
     *
     * @param path an array of Objects representing the path to a node
     */
    public SelectedTreePath(Object[] path)
    {
        super(path);
    }

    /**
     * Getter for isSelected.
     *
     * @return the isSelected
     */
    public boolean isSelected()
    {
        return myIsSelected;
    }

    /**
     * Setter for isSelected.
     *
     * @param isSelected the isSelected
     */
    public void setSelected(boolean isSelected)
    {
        myIsSelected = isSelected;
    }

    @Override
    public String toString()
    {
        return StringUtilities.concat(String.valueOf(myIsSelected), " ", super.toString());
    }
}
