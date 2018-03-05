package io.opensphere.analysis.heatmap;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import io.opensphere.analysis.heatmap.DataRegistryHelper.HeatmapImageInfo;
import io.opensphere.core.Notify;
import io.opensphere.core.Toolbox;
import io.opensphere.core.export.ExportUtilities;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;
import io.opensphere.mantle.data.tile.InterpolatedTileVisualizationSupport;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Does all the heat map things. */
public final class HeatmapController implements VisualizationStyleParameterChangeListener, HeatmapRecreator
{
    /** The menu text. */
    public static final String MENU_TEXT = "Create Heatmap...";

    /** The instance. */
    private static HeatmapController ourInstance;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The mantle controller. */
    private final HeatmapMantleController myMantleController;

    /** The data registry helper. */
    private final DataRegistryHelper myRegistryHelper;

    /** The creator instance used to generate new heatmaps. */
    private final HeatmapCreator myHeatmapCreator;

    /** A set of styles associated with heatmaps, to preserve references. */
    private final Set<HeatmapVisualizationStyle> myStyles;

    /** Map of data type key to heatmap. */
    private final Map<String, HeatmapImageInfo> myTypeKeyToHeatmap = Collections.synchronizedMap(New.map());

    /**
     * Handles when the user turns off custom styles.
     */
    @SuppressWarnings("unused")
    private final HeatmapDefaultStyleHandler myDefaultStyleHandler;

    /**
     * Constructs the instance of this class.
     *
     * @param toolbox the toolbox
     * @param mantleController the mantle controller
     * @return the instance
     */
    public static synchronized HeatmapController initInstance(Toolbox toolbox, HeatmapMantleController mantleController)
    {
        if (ourInstance == null)
        {
            ourInstance = new HeatmapController(toolbox, mantleController);
        }
        return ourInstance;
    }

    /**
     * Gets the instance of this class.
     *
     * @return the instance
     */
    public static synchronized HeatmapController getInstance()
    {
        return ourInstance;
    }

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param mantleController the mantle controller
     */
    private HeatmapController(Toolbox toolbox, HeatmapMantleController mantleController)
    {
        myToolbox = toolbox;
        myMantleController = mantleController;
        myRegistryHelper = new DataRegistryHelper(toolbox.getDataRegistry());
        myHeatmapCreator = new HeatmapCreator();
        myStyles = New.set();
        myDefaultStyleHandler = new HeatmapDefaultStyleHandler(this, MantleToolboxUtils.getMantleToolbox(toolbox),
                myRegistryHelper);

        myMantleController.setActivationListener(new AbstractActivationListener()
        {
            @Override
            public void commit(DataGroupActivationProperty property, ActivationState state, PhasedTaskCanceller canceller)
            {
                handleActivationChange(property, state);
            }
        });
    }

    /**
     * Creates a heat map for the data type.
     *
     * @param dataType the data type
     */
    public void create(DataTypeInfo dataType)
    {
        create(dataType.getDisplayName(), () -> myMantleController.getGeometries(Collections.singleton(dataType), null));
    }

    /**
     * Creates a heat map for the query box.
     *
     * @param queryGeom the query box
     */
    public void create(Geometry queryGeom)
    {
        create("Query Box", () -> myMantleController.getGeometries(null, queryGeom));
    }

    /**
     * Creates a heat map for the data element IDs.
     *
     * @param ids the data element IDs
     * @param dataTypeKey the data type key
     */
    public void create(List<Long> ids, String dataTypeKey)
    {
        create("Features", () -> myMantleController.getGeometries(ids, dataTypeKey));
    }

    /**
     * Handles a change in the activation state of a heatmap group.
     *
     * @param property the property containing the group
     * @param state the activation state
     */
    void handleActivationChange(DataGroupActivationProperty property, ActivationState state)
    {
        DataTypeInfo dataType = property.getDataGroup().getMembers(false).iterator().next();
        if (state == ActivationState.ACTIVATING)
        {
            // If this is a reactivation, just add the image back
            HeatmapImageInfo imageInfo = myTypeKeyToHeatmap.get(dataType.getTypeKey());
            if (imageInfo != null)
            {
                myRegistryHelper.addImage(imageInfo);
            }
        }
        else if (state == ActivationState.DEACTIVATING)
        {
            myRegistryHelper.removeImage(dataType.getTypeKey());
        }
    }

    /**
     * Creates a heat map for the query box.
     *
     * @param baseLayerName the base layer name
     * @param geometrySupplier the geometry supplier
     */
    private void create(String baseLayerName, Supplier<Collection<GeometryInfo>> geometrySupplier)
    {
        String layerName = myMantleController.getLayerName(baseLayerName);
        HeatmapOptions options = new HeatmapOptions();
        options.setLayerName(layerName);
        options.setGradient((HeatmapGradients)HeatmapVisualizationStyle.DEFAULT_COLOR_PALETTE_PARAMETER.getValue());
        options.setIntensity(((Integer)HeatmapVisualizationStyle.DEFAULT_INTENSITY_PARAMETER.getValue()).intValue());
        options.setSize(((Integer)HeatmapVisualizationStyle.DEFAULT_SIZE_PARAMETER.getValue()).intValue());

        ThreadUtilities.runBackground(() ->
        {
            Collection<GeometryInfo> geometries = geometrySupplier.get();
            if (geometries.isEmpty())
            {
                Notify.warn("No features available to create heatmap.");
            }
            else if (geometries.size() == 1)
            {
                Notify.warn("More than one feature is required to create heatmap.");
            }
            else
            {
                create(geometries, options);
            }
        });
    }

    /**
     * Creates a heat map.
     *
     * @param geometries the geometries with data type keys
     * @param options the options
     */
    private void create(Collection<? extends GeometryInfo> geometries, HeatmapOptions options)
    {
        try (TaskActivity ta = TaskActivity.createActive("Creating heatmap"))
        {
            myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);
            Projection projection = myToolbox.getMapManager().getProjection().getSnapshot();
            DataTypeController dataTypeController = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class)
                    .getDataTypeController();

            HeatmapModel model = new HeatmapModel(geometries, projection, dataTypeController, myToolbox.getMapManager());
            // Add layer to mantle
            DataTypeInfo dataType = myMantleController.addLayer(options.getLayerName());
            ((DefaultDataTypeInfo)dataType).setBoundingBox(model.getBbox());

            ExtentAccumulator accumulator = new ExtentAccumulator();
            geometries.stream().forEach(g -> accumulator.add(g.getTimeSpan()));
            DefaultTimeExtents extent = new DefaultTimeExtents(accumulator.getExtent());
            ((DefaultDataTypeInfo)dataType).setTimeExtents(extent, dataType);

            VisualizationStyleRegistry registry = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry();
            HeatmapVisualizationStyle style = (HeatmapVisualizationStyle)registry
                    .getStyle(InterpolatedTileVisualizationSupport.class, dataType.getTypeKey(), true);
            options.setGradient(style.getColorPalette());
            options.setIntensity(style.getIntensity());
            options.setSize(style.getSize());

            style.addStyleParameterChangeListener(this);
            myStyles.add(style);

            ta.setLabelValue("Rendering heatmap");

            // Create the image
            BufferedImage image = myHeatmapCreator.createImage(model, options);
            HeatmapImageInfo imageInfo = new HeatmapImageInfo(image, model, dataType);

            // Add image to data registry
            myRegistryHelper.addImage(imageInfo);
            myTypeKeyToHeatmap.put(dataType.getTypeKey(), imageInfo);

            // Export to file
            if (options.isExport())
            {
                EventQueue.invokeLater(() ->
                {
                    export(imageInfo);
                    ta.setComplete(true);
                });
            }
        }
    }

    /**
     * Creates a heat map using the supplied model and options.
     *
     * @param typeKey the key associated with the datatype.
     * @param dataType the data type for which the heatmap will be recreated.
     * @param model the model associated with the heatmap.
     * @param options the options
     */
    private void recreate(String typeKey, DataTypeInfo dataType, HeatmapModel model, HeatmapOptions options)
    {
        try (TaskActivity ta = TaskActivity.createActive("Applying changes to heatmap"))
        {
            myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);

            ((DefaultDataTypeInfo)dataType).setBoundingBox(model.getBbox());

            BufferedImage image = myHeatmapCreator.createImage(model, options);
            HeatmapImageInfo imageInfo = new HeatmapImageInfo(image, model, dataType);

            // Add image to data registry
            myRegistryHelper.removeImage(typeKey);
            myRegistryHelper.addImage(imageInfo);
            myTypeKeyToHeatmap.put(dataType.getTypeKey(), imageInfo);

            // Export to file
            if (options.isExport())
            {
                EventQueue.invokeLater(() ->
                {
                    export(imageInfo);
                    ta.setComplete(true);
                });
            }
        }
    }

    /**
     * Given the layer id and style, recreates the heat map.
     *
     * @param dtiKey The layer id.
     * @param style The style for the heat map.
     * @param imageInfo The heat map image info to recreate.
     */
    @Override
    public void recreate(String dtiKey, HeatmapVisualizationStyle style, HeatmapImageInfo imageInfo)
    {
        // update the size, and adjust the bounding box / image size:
        imageInfo.getModel().resetBoundingBox();
        DataTypeInfo dataType = imageInfo.getDataType();

        HeatmapOptions options = new HeatmapOptions();
        options.setLayerName(dataType.getDisplayName());
        options.setGradient(style.getColorPalette());
        options.setIntensity(style.getIntensity());
        options.setSize(style.getSize());
        style.removeStyleParameterChangeListener(this);
        style.addStyleParameterChangeListener(this);

        ThreadUtilities.runBackground(() ->
        {
            recreate(dtiKey, dataType, imageInfo.getModel(), options);
        });
    }

    /**
     * Exports the image.
     *
     * @param imageInfo the image info
     */
    private void export(HeatmapImageInfo imageInfo)
    {
        HeatmapExporter exporter = new HeatmapExporter();
        exporter.setToolbox(myToolbox);
        exporter.setImageInfo(imageInfo);
        ExportUtilities.export(myToolbox.getUIRegistry().getMainFrameProvider().get(), myToolbox.getPreferencesRegistry(),
                exporter);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener#styleParametersChanged(io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent)
     */
    @Override
    public void styleParametersChanged(VisualizationStyleParameterChangeEvent event)
    {
        String dtiKey = event.getDTIKey();
        HeatmapVisualizationStyle style = (HeatmapVisualizationStyle)event.getStyle();

        recreate(dtiKey, style, myRegistryHelper.queryImage(dtiKey));
    }
}
