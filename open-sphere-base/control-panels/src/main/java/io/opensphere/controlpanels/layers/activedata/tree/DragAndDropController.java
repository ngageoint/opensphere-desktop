package io.opensphere.controlpanels.layers.activedata.tree;

import javax.swing.tree.TreePath;

import io.opensphere.core.util.swing.tree.OrderTreeEventController;

/**
 * The generic drag and drop controller.
 */
public class DragAndDropController implements OrderTreeEventController
{
    /**
     * Whether to allow drag.
     */
    private boolean myAllowDrag;

    @Override
    public void dragEnd()
    {
    }

    @Override
    public void dragInProgress()
    {
    }

    @Override
    public boolean isAllowDrag()
    {
        return myAllowDrag;
    }

    @Override
    public void selectionChanged(TreePath path, boolean isSelected)
    {
    }

    @Override
    public void setAllowDrag(boolean allowDrag)
    {
        myAllowDrag = allowDrag;
    }
}
