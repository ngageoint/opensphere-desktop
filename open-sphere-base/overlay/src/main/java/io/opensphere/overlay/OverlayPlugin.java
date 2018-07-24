package io.opensphere.overlay;

import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.PluginProperty;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.overlay.arc.ArcTransformer;

/**
 * Main class for the overlay plug-in.
 */
public class OverlayPlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(OverlayPlugin.class);

    /** Transformer for the arc length feature. */
    private ArcTransformer myArcLengthTransformer;

    /** The manager for the background color of the globe. */
    @SuppressWarnings("unused")
    private GlobeBackgroundManager myBackgroundManager;

    /** The solid color background transformer. */
    private SolidBackgroundTransformer myColorBackgroundTransformer;

    /** The transformer for cursor position. */
    private CursorPositionTransformer myCursorPositionTransformer;

    /** The transformer for random dots. */
    private RandomDotTransformer myDotTransformer;

    /** The transformer for grid lines. */
    private GridTransformer myGridTransformer;

    /** Helper for listening to various events. */
    private OverlayListenerHelper myListenerHelper;

    /** The transformer for MGRS grids. */
    private MGRSTransformer myMGRSTransformer;

    /** Selection Box Controls. */
    private SelectionRegionControls mySelectionBoxControls;

    /** The transformer for the selection box. */
    private SelectionRegionTransformer mySelectionBoxTransformer;

    /** Handler for selection regions. */
    private SelectionRegionHandlerImpl mySelectionRegionHandler;

    /** The transformer for time span display. */
    private TimeDisplayTransformer myTimeDisplayTransformer;

    /** The tool box used by plugins to interact with the rest of the system. */
    private Toolbox myToolbox;

    /** The transformer for viewer position. */
    private ViewerPositionTransformer myViewerPositionTransformer;

    /** The transformer for the zoom box. */
    private SelectionRegionTransformer myZoomBoxTransformer;

    /** The zoom region controls. */
    private ZoomRegionControls myZoomRegionControls;

    @Override
    public void close()
    {
        myListenerHelper.close();
        mySelectionBoxControls.close(myToolbox.getUIRegistry(), myToolbox.getControlRegistry());
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return Arrays.asList(myCursorPositionTransformer, myViewerPositionTransformer, myGridTransformer, myDotTransformer,
                myTimeDisplayTransformer, myMGRSTransformer, myArcLengthTransformer, mySelectionBoxTransformer,
                myZoomBoxTransformer, myColorBackgroundTransformer);
    }

    @Override
    public void initialize(PluginLoaderData data, final Toolbox toolbox)
    {
        myToolbox = toolbox;
        SelectionModeController ctrl = new SelectionModeControllerImpl(toolbox);
        myArcLengthTransformer = new ArcTransformer(toolbox);
        mySelectionBoxTransformer = new SelectionRegionTransformer();
        mySelectionRegionHandler = new SelectionRegionHandlerImpl(mySelectionBoxTransformer,
                new SelectionRegionMenuProvider(toolbox.getUIRegistry().getContextActionManager()));
        mySelectionBoxControls = new SelectionRegionControls(myToolbox.getEventManager(), myToolbox.getMapManager(),
                myToolbox.getUnitsRegistry(), myToolbox.getUIRegistry(), mySelectionBoxTransformer, mySelectionRegionHandler,
                ctrl);
        mySelectionBoxControls.register(myToolbox.getControlRegistry());

        myZoomBoxTransformer = new SelectionRegionTransformer();
        myZoomRegionControls = new ZoomRegionControls(myToolbox.getMapManager(), myToolbox.getUnitsRegistry(),
                myZoomBoxTransformer);
        myZoomRegionControls.register(myToolbox.getControlRegistry());

        Preferences preferences = myToolbox.getPreferencesRegistry().getPreferences(OverlayPlugin.class);
        for (PluginProperty pluginProperty : data.getPluginProperty())
        {
            configureForProperty(data, preferences, pluginProperty);
        }

        myListenerHelper = new OverlayListenerHelper(toolbox, myArcLengthTransformer, myCursorPositionTransformer,
                myViewerPositionTransformer, myMGRSTransformer, myTimeDisplayTransformer);
        myListenerHelper.initialize();

        EventQueueUtilities.invokeLater(() -> PluginMenuBarHelper.initializeMenuBar(toolbox, myMGRSTransformer,
                myArcLengthTransformer, myDotTransformer));

        OverlayToolbox oToolbox = new OverlayToolboxImpl(toolbox, ctrl);
        myToolbox.getPluginToolboxRegistry().registerPluginToolbox(oToolbox);

        SpatialTemporalDetailsProvider stdp = new SpatialTemporalDetailsProvider(toolbox,
                SpatialTemporalDetailsProvider.DEFAULTS_TOPIC, myCursorPositionTransformer, myViewerPositionTransformer,
                myTimeDisplayTransformer);
        toolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(stdp);
    }

    /**
     * Configure the transformers for a particular property.
     *
     * @param data The configuration data.
     * @param preferences The preferences
     * @param pluginProperty The specific property.
     */
    private void configureForProperty(PluginLoaderData data, Preferences preferences, PluginProperty pluginProperty)
    {
        if (pluginProperty.getKey().equalsIgnoreCase("mousePosition"))
        {
            if (Boolean.parseBoolean(pluginProperty.getValue()))
            {
                myCursorPositionTransformer = new CursorPositionTransformer(myToolbox, preferences);
                myCursorPositionTransformer.enableMousePosition();
            }
        }
        else if (pluginProperty.getKey().equalsIgnoreCase("viewerPosition"))
        {
            if (Boolean.parseBoolean(pluginProperty.getValue()))
            {
                myViewerPositionTransformer = new ViewerPositionTransformer(myToolbox, preferences);
                myViewerPositionTransformer.enableViewerPosition();
            }
        }
        else if (pluginProperty.getKey().equalsIgnoreCase("colorBackground"))
        {
            if (Boolean.parseBoolean(pluginProperty.getValue()))
            {
                // Create colorBackgroundTransformer
                myColorBackgroundTransformer = new SolidBackgroundTransformer(myToolbox);
                myBackgroundManager = new GlobeBackgroundManager(myToolbox.getPreferencesRegistry(), myToolbox.getUIRegistry(),
                        myColorBackgroundTransformer);
            }
        }
        else if (pluginProperty.getKey().equalsIgnoreCase("randomDots"))
        {
            StringTokenizer tok = new StringTokenizer(pluginProperty.getValue(), ",");
            int count = Integer.parseInt(tok.nextToken());
            if (count > 0)
            {
                int sets = Math.max(1, Integer.parseInt(tok.nextToken()));
                myDotTransformer = new RandomDotTransformer();
                myDotTransformer.setRandomDotCount(count, sets);
            }
        }
        else if (pluginProperty.getKey().equalsIgnoreCase("latitudeLongitudeLines"))
        {
            if (Boolean.parseBoolean(pluginProperty.getValue()))
            {
                getGridTransformer().initializeMenus(myToolbox);
            }
        }
        else if (pluginProperty.getKey().equalsIgnoreCase("mgrsgrid"))
        {
            if (Boolean.parseBoolean(pluginProperty.getValue()))
            {
                myMGRSTransformer = new MGRSTransformer();
            }
        }
        else if (pluginProperty.getKey().equalsIgnoreCase("Denver"))
        {
            boolean denver = Boolean.parseBoolean(pluginProperty.getValue());
            getGridTransformer().setDenver(denver);
        }
        else if (pluginProperty.getKey().equalsIgnoreCase("timeDisplay"))
        {
            if (Boolean.parseBoolean(pluginProperty.getValue()))
            {
                myTimeDisplayTransformer = new TimeDisplayTransformer(myToolbox, preferences);
                myTimeDisplayTransformer.enableTimeDisplay();
            }
        }
        else
        {
            LOGGER.warn("Unexpected plugin property for plugin [" + data.getId() + "]: " + pluginProperty.getKey());
        }
    }

    /**
     * Get the grid transformer.
     *
     * @return The transformer.
     */
    private GridTransformer getGridTransformer()
    {
        if (myGridTransformer == null)
        {
            myGridTransformer = new GridTransformer(myToolbox.getUIRegistry().getMainFrameProvider());
        }
        return myGridTransformer;
    }
}
