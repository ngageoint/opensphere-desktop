package io.opensphere.geopackage.model;

import java.util.Map;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.collections.New;

/**
 * Contains tile specific information about a tile layer. This class is
 * serializable so it can be stored within the {@link DataRegistry}.
 */
public class GeoPackageTileLayer extends GeoPackageLayer
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The overall bounding box of this layer.
     */
    private GeographicBoundingBox myBoundingBox;

    /**
     * Contains any extensions defined in the geopackage file for this layer.
     */
    private final Map<String, String> myExtensions = New.map();

    /**
     * The layer id.
     */
    private final String myId;

    /**
     * The maximum zoom level of this layer.
     */
    private long myMaxZoomLevel;

    /**
     * The minimum zoom level of this layer.
     */
    private long myMinZoomLevel;

    /**
     * A map of zoom levels to tile matrices.
     */
    private final Map<Long, TileMatrix> myZoomLevelToMatrix = New.map();

    /**
     * Constructs a new {@link GeoPackageTileLayer}.
     *
     * @param packageName The name of the geopackage.
     * @param packageFile The file path to the geopackage file.
     * @param name The layer name.
     * @param recordCount The number of records this layer contains.
     */
    public GeoPackageTileLayer(String packageName, String packageFile, String name, int recordCount)
    {
        super(packageName, packageFile, name, LayerType.TILE, recordCount);
        myId = packageFile + name;
    }

    /**
     * Gets the overall bounding box of this layer.
     *
     * @return The bounding box covering all tiles for this layer.
     */
    public GeographicBoundingBox getBoundingBox()
    {
        return myBoundingBox;
    }

    /**
     * Gets any extensions defined in the geopackage file for this layer.
     *
     * @return the extensions.
     */
    public Map<String, String> getExtensions()
    {
        return myExtensions;
    }

    /**
     * Gets the layer id.
     *
     * @return The id of the layer.
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the maximum zoom level.
     *
     * @return The maximum zoom level.
     */
    public long getMaxZoomLevel()
    {
        return myMaxZoomLevel;
    }

    /**
     * Gets the minimum zoom level.
     *
     * @return The minimum zoom level.
     */
    public long getMinZoomLevel()
    {
        return myMinZoomLevel;
    }

    /**
     * Gets the mapping of zoom levels to matrices.
     *
     * @return The tile matrices for the different zoom levels.
     */
    public Map<Long, TileMatrix> getZoomLevelToMatrix()
    {
        return myZoomLevelToMatrix;
    }

    /**
     * Sets the overall bounding box of this layer.
     *
     * @param boundingBox The bounding box covering all tiles for this layer.
     */
    public void setBoundingBox(GeographicBoundingBox boundingBox)
    {
        myBoundingBox = boundingBox;
    }

    /**
     * Sets the max zoom level for this layer.
     *
     * @param maxZoomLevel The max zoom level.
     */
    public void setMaxZoomLevel(long maxZoomLevel)
    {
        myMaxZoomLevel = maxZoomLevel;
    }

    /**
     * Sets the minimum zoom level for this layer.
     *
     * @param minZoomLevel The minimum zoom level.
     */
    public void setMinZoomLevel(long minZoomLevel)
    {
        myMinZoomLevel = minZoomLevel;
    }
}
