package io.opensphere.core.util.swing.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * TransferableTreePath: a tree path to be used by the {@link TransferHandler}
 * for this JTree.
 */
public class TransferableTreePath extends TreePath implements Transferable
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Tree node data flavor. */
    public static final DataFlavor TREE_NODE_FLAVOR;

    /** The supported data flavors. */
    private static final DataFlavor[] ourFlavors;

    static
    {
        TREE_NODE_FLAVOR = new DataFlavor(
                DataFlavor.javaJVMLocalObjectMimeType + ";class=" + DefaultMutableTreeNode.class.getName(), "Tree Node Flavor");
        ourFlavors = new DataFlavor[1];
        ourFlavors[0] = TREE_NODE_FLAVOR;
    }

    /**
     * Instantiates a new transferable tree path.
     *
     * @param path the path
     */
    public TransferableTreePath(TreePath path)
    {
        super(path.getParentPath(), path.getLastPathComponent());
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
        if (isDataFlavorSupported(flavor))
        {
            return this;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return ourFlavors.clone();
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        for (int i = 0; i < ourFlavors.length; i++)
        {
            if (flavor.equals(ourFlavors[i]))
            {
                return true;
            }
        }
        return false;
    }
}
