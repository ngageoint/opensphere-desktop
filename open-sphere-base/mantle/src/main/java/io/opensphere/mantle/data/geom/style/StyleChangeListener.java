package io.opensphere.mantle.data.geom.style;

/**
 * Interface for style changes to style parameters.
 */
@FunctionalInterface
public interface StyleChangeListener
{
    /**
     * Style parameters changed.
     *
     * @param dataTypeKey the data type key
     * @param style the style
     */
    void styleParametersChanged(String dataTypeKey, VisualizationStyle style);
}
