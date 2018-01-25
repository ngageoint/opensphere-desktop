package io.opensphere.kml.tree.controller;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.LookAt;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLFeatureUtils;
import io.opensphere.kml.common.model.KMLMapController;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLToolboxUtils;

/**
 * MouseAdapter for the KML tree. Flies to the node's point(s) when the node is
 * double clicked.
 */
public class KMLTreeMouseAdapter extends MouseAdapter
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The mantle controller. */
    private final KMLMapController myMantleController;

    /** The popup menu. */
    private final JPopupMenu myPopupMenu;

    /** The fly-to menu item. */
    private final JMenuItem myFlyToMenuItem;

    /** The refresh menu item. */
    private final JMenuItem myRefreshMenuItem;

    /** The JTree from the event. */
    private JTree myTree;

    /**
     * Returns whether the given feature supports fly-to.
     *
     * @param feature The feature
     * @return Whether the given feature supports fly-to
     */
    private static boolean supportsFlyTo(Feature feature)
    {
        boolean supportsFlyTo = false;
        if (feature instanceof Placemark)
        {
            supportsFlyTo = ((Placemark)feature).getGeometry() != null;
        }
        else if (feature instanceof Folder)
        {
            supportsFlyTo = !((Folder)feature).getFeature().isEmpty();
        }
        else if (feature instanceof NetworkLink)
        {
            supportsFlyTo = KMLFeatureUtils.getLink((NetworkLink)feature) != null;
        }
        else if (feature instanceof GroundOverlay)
        {
            supportsFlyTo = true;
        }
        else if (feature instanceof Document)
        {
            supportsFlyTo = !((Document)feature).getFeature().isEmpty();
        }
        return supportsFlyTo;
    }

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param mantleController The mantle controller
     */
    public KMLTreeMouseAdapter(final Toolbox toolbox, final KMLMapController mantleController)
    {
        myToolbox = toolbox;

        myMantleController = mantleController;

        myPopupMenu = new JPopupMenu();

        myFlyToMenuItem = new JMenuItem("Center On");
        myFlyToMenuItem.addActionListener(e -> flyTo());

        myRefreshMenuItem = new JMenuItem("Refresh");
        myRefreshMenuItem.addActionListener(e -> refresh());
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (e.getSource() instanceof JTree)
        {
            myTree = (JTree)e.getSource();

            // They double clicked: fly to the features in the node
            if (e.getClickCount() == 2)
            {
                flyTo();
            }
            // They right clicked: select the node and show the popup
            else if (SwingUtilities.isRightMouseButton(e))
            {
                if (selectNode(e, myTree))
                {
                    doPopupMenu(e);
                }
            }
            // They single left clicked: show the popup if necessary
            else if (isEventOverNode(e, myTree) && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != InputEvent.CTRL_DOWN_MASK
                    && (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != InputEvent.SHIFT_DOWN_MASK)
            {
                showBalloon(e);
            }
        }
    }

    /**
     * Setup and show the popup menu.
     *
     * @param e The mouse event
     */
    private void doPopupMenu(MouseEvent e)
    {
        myPopupMenu.removeAll();

        KMLFeature kmlFeature = getSelectedUserObject();
        Feature feature = kmlFeature.getFeature();

        // Map items
        if (supportsFlyTo(feature))
        {
            myPopupMenu.add(myFlyToMenuItem);
        }

        // Network link items
        if (feature instanceof NetworkLink && KMLFeatureUtils.getLink((NetworkLink)feature) != null)
        {
            if (myPopupMenu.getComponentCount() > 0)
            {
                myPopupMenu.add(new JSeparator());
            }
            myPopupMenu.add(myRefreshMenuItem);
        }

        if (myPopupMenu.getComponentCount() > 0)
        {
            myPopupMenu.show(myTree, e.getX(), e.getY());
        }
    }

    /**
     * Flies to the first selected path in the tree.
     */
    private void flyTo()
    {
        final DefaultMutableTreeNode treeNode = getSelectedTreeNode();
        if (treeNode != null)
        {
            KMLToolboxUtils.getKmlToolbox().getPluginExecutor().execute(() -> flyToNode(treeNode));
        }
    }

    /**
     * Flies to the given node.
     *
     * @param treeNode The tree node
     */
    private void flyToNode(DefaultMutableTreeNode treeNode)
    {
        if (treeNode.getUserObject() instanceof KMLFeature)
        {
            KMLFeature kmlFeature = (KMLFeature)treeNode.getUserObject();

            if (kmlFeature.getAbstractView() instanceof LookAt)
            {
                KMLFlyToHelper.gotoFeatures(Collections.singletonList(kmlFeature));
            }
            else
            {
                KMLFlyToHelper.gotoFeatures(kmlFeature.getAllFeatures());
            }
        }
    }

    /**
     * Convenience method to get the selected tree node.
     *
     * @return The selected tree node
     */
    private DefaultMutableTreeNode getSelectedTreeNode()
    {
        DefaultMutableTreeNode treeNode = null;
        if (myTree != null)
        {
            TreePath path = myTree.getSelectionPath();
            if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode)
            {
                treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
            }
        }
        return treeNode;
    }

    /**
     * Convenience method to get the KMLFeature from the selected tree node.
     *
     * @return The KMLFeature
     */
    private KMLFeature getSelectedUserObject()
    {
        KMLFeature kmlFeature = null;
        DefaultMutableTreeNode treeNode = getSelectedTreeNode();
        if (treeNode.getUserObject() instanceof KMLFeature)
        {
            kmlFeature = (KMLFeature)treeNode.getUserObject();
        }
        return kmlFeature;
    }

    /**
     * Checks if a mouse event is over a tree node.
     *
     * @param e The mouse event
     * @param tree The tree
     * @return True if it's over a tree node, false otherwise
     */
    private boolean isEventOverNode(MouseEvent e, JTree tree)
    {
        boolean isEventOverNode = false;
        TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
        Rectangle bounds = tree.getUI().getPathBounds(tree, path);
        if (bounds != null && e.getY() < bounds.y + bounds.height)
        {
            // Perhaps they clicked the cell itself. If so, select it.
            int checkBoxWidth = 30;
            if (e.getX() >= bounds.x + checkBoxWidth && e.getX() < bounds.x + bounds.width)
            {
                isEventOverNode = true;
            }
        }
        return isEventOverNode;
    }

    /**
     * Refreshes the link.
     */
    private void refresh()
    {
        KMLFeature kmlFeature = getSelectedUserObject();
        if (kmlFeature != null)
        {
            KMLDataRegistryHelper.reloadData(myToolbox.getDataRegistry(), kmlFeature.getDataSource());
        }
    }

    /**
     * Selects a tree path based on the MouseEvent location.
     *
     * @param e The MouseEvent
     * @param tree The tree
     * @return True if a node was selected
     */
    private boolean selectNode(MouseEvent e, JTree tree)
    {
        boolean selected = false;
        if (isEventOverNode(e, tree))
        {
            TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
            tree.getSelectionModel().setSelectionPath(path);
            selected = true;
        }
        return selected;
    }

    /**
     * Shows the description balloon for the given MouseEvent.
     *
     * @param e The MouseEvent
     */
    private void showBalloon(MouseEvent e)
    {
        TreePath path = myTree.getClosestPathForLocation(e.getX(), e.getY());
        if (path.getLastPathComponent() instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (treeNode.getUserObject() instanceof KMLFeature)
            {
                final KMLFeature kmlFeature = (KMLFeature)treeNode.getUserObject();

                KMLToolboxUtils.getKmlToolbox().getPluginExecutor()
                        .execute(() -> myMantleController.showFeatureDetails(kmlFeature));
            }
        }
    }
}
