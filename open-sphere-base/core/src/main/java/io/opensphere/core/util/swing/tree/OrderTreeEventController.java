package io.opensphere.core.util.swing.tree;

import javax.swing.tree.TreePath;

/**
 * The Interface OrderTreeEventController.
 */
public interface OrderTreeEventController
{
    /** Dragging has ended. */
    void dragEnd();

    /** A drag is in progress. */
    void dragInProgress();

    /**
     * Gets whether to allow drag.
     *
     * @return whether to allow drag
     */
    boolean isAllowDrag();

    /**
     * Selection changed.
     *
     * @param path the path which is now selected.
     * @param isSelected true when the path has been selected and false when it
     *            has been deselected.
     */
    void selectionChanged(TreePath path, boolean isSelected);

    /**
     * Sets whether to allow drag.
     *
     * @param allowDrag whether to allow drag.
     */
    void setAllowDrag(boolean allowDrag);
}
