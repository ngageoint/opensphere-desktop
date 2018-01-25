package io.opensphere.server.toolbox;

import io.opensphere.server.customization.ServerCustomization;

/**
 * A generic configuration used to describe a given layer source.
 */
public interface LayerConfiguration
{
    /**
     * Gets the name of the layer described in the configuration instance.
     *
     * @return the name of the layer.
     */
    String getName();

    /**
     * Gets the XPath used to access the layer's information within a saved
     * state.
     *
     * @return The XPath used to access the layer's information within a saved
     *         state.
     */
    String getStateXPath();

    /**
     * Gets the ServerCustomization implementation class for the layer.
     *
     * @return The ServerCustomization implementation class for the layer.
     */
    Class<? extends ServerCustomization> getCustomizationImplementation();

    /**
     * Tests to determine if the time column can be changed for the layer.
     *
     * @return true if the time column can be changed, false otherwise.
     */
    boolean isTimeColumnChangeable();
}
