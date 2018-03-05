package io.opensphere.heatmap;

import io.opensphere.heatmap.DataRegistryHelper.HeatmapImageInfo;

/**
 * Interface to an object that knows how to recreate heatmap images but with a
 * new style.
 */
public interface HeatmapRecreator
{
    /**
     * Recreates heat map images but with a new style.
     *
     * @param dtiKey The layer to create heat map images for.
     * @param style The new style of heat map.
     * @param imageInfo The previous image info.
     */
    void recreate(String dtiKey, HeatmapVisualizationStyle style, HeatmapImageInfo imageInfo);
}
