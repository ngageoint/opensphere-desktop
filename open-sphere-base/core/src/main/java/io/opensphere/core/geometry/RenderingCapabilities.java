package io.opensphere.core.geometry;

import java.util.Set;

/**
 * API for accessing the geometry rendering capabilities.
 */
public interface RenderingCapabilities
{
    /** Identifier for tile shader rendering capability. */
    String TILE_SHADER = "TILE_SHADER";

    /**
     * Get a string that identifies the rendering engine.
     *
     * @return The renderer identifier.
     */
    String getRendererIdentifier();

    /**
     * Get the set of supported capabilities.
     *
     * @return The capabilities.
     */
    Set<? extends String> getSupportedCapabilities();

    /**
     * Get if a specific capability is supported.
     *
     * @param capability The capability.
     * @return {@code true} if the capability is supported.
     */
    boolean isCapabilitySupported(String capability);
}
