package io.opensphere.arcgis2.model;

import java.util.Set;

import io.opensphere.arcgis2.util.Constants;
import io.opensphere.core.Toolbox;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;

/**
 * Data group info for an ArcGIS layer.
 */
public class ArcGISDataGroupInfo extends DefaultDataGroupInfo
{
    /** The layer. */
    private final ArcGISLayer myLayer;

    /** The model id in the data registry. */
    private final long myDataRegistryId;

    /** The URL. */
    private final String myURL;

    /**
     * Constructor for a server group.
     *
     * @param aToolbox The a toolbox.
     * @param displayName The display name.
     * @param url The url.
     */
    public ArcGISDataGroupInfo(Toolbox aToolbox, String displayName, String url)
    {
        this(aToolbox, displayName, url, false);
    }

    /**
     * Constructor for a server group.
     *
     * @param aToolbox The a toolbox.
     * @param displayName The display name.
     * @param url The url.
     * @param isRoot True if this is a server group, false if it is a folder.
     */
    public ArcGISDataGroupInfo(Toolbox aToolbox, String displayName, String url, boolean isRoot)
    {
        super(isRoot, aToolbox, Constants.PROVIDER, url, displayName);
        myLayer = null;
        myURL = url;
        myDataRegistryId = -1;
    }

    /**
     * Constructor for a layer group.
     *
     * @param aToolbox The a toolbox.
     * @param layer The layer.
     * @param url The url.
     * @param dataRegistryId The model id in the data registry.
     */
    public ArcGISDataGroupInfo(Toolbox aToolbox, ArcGISLayer layer, String url, long dataRegistryId)
    {
        super(false, aToolbox, Constants.PROVIDER, url, layer.getLayerName());
        myLayer = layer;
        myURL = url;
        myDataRegistryId = dataRegistryId;
    }

    /**
     * Gets the layer.
     *
     * @return the layer
     */
    public ArcGISLayer getLayer()
    {
        return myLayer;
    }

    /**
     * Get the data registry id.
     *
     * @return The data registry id.
     */
    public long getDataRegistryId()
    {
        return myDataRegistryId;
    }

    /**
     * Get the URL.
     *
     * @return The URL.
     */
    public String getURL()
    {
        return myURL;
    }

    /**
     * Gets the feature type under this group, or null if there isn't one.
     *
     * @return the feature type, or null
     */
    public DataTypeInfo getFeatureType()
    {
        Set<DataTypeInfo> types = findMembers(ArcGISDataGroupInfo::isFeatureType, false, true);
        return types.isEmpty() ? null : types.iterator().next();
    }

    /**
     * Returns whether the data type is a feature type.
     *
     * @param dataType the data type
     * @return whether the data type is a feature type
     */
    public static boolean isFeatureType(DataTypeInfo dataType)
    {
        return dataType.getOrderKey().getCategory() == DefaultOrderCategory.FEATURE_CATEGORY;
    }
}
