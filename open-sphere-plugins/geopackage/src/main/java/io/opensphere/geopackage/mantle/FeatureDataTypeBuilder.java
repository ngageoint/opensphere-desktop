package io.opensphere.geopackage.mantle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.opensphere.core.MapManager;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;

/**
 * Builds and adds a {@link DataTypeInfo} to the system for a given geopackage
 * feature layer. This class also will create {@link DataElement} for the data
 * contained in the feature layer and add the elements to the system.
 */
public class FeatureDataTypeBuilder implements LayerActivationListener
{
    /**
     * Creates a data element per row in geopacakge feature layer.
     */
    private final DataElementPopulator myDataElementPopulator = new DataElementPopulator();

    /**
     * The controller used to add the {@link DataTypeInfo} and
     * {@link DataElement} to the system.
     */
    private final DataTypeController myDataTypeController;

    /**
     * Used to get the projection in order to set the location of the layer so
     * that a user can double click on it and fly to it.
     */
    private final MapManager myMapManager;

    /**
     * Builds the metadata info for the DataType.
     */
    private final MetaDataInfoBuilder myMetaDataInfoBuilder = new MetaDataInfoBuilder();

    /**
     * Used to get the zorder for the elements.
     */
    private final OrderManager myOrderManager;

    /**
     * Constructs a new data type builder.
     *
     * @param dataTypeController The controller used to add the
     *            {@link DataTypeInfo} and {@link DataElement} to the system.
     * @param orderManagerRegistry Contains order managers used for zorder.
     * @param mapManager Used to get the projection in order to set the location
     *            of the layer so that a user can double click on it and fly to
     *            it.
     */
    public FeatureDataTypeBuilder(DataTypeController dataTypeController, OrderManagerRegistry orderManagerRegistry,
            MapManager mapManager)
    {
        myDataTypeController = dataTypeController;
        myOrderManager = orderManagerRegistry.getOrderManager(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                DefaultOrderCategory.FEATURE_CATEGORY);
        myMapManager = mapManager;
    }

    /**
     * Configures the passed in dataType for a feature {@link DataTypeInfo}.
     * Also adds data from layer to the map.
     *
     * @param layer Contains the data to add to the map.
     * @param dataType The data type to configure.
     * @param layerId The id of the layer.
     */
    public void buildDataType(GeoPackageFeatureLayer layer, DefaultDataTypeInfo dataType, String layerId)
    {
        DefaultOrderParticipantKey orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                DefaultOrderCategory.FEATURE_CATEGORY, layerId);
        dataType.setOrderKey(orderKey);
        int zOrder = myOrderManager.activateParticipant(orderKey);

        MapVisualizationInfo visInfo = new DefaultMapFeatureVisualizationInfo(MapVisualizationType.MIXED_ELEMENTS, true);
        visInfo.setZOrder(zOrder, this);
        dataType.setMapVisualizationInfo(visInfo);
        MetaDataInfo metainfo = myMetaDataInfoBuilder.buildMetaDataInfo(layer);
        dataType.setMetaDataInfo(metainfo);
        dataType.getBasicVisualizationInfo().setUsesDataElements(true);
    }

    @Override
    public void layerActivated(GeoPackageDataTypeInfo layer)
    {
        GeoPackageFeatureLayer featureLayer = (GeoPackageFeatureLayer)layer.getLayer();
        myDataTypeController.addDataType(featureLayer.getPackageFile(), featureLayer.getName(), layer, this);

        List<DataElement> elements = New.list();
        TimeSpan layerSpan = TimeSpan.TIMELESS;
        for (Map<String, Serializable> row : featureLayer.getData())
        {
            DataElement element = myDataElementPopulator.populateDataElement(row, layer);
            if (layerSpan == TimeSpan.TIMELESS)
            {
                layerSpan = element.getTimeSpan();
            }
            else if (element.getTimeSpan() != null)
            {
                layerSpan = layerSpan.simpleUnion(element.getTimeSpan());
            }
            elements.add(element);
        }

        Projection projection = myMapManager.getProjection().getSnapshot();

        GeographicBoundingBox layerBox = null;

        if (!elements.isEmpty())
        {
            List<DataElement> dataElements = New.list();
            List<MapDataElement> mapElements = New.list();

            for (DataElement element : elements)
            {
                if (element instanceof MapDataElement)
                {
                    MapDataElement mapElement = (MapDataElement)element;
                    GeographicBoundingBox elementBox = mapElement.getMapGeometrySupport().getBoundingBox(projection);
                    if (layerBox == null)
                    {
                        layerBox = elementBox;
                    }
                    else
                    {
                        layerBox = GeographicBoundingBox.merge(layerBox, elementBox);
                    }
                    mapElements.add((MapDataElement)element);
                }
                else
                {
                    dataElements.add(element);
                }
            }

            if (layerBox != null)
            {
                layer.addBoundingBox(layerBox);
            }

            setTimeExtents(layerSpan, layer);

            if (!dataElements.isEmpty())
            {
                myDataTypeController.addDataElements(layer, null, dataElements, this);
            }

            if (!mapElements.isEmpty())
            {
                myDataTypeController.addMapDataElements(layer, null, null, mapElements, this);
            }
        }
    }

    @Override
    public void layerDeactivated(GeoPackageDataTypeInfo layer)
    {
        myDataTypeController.removeDataType(layer, this);
    }

    /**
     * Sets the time extents on the layer.
     *
     * @param layerSpan The calculated layer span.
     * @param layer The layer.
     */
    private void setTimeExtents(TimeSpan layerSpan, GeoPackageDataTypeInfo layer)
    {
        if (layerSpan != TimeSpan.TIMELESS)
        {
            layer.setTimeExtents(new DefaultTimeExtents(layerSpan), this);
        }
    }
}
