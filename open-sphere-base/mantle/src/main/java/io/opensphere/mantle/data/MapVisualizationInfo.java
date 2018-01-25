package io.opensphere.mantle.data;

import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.mantle.data.element.MapDataElement;

/**
 * A support interface for describing, at a type level, information relevant for
 * rendering the layer to assist the transformers in building the geometries.
 */
public interface MapVisualizationInfo
{
    /**
     * Gets the DataTypeInfo for this visualization info.
     *
     * @return The {@link DataTypeInfo}.
     */
    DataTypeInfo getDataTypeInfo();

    /**
     * Gets the tile level controller.
     *
     * @return the tile level controller
     */
    TileLevelController getTileLevelController();

    /**
     * Gets the tile render properties for this data type if the type is a
     * IMAGE_TILE type image, or null if this type does not support tiles.
     *
     * @return the tile render properties or null.
     */
    TileRenderProperties getTileRenderProperties();

    /**
     * Gets the default visualization type.
     *
     * @return the default visualization type
     */
    MapVisualizationType getVisualizationType();

    /**
     * Gets the Z-Order for this Type.
     *
     * @return the order.
     */
    int getZOrder();

    /**
     * Checks if is image tile type.
     *
     * @return true, if is image tile type
     */
    boolean isImageTileType();

    /**
     * Checks if is motion imagery type.
     *
     * @return true, if is motion imagery type
     */
    boolean isMotionImageryType();

    /**
     * Checks if is image type.
     *
     * @return true, if is image type
     */
    boolean isImageType();

    /**
     * Checks if is z-orderable.
     *
     * @return true, if is z-orderable
     */
    boolean isZOrderable();

    /**
     * Sets the DataTypeInfo for this visualization info.
     *
     * @param dti - the {@link DataTypeInfo}
     */
    void setDataTypeInfo(DataTypeInfo dti);

    /**
     * Sets the visualization type. Cannot be null.
     *
     * @param visType the new visualization type
     */
    void setVisualizationType(MapVisualizationType visType);

    /**
     * Sets the Z-Order for this Type.
     *
     * @param order - the order
     * @param source - the calling object
     */
    void setZOrder(int order, Object source);

    /**
     * True if this DataTypeInfo utilizes {@link MapDataElement}s.
     *
     * @return true if uses, false if not
     */
    boolean usesMapDataElements();

    /**
     * Returns true if this data type uses visualization styles, false if not.
     *
     * @return true, if uses visualization styles.
     */
    boolean usesVisualizationStyles();
}
