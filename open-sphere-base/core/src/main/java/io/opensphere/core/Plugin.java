package io.opensphere.core;

import java.util.Collection;

import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.geometry.Geometry;

/**
 * Interface for a plug-in.
 */
public interface Plugin
{
    /**
     * Initialize the plug-in.
     *
     * @param plugindata The configuration information for the plug-in.
     * @param toolbox References to facilities that may be used by the plug-in
     *            to interact with the rest of the system.
     */
    void initialize(PluginLoaderData plugindata, Toolbox toolbox);

    /**
     * Close the plug-in to further use and free any resources it is consuming.
     */
    void close();

    /**
     * Get the envoys for this plug-in. Envoys retrieve data and create models
     * for them.
     *
     * @return The envoys for this plug-in.
     */
    Collection<? extends Envoy> getEnvoys();

    /**
     * Get the transformers for this plug-in. Transformers convert models to
     * {@link Geometry}s.
     *
     * @return The transformers for this plug-in.
     */
    Collection<? extends Transformer> getTransformers();
}
