package io.opensphere.controlpanels.layers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import com.jidesoft.swing.JideTabbedPane;

import io.opensphere.controlpanels.layers.activedata.ActiveDataPanel;
import io.opensphere.controlpanels.layers.availabledata.AvailableDataPanel;
import io.opensphere.controlpanels.layers.event.ShowAvailableDataEvent;
import io.opensphere.controlpanels.layers.event.ShowLayerSetManagerEvent;
import io.opensphere.controlpanels.layers.layerdetail.LayerDetailsCoordinator;
import io.opensphere.controlpanels.layers.layersets.LayerSetFrame;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.iconlegend.IconLegendRegistry;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.FramePreferencesMonitor;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.mantle.data.geom.style.dialog.VisualizationStyleControlDialog;

/**
 * The frame for user management of active data layers.
 */
public class LayersFrame extends AbstractInternalFrame
{
    /** The title of the window. */
    public static final String TITLE = "Layers";

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(LayersFrame.class);

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The active data panel. */
    private ActiveDataPanel myActiveDataPanel;

    /** The add data dialog. */
    private OptionDialog myAddDataDialog;

    /** The book mark frame listener. */
    private final transient EventListener<ShowLayerSetManagerEvent> myBMListener = new EventListener<ShowLayerSetManagerEvent>()
    {
        @Override
        public void notify(ShowLayerSetManagerEvent event)
        {
            EventQueueUtilities.runOnEDT(() -> myLayerSetFrame.setFrameVisible(true));
        }
    };

    /** The manager for changes to the frame position and size preferences. */
    @SuppressWarnings("unused")
    private final transient FramePreferencesMonitor myFramePrefsMonitor;

    /** The layer details coordinator. */
    private final transient LayerDetailsCoordinator myLayerDetailsCoordinator;

    /** The Timeline tabbed pane. */
    private JideTabbedPane myLayerManagerTabbedPane;

    /** The active layer book mark frame. */
    private transient LayerSetFrame myLayerSetFrame;

    /**
     * The listener for the event which requests that the available data dialog
     * be shown.
     */
    private final transient EventListener<ShowAvailableDataEvent> myShowAvailableDataListener = new EventListener<ShowAvailableDataEvent>()
    {
        @Override
        public void notify(ShowAvailableDataEvent event)
        {
            EventQueueUtilities.runOnEDT(() -> myAddDataDialog.setVisible(true));
        }
    };

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /**
     * Instantiates a new timeline setup and control.
     *
     * @param toolbox the toolbox
     */
    public LayersFrame(Toolbox toolbox)
    {
        super();
        setTitle(TITLE);
        setOpaque(false);
        setIconifiable(false);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        myFramePrefsMonitor = new FramePreferencesMonitor(toolbox.getPreferencesRegistry(), TITLE, this, new Rectangle(300, 580));

        myToolbox = toolbox;

        myLayerDetailsCoordinator = new LayerDetailsCoordinator(myToolbox);

        myToolbox.getEventManager().subscribe(ShowAvailableDataEvent.class, myShowAvailableDataListener);
        myToolbox.getEventManager().subscribe(ShowLayerSetManagerEvent.class, myBMListener);

        addLegendIcons(toolbox.getUIRegistry().getIconLegendRegistry());

        initGuiElements();
    }

    /** Set up the elements which make up the contents of this frame. */
    protected final void initGuiElements()
    {
        myActiveDataPanel = new ActiveDataPanel(myToolbox, myLayerDetailsCoordinator);
        myActiveDataPanel.initGuiElements();

        myLayerSetFrame = new LayerSetFrame(myToolbox);

        AvailableDataPanel addDataPanel = new AvailableDataPanel(myToolbox, myLayerDetailsCoordinator);
        addDataPanel.initGuiElements();

        myAddDataDialog = new OptionDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), addDataPanel, "Add Data");
        myAddDataDialog.setModal(false);
        myAddDataDialog.setButtonLabels(Collections.singletonList(ButtonPanel.OK));
        myAddDataDialog.build(775, getHeight() - 4);
        myAddDataDialog.setMinimumSize(new Dimension(100, 100));
        ComponentUtilities.setLocationAdjacentTo(myAddDataDialog, this, myToolbox.getUIRegistry().getMainFrameProvider().get());

        myLayerManagerTabbedPane = new JideTabbedPane();
        myLayerManagerTabbedPane.setUseDefaultShowIconsOnTab(false);
        myLayerManagerTabbedPane.setShowIconsOnTab(true);
        myLayerManagerTabbedPane.setOpaque(false);
        myLayerManagerTabbedPane.setBackground(new Color(0, 0, 0, 0));
        myLayerManagerTabbedPane.setTabShape(JideTabbedPane.SHAPE_ECLIPSE3X);
        myLayerManagerTabbedPane.setTabPlacement(SwingConstants.TOP);
        myLayerManagerTabbedPane.addTab("Active", null, myActiveDataPanel, null);
        myLayerManagerTabbedPane.setSelectedIndex(0);

        // Add this to the shared component registry.
        myToolbox.getUIRegistry().getSharedComponentRegistry().registerComponent("ControlPanels.LayerManager.tabbedPane",
                myLayerManagerTabbedPane);

        AbstractHUDPanel mainPanel = new AbstractHUDPanel(myToolbox.getPreferencesRegistry());
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(myLayerManagerTabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    /**
     * Adds the legend icons.
     *
     * @param iconLegendRegistry the icon legend registry
     */
    private void addLegendIcons(IconLegendRegistry iconLegendRegistry)
    {
        try
        {
            Icon clockIcon = IconUtil.getIcon("/images/small_white_clock2.png");
            iconLegendRegistry.addIconToLegend(clockIcon, "Timeline Layer", "Timeline layers display data with time attributes. "
                    + "The timeline tool can be use to show how this data changes over time.");

            Icon extentsIcon = IconUtil.getNormalIcon("/images/arrows-collapse.png");
            iconLegendRegistry.addIconToLegend(extentsIcon, "Layer Extents",
                    "The layer extents is the bounding time span where data exists for each layer. Clicking the Layer Extents "
                            + "button will set the the earliest start date and latest end date for one or a group of selected data layers.");

            Icon tagIcon = IconUtil.getNormalIcon(IconType.TAG);
            iconLegendRegistry.addIconToLegend(tagIcon, "Layer Tags",
                    "Layer tags are key words used to identify layers. Users can add custom tags to identify a layer or group of layers. "
                            + "To view layers by tag, select the 'Tag' Group By type from the active layers tree.");

            Icon tileVisStyleIcon = IconUtil.getNormalIcon("/images/cog-tile.png");
            iconLegendRegistry.addIconToLegend(tileVisStyleIcon,
                    StringUtilities.concat(VisualizationStyleControlDialog.TITLE, " (Tiles)"),
                    StringUtilities.concat("Opens the ", VisualizationStyleControlDialog.TITLE,
                            " dialog and automatically selects the 'Tiles' tab. ",
                            "Users are provided with many styles to change how tiles appear on the map."));

            Icon featureVisStyleIcon = IconUtil.getNormalIcon("/images/cog-feature.png");
            iconLegendRegistry.addIconToLegend(featureVisStyleIcon,
                    StringUtilities.concat(VisualizationStyleControlDialog.TITLE, " (Features)"),
                    StringUtilities.concat("Opens the ", VisualizationStyleControlDialog.TITLE,
                            " dialog and automatically selects the 'Features' tab. ",
                            "Users are provided with many styles to change how features appear on the map."));

            Icon reloadIcon = IconUtil.getNormalIcon(IconType.RELOAD);
            iconLegendRegistry.addIconToLegend(reloadIcon, "Reload",
                    "The reload button provides users with a way to reload file/url based data without having to "
                            + "remove the data and reimport it using the import wizard.");

            Icon layerSetIcon = IconUtil.getNormalIcon(IconType.STAR);
            iconLegendRegistry.addIconToLegend(layerSetIcon, "Layer Set",
                    "A layer set is the set of layers currently displayed in the 'Active' data layer tree. "
                            + "To save a set, either click the 'Layer Set' button or use the dropdown.");

            ImageIcon screenCaptureIcon = IconUtil.getNormalIcon("/images/screencapture.png");
            iconLegendRegistry.addIconToLegend(screenCaptureIcon, "Screen Capture",
                    "Opens the 'Screen Capture' dialog. Click the 'Snapshot' menu and select 'New' to create a screen capture. ");

            Icon popoutIcon = new ImageIcon(ImageIO.read(LayersFrame.class.getResource("/images/new-tab.png")));
            iconLegendRegistry.addIconToLegend(popoutIcon, "Open Layer in New Window",
                    "Opens a new Active Layer window showing only the clicked on layer and the layers below the clicked on layer.");

            Icon addDataIcon = IconUtil.getColorizedIcon(IconType.PLUS, Color.GREEN);
            iconLegendRegistry.addIconToLegend(addDataIcon, "Add Data",
                    "Opens the 'Add Data' window. Users can import, activate/deactivate, edit, and remove layers from this window. "
                    + "Use the dropdown menu to directly select a type of data to import.");

            Icon layersIcon = IconUtil.getNormalIcon(IconType.STACK);
            iconLegendRegistry.addIconToLegend(layersIcon, "Active Layers",
                    "Opens the 'Layers' window, where users can interact with Layers, My Places, Areas, etc.");

            Icon deleteIcon = IconUtil.getNormalIcon(IconType.DELETE);
            iconLegendRegistry.addIconToLegend(deleteIcon, "Delete My Place",
                    "Deletes all currently selected My Places.");

            Icon playIcon = IconUtil.getColorizedIcon(IconType.PLAY, Color.GREEN);
            iconLegendRegistry.addIconToLegend(playIcon, "Play Streaming Layers",
                    "Starts the streaming application for all applicable layers.");

            Icon pauseIcon = IconUtil.getColorizedIcon(IconType.PAUSE, Color.YELLOW);
            iconLegendRegistry.addIconToLegend(pauseIcon, "Pause Streaming Layers",
                    "Pauses the streams for all layers that are currently streaming.");

            Icon stopIcon = IconUtil.getColorizedIcon(IconType.STOP, Color.RED);
            iconLegendRegistry.addIconToLegend(stopIcon, "Stop Streaming Layers",
                    "Stops the streams for all layers that have enabled streams.");
        }
        catch (IOException e)
        {
            LOGGER.error(e);
        }
    }
}
