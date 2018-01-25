package io.opensphere.controlpanels.layers.util;

import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;

/** Utilities for dealing with LoadsTo. */
public final class LoadsToUtilities
{
    /**
     * Checks if the analyze button can be shown.
     *
     * @param layer the layer
     * @return true, if the analyze button can be shown
     */
    public static boolean allowAnalyzeSelection(DataTypeInfo layer)
    {
        if (layer != null && layer.getBasicVisualizationInfo() != null)
        {
            BasicVisualizationInfo bvi = layer.getBasicVisualizationInfo();
            return bvi.supportsLoadsTo(LoadsTo.BASE) && bvi.supportsLoadsTo(LoadsTo.STATIC)
                    || bvi.supportsLoadsTo(LoadsTo.BASE_WITH_TIMELINE) && bvi.supportsLoadsTo(LoadsTo.TIMELINE);
        }
        return false;
    }

    /**
     * Checks if the timeline button can be shown.
     *
     * @param layer the layer
     * @return true, if the timeline button can be shown
     */
    public static boolean allowTimelineSelection(DataTypeInfo layer)
    {
        if (layer != null && layer.getBasicVisualizationInfo() != null)
        {
            BasicVisualizationInfo bvi = layer.getBasicVisualizationInfo();
            return bvi.supportsLoadsTo(LoadsTo.STATIC) && bvi.supportsLoadsTo(LoadsTo.TIMELINE)
                    || bvi.supportsLoadsTo(LoadsTo.BASE) && bvi.supportsLoadsTo(LoadsTo.BASE_WITH_TIMELINE);
        }
        return false;
    }

    /**
     * Checks if the analyze is enabled for the current layer.
     *
     * @param layer the layer
     * @return true, if analyze is enabled
     */
    public static boolean isAnalyzeEnabled(DataTypeInfo layer)
    {
        if (layer != null && layer.getBasicVisualizationInfo() != null)
        {
            return layer.getBasicVisualizationInfo().getLoadsTo().isAnalysisEnabled();
        }
        return false;
    }

    /**
     * Checks if the timeline is enabled for the current layer.
     *
     * @param layer the layer
     * @return true, if timeline is enabled
     */
    public static boolean isTimelineEnabled(DataTypeInfo layer)
    {
        if (layer != null && layer.getBasicVisualizationInfo() != null)
        {
            return layer.getBasicVisualizationInfo().getLoadsTo().isTimelineEnabled();
        }
        return false;
    }

    /**
     * Sets whether to include/exclude the layer in the analyze tools.
     *
     * @param layer the layer
     * @param include whether to include/exclude
     */
    public static void setIncludeInAnalyze(DataTypeInfo layer, boolean include)
    {
        if (layer != null && layer.getBasicVisualizationInfo() != null)
        {
            BasicVisualizationInfo visualization = layer.getBasicVisualizationInfo();
            if (include)
            {
                visualization.setLoadsTo(visualization.getLoadsTo().withAnalysis(), null);
            }
            else
            {
                visualization.setLoadsTo(visualization.getLoadsTo().withoutAnalysis(), null);
            }
        }
    }

    /**
     * Sets whether to include/exclude the layer in the timeline.
     *
     * @param layer the layer
     * @param include whether to include/exclude
     */
    public static void setIncludeInTimeline(DataTypeInfo layer, boolean include)
    {
        if (layer != null && layer.getBasicVisualizationInfo() != null)
        {
            BasicVisualizationInfo visualization = layer.getBasicVisualizationInfo();
            if (include)
            {
                visualization.setLoadsTo(visualization.getLoadsTo().withTimeline(), null);
            }
            else
            {
                visualization.setLoadsTo(visualization.getLoadsTo().withoutTimeline(), null);
            }
        }
    }

    /** Disallow instantiation. */
    private LoadsToUtilities()
    {
    }
}
