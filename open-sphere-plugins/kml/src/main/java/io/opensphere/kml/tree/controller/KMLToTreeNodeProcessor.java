package io.opensphere.kml.tree.controller;

import java.util.Collection;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.Processor;

/**
 * Processes a KMLFeature object into a TreeNode.
 */
public class KMLToTreeNodeProcessor implements Processor<KMLFeature, DefaultMutableTreeNode>
{
    /** The data source to parent tree node map. */
    private final Map<KMLDataSource, DefaultMutableTreeNode> myDataSourceToNodeMap;

    /**
     * Constructor.
     *
     * @param dataSourceToNodeMap The data source to parent tree node map
     */
    public KMLToTreeNodeProcessor(final Map<KMLDataSource, DefaultMutableTreeNode> dataSourceToNodeMap)
    {
        myDataSourceToNodeMap = dataSourceToNodeMap;
    }

    @Override
    public DefaultMutableTreeNode process(final KMLFeature rootKmlFeature)
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        processFeature(rootNode, rootKmlFeature);
        return rootNode;
    }

    /**
     * Adds child features to the given tree node.
     *
     * @param treeNode The tree node
     * @param features The features
     */
    private void addChildren(final DefaultMutableTreeNode treeNode, final Collection<KMLFeature> features)
    {
        for (KMLFeature feature : features)
        {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
            processFeature(childNode, feature);
            treeNode.add(childNode);
        }
    }

    /**
     * Recursively processes features.
     *
     * @param treeNode The tree node
     * @param feature The feature
     */
    private void processFeature(final DefaultMutableTreeNode treeNode, final KMLFeature feature)
    {
        treeNode.setUserObject(feature);

        // Make a mapping here so we know what node to attach to when the
        // network link is loaded
        if (feature.getFeature() instanceof NetworkLink)
        {
            myDataSourceToNodeMap.put(feature.getResultingDataSource(), treeNode);
        }

        if (!feature.getChildren().isEmpty())
        {
            addChildren(treeNode, feature.getChildren());
        }
    }
}
