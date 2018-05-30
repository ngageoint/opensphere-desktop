package io.opensphere.controlpanels.layers.activedata.tabletree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import io.opensphere.controlpanels.layers.prefs.DataDiscoveryPreferences;
import io.opensphere.controlpanels.layers.util.ClockAndOrColorLabel;
import io.opensphere.controlpanels.layers.util.FeatureTypeLabel;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.tree.ButtonModelPayload;
import io.opensphere.core.util.swing.tree.CustomTreeTableModelButtonBuilder;
import io.opensphere.core.util.swing.tree.DragNDropTreeCellRenderer;
import io.opensphere.core.util.swing.tree.HoverButtonTreeCellRenderer;
import io.opensphere.core.util.swing.tree.NodeImageObserver;
import io.opensphere.core.util.swing.tree.TreeTableTreeCellRenderer;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.PlayState;
import io.opensphere.mantle.data.filter.DataLayerFilter;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * The active data TreeTableTreeCellRenderer.
 */
public class ActiveDataTreeTableTreeCellRenderer extends TreeTableTreeCellRenderer
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The active streaming icon label. */
    private final ImageIcon myActiveStreamingIcon;

    /** The clock icon. */
    private final ClockAndOrColorLabel myColorClockLabel;

    /** The dgc. */
    private final DataGroupController myDGC;

    /** The drag-n-drop renderer. */
    private final DragNDropTreeCellRenderer myDragNDropRenderer;

    /** The feature type label. */
    private final FeatureTypeLabel myFeatureTypeLabel;

    /** The hover button renderer. */
    private final HoverButtonTreeCellRenderer myHoverButtonRenderer;

    /** The streaming icon. */
    private final ImageIcon myStreamingIcon;

    /** The streaming icon label. */
    private final JLabel myStreamingIconLabel;

    /** The label used to annotate a process. */
    private final JLabel myProcessLabel;

    /** The image observer for the animated streaming image. */
    private NodeImageObserver myStreamingImageObserver;

    /**
     * Gets the GroupByNodeUserObject from the given ButtonModelPayload.
     *
     * @param payload the ButtonModelPayload
     * @return the GroupByNodeUserObject or null
     */
    private static GroupByNodeUserObject getUserObject(ButtonModelPayload payload)
    {
        GroupByNodeUserObject userObject = null;
        if (payload != null && payload.getPayloadData() instanceof GroupByNodeUserObject)
        {
            userObject = (GroupByNodeUserObject)payload.getPayloadData();
        }
        return userObject;
    }

    /**
     * Instantiates a new adds the data tree table tree cell renderer.
     *
     * @param dgc the dgc
     */
    public ActiveDataTreeTableTreeCellRenderer(DataGroupController dgc)
    {
        super(false);
        myDGC = dgc;
        myFeatureTypeLabel = new FeatureTypeLabel(dgc.getToolbox());
        myColorClockLabel = new ClockAndOrColorLabel();
        myStreamingIcon = new ImageIcon(ActiveDataTreeTableTreeCellRenderer.class.getResource("/images/streaming.png"));
        myActiveStreamingIcon = new ImageIcon(
                ActiveDataTreeTableTreeCellRenderer.class.getResource("/images/streaming_animated.gif"));
        myStreamingIconLabel = new JLabel();
        myStreamingIconLabel.setPreferredSize(new Dimension(12, 20));
        myDragNDropRenderer = new DragNDropTreeCellRenderer(this);
        myHoverButtonRenderer = new HoverButtonTreeCellRenderer(this);
        myProcessLabel = new JLabel();
        myProcessLabel.setPreferredSize(new Dimension(16, 17));

        Icon baseIcon = IconUtil.getNormalIcon("/images/base.png");
        dgc.getToolbox().getUIRegistry().getIconLegendRegistry().addIconToLegend(baseIcon, "Base Layer",
                "Base layers are different from 'Static' and 'Timeline' layers. Their geometries will "
                        + "be displayed on the map but will not be displayed in the list tool, will not "
                        + "be part of any timeline, and associated meta data is not loaded into memory. Basically, "
                        + "these layers display data without using extra memory.");
    }

    @Override
    public void addPrefixIcons(JTree tree, JPanel panel, TreeTableTreeNode node)
    {
        GroupByNodeUserObject userObject = getUserObject(node.getPayload());
        if (userObject != null)
        {
            DataGroupInfo dgi = userObject.getDataGroupInfo();
            DataTypeInfo dti = userObject.getDataTypeInfo();

            if (dgi != null && dti == null && dgi.numMembers(false) == 1 && dgi.isFlattenable())
            {
                dti = dgi.getMembers(false).iterator().next();
            }
            if (dti != null)
            {
                // Feature type icon
                LoadsTo loadsTo = dti.getBasicVisualizationInfo() == null ? LoadsTo.BASE
                        : dti.getBasicVisualizationInfo().getLoadsTo();
                if (DataDiscoveryPreferences.isShowActiveLayerTypeIcons(myDGC.getToolbox().getPreferencesRegistry()))
                {
                    myFeatureTypeLabel.setIconByType(dti);
                    panel.add(myFeatureTypeLabel);
                    addComponentWidth(myFeatureTypeLabel);
                }

                // Clock icon
                myColorClockLabel.setType(dti);
                if (shouldShowClockColorLabel(dti, loadsTo))
                {
                    panel.add(myColorClockLabel);
                    addComponentWidth(myColorClockLabel);
                }

                addStreamingIcon(tree, panel, node, dti);
                addProcessIcon(tree, panel, node, dti);
            }
            else if (dgi != null && !tree.isExpanded(new TreePath(node.getPath())))
            {
                // If there are any active streaming children, add the streaming
                // icon.
                for (DataTypeInfo child : dgi.getMembers(true))
                {
                    if (child.getStreamingSupport().isStreamingEnabled()
                            && child.getStreamingSupport().getPlayState().get() != PlayState.STOP)
                    {
                        addStreamingIcon(tree, panel, node, child);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void formatText(ButtonModelPayload payload, JLabel label)
    {
        GroupByNodeUserObject userObject = getUserObject(payload);
        if (userObject != null)
        {
            DataGroupInfo dgi = userObject.getDataGroupInfo();
            DataTypeInfo dti = userObject.getDataTypeInfo();
            boolean hasActiveFilter = false;

            if (dti != null)
            {
                hasActiveFilter = DataLayerFilter.hasActiveLoadFilter(myDGC.getToolbox(), dti);
            }
            else if (dgi != null && dgi.numMembers(false) == 1)
            {
                hasActiveFilter = DataLayerFilter.hasActiveLoadFilter(myDGC.getToolbox(), dgi);
            }

            if (hasActiveFilter)
            {
                Font f = label.getFont();
                label.setFont(f.deriveFont(Font.ITALIC));
            }
        }
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
            int row, boolean hasFocus)
    {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        myHoverButtonRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        myDragNDropRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        return this;
    }

    @Override
    public boolean isBusyLabelVisible(ButtonModelPayload buttonModelPayload)
    {
        boolean visible = false;
        GroupByNodeUserObject userObject = getUserObject(buttonModelPayload);
        if (userObject != null)
        {
            DataGroupInfo dgi = userObject.getDataGroupInfo();
            if (dgi != null)
            {
                visible = dgi.activationProperty().isActivatingOrDeactivating();
            }
        }
        return visible;
    }

    /**
     * Sets the button builders.
     *
     * @param buttonBuilders the button builders
     */
    public void setButtonBuilders(Collection<CustomTreeTableModelButtonBuilder> buttonBuilders)
    {
        myHoverButtonRenderer.setButtonBuilders(buttonBuilders);
    }

    /**
     * Adds a new icon representing a process.
     *
     * @param pTree the tree in which the cell is rendered.
     * @param pPanel the panel in which the cell is rendered (specifically,
     *            where the icon will be displayed).
     * @param pNode The tree node to modify.
     * @param pDataTypeInfo the data type for which to add the icon.
     */
    protected void addProcessIcon(JTree pTree, JPanel pPanel, TreeTableTreeNode pNode, DataTypeInfo pDataTypeInfo)
    {
        if (pDataTypeInfo.getMapVisualizationInfo() != null
                && pDataTypeInfo.getMapVisualizationInfo().getVisualizationType() == MapVisualizationType.PROCESS_RESULT_ELEMENTS)
        {
            Icon icon = new GenericFontIcon(AwesomeIconSolid.FLASK, Color.WHITE, 14);
            myProcessLabel.setIcon(icon);
            pPanel.add(myProcessLabel);
            addComponentWidth(myProcessLabel);
        }
    }

    /**
     * Add the streaming icon.
     *
     * @param tree The tree.
     * @param panel The panel.
     * @param node The node.
     * @param dti The data type info.
     */
    private void addStreamingIcon(JTree tree, JPanel panel, TreeTableTreeNode node, DataTypeInfo dti)
    {
        if (dti.getStreamingSupport().isStreamingEnabled())
        {
            ImageIcon icon;
            TreePath path = new TreePath(node.getPath());
            if (dti.getStreamingSupport().getPlayState().get() != PlayState.STOP)
            {
                if (myStreamingImageObserver == null)
                {
                    myStreamingImageObserver = new NodeImageObserver(tree);
                    myActiveStreamingIcon.setImageObserver(myStreamingImageObserver);
                }
                myStreamingImageObserver.addPath(path);
                icon = IconUtil.createIconWithMixInColor(myActiveStreamingIcon, dti.getBasicVisualizationInfo().getTypeColor());
            }
            else
            {
                icon = IconUtil.createIconWithMixInColor(myStreamingIcon, dti.getBasicVisualizationInfo().getTypeColor());
                if (myStreamingImageObserver != null)
                {
                    myStreamingImageObserver.removePath(path);
                }
            }
            myStreamingIconLabel.setIcon(icon);
            panel.add(myStreamingIconLabel);
            addComponentWidth(myStreamingIconLabel);
        }
    }

    /**
     * Get if the clock label should be shown for a dti.
     *
     * @param dti The dti.
     * @param loadsTo The loads-to.
     * @return {@code true} if the clock label should be shown.
     */
    private boolean shouldShowClockColorLabel(DataTypeInfo dti, LoadsTo loadsTo)
    {
        boolean showClockColorLabel = loadsTo != null && loadsTo.isTimelineEnabled();
        if (showClockColorLabel && !myColorClockLabel.isTimeDriven() && dti.getMapVisualizationInfo() != null)
        {
            MapVisualizationType type = dti.getMapVisualizationInfo().getVisualizationType();
            if (type == MapVisualizationType.TERRAIN_TILE || type == MapVisualizationType.IMAGE
                    || type == MapVisualizationType.IMAGE_TILE || type == MapVisualizationType.MOTION_IMAGERY
                    || type == MapVisualizationType.MOTION_IMAGERY_DATA)
            {
                showClockColorLabel = false;
            }
        }
        return showClockColorLabel;
    }
}
