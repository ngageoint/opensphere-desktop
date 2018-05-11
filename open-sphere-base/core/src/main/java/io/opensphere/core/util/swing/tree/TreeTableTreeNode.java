package io.opensphere.core.util.swing.tree;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/** A tree node for the tree table. */
@SuppressWarnings("PMD.GodClass")
public class TreeTableTreeNode implements MutableTreeNode
{
    /** The children of this tree node. */
    private List<TreeTableTreeNode> myChildren;

    /** The depth in the tree at which this node resides. */
    private final int myDepth;

    /** When true this node is not displayed. */
    private boolean myFiltered;

    /** When true the mouse is over this node in the table. */
    private boolean myMouseOver;

    /** The parent of this node. */
    private final TreeTableTreeNode myParent;

    /** The children which are to be rendered. */
    private List<TreeTableTreeNode> myRenderableChildren;

    /** The payload which this node encapsulates. */
    private ButtonModelPayload myPayload;

    /**
     * Constructor.
     *
     * @param parent The parent of this node.
     * @param payload The payload containing the information used by the node.
     */
    public TreeTableTreeNode(TreeTableTreeNode parent, ButtonModelPayload payload)
    {
        myParent = parent;
        myPayload = payload;
        myDepth = myParent == null ? 0 : myParent.getDepth() + 1;
    }

    /**
     * Add a child to this node.
     *
     * @param child The child to add.
     */
    public void add(TreeTableTreeNode child)
    {
        if (myRenderableChildren != null)
        {
            myRenderableChildren.add(child);
        }
        if (myChildren == null)
        {
            myChildren = New.list();
        }
        myChildren.add(child);
    }

    @Override
    public Enumeration<? extends TreeNode> children()
    {
        return new Enumeration<TreeTableTreeNode>()
        {
            private final Iterator<TreeTableTreeNode> myNodeIterator = getChildren().iterator();

            @Override
            public boolean hasMoreElements()
            {
                return myNodeIterator.hasNext();
            }

            @Override
            public TreeTableTreeNode nextElement()
            {
                return myNodeIterator.next();
            }
        };
    }

    /**
     * Finds all the leaves that descend from this node.
     *
     * @param leaves Return collection of leaves in the subtree rooted at this
     *            node.
     */
    public void getAllLeaves(Collection<? super TreeTableTreeNode> leaves)
    {
        if (isLeaf())
        {
            leaves.add(this);
        }
        else
        {
            for (TreeTableTreeNode child : getChildren())
            {
                child.getAllLeaves(leaves);
            }
        }
    }

    @Override
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getAllowsChildren()
    {
        return true;
    }

    /**
     * Get the renderable child at the requested position.
     *
     * @param index The index of the child to retrieve
     * @return The child at the requested position.
     */
    public Object getChild(int index)
    {
        if (myChildren == null)
        {
            return null;
        }
        return getChildren().get(index);
    }

    @Override
    public TreeNode getChildAt(int childIndex)
    {
        return (TreeNode)getChild(childIndex);
    }

    /**
     * Get the number of renderable children.
     *
     * @return The number of renderable children.
     */
    @Override
    public int getChildCount()
    {
        if (myChildren == null)
        {
            return 0;
        }
        return getChildren().size();
    }

    /**
     * Get the children.
     *
     * @return the children
     */
    public List<TreeTableTreeNode> getChildren()
    {
        return myRenderableChildren == null ? myChildren : myRenderableChildren;
    }

    /**
     * Get the depth.
     *
     * @return the depth
     */
    public int getDepth()
    {
        return myDepth;
    }

    /**
     * Finds and returns the first leaf that is a descendant of this node --
     * either this node or its first child's first leaf. Returns this node if it
     * is a leaf.
     *
     * @see #isLeaf
     * @return the first leaf in the subtree rooted at this node
     */
    public TreeNode getFirstLeaf()
    {
        TreeNode node = this;

        while (!node.isLeaf())
        {
            node = node.getChildAt(0);
        }

        return node;
    }

    @Override
    public int getIndex(TreeNode node)
    {
        return getIndexOfRenderableChild(node);
    }

    /**
     * Get the index of this child if the child is renderable.
     *
     * @param child the child for which the index is desired.
     * @return the index of the child if it is renderable or "-1" otherwise.
     */
    public int getIndexOfRenderableChild(Object child)
    {
        int index = 0;
        for (TreeTableTreeNode ch : getChildren())
        {
            if (Utilities.sameInstance(ch, child))
            {
                return index;
            }
            ++index;
        }
        return -1;
    }

    /**
     * Get the parent of this node.
     *
     * @return The parent.
     */
    @Override
    public TreeTableTreeNode getParent()
    {
        return myParent;
    }

    /**
     * Returns the path from the root, to get to this node. The last element in
     * the path is this node.
     *
     * @return an array of TreeNode objects giving the path, where the first
     *         element in the path is the root and the last element is this
     *         node.
     */
    public TreeNode[] getPath()
    {
        return getPathToRoot(this, 0);
    }

    /**
     * Get the payload which this node encapsulates.
     *
     * @return The payload of this node.
     */
    public ButtonModelPayload getPayload()
    {
        return myPayload;
    }

    /**
     * Gets the payload data.
     *
     * @return the payload data
     */
    public Object getPayloadData()
    {
        return myPayload == null ? null : myPayload.getPayloadData();
    }

    /**
     * Get the index of this child.
     *
     * @param child the child for which the index is desired.
     * @return the index of the child.
     */
    public int getRawIndexOfChild(Object child)
    {
        if (myChildren == null)
        {
            return -1;
        }
        int index = 0;
        for (TreeTableTreeNode ch : myChildren)
        {
            if (Utilities.sameInstance(child, ch))
            {
                return index;
            }
            ++index;
        }
        return -1;
    }

    @Override
    public void insert(MutableTreeNode child, int index)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Tell whether this node is filtered.
     *
     * @return the filtered status of this node.
     */
    public boolean isFiltered()
    {
        return myFiltered;
    }

    /**
     * Tell whether this node is a leaf.
     *
     * @return true when this node is a leaf.
     */
    @Override
    public boolean isLeaf()
    {
        return myChildren == null || getChildren().isEmpty();
    }

    /**
     * Get the mouseOver.
     *
     * @return the mouseOver
     */
    public boolean isMouseOver()
    {
        return myMouseOver;
    }

    @Override
    public void remove(int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(MutableTreeNode node)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeFromParent()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the filtered.
     *
     * @param filtered the filtered to set
     * @return The index in my parent's list of nodes and this node which may be
     *         used to generated a TreeModelEvent.
     */
    public Pair<int[], TreeTableTreeNode[]> setFiltered(boolean filtered)
    {
        myFiltered = filtered;
        if (myParent != null)
        {
            return myParent.filterChild(this);
        }
        return null;
    }

    /**
     * Set the mouseOver.
     *
     * @param mouseOver the mouseOver to set
     */
    public void setMouseOver(boolean mouseOver)
    {
        myMouseOver = mouseOver;
    }

    @Override
    public void setParent(MutableTreeNode newParent)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUserObject(Object object)
    {
        if (object instanceof ButtonModelPayload)
        {
            myPayload = (ButtonModelPayload)object;
        }
    }

    @Override
    public String toString()
    {
        return myPayload.getButton().getText();
    }

    /**
     * Un-filter all of my filtered children.
     *
     * @return The indices of the changed nodes and the changed nodes which may
     *         be used to generated a TreeModelEvent.
     */
    public Pair<int[], TreeTableTreeNode[]> unfilterChildren()
    {
        if (myRenderableChildren != null)
        {
            int changedNodes = myChildren.size() - myRenderableChildren.size();
            int[] indices = new int[changedNodes];
            TreeTableTreeNode[] nodes = new TreeTableTreeNode[changedNodes];
            int changedIndex = 0;
            int childIndex = 0;
            for (TreeTableTreeNode child : myChildren)
            {
                if (!myRenderableChildren.contains(child))
                {
                    indices[changedIndex] = childIndex;
                    nodes[changedIndex] = child;
                    ++changedIndex;
                }
                ++childIndex;
            }
            myRenderableChildren = null;

            return new Pair<int[], TreeTableTreeNode[]>(indices, nodes);
        }
        return null;
    }

    /**
     * Builds the parents of node up to and including the root node, where the
     * original node is the last element in the returned array. The length of
     * the returned array gives the node's depth in the tree.
     *
     * @param aNode the TreeNode to get the path for
     * @param pDepth an int giving the number of steps already taken towards the
     *            root (on recursive calls), used to size the returned array
     * @return an array of TreeNodes giving the path from the root to the
     *         specified node
     */
    protected TreeNode[] getPathToRoot(TreeNode aNode, int pDepth)
    {
        TreeNode[] retNodes;

        int depth = pDepth;

        /* Check for null, in case someone passed in a null node, or they passed
         * in an element that isn't rooted at root. */
        if (aNode == null)
        {
            if (depth == 0)
            {
                return null;
            }
            else
            {
                retNodes = new TreeNode[depth];
            }
        }
        else
        {
            depth++;
            retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }

    /**
     * Filter a child.
     *
     * @param node The child whose filter state is being changed.
     * @return The index of the node and the node which may be used to generated
     *         a TreeModelEvent.
     */
    private Pair<int[], TreeTableTreeNode[]> filterChild(TreeTableTreeNode node)
    {
        if (myChildren == null)
        {
            return null;
        }
        Pair<int[], TreeTableTreeNode[]> changes = null;
        if (myRenderableChildren == null)
        {
            myRenderableChildren = New.list(myChildren);
        }

        changes = new Pair<int[], TreeTableTreeNode[]>(new int[] { getIndexOfRenderableChild(node) },
                new TreeTableTreeNode[] { node });
        myRenderableChildren.remove(node);
        return changes;
    }
}
