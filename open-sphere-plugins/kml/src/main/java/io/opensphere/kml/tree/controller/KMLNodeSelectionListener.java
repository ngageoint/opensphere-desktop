package io.opensphere.kml.tree.controller;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLMapController;
import io.opensphere.kml.common.util.KMLToolboxUtils;

/**
 * Node selection listener for the KML tree. Selects (highlights) elements in
 * mantle.
 */
public class KMLNodeSelectionListener implements TreeSelectionListener
{
    /** The mantle controller. */
    private final KMLMapController myMantleController;

    /**
     * Converts a DefaultMutableTreeNode into a KMLFeature.
     *
     * @param node the tree node
     * @return the KMLFeature or null
     */
    public static KMLFeature getFeatureFromNode(DefaultMutableTreeNode node)
    {
        return node.getUserObject() instanceof KMLFeature ? (KMLFeature)node.getUserObject() : null;
    }

    /**
     * Constructor.
     *
     * @param mantleController The mantle controller
     */
    public KMLNodeSelectionListener(final KMLMapController mantleController)
    {
        myMantleController = mantleController;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        handleNodeSelectionEvent(e);
    }

    /**
     * Handle a tree node selection.
     *
     * @param treeNode The tree node
     * @param isSelected Whether the feature is selected
     */
    private void handleNodeSelection(final DefaultMutableTreeNode treeNode, final boolean isSelected)
    {
        // Get a list of placemark features under this node
        Collection<KMLFeature> features = JTreeUtilities.flattenDefault(treeNode, KMLNodeSelectionListener::isPlacemark).stream()
                .map(KMLNodeSelectionListener::getFeatureFromNode).collect(Collectors.toList());

        // Update the selection state
        myMantleController.setFeaturesSelected(features, isSelected);
    }

    /**
     * Handles a tree node selection event by updating the mantle as necessary.
     *
     * @param e The TreeSelectionEvent
     */
    private void handleNodeSelectionEvent(TreeSelectionEvent e)
    {
        TreePath[] paths = e.getPaths();
        // Do these in reverse to compensate for out of order paths
        for (int i = paths.length - 1; i >= 0; i--)
        {
            TreePath path = paths[i];

            if (path.getLastPathComponent() instanceof DefaultMutableTreeNode)
            {
                final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
                final boolean isSelected = e.isAddedPath(i);

                KMLToolboxUtils.getKmlToolbox().getPluginExecutor().execute(() -> handleNodeSelection(treeNode, isSelected));
            }
        }
    }

    /**
     * Checks whether the feature in the node is a placemark.
     *
     * @param node the node
     * @return the whether it's a placemark
     */
    private static boolean isPlacemark(DefaultMutableTreeNode node)
    {
        KMLFeature feature = getFeatureFromNode(node);
        return feature != null && feature.getFeature() instanceof Placemark;
    }
}
