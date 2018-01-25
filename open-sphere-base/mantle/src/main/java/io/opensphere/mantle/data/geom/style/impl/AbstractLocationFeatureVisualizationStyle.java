package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.geom.style.LocationVisualizationStyle;

/**
 * The Class AbstractLocationFeatureVisualizationStyle.
 */
public abstract class AbstractLocationFeatureVisualizationStyle extends AbstractFeatureVisualizationStyle
        implements LocationVisualizationStyle
{
    /**
     * Instantiates a new abstract location feature visualization style.
     *
     * @param tb the tb
     */
    public AbstractLocationFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new abstract location feature visualization style.
     *
     * @param tb the tb
     * @param dtiKey the dti key
     */
    public AbstractLocationFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public AbstractLocationFeatureVisualizationStyle clone()
    {
        return (AbstractLocationFeatureVisualizationStyle)super.clone();
    }
}
