package io.opensphere.kml.tree.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.jidesoft.swing.CheckBoxTree;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Overlay;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.kml.common.model.KMLController;
import io.opensphere.kml.common.model.KMLDataEvent;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLMapController;
import io.opensphere.kml.common.util.KMLToolbox;
import io.opensphere.kml.tree.view.KMLTreePanel;

/**
 * Controller for KML tree.
 */
@ThreadSafe
public class KMLTreeController implements KMLController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(KMLTreeController.class);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The data source to tree node map. */
    @GuardedBy("myDataSourceToNodeMap")
    private final Map<KMLDataSource, DefaultMutableTreeNode> myDataSourceToNodeMap;

    /** The tree. */
    private CheckBoxTree myTree;

    /** The check box selection listener. */
    private KMLCheckBoxSelectionListener myCheckBoxSelectionListener;

    /** The mouse adapter. */
    private KMLTreeMouseAdapter myKMLTreeMouseAdapter;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param mantleController The mantle controller
     */
    public KMLTreeController(Toolbox toolbox, final KMLMapController mantleController)
    {
        myToolbox = toolbox;
        myDataSourceToNodeMap = Collections.synchronizedMap(new HashMap<KMLDataSource, DefaultMutableTreeNode>());
        EventQueueUtilities.invokeLater(() -> init(mantleController));
    }

    /**
     * Initializes swing stuff.
     *
     * @param mantleController The mantle controller
     */
    private void init(KMLMapController mantleController)
    {
        KMLTreePanel treePanel = new KMLTreePanel(myToolbox.getDataRegistry());
        myTree = treePanel.getTree();
        KMLToolbox kmlToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(KMLToolbox.class);
        kmlToolbox.setTreePanel(treePanel);
        myCheckBoxSelectionListener = new KMLCheckBoxSelectionListener(mantleController, myToolbox.getDataRegistry());
        myTree.getCheckBoxTreeSelectionModel().addTreeSelectionListener(myCheckBoxSelectionListener);
        myTree.getSelectionModel().addTreeSelectionListener(new KMLNodeSelectionListener(mantleController));
        myKMLTreeMouseAdapter = new KMLTreeMouseAdapter(myToolbox, mantleController);
        myTree.addMouseListener(myKMLTreeMouseAdapter);
        myTree.addTreeExpansionListener(new KMLTreeExpansionListener());
    }

    @Override
    public void addData(final KMLDataEvent dataEvent, boolean reload)
    {
        assert !SwingUtilities.isEventDispatchThread();

        KMLFeature rootFeature = dataEvent.getData();

        // Create the node from the kml document
        final DefaultMutableTreeNode documentNode = new KMLToTreeNodeProcessor(myDataSourceToNodeMap).process(rootFeature);

        // Update the tree
        EventQueueUtilities.runOnEDT(() -> addNode(documentNode, dataEvent));
    }

    @Override
    public void removeData(KMLDataSource dataSource)
    {
        assert !SwingUtilities.isEventDispatchThread();

        if (!(dataSource.getCreatingFeature() instanceof Overlay))
        {
            // Clean up node map
            final DefaultMutableTreeNode documentNode = myDataSourceToNodeMap.remove(dataSource);
            for (KMLDataSource childDataSource : dataSource.getAllChildDataSources())
            {
                myDataSourceToNodeMap.remove(childDataSource);
            }
            if (documentNode == null)
            {
                return;
            }

            // Update the tree
            if (!(dataSource.getCreatingFeature() instanceof NetworkLink))
            {
                EventQueueUtilities.runOnEDT(() -> removeNode(documentNode));
            }
        }
    }

    /**
     * Adds a node to the tree.
     *
     * @param documentNode The document tree node to add
     * @param dataEvent The data event
     */
    private void addNode(final DefaultMutableTreeNode documentNode, final KMLDataEvent dataEvent)
    {
        myCheckBoxSelectionListener.setIgnoreSelections(true);

        KMLDataSource dataSource = dataEvent.getDataSource();

        // Get the existing node (if it exists)
        DefaultMutableTreeNode existingNode = myDataSourceToNodeMap.get(dataSource);

        // Determine the parent node and remove any necessary children
        DefaultMutableTreeNode parentNode = null;
        List<DefaultMutableTreeNode> nodesToAdd;
        DefaultMutableTreeNode newNode;
        if (dataSource.getCreatingFeature() instanceof NetworkLink)
        {
            if (existingNode != null)
            {
                parentNode = existingNode;
                parentNode.removeAllChildren();
            }
            nodesToAdd = documentNode.getChildCount() > 0 ? JTreeUtilities.getChildren(documentNode)
                    : Collections.singletonList(documentNode);
            newNode = parentNode;
        }
        else
        {
            parentNode = getRootNode();
            if (existingNode != null)
            {
                parentNode.remove(existingNode);
            }
            nodesToAdd = Collections.singletonList(documentNode);
            newNode = documentNode;
        }

        if (parentNode == null)
        {
            LOGGER.warn("No parent node found, unable to add node");
            return;
        }

        // Keep track of stuff
        myDataSourceToNodeMap.put(dataSource, newNode);

        // Add the node(s) to the parent node
        addNodes(parentNode, nodesToAdd);

        // Expand the folders that are open in the model
        expandNode(parentNode);

        // Select this node and its children based on the feature visibilities
        selectNode(newNode);

        // Expand the root node
        if (!(dataSource.getCreatingFeature() instanceof NetworkLink))
        {
            myTree.expandRow(0);
        }

        myCheckBoxSelectionListener.setIgnoreSelections(false);
    }

    /**
     * Adds the nodesToAdd to the parentNode.
     *
     * @param parentNode The parent node
     * @param childNodes The nodes to add
     */
    private void addNodes(final DefaultMutableTreeNode parentNode, final List<DefaultMutableTreeNode> childNodes)
    {
        // Add the node(s)
        for (DefaultMutableTreeNode childNode : childNodes)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Adding " + new TreePath(childNode.getPath()) + " to " + new TreePath(parentNode.getPath()));
            }
            parentNode.add(childNode);
        }

        // Fire an event that the nodes were inserted
        ((DefaultTreeModel)myTree.getModel()).nodeStructureChanged(parentNode);
    }

    /**
     * Expand the folders that are open in the model of the given node.
     *
     * @param node The node to expand
     */
    private void expandNode(DefaultMutableTreeNode node)
    {
        JTreeUtilities.flattenDefault(node, KMLTreeController::isOpen).stream().map(JTreeUtilities.NODE_TO_PATH)
                .forEach(p -> myTree.expandPath(p));
    }

    /**
     * Helper method to get the root node, or null.
     *
     * @return The root node, or null
     */
    private DefaultMutableTreeNode getRootNode()
    {
        DefaultMutableTreeNode rootNode = null;
        Object root = myTree.getModel().getRoot();
        if (root instanceof DefaultMutableTreeNode)
        {
            rootNode = (DefaultMutableTreeNode)root;
        }
        return rootNode;
    }

    /**
     * Removes a node from the tree.
     *
     * @param documentNode The document tree node to remove
     */
    private void removeNode(final DefaultMutableTreeNode documentNode)
    {
        myCheckBoxSelectionListener.setIgnoreSelections(true);

        // Get the root node
        DefaultMutableTreeNode rootNode = getRootNode();
        if (rootNode == null)
        {
            return;
        }

        // Remove the node from the tree and fire an event
        int childIndex = rootNode.getIndex(documentNode);
        if (childIndex != -1)
        {
            int[] childIndices = { childIndex };
            Object[] removedChildren = { documentNode };
            rootNode.remove(documentNode);
            ((DefaultTreeModel)myTree.getModel()).nodesWereRemoved(rootNode, childIndices, removedChildren);
        }

        myCheckBoxSelectionListener.setIgnoreSelections(false);
    }

    /**
     * Selects the features and all child features of the given node based on
     * the model.
     *
     * @param node The node to select
     */
    private void selectNode(final DefaultMutableTreeNode node)
    {
        // Remove any selections that might have gotten added when we added
        // the node
        TreePath nodePath = new TreePath(node.getPath());
        myTree.getCheckBoxTreeSelectionModel().removeSelectionPath(nodePath);

        // Add the node selection paths
        List<TreePath> selectedPaths = KMLSelectedPathHelper.getSelectedPaths(node);
        if (!selectedPaths.isEmpty())
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Selecting " + selectedPaths);
            }
            TreePath[] selectedPathArray = selectedPaths.toArray(new TreePath[selectedPaths.size()]);
            myTree.getCheckBoxTreeSelectionModel().addSelectionPaths(selectedPathArray);
        }
    }

    /**
     * Checks whether the feature in the node is open.
     *
     * @param node the node
     * @return the whether it's open
     */
    private static boolean isOpen(DefaultMutableTreeNode node)
    {
        KMLFeature feature = KMLNodeSelectionListener.getFeatureFromNode(node);
        return feature != null && feature.isOpen().booleanValue();
    }

    /**
     * TreeExpansionListener for the KML tree.
     */
    private static class KMLTreeExpansionListener implements TreeExpansionListener
    {
        @Override
        public void treeCollapsed(TreeExpansionEvent event)
        {
            handleTreeExpansionEvent(event, false);
        }

        @Override
        public void treeExpanded(TreeExpansionEvent event)
        {
            handleTreeExpansionEvent(event, true);
        }

        /**
         * Handles a TreeExpansionEvent.
         *
         * @param event The event
         * @param isExpanded True for expanded, false for collapsed
         */
        private void handleTreeExpansionEvent(TreeExpansionEvent event, boolean isExpanded)
        {
            if (event.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode)
            {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
                if (treeNode.getUserObject() instanceof KMLFeature)
                {
                    KMLFeature kmlFeature = (KMLFeature)treeNode.getUserObject();
                    if (kmlFeature.getFeature() instanceof Folder)
                    {
                        kmlFeature.setOpen(Boolean.valueOf(isExpanded));
                    }
                }
            }
        }
    }
}
