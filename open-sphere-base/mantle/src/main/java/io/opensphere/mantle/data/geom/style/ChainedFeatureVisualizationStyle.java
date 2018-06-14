package io.opensphere.mantle.data.geom.style;

/**
 * Interface for a VisualizationStyle that supports chaining, represented in a
 * similar manner to the tail node in a linked list.
 *
 * @see VisualizationStyle
 */
public interface ChainedFeatureVisualizationStyle
{
    /**
     * Whether or not there is an earlier VisualizationStyle in the chain.
     *
     * @return true if a previous style exists, false otherwise
     */
    public boolean hasPrevious();

    /**
     * Retrieves the previous VisualizationStyle in the chain.
     *
     * @return the previous style
     */
    public FeatureVisualizationStyle getPrevious();

    /**
     * Sets the previous VisualizationStyle in the chain.
     * <p>
     * This method should be used carefully, as it will invalidate an existing
     * style chain.
     *
     * @param visualizationStyle the style to set
     */
    public void setPrevious(FeatureVisualizationStyle visualizationStyle);

    /**
     * Retrieves the base VisualizationStyle, or the head of the chain.
     *
     * @return the base style
     */
    public FeatureVisualizationStyle getBaseStyle();
}
