package io.opensphere.heatmap;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.MapVisualizationType;

/** Heat map utilities. */
public final class HeatmapUtilities
{
    /**
     * Determines if the data type key is a feature layer.
     *
     * @param typeKey the data type key
     * @param toolbox the toolbox
     * @return whether it's a feature layer
     */
    public static boolean isFeatureLayer(String typeKey, Toolbox toolbox)
    {
        MantleToolbox mantleToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        DataTypeInfo dataType = mantleToolbox.getDataTypeController().getDataTypeInfoForType(typeKey);
        return isFeatureLayer(dataType);
    }

    /**
     * Determines if the data type is a feature layer.
     *
     * @param dataType the data type
     * @return whether it's a feature layer
     */
    public static boolean isFeatureLayer(DataTypeInfo dataType)
    {
        boolean isFeatureLayer = false;
        if (dataType != null)
        {
            MapVisualizationInfo mapVisualization = dataType.getMapVisualizationInfo();
            if (mapVisualization != null)
            {
                MapVisualizationType visualizationType = mapVisualization.getVisualizationType();
                if (visualizationType != null)
                {
                    MapVisualizationStyleCategory styleCategory = visualizationType.getStyleCategory();
                    isFeatureLayer = styleCategory == MapVisualizationStyleCategory.FEATURE
                            || styleCategory == MapVisualizationStyleCategory.LOCATION_FEATURE
                            || styleCategory == MapVisualizationStyleCategory.PATH_FEATURE;
                }
            }
        }
        return isFeatureLayer;
    }

    /** Disallow instantiation. */
    private HeatmapUtilities()
    {
    }
}
