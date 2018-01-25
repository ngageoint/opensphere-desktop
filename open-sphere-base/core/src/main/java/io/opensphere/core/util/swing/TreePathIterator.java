package io.opensphere.core.util.swing;

import java.util.Iterator;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * A recursive depth-first iterator over all the child paths for a given node.
 */
public class TreePathIterator implements Iterator<TreePath>
{
    /** The Tree iterator. */
    private final TreeNodeIterator myTreeIterator;

    /**
     * Instantiates a new tree path iterator.
     *
     * @param treeNode the tree node
     */
    public TreePathIterator(TreeNode treeNode)
    {
        myTreeIterator = new TreeNodeIterator(treeNode);
    }

    @Override
    public boolean hasNext()
    {
        return myTreeIterator.hasNext();
    }

    @Override
    public TreePath next()
    {
        TreeNode tn = myTreeIterator.next();
        return JTreeUtilities.buildPathToNode(tn);
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
