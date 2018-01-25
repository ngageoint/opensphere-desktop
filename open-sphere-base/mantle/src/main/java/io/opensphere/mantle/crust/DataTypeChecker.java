package io.opensphere.mantle.crust;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Utility class that checks to see if a given {@link DataTypeInfo} contains
 * features.
 */
public final class DataTypeChecker
{
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private DataTypeChecker()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Checks to see if the specified data type is a feature layer.
     *
     * @param type the data type to check
     * @return true if and only if the argument is a feature layer
     */
    public static boolean isFeatureType(DataTypeInfo type)
    {
        return type.getBasicVisualizationInfo() != null && type.getBasicVisualizationInfo().usesDataElements()
                || type.getMapVisualizationInfo() != null && type.getMapVisualizationInfo().usesMapDataElements();
    }
}
