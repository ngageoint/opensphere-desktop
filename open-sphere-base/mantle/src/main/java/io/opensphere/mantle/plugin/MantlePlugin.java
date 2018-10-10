package io.opensphere.mantle.plugin;

import java.util.Properties;

import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.util.event.EventCoalescer;
import io.opensphere.core.util.property.PluginPropertyUtils;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.ColumnTypeDetector;
import io.opensphere.mantle.data.element.event.DataElementAltitudeChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementColorChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementHighlightChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementLOBVsibilityChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementMapGeometrySupportChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementMetaDataValueChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementSelectionChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementVisibilityChangeEvent;
import io.opensphere.mantle.data.element.event.consolidators.DataElementAltitudeChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementColorChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementHighlightChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementLOBVisibilityChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementMapGeometrySupportChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementMetaDataValueChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementSelectionChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementVisibilityChangeConsolidator;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.geom.style.impl.BlendBlackTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.BlendColorTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.BlendWhitenTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.FilterDarkenBrightenTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.FilterGuiModTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.FilterSharpenTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.FilterSmoothTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.FilterSquareToothFaderTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.LaplacianEdgeDetectionTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.NormalTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PointFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PolygonFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PolylineFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PrewittEdgeDetectionTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.SobelEdgeDetectionTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.StyleUtils;
import io.opensphere.mantle.data.geom.style.labelcontroller.LabelHoverController;
import io.opensphere.mantle.data.geom.style.tilecontroller.TileStyleTransformController;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMajorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMinorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.HeadingKey;
import io.opensphere.mantle.data.impl.specialkey.SpeedKey;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;
import io.opensphere.mantle.toolbox.MantleToolboxImpl;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * This plugin installs the Mantle support classes into the runtime for use by
 * other plugins.
 */
public class MantlePlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MantlePlugin.class);

    /** The data element altitude change coalescer. */
    private EventCoalescer<DataElementAltitudeChangeEvent> myDataElementAltitudeChangeCoalescer;

    /** The data element color change coalescer. */
    private EventCoalescer<DataElementColorChangeEvent> myDataElementColorChangeCoalescer;

    /** The data element highlight change coalescer. */
    private EventCoalescer<DataElementHighlightChangeEvent> myDataElementHighlightChangeCoalescer;

    /** The data element lob visible change coalescer. */
    private EventCoalescer<DataElementLOBVsibilityChangeEvent> myDataElementLOBVisibleChangeCoalescer;

    /** The data element map geometry support change coalescer. */
    private EventCoalescer<DataElementMapGeometrySupportChangeEvent> myDataElementMapGeometrySupportChangeCoalescer;

    /** The data element meta data value change coalescer. */
    private EventCoalescer<DataElementMetaDataValueChangeEvent> myDataElementMetaDataChangeCoalescer;

    /** The data element selection change coalescer. */
    private EventCoalescer<DataElementSelectionChangeEvent> myDataElementSelectionChangeCoalescer;

    /** The data element visibility change coalescer. */
    private EventCoalescer<DataElementVisibilityChangeEvent> myDataElementVisibilityChangeCoalescer;

    /** The Tile style transform controller. */
    @SuppressWarnings("unused")
    private TileStyleTransformController myTileStyleTransformController;

    /** The controller used to detect hover events when labels are disabled. */
    @SuppressWarnings("unused")
    private LabelHoverController myLabelHoverController;

    /** The my toolbox. */
    private Toolbox myToolbox;

    /**
     * Instantiates a new mantle plugin.
     */
    public MantlePlugin()
    {
    }

    @Override
    public void close()
    {
        myDataElementHighlightChangeCoalescer.stop();
        myDataElementVisibilityChangeCoalescer.stop();
        myDataElementMetaDataChangeCoalescer.stop();
        myDataElementAltitudeChangeCoalescer.stop();
        myDataElementColorChangeCoalescer.stop();
        myDataElementLOBVisibleChangeCoalescer.stop();
        myDataElementSelectionChangeCoalescer.stop();
        myDataElementMapGeometrySupportChangeCoalescer.stop();

        MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler().uninstall(myToolbox);
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("INITIALIZE");
        }

        Properties pluginProperties = PluginPropertyUtils.convertToProperties(plugindata.getPluginProperty());
        MantleToolboxImpl mantleToolbox = new MantleToolboxImpl(myToolbox, pluginProperties);
        myToolbox.getPluginToolboxRegistry().registerPluginToolbox(mantleToolbox);

        ColumnTypeDetector columnTypeDetector = mantleToolbox.getColumnTypeDetector();
        columnTypeDetector.addSpecialColumnDetector(EllipseSemiMajorAxisKey.DEFAULT);
        columnTypeDetector.addSpecialColumnDetector(EllipseSemiMinorAxisKey.DEFAULT);
        columnTypeDetector.addSpecialColumnDetector(HeadingKey.DEFAULT);
        columnTypeDetector.addSpecialColumnDetector(SpeedKey.DEFAULT);

        createAndInstallEventCoalescers();

        // We need to create and install the vis styles before
        // we do the style control dialog.
        createInstallAndInitializeDefaultVisualizationStyles();

        myTileStyleTransformController = new TileStyleTransformController(toolbox);
        myLabelHoverController = new LabelHoverController(toolbox);

        EventQueueUtilities.runOnEDTAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                MantleMenuInit menuInit = new MantleMenuInit(myToolbox);
                menuInit.createAndInstallDataElementCacheSummaryMenuItem();
                menuInit.createAndInstallVisStyleControlDialog();
                menuInit.createAndInstallIconManagerMenuItem();
                menuInit.createAndInstallDynamicEnumDebugPrintMenuItem();
            }
        });
    }

    /**
     * Creates the and install event coalescers.
     */
    private void createAndInstallEventCoalescers()
    {
        myDataElementHighlightChangeCoalescer = new EventCoalescer<>(myToolbox.getEventManager(),
                DataElementHighlightChangeEvent.class, new DataElementHighlightChangeConsolidator());
        myDataElementHighlightChangeCoalescer.start();

        myDataElementVisibilityChangeCoalescer = new EventCoalescer<>(myToolbox.getEventManager(),
                DataElementVisibilityChangeEvent.class, new DataElementVisibilityChangeConsolidator());
        myDataElementVisibilityChangeCoalescer.start();

        myDataElementAltitudeChangeCoalescer = new EventCoalescer<>(myToolbox.getEventManager(),
                DataElementAltitudeChangeEvent.class, new DataElementAltitudeChangeConsolidator());
        myDataElementAltitudeChangeCoalescer.start();

        myDataElementColorChangeCoalescer = new EventCoalescer<>(myToolbox.getEventManager(),
                DataElementColorChangeEvent.class, new DataElementColorChangeConsolidator());
        myDataElementColorChangeCoalescer.start();

        myDataElementLOBVisibleChangeCoalescer = new EventCoalescer<>(
                myToolbox.getEventManager(), DataElementLOBVsibilityChangeEvent.class,
                new DataElementLOBVisibilityChangeConsolidator());
        myDataElementLOBVisibleChangeCoalescer.start();

        myDataElementSelectionChangeCoalescer = new EventCoalescer<>(myToolbox.getEventManager(),
                DataElementSelectionChangeEvent.class, new DataElementSelectionChangeConsolidator());
        myDataElementSelectionChangeCoalescer.start();

        myDataElementMapGeometrySupportChangeCoalescer = new EventCoalescer<>(
                myToolbox.getEventManager(), DataElementMapGeometrySupportChangeEvent.class,
                new DataElementMapGeometrySupportChangeConsolidator());
        myDataElementSelectionChangeCoalescer.start();

        myDataElementMetaDataChangeCoalescer = new EventCoalescer<>(
                myToolbox.getEventManager(), DataElementMetaDataValueChangeEvent.class,
                new DataElementMetaDataValueChangeConsolidator());
        myDataElementMetaDataChangeCoalescer.start();
    }

    /**
     * Creates, install and initialize default visualization styles.
     */
    private void createInstallAndInitializeDefaultVisualizationStyles()
    {
        VisualizationStyleRegistry vsr = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry();

        // Feature styles
        for (Class<? extends VisualizationStyle> c : StyleUtils.FEATURE_STYLES)
        {
            vsr.installStyle(c, this);
        }

        vsr.setDefaultStyle(MapLocationGeometrySupport.class, PointFeatureVisualizationStyle.class, this);
        vsr.setDefaultStyle(MapPolygonGeometrySupport.class, PolygonFeatureVisualizationStyle.class, this);
        vsr.setDefaultStyle(MapPolylineGeometrySupport.class, PolylineFeatureVisualizationStyle.class, this);

        // Tile styles
        vsr.installStyle(NormalTileVisualizationStyle.class, this);
        // vsr.installStyle(BlendAddTileVisualizationStyle.class, this);
        vsr.installStyle(BlendWhitenTileVisualizationStyle.class, this);
        vsr.installStyle(BlendBlackTileVisualizationStyle.class, this);
        vsr.installStyle(BlendColorTileVisualizationStyle.class, this);
        vsr.installStyle(FilterSquareToothFaderTileVisualizationStyle.class, this);
        vsr.installStyle(FilterDarkenBrightenTileVisualizationStyle.class, this);
        vsr.installStyle(FilterSharpenTileVisualizationStyle.class, this);
        vsr.installStyle(FilterSmoothTileVisualizationStyle.class, this);
        vsr.installStyle(FilterGuiModTileVisualizationStyle.class, this);
        vsr.installStyle(LaplacianEdgeDetectionTileVisualizationStyle.class, this);
        vsr.installStyle(PrewittEdgeDetectionTileVisualizationStyle.class, this);
        vsr.installStyle(SobelEdgeDetectionTileVisualizationStyle.class, this);

        vsr.setDefaultStyle(TileVisualizationSupport.class, NormalTileVisualizationStyle.class, this);
    }
}
