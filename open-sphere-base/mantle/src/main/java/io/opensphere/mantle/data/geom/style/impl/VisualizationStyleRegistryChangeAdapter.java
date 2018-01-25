package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener;

/**
 * The Class VisualizationStyleRegistryChangeAdapter.
 */
public class VisualizationStyleRegistryChangeAdapter implements VisualizationStyleRegistryChangeListener
{
    @Override
    public void defaultStyleChanged(Class<? extends VisualizationSupport> mgsClass,
            Class<? extends VisualizationStyle> styleClass, Object source)
    {
    }

    @Override
    public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
    {
    }

    @Override
    public void visualizationStyleInstalled(Class<? extends VisualizationStyle> styleClass, Object source)
    {
    }
}
