package io.opensphere.core.util.swing;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.tree.TreeNode;

/**
 * /** A recursive depth-first iterator over all the child nodes for a given
 * node.
 */
public class TreeNodeIterator implements Iterator<TreeNode>
{
    /** The Node. */
    private final TreeNode myNode;

    /** The Index. */
    private int myIndex = -1;

    /** The Child count. */
    private final int myChildCount;

    /** The Child iterator. */
    private TreeNodeIterator myCurrentChildIterator;

    /**
     * Instantiates a new tree iterator.
     *
     * @param node the node
     */
    public TreeNodeIterator(TreeNode node)
    {
        myNode = node;
        myChildCount = myNode.getChildCount();
    }

    @Override
    public boolean hasNext()
    {
        return myCurrentChildIterator != null && myCurrentChildIterator.hasNext()
                || myChildCount > 0 && myIndex + 1 < myChildCount;
    }

    @Override
    public TreeNode next()
    {
        if (myCurrentChildIterator != null)
        {
            if (myCurrentChildIterator.hasNext())
            {
                return myCurrentChildIterator.next();
            }
            myCurrentChildIterator = null;
        }
        if (myChildCount == 0 || myIndex + 1 == myChildCount)
        {
            throw new NoSuchElementException();
        }
        myIndex++;
        TreeNode child = myNode.getChildAt(myIndex);
        if (child.getChildCount() > 0)
        {
            myCurrentChildIterator = new TreeNodeIterator(child);
        }
        return child;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
