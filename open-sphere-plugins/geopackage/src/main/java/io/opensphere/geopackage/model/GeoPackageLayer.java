package io.opensphere.geopackage.model;

import java.io.Serializable;

import io.opensphere.core.data.DataRegistry;

/**
 * Represents a feature layer or tile layer that is contained in a geopackage
 * file. This class is serializable so it can be stored within the
 * {@link DataRegistry}.
 */
public class GeoPackageLayer implements Serializable
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The layer type.
     */
    private final LayerType myLayerType;

    /**
     * The layer name.
     */
    private final String myName;

    /**
     * The file path to the geopackage file.
     */
    private final String myPackageFile;

    /**
     * The name of the geopackage.
     */
    private final String myPackageName;

    /**
     * The number of records this layer contains.
     */
    private final int myRecordCount;

    /**
     * Constructs a new layer.
     *
     * @param packageName The name of the geopackage.
     * @param packageFile The file path to the geopackage file.
     * @param name The layer name.
     * @param layerType The layer type.
     * @param recordCount The number of records this layer contains.
     */
    public GeoPackageLayer(String packageName, String packageFile, String name, LayerType layerType, int recordCount)
    {
        myPackageName = packageName;
        myPackageFile = packageFile;
        myName = name;
        myLayerType = layerType;
        myRecordCount = recordCount;
    }

    /**
     * Gets the layer type.
     *
     * @return The layer type.
     */
    public LayerType getLayerType()
    {
        return myLayerType;
    }

    /**
     * Gets the name of the layer.
     *
     * @return The layer name.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the file path to the geopackage file this layer is contained in.
     *
     * @return The geopackage file path.
     */
    public String getPackageFile()
    {
        return myPackageFile;
    }

    /**
     * Gets the geopackage name this layer belongs to.
     *
     * @return The geopackage name.
     */
    public String getPackageName()
    {
        return myPackageName;
    }

    /**
     * Gets the number of records this layer has.
     *
     * @return The record count.
     */
    public int getRecordCount()
    {
        return myRecordCount;
    }
}
