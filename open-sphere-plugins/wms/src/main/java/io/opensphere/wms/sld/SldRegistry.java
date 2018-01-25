package io.opensphere.wms.sld;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.wms.sld.event.SldChangeListener;
import net.opengis.sld._100.StyledLayerDescriptor;

/**
 * The Interface SldRegistry.
 */
public interface SldRegistry
{
    /**
     * Adds the new {@link StyledLayerDescriptor}.
     *
     * @param layerKey unique key for the layer associated with the added SLD.
     * @param sldConfig the sld config
     */
    void addNewSld(String layerKey, StyledLayerDescriptor sldConfig);

    /**
     * Adds the sld change listener.
     *
     * @param listener the listener
     */
    void addSldChangeListener(SldChangeListener listener);

    /**
     * Gets the {@link StyledLayerDescriptor} for a layer given the SLD name.
     *
     * @param layerKey the key for the desired layer
     * @param sldName the name of the desired SLD
     * @return the {@link StyledLayerDescriptor} associated with the given layer
     *         and SLD name
     */
    StyledLayerDescriptor getSldByLayerAndName(String layerKey, String sldName);

    /**
     * Gets the sld names for layer.
     *
     * @param layerKey the layer key
     * @return the sld names for layer
     */
    List<String> getSldNamesForLayer(String layerKey);

    /**
     * Gets the slds for layer.
     *
     * @param layerKey the layer key
     * @return the slds for layer
     */
    List<StyledLayerDescriptor> getSldsForLayer(String layerKey);

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    Toolbox getToolbox();

    /**
     * Removes the {@link StyledLayerDescriptor} with the specified name.
     *
     * @param layerKey the key for the layer whose SLD is being removed
     * @param sldName the name of the SLD to remove
     */
    void removeSld(String layerKey, String sldName);

    /**
     * Removes the sld change listener.
     *
     * @param listener the listener
     */
    void removeSldChangeListener(SldChangeListener listener);

    /**
     * Replaces a {@link StyledLayerDescriptor} with the one specified.
     *
     * @param layerKey unique key for the layer associated with the changed SLD
     * @param sldConfig the updated SLD
     */
    void replaceSld(String layerKey, StyledLayerDescriptor sldConfig);
}
