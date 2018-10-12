package io.opensphere.core.util.swing;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The Class TreeNodeData.
 */
public class TreeNodeData
{
    /** The Node. */
    private final DefaultMutableTreeNode myNode;

    /** The Depth. */
    private final int myDepth;

    /** The Is expanded. */
    private boolean myIsExpanded;

    /** The Icon path. */
    private final String myIconPath;

    /** The Show root node. */
    private boolean myShowRootNode;

    //    /** The Use label and button. */
    //    private boolean myUseLabelAndButton;

    /**
     * Instantiates a new tree node data.
     *
     * @param pNode the node
     * @param pDepth the depth
     * @param pExpanded the expanded
     * @param pIconPath the icon path
     * @param pShowRootNode the show root node
     */
    public TreeNodeData(DefaultMutableTreeNode pNode, int pDepth, boolean pExpanded, String pIconPath, boolean pShowRootNode)
    {
        myNode = pNode;
        myDepth = pDepth;
        myIsExpanded = pExpanded;
        myIconPath = pIconPath;
        myShowRootNode = pShowRootNode;
    }

    /**
     * Gets the depth.
     *
     * @return the depth
     */
    public int getDepth()
    {
        return myDepth;
    }

    /**
     * Gets the icon path.
     *
     * @return the icon path
     */
    public String getIconPath()
    {
        return myIconPath;
    }

    /**
     * Gets the node.
     *
     * @return the node
     */
    public DefaultMutableTreeNode getNode()
    {
        return myNode;
    }

    /**
     * Checks if is checks if is expanded.
     *
     * @return true, if is checks if is expanded
     */
    public boolean isExpaned()
    {
        return myIsExpanded;
    }

    /**
     * Checks if is show root node.
     *
     * @return true, if is show root node
     */
    public boolean isShowRootNode()
    {
        return myShowRootNode;
    }

    /**
     * Sets the expanded.
     *
     * @param pExpanded the new expanded
     */
    public void setExpanded(boolean pExpanded)
    {
        myIsExpanded = pExpanded;
    }

    /**
     * Sets the show root node.
     *
     * @param showRootNode the new show root node
     */
    public void setShowRootNode(boolean showRootNode)
    {
        myShowRootNode = showRootNode;
    }
}
