package io.opensphere.core.util.swing;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import io.opensphere.core.util.collections.New;

/**
 * Utility functions for working with {@link JTree}.
 */
public final class JTreeUtilities
{
    /**
     * The function to convert from a node to a path.
     */
    public static final Function<DefaultMutableTreeNode, TreePath> NODE_TO_PATH = node -> new TreePath(node.getPath());

    /**
     * Builds a tree path starting with the provided node back up the parent
     * hierarchy to the root node, then reverses the path to define a
     * root-to-provided node path.
     *
     * @param node the node
     * @return the tree path
     */
    public static TreePath buildPathToNode(TreeNode node)
    {
        List<TreeNode> nodeList = New.list();
        nodeList.add(node);
        TreeNode parent = node.getParent();
        while (parent != null)
        {
            nodeList.add(parent);
            parent = parent.getParent();
        }
        Collections.reverse(nodeList);
        return new TreePath(nodeList.toArray());
    }

    /**
     * Expand or collapse all nodes in a JTree.
     *
     * @param tree the tree to expand or collapse.
     * @param expand - true to expand, false to collapse.
     */
    public static void expandOrCollapseAll(JTree tree, boolean expand)
    {
        if (tree.isRootVisible())
        {
            expandOrCollapsePath(tree, new TreePath(tree.getModel().getRoot()), expand);
        }
        else
        {
            if (tree.getModel().getRoot() instanceof TreeNode)
            {
                TreeNode rootNode = (TreeNode)tree.getModel().getRoot();
                TreePath rootPath = new TreePath(rootNode);
                if (rootNode.getChildCount() > 0)
                {
                    for (Enumeration<?> e = rootNode.children(); e.hasMoreElements();)
                    {
                        TreeNode n = (TreeNode)e.nextElement();
                        TreePath aPath = rootPath.pathByAddingChild(n);
                        expandOrCollapsePath(tree, aPath, expand);
                    }
                }
            }
        }
    }

    /**
     * Expand or collapse tree path.
     *
     * @param tree the tree
     * @param path the path to expand or collapse.
     * @param expand true to expand, false to collapse.
     */
    public static void expandOrCollapsePath(JTree tree, TreePath path, boolean expand)
    {
        if (tree.getModel().getRoot() instanceof TreeNode)
        {
            // Traverse children
            TreeNode node = (TreeNode)path.getLastPathComponent();
            if (node.getChildCount() > 0)
            {
                for (Enumeration<?> e = node.children(); e.hasMoreElements();)
                {
                    TreeNode n = (TreeNode)e.nextElement();
                    TreePath aPath = path.pathByAddingChild(n);
                    expandOrCollapsePath(tree, aPath, expand);
                }
            }

            // Expansion or collapse must be done bottom-up
            if (expand)
            {
                tree.expandPath(path);
            }
            else
            {
                tree.collapsePath(path);
            }
        }
    }

    /**
     * Searches a JTree for a node that meets some end criteria, if the end
     * criteria finds a match it will return the {@link TreePath} to that
     * matched node.
     *
     * @param tree the {@link JTree} to search.
     * @param endCriteria the EndCriteria that determines when the node being
     *            searched for is found.
     * @return the {@link TreePath} to the found node or null if not found.
     */
    public static TreePath findPathToNode(JTree tree, EndCriteria<TreeNode> endCriteria)
    {
        TreePath result = null;
        Object root = tree.getModel().getRoot();
        if (root instanceof TreeNode)
        {
            result = findPathToNode((TreeNode)root, new TreePath(tree.getModel().getRoot()), endCriteria);
        }
        return result;
    }

    /**
     * Searches TreeNode and its descendants for a node that meets some end
     * criteria, if the end criteria finds a match it will return the
     * {@link TreePath} to that matched node.
     *
     * @param startNode the {@link TreeNode} to use to start the search.
     * @param path the {@link TreePath} to the startNode it its tree.
     * @param endCriteria the EndCriteria that determines when the node being
     *            searched for is found.
     * @return the {@link TreePath} to the found node or null if not found.
     */
    public static TreePath findPathToNode(TreeNode startNode, TreePath path, EndCriteria<TreeNode> endCriteria)
    {
        TreePath result = null;
        if (endCriteria.found(startNode))
        {
            result = path;
        }
        else
        {
            if (startNode.getChildCount() >= 0)
            {
                for (Enumeration<?> e = startNode.children(); e.hasMoreElements();)
                {
                    TreeNode n = (TreeNode)e.nextElement();
                    TreePath aPath = path.pathByAddingChild(n);
                    result = findPathToNode(n, aPath, endCriteria);
                    if (result != null)
                    {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Filters the accumulation of the given nodes and all their descendants.
     *
     * @param pNodes The tree nodes
     * @param filter The filter
     * @return The list of filtered nodes
     */
    public static List<TreeNode> flatten(Collection<? extends TreeNode> pNodes, Predicate<? super TreeNode> filter)
    {
        List<TreeNode> nodes = New.list();
        for (TreeNode node : pNodes)
        {
            flatten(node, nodes, filter);
        }
        return nodes;
    }

    /**
     * Filters the accumulation of the given node and all its descendants.
     *
     * @param node The tree node
     * @param filter The filter
     * @return The list of filtered nodes
     */
    public static List<TreeNode> flatten(TreeNode node, Predicate<? super TreeNode> filter)
    {
        List<TreeNode> nodes = New.list();
        flatten(node, nodes, filter);
        return nodes;
    }

    /**
     * Filters the accumulation of the given node and all its descendants.
     *
     * @param node The tree node
     * @param filter The filter
     * @return The list of filtered nodes
     */
    public static List<DefaultMutableTreeNode> flattenDefault(DefaultMutableTreeNode node,
            Predicate<? super DefaultMutableTreeNode> filter)
    {
        List<DefaultMutableTreeNode> nodes = New.list();
        flattenDefault(node, nodes, filter);
        return nodes;
    }

    /**
     * Gets a List of the given node's children. This assumes that the children
     * are {@link DefaultMutableTreeNode}s.
     *
     * @param node The node
     * @return The List of children
     */
    public static List<DefaultMutableTreeNode> getChildren(DefaultMutableTreeNode node)
    {
        int childCount = node.getChildCount();
        List<DefaultMutableTreeNode> children = New.list(childCount);
        for (int i = 0; i < childCount; i++)
        {
            children.add((DefaultMutableTreeNode)node.getChildAt(i));
        }
        return children;
    }

    /**
     * Accumulates a list of selected nodes.
     *
     * @param node The tree node
     * @param nodes The list of nodes to accumulate
     * @param filter The filter of which nodes to accumulate
     */
    private static void flatten(TreeNode node, Collection<? super TreeNode> nodes, Predicate<? super TreeNode> filter)
    {
        if (filter.test(node))
        {
            nodes.add(node);
        }
        for (int i = 0, n = node.getChildCount(); i < n; i++)
        {
            flatten(node.getChildAt(i), nodes, filter);
        }
    }

    /**
     * Accumulates a list of selected nodes (only includes
     * DefaultMutableTreeNodes).
     *
     * @param node The tree node
     * @param nodes The list of nodes to accumulate
     * @param filter The filter of which nodes to accumulate
     */
    private static void flattenDefault(DefaultMutableTreeNode node, Collection<? super DefaultMutableTreeNode> nodes,
            Predicate<? super DefaultMutableTreeNode> filter)
    {
        if (filter.test(node))
        {
            nodes.add(node);
        }
        for (int i = 0, n = node.getChildCount(); i < n; i++)
        {
            TreeNode childNode = node.getChildAt(i);
            if (childNode instanceof DefaultMutableTreeNode)
            {
                flattenDefault((DefaultMutableTreeNode)childNode, nodes, filter);
            }
        }
    }

    /**
     * Constructor ( not allowed ).
     */
    private JTreeUtilities()
    {
        // Do not allow instantiation.
    }

    /**
     * An interface for the criteria that determine when to stop processing a
     * tree.
     *
     * @param <T> The type tested by the criteria.
     */
    @FunctionalInterface
    public interface EndCriteria<T>
    {
        /**
         * Found.
         *
         * @param value the value to be evaluated.
         * @return true, if the value satisfies the end of the search, false if
         *         search should continue.
         */
        boolean found(T value);
    }
}
