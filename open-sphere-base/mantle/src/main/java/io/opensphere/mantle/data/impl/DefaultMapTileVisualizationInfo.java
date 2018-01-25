package io.opensphere.mantle.data.impl;

import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.TileLevelController;
import io.opensphere.mantle.data.event.DataTypeInfoZOrderChangeEvent;

/**
 * A support class for describing, at a type level, information relevant for
 * rendering the layer to assist the transformers in building the geometries.
 */
public class DefaultMapTileVisualizationInfo extends AbstractMapVisualizationInfo
{
    /** The Tile level controller. */
    private TileLevelController myTileLevelController;

    /** The Tile render properties. */
    private TileRenderProperties myTileRenderProperties;

    /**
     * CTOR with default type color.
     *
     * @param visType - the visualization type, should be
     *            {@link MapVisualizationType}.TERRAIN_TILE or IMAGE_TILE
     * @param tileRenderProperties the tile render properties
     * @param usesVisualizationStyles the true if this type uses the
     *            visualization styles, false if native styles
     */
    public DefaultMapTileVisualizationInfo(MapVisualizationType visType, TileRenderProperties tileRenderProperties,
            boolean usesVisualizationStyles)
    {
        super(visType, usesVisualizationStyles);
        validateMapVisualizationType(visType);
        myTileRenderProperties = tileRenderProperties;
    }

    @Override
    public TileLevelController getTileLevelController()
    {
        return myTileLevelController;
    }

    @Override
    public TileRenderProperties getTileRenderProperties()
    {
        return myTileRenderProperties;
    }

    @Override
    public int getZOrder()
    {
        if (myTileRenderProperties != null)
        {
            return myTileRenderProperties.getZOrder();
        }
        return 0;
    }

    /**
     * Sets the tile level controller.
     *
     * @param controller the new tile level controller
     */
    public void setTileLevelController(TileLevelController controller)
    {
        myTileLevelController = controller;
    }

    /**
     * Sets the tile render properties.
     *
     * @param props the props
     * @param source the source
     */
    public void setTileRenderProperties(TileRenderProperties props, Object source)
    {
        myTileRenderProperties = Utilities.checkNull(props, "props");
        if (getDataTypeInfo() != null)
        {
            getDataTypeInfo().fireChangeEvent(
                    new DataTypeInfoZOrderChangeEvent(getDataTypeInfo(), myTileRenderProperties.getZOrder(), source));
        }
    }

    @Override
    public void setVisualizationType(MapVisualizationType visType)
    {
        if (visType == null)
        {
            throw new IllegalArgumentException("Visualization Type Cannot be Null");
        }
        else if (visType != MapVisualizationType.IMAGE_TILE && visType != MapVisualizationType.TERRAIN_TILE
                && visType != MapVisualizationType.IMAGE)
        {
            throw new IllegalArgumentException(
                    "Visualization Type Must Be Either IMAGE_TILE or TERRAIN_TILE or IMAGE " + visType + " is not allowed.");
        }
        super.setVisualizationType(visType);
    }

    /**
     * Sets the Z-Order for this Type.
     *
     * @param order - the order
     * @param source - the calling object
     */
    @Override
    public void setZOrder(int order, Object source)
    {
        if (order != getZOrder() && myTileRenderProperties != null)
        {
            if (myTileRenderProperties instanceof DefaultTileRenderProperties)
            {
                myTileRenderProperties = myTileRenderProperties.clone();
                ((DefaultTileRenderProperties)myTileRenderProperties).setZOrder(order);
            }
            else
            {
                myTileRenderProperties = new DefaultTileRenderProperties(order, myTileRenderProperties.isDrawable(),
                        myTileRenderProperties.isPickable());
            }
            if (getDataTypeInfo() != null)
            {
                getDataTypeInfo().fireChangeEvent(new DataTypeInfoZOrderChangeEvent(getDataTypeInfo(), order, source));
            }
        }
    }

    /**
     * Validate map visualization type.
     *
     * @param visType the vis type
     * @return true, if successful
     */
    private boolean validateMapVisualizationType(MapVisualizationType visType)
    {
        if (visType != MapVisualizationType.IMAGE_TILE && visType != MapVisualizationType.TERRAIN_TILE
                && visType != MapVisualizationType.IMAGE && visType != MapVisualizationType.INTERPOLATED_IMAGE_TILES)
        {
            throw new IllegalArgumentException(
                    "Visualization Type Must Be Either IMAGE_TILE or TERRAIN_TILE or IMAGE " + visType + " is not allowed.");
        }
        return true;
    }
}
