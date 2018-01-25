package io.opensphere.core.util.swing.tree;

import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import io.opensphere.core.util.collections.New;

/**
 * Utilities for managing the state of tree nodes which are backed by button
 * models.
 */
@SuppressWarnings("unchecked")
public final class ButtonModelTreeUtilities
{
    /**
     * Set any ancestors to selected if all of the ancestor's descendants are
     * selected.
     *
     * @param node The node to check.
     */
    public static void checkSelectAncestor(DefaultMutableTreeNode node)
    {
        if (node == null || node.getUserObject() == null || isSelected(node))
        {
            return;
        }

        Enumeration<DefaultMutableTreeNode> children = node.children();
        while (children.hasMoreElements())
        {
            DefaultMutableTreeNode child = children.nextElement();
            if (!isSelected(child))
            {
                return;
            }
        }

        Object userObject = node.getUserObject();
        if (userObject instanceof ButtonModelPayload)
        {
            ((ButtonModelPayload)userObject).getButton().setSelected(true);
            checkSelectAncestor((DefaultMutableTreeNode)node.getParent());
        }
    }

    /**
     * Set any ancestors to unselected if any of the ancestor's descendants are
     * unselected.
     *
     * @param node The node to check.
     */
    public static void checkUnselectAncestor(DefaultMutableTreeNode node)
    {
        if (node == null || node.getUserObject() == null || !isSelected(node))
        {
            return;
        }

        if (node.getUserObject() instanceof ButtonModelPayload)
        {
            ((ButtonModelPayload)node.getUserObject()).getButton().setSelected(false);
        }
        checkUnselectAncestor((DefaultMutableTreeNode)node.getParent());
    }

    /**
     * Expand the tree so that the nodes whose names match the paths will be
     * visible.
     *
     * @param tree The tree to expand.
     * @param node The root node to search for path matches.
     * @param paths The paths to make visible.
     */
    public static void expandPaths(JTree tree, DefaultMutableTreeNode node, List<TreePath> paths)
    {
        for (TreePath path : paths)
        {
            DefaultMutableTreeNode pathNode = getNodeForPathByName(node, path);
            if (pathNode != null)
            {
                tree.makeVisible(new TreePath(pathNode.getPath()));
            }
        }
    }

    /**
     * Find the node whose path names match the path.
     *
     * @param node The root node to search for path matches.
     * @param path The path for which the node is desired.
     * @return The node whose path names match the path.
     */
    public static DefaultMutableTreeNode getNodeForPathByName(DefaultMutableTreeNode node, TreePath path)
    {
        String[] pathStrings = convertToStrings(path);

        if (pathStrings[0].equals(getNodeName(node)))
        {
            DefaultMutableTreeNode currentNode = node;

            for (int i = 1; i < pathStrings.length; ++i)
            {
                String pathElement = pathStrings[i];

                boolean foundNode = false;
                Enumeration<DefaultMutableTreeNode> children = currentNode.children();
                while (children.hasMoreElements())
                {
                    DefaultMutableTreeNode child = children.nextElement();
                    if (getNodeName(child).equals(pathElement))
                    {
                        currentNode = child;
                        foundNode = true;
                        break;
                    }
                }

                if (!foundNode)
                {
                    return null;
                }
            }

            return currentNode;
        }

        return null;
    }

    /**
     * Get the paths for nodes whose button models are selected.
     *
     * @param node The node for which the paths are desired.
     * @return The selected paths.
     */
    public static List<TreePath> getSelectedPaths(DefaultMutableTreeNode node)
    {
        List<TreePath> paths = New.list();
        if (isSelected(node))
        {
            paths.add(new TreePath(node.getPath()));
        }

        Enumeration<DefaultMutableTreeNode> children = node.children();
        while (children.hasMoreElements())
        {
            DefaultMutableTreeNode child = children.nextElement();
            paths.addAll(getSelectedPaths(child));
        }

        return paths;
    }

    /**
     * Check the state of the node.
     *
     * @param node The node to check.
     * @return The state of the node.
     */
    public static boolean isSelected(DefaultMutableTreeNode node)
    {
        if (node == null || !(node.getUserObject() instanceof ButtonModelPayload))
        {
            return false;
        }
        return ((ButtonModelPayload)node.getUserObject()).getButton().isSelected();
    }

    /**
     * Set the state to selected for the node or any descendants whose button's
     * label matches any of the given labels.
     *
     * @param node The base node to search.
     * @param labels The labels to select.
     * @return The number of nodes selected.
     */
    public static int selectNodesByName(DefaultMutableTreeNode node, List<String> labels)
    {
        if (node == null)
        {
            return 0;
        }

        int numSelected = 0;
        if (node.getUserObject() != null && node.isLeaf())
        {
            for (String layer : labels)
            {
                if (getNodeName(node).equals(layer))
                {
                    setSelected(node, true);
                    ++numSelected;
                }
            }
        }

        Enumeration<DefaultMutableTreeNode> children = node.children();
        while (children.hasMoreElements())
        {
            numSelected += selectNodesByName(children.nextElement(), labels);
        }

        return numSelected;
    }

    /**
     * Set the button models to selected for the nodes whose names match the
     * names of of the nodes in the paths.
     *
     * @param node The root node to search for path matches.
     * @param paths The paths to mark as selected.
     */
    public static void selectPaths(DefaultMutableTreeNode node, List<TreePath> paths)
    {
        for (TreePath path : paths)
        {
            DefaultMutableTreeNode pathNode = getNodeForPathByName(node, path);
            if (pathNode != null)
            {
                setSelected(pathNode, true);
            }
        }
    }

    /**
     * Set the state of the node and set the state of any descendants and
     * ancestors as necessary.
     *
     * @param node The node for which to set the state.
     * @param selected The state to which to set the node.
     */
    public static void setSelected(DefaultMutableTreeNode node, boolean selected)
    {
        if (node.getUserObject() instanceof ButtonModelPayload)
        {
            ((ButtonModelPayload)node.getUserObject()).getButton().setSelected(selected);
        }
        setDescendantSelectState(node, selected);

        if (selected)
        {
            checkSelectAncestor((DefaultMutableTreeNode)node.getParent());
        }
        else
        {
            checkUnselectAncestor((DefaultMutableTreeNode)node.getParent());
        }
    }

    /**
     * Convert the path to an array of node names.
     *
     * @param path The path to convert.
     * @return The names which match the path elements.
     */
    private static String[] convertToStrings(TreePath path)
    {
        String[] pathStrings = new String[path.getPath().length];
        for (int i = 0; i < pathStrings.length; ++i)
        {
            pathStrings[i] = getNodeName((DefaultMutableTreeNode)path.getPath()[i]);
        }
        return pathStrings;
    }

    /**
     * Get the name of the node.
     *
     * @param node The node for which the name is desired.
     * @return The name of the node.
     */
    private static String getNodeName(DefaultMutableTreeNode node)
    {
        return node == null ? "" : node.getUserObject() instanceof ButtonModelPayload
                ? ((ButtonModelPayload)node.getUserObject()).getButton().getText() : node.toString();
    }

    /**
     * Set the state of the descendants without checking ancestor states.
     *
     * @param node The node for which to set the state.
     * @param selected The state to which to set the node.
     */
    private static void setDescendantSelectState(DefaultMutableTreeNode node, boolean selected)
    {
        Enumeration<DefaultMutableTreeNode> children = node.children();
        while (children.hasMoreElements())
        {
            DefaultMutableTreeNode child = children.nextElement();
            setSelected(child, selected);
        }
    }

    /** Disallow instantiation. */
    private ButtonModelTreeUtilities()
    {
    }
}
