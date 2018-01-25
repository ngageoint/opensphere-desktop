package io.opensphere.analysis.heatmap;

import io.opensphere.analysis.heatmap.DataRegistryHelper.HeatmapImageInfo;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener;

/**
 * Handles when the user disables custom styles and it goes back to default
 * styles.
 */
public class HeatmapDefaultStyleHandler implements VisualizationStyleRegistryChangeListener
{
    /**
     * The {@link HeatmapController} used to recreate heat map images.
     */
    private final HeatmapRecreator myController;

    /**
     * Mantle.
     */
    private final MantleToolbox myMantle;

    /**
     * Used to help get the existing heat map image to recreate.
     */
    private final DataRegistryHelper myRegistryHelper;

    /**
     * Constructs a default style handler.
     *
     * @param controller The {@link HeatmapController} used to recreate heat map
     *            images.
     * @param mantle Mantle.
     * @param registryHelper Used to help get the existing heat map image to
     *            recreate.
     */
    public HeatmapDefaultStyleHandler(HeatmapRecreator controller, MantleToolbox mantle, DataRegistryHelper registryHelper)
    {
        myController = controller;
        myMantle = mantle;
        myRegistryHelper = registryHelper;
        myMantle.getVisualizationStyleRegistry().addVisualizationStyleRegistryChangeListener(this);
    }

    /**
     * Stops listening for visualization changes.
     */
    public void close()
    {
        myMantle.getVisualizationStyleRegistry().removeVisualizationStyleRegistryChangeListener(this);
    }

    @Override
    public void defaultStyleChanged(Class<? extends VisualizationSupport> mgsClass,
            Class<? extends VisualizationStyle> styleClass, Object source)
    {
    }

    @Override
    public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
    {
        if (evt.getNewStyle() instanceof HeatmapVisualizationStyle)
        {
            HeatmapImageInfo imageInfo = myRegistryHelper.queryImage(evt.getDTIKey());
            if (imageInfo != null)
            {
                myController.recreate(evt.getDTIKey(), (HeatmapVisualizationStyle)evt.getNewStyle(), imageInfo);
            }
        }
    }

    @Override
    public void visualizationStyleInstalled(Class<? extends VisualizationStyle> styleClass, Object source)
    {
    }
}
