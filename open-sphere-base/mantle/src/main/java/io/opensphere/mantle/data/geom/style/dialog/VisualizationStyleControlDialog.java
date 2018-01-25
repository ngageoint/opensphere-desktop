package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.tools.Tool;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.FramePreferencesMonitor;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class VisualizationStyleControlDialog.
 */
public class VisualizationStyleControlDialog extends JDialog
{
    /** The dialog title. */
    public static final String TITLE = "Styles";

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The Data type tree panel. */
    private final VisualizationStyleDataTypePanel myFeatureDataTypePanel;

    /** The my show style event listener. */
    private final EventListener<ShowTypeVisualizationStyleEvent> myShowStyleEventListener;

    /** The Tabbed pane. */
    private final JTabbedPane myTabbedPane;

    /** The Tile data type tree panel. */
    private final VisualizationStyleDataTypePanel myTileDataTypePanel;

    /** The Heatmap data type tree panel. */
    private final VisualizationStyleDataTypePanel myHeatmapDataTypePanel;

    /** The Visualization style options provider. */
    private final VisualizationStyleOptionsProvider myVisualizationStyleOptionsProvider;

    /** The main window prefs monitor. */
    private final FramePreferencesMonitor myWindowPrefsMonitor;

    /**
     * Instantiates a new visualization style control dialog.
     *
     * @param owner the owner
     * @param tb the {@link Tool}
     */
    public VisualizationStyleControlDialog(Frame owner, Toolbox tb)
    {
        super(owner);
        Rectangle bounds = owner.getBounds();
        int xLoc = bounds.x + bounds.width / 2 - 910 / 2;
        int yLoc = bounds.y + bounds.height / 2 - 600 / 2;
        myWindowPrefsMonitor = new FramePreferencesMonitor(tb.getPreferencesRegistry(), "VisualizationStyleMananger", this,
                new Rectangle(xLoc, yLoc, 910, 600));
        setTitle(TITLE);
        setIconImage(owner.getIconImage());
        setMinimumSize(new Dimension(910, 600));
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        StyleManagerController controller = (StyleManagerController)MantleToolboxUtils.getMantleToolbox(tb)
                .getVisualizationStyleController();

        myFeatureDataTypePanel = new VisualizationStyleDataTypePanel(tb, controller, VisualizationStyleGroup.FEATURES);
        myTileDataTypePanel = new VisualizationStyleDataTypePanel(tb, controller, VisualizationStyleGroup.TILES);
        myHeatmapDataTypePanel = new VisualizationStyleDataTypePanel(tb, controller, VisualizationStyleGroup.HEATMAPS);

        myTabbedPane = new JTabbedPane();
        myTabbedPane.insertTab("Features", IconUtil.getColorizedIcon("/images/cog-feature.png", Color.WHITE, 14),
                myFeatureDataTypePanel, null, 0);
        myTabbedPane.insertTab("Tiles", IconUtil.getColorizedIcon("/images/cog-tile.png", Color.WHITE, 14), myTileDataTypePanel,
                null, 1);
        myTabbedPane.insertTab("Heatmaps", IconUtil.getColorizedIcon("/images/cog-heatmap.png", Color.WHITE, 14),
                myHeatmapDataTypePanel, null, 2);

        getContentPane().add(myTabbedPane);

        ThreadUtilities.runBackground(controller::initializeRegistryFromConfig);

        myShowStyleEventListener = event -> handleShowTypeVisualizationStyleEvent(event);
        tb.getEventManager().subscribe(ShowTypeVisualizationStyleEvent.class, myShowStyleEventListener);
        myVisualizationStyleOptionsProvider = new VisualizationStyleOptionsProvider(controller, tb.getPreferencesRegistry());
        tb.getUIRegistry().getOptionsRegistry().addOptionsProvider(myVisualizationStyleOptionsProvider);
    }

    /**
     * Gets the window prefs monitor.
     *
     * @return the window prefs monitor
     */
    public FramePreferencesMonitor getWindowPrefsMonitor()
    {
        return myWindowPrefsMonitor;
    }

    /**
     * Handle show type visualization style event.
     *
     * @param event the event
     */
    private void handleShowTypeVisualizationStyleEvent(final ShowTypeVisualizationStyleEvent event)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                DataTypeInfo dti = event.getType();
                if (dti != null && dti.getMapVisualizationInfo() != null
                        && dti.getMapVisualizationInfo().getVisualizationType() != null)
                {
                    if (dti.getMapVisualizationInfo().getVisualizationType().isMapDataElementType())
                    {
                        // Feature
                        myTabbedPane.setSelectedIndex(0);
                        if (myFeatureDataTypePanel.switchToDataType(event))
                        {
                            setVisible(true);
                        }
                    }
                    else if (dti.getMapVisualizationInfo().getVisualizationType().isImageTileType()
                            || dti.getMapVisualizationInfo().getVisualizationType().isImageType())
                    {
                        // Tile
                        myTabbedPane.setSelectedIndex(1);
                        if (myTileDataTypePanel.switchToDataType(event))
                        {
                            setVisible(true);
                        }
                    }
                    else if (dti.getMapVisualizationInfo().getVisualizationType().isHeatmapType())
                    {
                        // Heatmap
                        myTabbedPane.setSelectedIndex(2);
                        if (myHeatmapDataTypePanel.switchToDataType(event))
                        {
                            setVisible(true);
                        }
                    }
                }
            }
        });
    }
}
