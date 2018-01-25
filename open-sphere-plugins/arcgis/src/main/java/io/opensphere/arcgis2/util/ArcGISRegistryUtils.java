package io.opensphere.arcgis2.util;

import io.opensphere.arcgis2.esri.Response;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.datafilter.DataFilter;

/** ArcGIS data registry utilities. */
public final class ArcGISRegistryUtils
{
    /** The {@link PropertyDescriptor} for features. */
    public static final PropertyDescriptor<Response> FEATURE_DESCRIPTOR = new PropertyDescriptor<>("Features", Response.class);

    /** Property descriptor for data filter. */
    public static final PropertyDescriptor<DataFilter> DATA_FILTER_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>("DataFilter",
            DataFilter.class);

    /** The feature data model category family. */
    public static final String FEATURE_FAMILY = "ArcGIS.Features";

    /**
     * Creates a get feature data model category.
     *
     * @param url the URL string
     * @return the data model category
     */
    public static DataModelCategory newGetFeatureCategory(String url)
    {
        return new DataModelCategory(ArcGISRegistryUtils.class.getName(), FEATURE_FAMILY, url);
    }

    /**
     * Gets the layer URL from the data model category.
     *
     * @param category the data model category
     * @return the layer URL
     */
    public static String getLayerUrl(DataModelCategory category)
    {
        return category.getCategory();
    }

    /** Disallow instantiation. */
    private ArcGISRegistryUtils()
    {
    }
}
