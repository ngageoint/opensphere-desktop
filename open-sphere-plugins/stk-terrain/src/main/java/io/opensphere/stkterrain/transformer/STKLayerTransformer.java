package io.opensphere.stkterrain.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.util.Constants;

/**
 * Adds/removes {@link TerrainTileGeometry} to and from the system when STK
 * layers are activated and deactivated.
 */
public class STKLayerTransformer extends DefaultTransformer implements DataRegistryListener<TileSetMetadata>
{
    /**
     * Used to build initial geometries for an STK terrain layer.
     */
    private final STKGeometryBuilder myBuilder;

    /**
     * Used to listen for a layer's visibility change.
     */
    private final EventManager myEventManager;

    /**
     * Used to get {@link TileRenderProperties} for activated terrain layers.
     */
    private final DataGroupController myGroupController;

    /**
     * The published geometries mapped by their owning layer key.
     */
    private final Map<String, List<TerrainTileGeometry>> myPublishedGeometries = Collections.synchronizedMap(New.map());

    /**
     * The visibility listener.
     */
    private final EventListener<DataTypeVisibilityChangeEvent> myVisibilityListener = this::handleVisibilityChanged;

    /**
     * Constructs a new {@link STKLayerTransformer}.
     *
     * @param dataRegistry Used to publish geometries.
     * @param groupController Used to get {@link TileRenderProperties} for
     *            activated terrain layers.
     * @param eventManager Used to listen for a layer's visibility change.
     */
    public STKLayerTransformer(DataRegistry dataRegistry, DataGroupController groupController, EventManager eventManager)
    {
        super(dataRegistry);
        myBuilder = new STKGeometryBuilder(dataRegistry);
        myGroupController = groupController;
        myEventManager = eventManager;
        dataRegistry.addChangeListener(this, new DataModelCategory(null, TileSetMetadata.class.getName(), null),
                Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR);
        myEventManager.subscribe(DataTypeVisibilityChangeEvent.class, myVisibilityListener);
    }

    @Override
    public void allValuesRemoved(Object source)
    {
        List<TerrainTileGeometry> removals = New.list();
        for (Entry<String, List<TerrainTileGeometry>> entry : myPublishedGeometries.entrySet())
        {
            removals.addAll(entry.getValue());
        }

        myPublishedGeometries.clear();

        publishGeometries(New.collection(), removals);
    }

    @Override
    public void close()
    {
        getDataRegistry().removeChangeListener(this);
        myEventManager.unsubscribe(DataTypeVisibilityChangeEvent.class, myVisibilityListener);
        super.close();
    }

    @Override
    public boolean isIdArrayNeeded()
    {
        return false;
    }

    @Override
    public boolean isWantingRemovedObjects()
    {
        return false;
    }

    @Override
    public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends TileSetMetadata> newValues,
            Object source)
    {
        String serverUrl = dataModelCategory.getSource();
        String tileSetName = dataModelCategory.getCategory();
        String layerKey = serverUrl + tileSetName;
        TileSetMetadata tileSetMetadata = newValues.iterator().next();
        STKTerrainTileDivider divider = new STKTerrainTileDivider(layerKey, tileSetMetadata);
        DataTypeInfo dataType = myGroupController.findMemberById(layerKey);

        List<TerrainTileGeometry> geometries = myBuilder.buildInitialGeometries(layerKey, serverUrl, tileSetName, tileSetMetadata,
                divider, dataType.getMapVisualizationInfo().getTileRenderProperties());
        myPublishedGeometries.put(layerKey, geometries);

        if (dataType.isVisible())
        {
            publishGeometries(geometries, New.collection());
        }
    }

    @Override
    public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends TileSetMetadata> removedValues,
            Object source)
    {
        String layerKey = dataModelCategory.getSource() + dataModelCategory.getCategory();
        List<TerrainTileGeometry> removed = myPublishedGeometries.remove(layerKey);
        if (removed != null)
        {
            publishGeometries(New.collection(), removed);
        }
    }

    @Override
    public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Object source)
    {
        String layerKey = dataModelCategory.getSource() + dataModelCategory.getCategory();
        List<TerrainTileGeometry> removed = myPublishedGeometries.remove(layerKey);
        if (removed != null)
        {
            publishGeometries(New.collection(), removed);
        }
    }

    @Override
    public void valuesUpdated(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends TileSetMetadata> newValues,
            Object source)
    {
    }

    /**
     * Handles visibility changes and notifies the activation listener.
     *
     * @param event The event.
     */
    private void handleVisibilityChanged(DataTypeVisibilityChangeEvent event)
    {
        String typeKey = event.getDataTypeKey();
        if (myPublishedGeometries.containsKey(typeKey))
        {
            if (event.isVisible())
            {
                publishGeometries(myPublishedGeometries.get(typeKey), New.collection());
            }
            else
            {
                publishGeometries(New.collection(), myPublishedGeometries.get(typeKey));
            }
        }
    }
}
