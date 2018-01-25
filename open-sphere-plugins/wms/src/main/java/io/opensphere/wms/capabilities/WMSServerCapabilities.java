package io.opensphere.wms.capabilities;

import java.util.Collection;

import io.opensphere.wms.sld.WMSUserDefinedSymbolization;

/**
 * Interface that describes server WMS capabilities.
 */
public interface WMSServerCapabilities
{
    /**
     * Get the access constraints from the server capabilities.
     *
     * @return The access constraints, or {@code null} if there are none.
     */
    String getAccessConstraints();

    /**
     * Get the exception formats.
     *
     * @return A list of acceptable exception formats.
     */
    Collection<String> getExceptionFormats();

    /**
     * Get the GetMap formats.
     *
     * @return A list of acceptable GetMap formats.
     */
    Collection<String> getGetMapFormats();

    /**
     * Get the GetMap URL.
     *
     * @return The GetMap URL.
     */
    String getGetMapURL();

    /**
     * Gets a flat (non-hierarchical) collection of this server's layers.
     *
     * @return the flat list of layers
     */
    Collection<WMSCapsLayer> getLayerList();

    /**
     * Get the title of the server.
     *
     * @return The server title.
     */
    String getTitle();

    /**
     * Get the user-defined symbolization, if one exists.
     *
     * @return The symbolization, or {@code null} if there is none.
     */
    WMSUserDefinedSymbolization getUserDefinedSymbolization();

    /**
     * Get the WMS version number for these capabilities.
     *
     * @return The version number (for example, "1.1.1" or "1.3.0".
     */
    String getVersion();
}
