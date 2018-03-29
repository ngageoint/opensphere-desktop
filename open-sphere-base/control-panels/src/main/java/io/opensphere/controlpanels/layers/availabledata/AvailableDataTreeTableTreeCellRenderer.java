package io.opensphere.controlpanels.layers.availabledata;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.jidesoft.swing.CheckBoxTree;
import com.jidesoft.swing.TristateCheckBox;

import io.opensphere.controlpanels.layers.util.FeatureTypeLabel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.AwesomeIcon;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.tree.ButtonModelPayload;
import io.opensphere.core.util.swing.tree.CustomTreeTableModelButtonBuilder;
import io.opensphere.core.util.swing.tree.HoverButtonTreeCellRenderer;
import io.opensphere.core.util.swing.tree.ListCheckBoxTree;
import io.opensphere.core.util.swing.tree.TreeTableTreeCellRenderer;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.core.util.swing.tree.TristateCheckBoxWithButtonModeSupport;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.StreamingSupport;
import io.opensphere.mantle.data.filter.DataLayerFilter;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * The Class AddDataTreeTableTreeCellRenderer.
 */
public class AvailableDataTreeTableTreeCellRenderer extends TreeTableTreeCellRenderer
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(AvailableDataTreeTableTreeCellRenderer.class);

    /** The Constant ourDefaultLabelForeground. */
    private static final Color ourDefaultLabelForeground = new JLabel().getForeground();

    /** The slider mixed icon. */
    private static ImageIcon ourSliderMixedIcon;

    /** The slider off icon. */
    private static ImageIcon ourSliderOffIcon;

    /** The slider on icon. */
    private static ImageIcon ourSliderOnIcon;

    /** The streaming icon. */
    private static ImageIcon ourStreamingIcon;

    /** The white clock icon. */
    private static ImageIcon ourWhiteClockIcon;

    /** The alert icon. */
    private static ImageIcon ourAlertIcon;

    /** The icon used for a process instance. */
    private static Icon ourProcessIcon;

    /** The icon used to create a new instance. */
    private static Icon ourCreateInstanceIcon;

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The clock icon. */
    private final JLabel myClockIcon;

    /** The feature label. */
    private final FeatureTypeLabel myFeatureLabel;

    /** The hover button renderer. */
    private final HoverButtonTreeCellRenderer myHoverButtonRenderer;

    /** The motion imagery label. */
    private final FeatureTypeLabel myMotionImageryLabel;

    /** The streaming label. */
    private final JLabel myStreamingLabel;

    /** The label used for process-enabled datatypes. */
    private final JLabel myProcessLabel;

    /** The tile label. */
    private final FeatureTypeLabel myTileLabel;

    /** The alert label. */
    private final JLabel myAlertLabel;

    static
    {
        try
        {
            ourWhiteClockIcon = new ImageIcon(
                    ImageIO.read(AvailableDataTreeTableTreeCellRenderer.class.getResource("/images/small_white_clock2.png")));
            ourSliderOffIcon = new ImageIcon(
                    ImageIO.read(AvailableDataTreeTableTreeCellRenderer.class.getResource("/images/slider-up.png")));
            ourSliderOnIcon = new ImageIcon(
                    ImageIO.read(AvailableDataTreeTableTreeCellRenderer.class.getResource("/images/slider-upAndSelected.png")));
            ourSliderMixedIcon = new ImageIcon(
                    ImageIO.read(AvailableDataTreeTableTreeCellRenderer.class.getResource("/images/slider-upAndMixed.png")));
            ourStreamingIcon = new ImageIcon(
                    ImageIO.read(AvailableDataTreeTableTreeCellRenderer.class.getResource("/images/streaming.png")));
            ourAlertIcon = new ImageIcon(
                    ImageIO.read(AvailableDataTreeTableTreeCellRenderer.class.getResource("/images/bang_12x12.png")));
            ourProcessIcon = new GenericFontIcon(AwesomeIcon.ICON_FLASK, Color.WHITE, 14);
            ourCreateInstanceIcon = new GenericFontIcon(AwesomeIcon.ICON_PLUS_SQUARE, Color.GREEN, 14);
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to load image icons for AvailableDataTreeTableTreeCellRenderer. " + e);
        }
    }

    /**
     * Instantiates a new adds the data tree table tree cell renderer.
     *
     * @param toolbox the toolbox
     */
    public AvailableDataTreeTableTreeCellRenderer(Toolbox toolbox)
    {
        super(true);
        myToolbox = toolbox;
        myClockIcon = new JLabel(ourWhiteClockIcon);
        myFeatureLabel = new FeatureTypeLabel();
        myFeatureLabel.setIconByType(Color.WHITE, MapVisualizationType.POINT_ELEMENTS);
        myTileLabel = new FeatureTypeLabel();
        myTileLabel.setIconByType(Color.WHITE, MapVisualizationType.IMAGE_TILE);
        myMotionImageryLabel = new FeatureTypeLabel();
        myMotionImageryLabel.setIconByType(Color.WHITE, MapVisualizationType.MOTION_IMAGERY);
        myStreamingLabel = new JLabel(ourStreamingIcon);
        myStreamingLabel.setPreferredSize(new Dimension(12, 17));

        myProcessLabel = new JLabel(ourProcessIcon);
        myProcessLabel.setPreferredSize(new Dimension(16, 17));

        myAlertLabel = new JLabel(IconUtil.getColorizedIcon(ourAlertIcon, Color.RED));
        myAlertLabel.setPreferredSize(new Dimension(12, 17));
        myHoverButtonRenderer = new HoverButtonTreeCellRenderer(this);
    }

    @Override
    public void addPrefixIcons(JTree tree, JPanel panel, TreeTableTreeNode node)
    {
        DataGroupInfo dgi = getDataGroupInfoPayload(node.getPayload());
        if (dgi != null)
        {
            if (dgi.hasMember(t -> t.isAlert(), false))
            {
                addLabel(panel, myAlertLabel);
            }
            if (dgi.hasMember(t -> t.getMapVisualizationInfo() != null && t.getMapVisualizationInfo().isMotionImageryType(),
                    false))
            {
                addLabel(panel, myMotionImageryLabel);
            }
            if (dgi.hasImageTileTypes(false))
            {
                addLabel(panel, myTileLabel);
            }
            if (dgi.hasFeatureTypes(false))
            {
                addLabel(panel, myFeatureLabel);
            }
            if (dgi.hasTimelineMember(false))
            {
                addLabel(panel, myClockIcon);
            }
            if (dgi.hasMember(StreamingSupport.IS_STREAMING_ENABLED, false))
            {
                addLabel(panel, myStreamingLabel);
            }
            if (dgi.hasMember(
                    t -> t.getMapVisualizationInfo() != null
                            && t.getMapVisualizationInfo().getVisualizationType() == MapVisualizationType.PROCESS_RESULT_ELEMENTS,
                    false))
            {
                addLabel(panel, myProcessLabel);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.swing.tree.TreeTableTreeCellRenderer#setState(io.opensphere.core.util.swing.tree.TreeTableTreeNode,
     *      com.jidesoft.swing.TristateCheckBox)
     */
    @Override
    public void setState(JTree tree, TreeTableTreeNode pNode, TristateCheckBox pTristateCheckBox)
    {
        DataGroupInfo dgi = getDataGroupInfoPayload(pNode.getPayload());

        // Hide the checkbox for the parent of triggering layers (i.e. WPS)
        if (tree instanceof ListCheckBoxTree)
        {
            TreePath path = new TreePath(pNode.getPath());
            boolean hideCheckbox = dgi != null && dgi.hasChildren()
                    && dgi.getChildren().stream().allMatch(g -> g.isTriggeringSupported());
            ((ListCheckBoxTree)tree).setCheckBoxVisible(path, !hideCheckbox);
        }

        if (dgi != null && pTristateCheckBox instanceof TristateCheckBoxWithButtonModeSupport)
        {
            ((TristateCheckBoxWithButtonModeSupport)pTristateCheckBox).setButtonModeEnabled(dgi.isTriggeringSupported());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.swing.tree.TreeTableTreeCellRenderer#formatText(io.opensphere.core.util.swing.tree.ButtonModelPayload,
     *      javax.swing.JLabel)
     */
    @Override
    public void formatText(ButtonModelPayload payload, JLabel label)
    {
        Color c = ourDefaultLabelForeground;
        DataGroupInfo dgi = getDataGroupInfoPayload(payload);
        if (dgi != null)
        {
            if (dgi.activationProperty().getValue() == ActivationState.ERROR)
            {
                c = Color.red;
            }

            if (DataLayerFilter.hasActiveLoadFilter(myToolbox, dgi))
            {
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
            }
        }
        label.setForeground(c);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.swing.tree.TreeTableTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
     *      java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
            int row, boolean hasFocus)
    {
        Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        myHoverButtonRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (tree instanceof CheckBoxTree)
        {
            TristateCheckBox checkBox = ((CheckBoxTree)tree).getCheckBox();
            if (((CheckBoxTree)tree).isCheckBoxEnabled())
            {
                if (checkBox instanceof TristateCheckBoxWithButtonModeSupport)
                {
                    if (((TristateCheckBoxWithButtonModeSupport)checkBox).isButtonModeEnabled())
                    {
                        checkBox.setIcon(ourCreateInstanceIcon);
                        checkBox.setSelectedIcon(null);
                    }
                    else if (checkBox.isMixed())
                    {
                        checkBox.setIcon(ourSliderMixedIcon);
                        checkBox.setSelectedIcon(null);
                    }
                    else
                    {
                        checkBox.setIcon(ourSliderOffIcon);
                        checkBox.setSelectedIcon(ourSliderOnIcon);
                    }
                }
                else
                {
                    if (checkBox.isMixed())
                    {
                        checkBox.setIcon(ourSliderMixedIcon);
                        checkBox.setSelectedIcon(null);
                    }
                    else
                    {
                        checkBox.setIcon(ourSliderOffIcon);
                        checkBox.setSelectedIcon(ourSliderOnIcon);
                    }
                }
            }
        }
        return component;
    }

    @Override
    public boolean isBusyLabelVisible(ButtonModelPayload payload)
    {
        boolean visible = false;
        DataGroupInfo dgi = getDataGroupInfoPayload(payload);
        if (dgi != null)
        {
            visible = dgi.activationProperty().isActivatingOrDeactivating();
        }
        return visible;
    }

    @Override
    public boolean isShowCheckBox(ButtonModelPayload payload)
    {
        return true;
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
     * Adds the label to the panel.
     *
     * @param panel the panel
     * @param label the label
     */
    private void addLabel(Container panel, Component label)
    {
        panel.add(label);
        addComponentWidth(label);
    }

    /**
     * Gets the data group info payload.
     *
     * @param payload the payload
     * @return the data group info payload
     */
    private static DataGroupInfo getDataGroupInfoPayload(ButtonModelPayload payload)
    {
        DataGroupInfo dgi = null;
        if (payload != null && payload.getPayloadData() instanceof GroupByNodeUserObject)
        {
            dgi = ((GroupByNodeUserObject)payload.getPayloadData()).getDataGroupInfo();
        }
        return dgi;
    }
}
