package io.opensphere.controlpanels.layers.activedata.tree;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import io.opensphere.core.util.swing.tree.DirectionalTransferHandler;
import io.opensphere.core.util.swing.tree.ListCheckBoxTree;
import io.opensphere.core.util.swing.tree.OrderTreeEventController;
import io.opensphere.core.util.swing.tree.TransferableTreePath;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * Controls drag and drop capabilites within the active data panel.
 */
public class DragAndDropTreeTransferHandler extends DirectionalTransferHandler
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(DragAndDropTreeTransferHandler.class);

    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * True if the node can be imported, false otherwise.
     */
    private boolean myCanImport;

    /**
     * The drag and drop controller.
     */
    private final transient DragAndDropController myController = new DragAndDropController();

    /**
     * The current drop node.
     */
    private transient TreeTableTreeNode myDropNode;

    /**
     * The current lift node.
     */
    private transient TreeTableTreeNode myLiftNode;

    /**
     * Constructs a new drag and drop tree handler.
     */
    public DragAndDropTreeTransferHandler()
    {
    }

    @Override
    public boolean canImport(TransferSupport support)
    {
        myCanImport = false;

        try
        {
            support.setShowDropLocation(true);
            if (support.isDataFlavorSupported(TransferableTreePath.TREE_NODE_FLAVOR) && myController.isAllowDrag())
            {
                TreePath liftPath = (TreePath)support.getTransferable().getTransferData(TransferableTreePath.TREE_NODE_FLAVOR);
                TreePath dropPath = ((JTree.DropLocation)support.getDropLocation()).getPath();

                if (dropPath != null && !liftPath.equals(dropPath))
                {
                    if (liftPath.getParentPath() == null || dropPath.getParentPath() == null)
                    {
                        return false;
                    }
                    if (liftPath.getLastPathComponent() instanceof TreeTableTreeNode
                            && dropPath.getLastPathComponent() instanceof TreeTableTreeNode)
                    {
                        myLiftNode = (TreeTableTreeNode)liftPath.getLastPathComponent();
                        myDropNode = (TreeTableTreeNode)dropPath.getLastPathComponent();

                        Object liftPayload = myLiftNode.getPayloadData();
                        Object dropPayload = myDropNode.getPayloadData();
                        if (liftPayload instanceof GroupByNodeUserObject && dropPayload instanceof GroupByNodeUserObject)
                        {
                            GroupByNodeUserObject liftUO = (GroupByNodeUserObject)liftPayload;
                            DataGroupInfo liftDataGroupInfo = liftUO.getDataGroupInfo();

                            GroupByNodeUserObject dropUO = (GroupByNodeUserObject)dropPayload;
                            DataGroupInfo dropDataGroupInfo = dropUO.getDataGroupInfo();
                            boolean isDescendant = false;
                            if (dropDataGroupInfo == null)
                            {
                                isDescendant = dropPath.isDescendant(liftPath);
                            }

                            if (dropDataGroupInfo == null && isDescendant
                                    || dropDataGroupInfo != null && dropDataGroupInfo.isDragAndDrop()
                                            && liftDataGroupInfo.getClass().equals(dropDataGroupInfo.getClass()))
                            {
                                myCanImport = true;
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

        return myCanImport;
    }

    @Override
    public boolean couldImport()
    {
        return myCanImport;
    }

    @Override
    public Transferable createTransferable(ListCheckBoxTree c)
    {
        TreePath path = c.getMouseOverPath();
        if (path != null)
        {
            return new TransferableTreePath(path);
        }
        return null;
    }

    @Override
    public OrderTreeEventController getController()
    {
        return myController;
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE;
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        boolean imported = false;
        if (canImport(support) && myLiftNode != null && myDropNode != null)
        {
            Object liftObject = myLiftNode.getPayloadData();
            Object dropObject = myDropNode.getPayloadData();

            if (liftObject instanceof GroupByNodeUserObject && dropObject instanceof GroupByNodeUserObject)
            {
                DataTypeInfo liftDataType = ((GroupByNodeUserObject)liftObject).getDataTypeInfo();
                DataGroupInfo liftDataGroup = ((GroupByNodeUserObject)liftObject).getDataGroupInfo();
                DataGroupInfo dropDataGroup = ((GroupByNodeUserObject)dropObject).getDataGroupInfo();

                if (dropDataGroup == null)
                {
                    dropDataGroup = getRoot(liftDataGroup);
                }

                if (liftDataType != null)
                {
                    liftDataGroup.removeMember(liftDataType, false, this);
                    dropDataGroup.addMember(liftDataType, this);
                }
                else
                {
                    DataGroupInfo parent = liftDataGroup.getParent();

                    parent.removeChildKeepActive(liftDataGroup, this);

                    dropDataGroup.addChild(liftDataGroup, this);
                }

                imported = true;
            }
        }

        return imported;
    }

    @Override
    public boolean isUp()
    {
        return false;
    }

    /**
     * Gets the root group to drop to.
     *
     * @param dataGroupInfo The data group contained in the root group.
     * @return The root group.
     */
    private DataGroupInfo getRoot(DataGroupInfo dataGroupInfo)
    {
        DataGroupInfo rootGroup = dataGroupInfo;
        while (rootGroup.getParent() != null)
        {
            rootGroup = rootGroup.getParent();
        }

        return rootGroup;
    }
}
