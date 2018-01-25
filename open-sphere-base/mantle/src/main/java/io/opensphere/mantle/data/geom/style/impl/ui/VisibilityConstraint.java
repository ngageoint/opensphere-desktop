package io.opensphere.mantle.data.geom.style.impl.ui;

import io.opensphere.mantle.data.geom.style.VisualizationStyle;

/**
 * The Class VisibilityConstraint.
 */
@FunctionalInterface
public interface VisibilityConstraint
{
    /**
     * Checks if is visible.
     *
     * @param styleToEvaluate the style to evaluate
     * @return true, if is visible
     */
    boolean isVisible(VisualizationStyle styleToEvaluate);
}
