package io.opensphere.mantle.data.geom.style;

/**
 * The listener interface for receiving
 * {@link VisualizationStyleParameterChangeEvent}s.
 *
 * @see VisualizationStyleParameterChangeEvent
 */
@FunctionalInterface
public interface VisualizationStyleParameterChangeListener
{
    /**
     * Style parameters changed.
     *
     * @param evt the evt
     */
    void styleParametersChanged(VisualizationStyleParameterChangeEvent evt);
}
