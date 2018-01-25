package io.opensphere.geopackage.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.opensphere.core.util.collections.New;

/**
 * A {@link GeoPackageLayer} that contains feature data.
 */
public class GeoPackageFeatureLayer extends GeoPackageLayer
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The data.
     */
    private final List<Map<String, Serializable>> myData = New.list();

    /**
     * Constructs a new feature layer.
     *
     * @param packageName The name of the package.
     * @param packageFile The path to the geo package file.
     * @param name The name of the layer.
     * @param recordCount The number of records this layer contains.
     */
    public GeoPackageFeatureLayer(String packageName, String packageFile, String name, int recordCount)
    {
        super(packageName, packageFile, name, LayerType.FEATURE, recordCount);
    }

    /**
     * Gets the data of this feature layer.
     *
     * @return The data of the feature layer.
     */
    public List<Map<String, Serializable>> getData()
    {
        return myData;
    }
}
