package io.opensphere.geopackage.mantle;

import java.awt.Color;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.util.Constants;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDeletableDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultTileLevelController;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Creates the {@link DataGroupInfo} and {@link DataTypeInfo} for the layers
 * contained in a geopackage file.
 */
public class DataGroupBuilder
{
    /**
     * The data group activation listener.
     */
    private final GeoPackageLayerActivationHandler myActivationListener;

    /**
     * Builds the {@link DataTypeInfo} for feature layers.
     */
    private final FeatureDataTypeBuilder myFeatureBuilder;

    /**
     * Used to manage zorders of the layers.
     */
    private final OrderManager myOrderManager;

    /**
     * Order manager used for the terrain tiles.
     */
    private final OrderManager myTerrainOrderManager;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new data group builder.
     *
     * @param toolbox The system toolbox.
     * @param tileListener The listener wanting notification when a geopackage
     *            tile layer is activated.
     */
    public DataGroupBuilder(Toolbox toolbox, LayerActivationListener tileListener)
    {
        myToolbox = toolbox;
        myFeatureBuilder = new FeatureDataTypeBuilder(MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController(),
                myToolbox.getOrderManagerRegistry(), myToolbox.getMapManager());
        myOrderManager = toolbox.getOrderManagerRegistry().getOrderManager(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY);
        myTerrainOrderManager = toolbox.getOrderManagerRegistry().getOrderManager(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                DefaultOrderCategory.EARTH_ELEVATION_CATEGORY);
        myActivationListener = new GeoPackageLayerActivationHandler(myToolbox.getEventManager(), tileListener, myFeatureBuilder);
    }

    /**
     * Stops listening for visibility changes, so the tile listener will not get
     * any notifications when visibility changes.
     */
    public void close()
    {
        myActivationListener.close();
    }

    /**
     * Creates a {@link DataTypeInfo} that represents a specific layer within
     * the geopackage file.
     *
     * @param layer The layer to create the type for.
     * @param layerId The id of the layer.
     * @return The layer's {@link DataTypeInfo}.
     */
    public DataTypeInfo createDataType(GeoPackageLayer layer, String layerId)
    {
        DefaultDataTypeInfo dataType = new GeoPackageDataTypeInfo(myToolbox, layer, layerId + layer.getLayerType());

        if (layer instanceof GeoPackageFeatureLayer)
        {
            myFeatureBuilder.buildDataType((GeoPackageFeatureLayer)layer, dataType, layerId);
            dataType.getBasicVisualizationInfo().setSupportedLoadsToTypes(New.<LoadsTo>list(LoadsTo.STATIC, LoadsTo.BASE));
            dataType.getBasicVisualizationInfo().setLoadsTo(LoadsTo.TIMELINE, this);
            dataType.getBasicVisualizationInfo().setTypeColor(Color.BLUE, this);
        }
        else if (layer instanceof GeoPackageTileLayer)
        {
            GeoPackageTileLayer tileLayer = (GeoPackageTileLayer)layer;
            DefaultOrderParticipantKey orderKey = null;

            MapVisualizationType visType = MapVisualizationType.IMAGE_TILE;
            int zOrder = ZOrderRenderProperties.TOP_Z;
            if (tileLayer.getExtensions().containsKey(Constants.TERRAIN_EXTENSION))
            {
                orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                        DefaultOrderCategory.EARTH_ELEVATION_CATEGORY, layerId);
                visType = MapVisualizationType.TERRAIN_TILE;
                zOrder = myTerrainOrderManager.activateParticipant(orderKey);
            }
            else
            {
                orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                        DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY, layerId);
                zOrder = myOrderManager.activateParticipant(orderKey);
            }

            dataType.setOrderKey(orderKey);
            TileRenderProperties props = new DefaultTileRenderProperties(zOrder, true, false);

            float opacity = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeInfoPreferenceAssistant()
                    .getOpacityPreference(dataType.getTypeKey(), ColorUtilities.COLOR_COMPONENT_MAX_VALUE)
                    / (float)ColorUtilities.COLOR_COMPONENT_MAX_VALUE;
            props.setOpacity(opacity);

            DefaultMapTileVisualizationInfo mapVisInfo = new DefaultMapTileVisualizationInfo(visType, props, true);
            mapVisInfo.setTileLevelController(new DefaultTileLevelController());
            dataType.setMapVisualizationInfo(mapVisInfo);
            dataType.getBasicVisualizationInfo().setLoadsTo(LoadsTo.BASE, this);
        }

        return dataType;
    }

    /**
     * Creates a {@link DataGroupInfo} that represents a specific layer within
     * the geopackage file.
     *
     * @param layer The layer to create the group for.
     * @param layerId The id of the layer.
     * @return The layer's {@link DataGroupInfo}.
     */
    public DataGroupInfo createLayerGroup(GeoPackageLayer layer, String layerId)
    {
        DefaultDataGroupInfo dataGroup = new DefaultDataGroupInfo(false, myToolbox, "GeoPackage", layerId);
        dataGroup.setDisplayName(layer.getName(), this);
        dataGroup.activationProperty().addListener(myActivationListener);

        return dataGroup;
    }

    /**
     * Creates a group representing the geopackage file.
     *
     * @param layer Any layer within the file.
     * @param packageLayerId The id of the geopackage file.
     * @param deleter The class to delete the imported data when the user wants
     *            this {@link DataGroupInfo} deleted.
     * @return The group for the file.
     */
    public DataGroupInfo createPackageGroup(GeoPackageLayer layer, String packageLayerId, GeoPackageDeleter deleter)
    {
        DefaultDeletableDataGroupInfo dataGroup = new DefaultDeletableDataGroupInfo(false, myToolbox, "GeoPackage",
                packageLayerId);
        dataGroup.setAssistant(deleter);
        dataGroup.setDisplayName(layer.getPackageName(), this);
        dataGroup.activationProperty().addListener(myActivationListener);

        return dataGroup;
    }

    /**
     * Gets the activation listener.
     *
     * @return The activation listener.
     */
    protected GeoPackageLayerActivationHandler getActivationListener()
    {
        return myActivationListener;
    }
}
