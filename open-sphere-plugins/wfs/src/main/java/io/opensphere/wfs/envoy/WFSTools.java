package io.opensphere.wfs.envoy;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.wfs.consumer.FeatureConsumerManager;
import io.opensphere.wfs.layer.WFSLayerColumnManager;
import io.opensphere.wfs.placenames.PlaceNameLayerManager;

/**
 * The Class WFSTools.
 */
public class WFSTools
{
    /** My download monitor. */
    private final WFSDownloadMonitor myDownloadMonitor;

    /** The feature consumer manager. */
    private final FeatureConsumerManager myFeatureConsumerManager;

    /** The WFS layer column filter manager. */
    private final WFSLayerColumnManager myWFSLayerColumnManager;

    /** Manager for place name layers. */
    private final PlaceNameLayerManager myPlaceNamesManager;

    /**
     * Instantiates a new holder for WFS tools.
     *
     * @param toolbox the core toolbox
     */
    public WFSTools(Toolbox toolbox)
    {
        myDownloadMonitor = new WFSDownloadMonitor(toolbox);
        myWFSLayerColumnManager = new WFSLayerColumnManager(toolbox);
        myFeatureConsumerManager = new FeatureConsumerManager(MantleToolboxUtils.getMantleToolbox(toolbox), toolbox.getTimeManager());
        myPlaceNamesManager = new PlaceNameLayerManager(toolbox);
    }

    /**
     * Gets the download monitor.
     *
     * @return the download monitor
     */
    public WFSDownloadMonitor getDownloadMonitor()
    {
        return myDownloadMonitor;
    }

    /**
     * Gets the feature consumer manager.
     *
     * @return the feature consumer manager
     */
    public FeatureConsumerManager getFeatureConsumerManager()
    {
        return myFeatureConsumerManager;
    }

    /**
     * Gets the layer column manager.
     *
     * @return the layer column manager
     */
    public WFSLayerColumnManager getLayerColumnManager()
    {
        return myWFSLayerColumnManager;
    }

    /**
     * Gets the place names manager.
     *
     * @return the place names manager
     */
    public PlaceNameLayerManager getPlaceNamesManager()
    {
        return myPlaceNamesManager;
    }
}
