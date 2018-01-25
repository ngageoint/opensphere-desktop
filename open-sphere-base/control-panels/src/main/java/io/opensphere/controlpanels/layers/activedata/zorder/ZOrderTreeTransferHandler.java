package io.opensphere.controlpanels.layers.activedata.zorder;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.tree.DirectionalTransferHandler;
import io.opensphere.core.util.swing.tree.ListCheckBoxTree;
import io.opensphere.core.util.swing.tree.OrderTreeEventController;
import io.opensphere.core.util.swing.tree.TransferableTreePath;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * TransferHandler for the Z-order tree.
 */
@SuppressWarnings("PMD.GodClass")
public class ZOrderTreeTransferHandler extends DirectionalTransferHandler
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(ZOrderTreeTransferHandler.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The current value of canImport(). */
    private boolean myCanImport;

    /** The current drop node. */
    private transient TreeTableTreeNode myDropNode;

    /** The drag direction (true for up, false for down). */
    private boolean myIsUp;

    /** The current lift node. */
    private transient TreeTableTreeNode myLiftNode;

    /** The toolbox. */
    private final transient Toolbox myToolbox;

    /** The Active layer controller. */
    private final transient OrderTreeEventController myZOrderDNDController;

    /**
     * Instantiates a new tree transfer handler.
     *
     * @param toolbox the toolbox
     * @param controller the active data layer controller
     */
    public ZOrderTreeTransferHandler(Toolbox toolbox, OrderTreeEventController controller)
    {
        super();
        myToolbox = toolbox;
        myZOrderDNDController = controller;
    }

    @Override
    public boolean canImport(TransferSupport support)
    {
        myCanImport = false;
        support.setShowDropLocation(true);
        if (support.isDataFlavorSupported(TransferableTreePath.TREE_NODE_FLAVOR) && myZOrderDNDController.isAllowDrag())
        {
            try
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

                        // Determine the direction of the drag
                        int liftIndex = myLiftNode.getParent().getIndex(myLiftNode);
                        int dropIndex = myLiftNode.getParent().getIndex(myDropNode);
                        myIsUp = liftIndex > dropIndex;
                    }

                    // Determine if this import is supported
                    // If the parent path is the same for both, then the types
                    // are the same.
                    myCanImport = liftPath.getParentPath().equals(dropPath.getParentPath())
                            && !Utilities.sameInstance(liftPath, dropPath);

                    if (myCanImport)
                    {
                        Object liftPayload = myLiftNode.getPayloadData();
                        Object dropPayload = myDropNode.getPayloadData();
                        if (liftPayload instanceof GroupByNodeUserObject && dropPayload instanceof GroupByNodeUserObject)
                        {
                            checkCanImportPayload(liftPayload, dropPayload);
                        }
                    }
                }
            }
            catch (IOException e)
            {
                LOGGER.error(e.getMessage());
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(StringUtilities.stackTraceToString(e));
                }
            }
            catch (UnsupportedFlavorException e)
            {
                LOGGER.error(e.getMessage());
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(StringUtilities.stackTraceToString(e));
                }
            }
        }
        if (myCanImport)
        {
            myZOrderDNDController.dragInProgress();
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
        return myZOrderDNDController;
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE;
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        if (canImport(support) && myLiftNode != null && myDropNode != null)
        {
            Object liftObject = myLiftNode.getPayloadData();
            Object dropObject = myDropNode.getPayloadData();

            if (liftObject instanceof GroupByNodeUserObject && dropObject instanceof GroupByNodeUserObject)
            {
                final DataTypeInfo liftDataType = ((GroupByNodeUserObject)liftObject).getDataTypeInfo();
                final DataTypeInfo dropDataType = ((GroupByNodeUserObject)dropObject).getDataTypeInfo();

                if (liftDataType != null)
                {
                    zorderDataTypes(liftDataType, dropDataType);
                }
                else
                {
                    DataGroupInfo liftDataGroup = ((GroupByNodeUserObject)liftObject).getDataGroupInfo();
                    DataGroupInfo dropDataGroup = ((GroupByNodeUserObject)dropObject).getDataGroupInfo();
                    zorderDataGroups(liftDataGroup, dropDataGroup);
                }

                myZOrderDNDController.dragEnd();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUp()
    {
        return myIsUp;
    }

    /**
     * Checks the lift and drop pay loads and determines if they can be dropped.
     *
     * @param liftPayload the lift payload
     * @param dropPayload the drop payload
     */
    private void checkCanImportPayload(Object liftPayload, Object dropPayload)
    {
        GroupByNodeUserObject liftGroup = (GroupByNodeUserObject)liftPayload;
        GroupByNodeUserObject dropGroup = (GroupByNodeUserObject)dropPayload;
        DataGroupInfo liftGroupInfo = liftGroup.getDataGroupInfo();
        DataGroupInfo dropGroupInfo = dropGroup.getDataGroupInfo();
        DataTypeInfo liftTypeInfo = liftGroup.getDataTypeInfo();
        DataTypeInfo dropTypeInfo = dropGroup.getDataTypeInfo();

        if (liftTypeInfo != null && dropTypeInfo == null)
        {
            myCanImport = false;
        }
        else if (liftGroupInfo != null && dropGroupInfo != null && !liftGroupInfo.isFlattenable()
                && !dropGroupInfo.isFlattenable())
        {
            myCanImport = false;
        }
        else if (liftGroup.isCategoryNode())
        {
            myCanImport = false;
        }
        else if (liftTypeInfo != null && dropTypeInfo != null)
        {
            myCanImport = false;
            if (liftTypeInfo.getOrderKey() != null && dropTypeInfo.getOrderKey() != null
                    && liftTypeInfo.getOrderKey().getCategory().equals(dropTypeInfo.getOrderKey().getCategory()))
            {
                myCanImport = true;
            }
        }
    }

    /**
     * zorders the data types within the data groups.
     *
     * @param liftDataGroup The data group to lift.
     * @param dropDataGroup The data group to drop.f
     */
    private void zorderDataGroups(DataGroupInfo liftDataGroup, DataGroupInfo dropDataGroup)
    {
        Collection<DataTypeInfo> liftMembers = liftDataGroup.getMembers(false);
        Collection<DataTypeInfo> dropMembers = dropDataGroup.getMembers(false);

        for (DataTypeInfo liftMember : liftMembers)
        {
            if (liftMember.getOrderKey() != null)
            {
                for (DataTypeInfo dropMember : dropMembers)
                {
                    if (dropMember.getOrderKey() != null
                            && dropMember.getOrderKey().getCategory().equals(liftMember.getOrderKey().getCategory()))
                    {
                        zorderDataTypes(liftMember, dropMember);
                    }
                }
            }
        }
    }

    /**
     * Z orders the specified data types.
     *
     * @param liftDataType The lift data type.
     * @param dropDataType The drop data type.
     */
    private void zorderDataTypes(DataTypeInfo liftDataType, DataTypeInfo dropDataType)
    {
        // Update the z-order in ZOrderManager
        OrderManager manager;
        manager = myToolbox.getOrderManagerRegistry().getOrderManager(liftDataType.getOrderKey());
        if (manager != null)
        {
            if (myIsUp)
            {
                manager.moveAbove(liftDataType.getOrderKey(), dropDataType.getOrderKey());
            }
            else
            {
                manager.moveBelow(liftDataType.getOrderKey(), dropDataType.getOrderKey());
            }
        }
    }
}
