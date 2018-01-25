package io.opensphere.kml.tree.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.tree.model.SelectedTreePath;

/**
 * Helps with getting a list of resolved selected tree paths.
 */
public final class KMLSelectedPathHelper
{
    /**
     * Gets a list of resolved selected tree paths.
     *
     * @param documentNode The document tree node
     * @return The list of resolved selected tree paths
     */
    public static List<TreePath> getSelectedPaths(final DefaultMutableTreeNode documentNode)
    {
        List<TreePath> selectedPaths = new ArrayList<>();

        // Build up a list of all paths and their resolved selection states
        List<SelectedTreePath> allPaths = new ArrayList<>();
        accumulateAndResolve(documentNode, allPaths);

        // Add only the selected paths
        for (SelectedTreePath path : allPaths)
        {
            if (path.isSelected())
            {
                selectedPaths.add(path);
            }
        }

        return selectedPaths;
    }

    /**
     * Accumulates a list of selected TreePaths and resolves their selection
     * states.
     *
     * @param treeNode The tree node
     * @param paths The list of TreePaths to accumulate
     * @return The TreePath of the tree node argument
     */
    private static SelectedTreePath accumulateAndResolve(final DefaultMutableTreeNode treeNode,
            final List<SelectedTreePath> paths)
    {
        // Create this SelectedTreePath and accumulate it
        SelectedTreePath thisPath = new SelectedTreePath(treeNode.getPath());
        if (treeNode.getUserObject() instanceof KMLFeature)
        {
            KMLFeature kmlFeature = (KMLFeature)treeNode.getUserObject();
            thisPath.setSelected(kmlFeature.isVisibility().booleanValue());
        }
        paths.add(thisPath);

        // Recursively accumulate all child paths, plus build a list of direct
        // child paths
        List<SelectedTreePath> directChildPaths = new ArrayList<>();
        for (int i = 0, n = treeNode.getChildCount(); i < n; i++)
        {
            TreeNode childNode = treeNode.getChildAt(i);
            if (childNode instanceof DefaultMutableTreeNode)
            {
                directChildPaths.add(accumulateAndResolve((DefaultMutableTreeNode)childNode, paths));
            }
        }

        // Resolve selection states between this path and its direct children
        resolveSelections(thisPath, directChildPaths);

        return thisPath;
    }

    /**
     * Resolves selection states between parent and children.
     *
     * @param parentPath The parent path
     * @param directChildPaths The direct child paths
     */
    private static void resolveSelections(final SelectedTreePath parentPath, final List<SelectedTreePath> directChildPaths)
    {
        // Determine direct children selection states
        int selectedCount = 0;
        int unselectedCount = 0;
        for (SelectedTreePath directChildPath : directChildPaths)
        {
            if (directChildPath.isSelected())
            {
                selectedCount++;
            }
            else
            {
                unselectedCount++;
            }
        }

        // Parent = selected
        if (parentPath.isSelected())
        {
            // Children = all selected
            if (selectedCount > 0 && unselectedCount == 0)
            {
                // Unselect children
                for (SelectedTreePath childPath : directChildPaths)
                {
                    childPath.setSelected(false);
                }
            }
            // Children = some/none selected
            else if (selectedCount >= 0 && unselectedCount > 0)
            {
                // Unselect parent
                parentPath.setSelected(false);
            }
        }
        // Parent = unselected, Children = all selected
        else if (!parentPath.isSelected() && selectedCount > 0 && unselectedCount == 0)
        {
            // Select parent
            parentPath.setSelected(true);

            // Unselect children
            for (SelectedTreePath childPath : directChildPaths)
            {
                childPath.setSelected(false);
            }
        }
    }

    /**
     * Private constructor.
     */
    private KMLSelectedPathHelper()
    {
    }
}
