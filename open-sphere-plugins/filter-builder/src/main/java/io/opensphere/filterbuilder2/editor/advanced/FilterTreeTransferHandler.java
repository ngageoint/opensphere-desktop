package io.opensphere.filterbuilder2.editor.advanced;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import io.opensphere.core.util.swing.tree.TransferableTreePath;
import io.opensphere.filterbuilder2.editor.model.CriterionModel;
import io.opensphere.filterbuilder2.editor.model.GroupModel;

/**
 * TransferHandler for the filter tree.
 */
public class FilterTreeTransferHandler extends TransferHandler
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FilterTreeTransferHandler.class);

    /** The from object. */
    private Object myFromObject;

    /** The to group. */
    private GroupModel myToGroup;

    @Override
    public boolean canImport(TransferSupport support)
    {
        myFromObject = null;
        myToGroup = null;
        try
        {
            if (support.isDataFlavorSupported(TransferableTreePath.TREE_NODE_FLAVOR))
            {
                TreePath liftPath = (TreePath)support.getTransferable().getTransferData(TransferableTreePath.TREE_NODE_FLAVOR);
                TreePath dropPath = ((JTree.DropLocation)support.getDropLocation()).getPath();
                if (dropPath != null && !liftPath.equals(dropPath))
                {
                    Object liftObject = FilterTreeUtilities.getUserObject(liftPath.getLastPathComponent());
                    if (liftObject instanceof CriterionModel
                            || liftObject instanceof GroupModel && ((GroupModel)liftObject).getParent() != null)
                    {
                        myFromObject = liftObject;
                    }

                    Object dropObject = FilterTreeUtilities.getUserObject(dropPath.getLastPathComponent());
                    if (dropObject instanceof GroupModel)
                    {
                        GroupModel group = (GroupModel)dropObject;
                        if (myFromObject instanceof CriterionModel)
                        {
                            CriterionModel fromCriterion = (CriterionModel)myFromObject;
                            if (fromCriterion.getParent() != group)
                            {
                                myToGroup = group;
                            }
                        }
                        else if (myFromObject instanceof GroupModel)
                        {
                            GroupModel fromGroup = (GroupModel)myFromObject;
                            if (fromGroup.getParent() != group)
                            {
                                myToGroup = group;
                            }
                        }
                    }
                }
            }
        }
        catch (IOException | UnsupportedFlavorException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
        boolean canImport = myFromObject != null && myToGroup != null;
        return canImport;
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE;
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        if (myFromObject instanceof CriterionModel)
        {
            CriterionModel fromCriterion = (CriterionModel)myFromObject;
            fromCriterion.getParent().removeCriterion(fromCriterion);
            myToGroup.addCriterion(fromCriterion);
        }
        else if (myFromObject instanceof GroupModel)
        {
            GroupModel fromGroup = (GroupModel)myFromObject;
            fromGroup.getParent().removeGroup(fromGroup);
            myToGroup.addGroup(fromGroup);
        }
        return true;
    }

    @Override
    protected Transferable createTransferable(JComponent c)
    {
        if (c instanceof JTree)
        {
            TreePath path = ((JTree)c).getSelectionPath();
            if (path != null)
            {
                return new TransferableTreePath(path);
            }
        }
        return null;
    }
}
