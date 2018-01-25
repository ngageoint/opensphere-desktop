package io.opensphere.wfs.envoy;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.server.services.ServerConnectionParams;

/**
 * A default implementation of the {@link WFSEnvoyFactory}, configured to
 * generate and return a {@link WFSEnvoy}.
 */
public class DefaultWFSEnvoyFactory implements WFSEnvoyFactory
{
    /**
     * The {@link Logger} instance used to capture output.
     */
    private static final Logger LOG = Logger.getLogger(DefaultWFSEnvoyFactory.class);

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wfs.envoy.WFSEnvoyFactory#createEnvoy(io.opensphere.core.Toolbox,
     *      io.opensphere.core.preferences.Preferences,
     *      io.opensphere.server.services.ServerConnectionParams,
     *      io.opensphere.wfs.envoy.WFSTools)
     */
    @Override
    public AbstractWFSEnvoy createEnvoy(Toolbox toolbox, Preferences preferences, ServerConnectionParams connection, WFSTools tools)
    {
        LOG.info("Creating default WFS Envoy.");

        return new WFSEnvoy(toolbox, preferences, connection, tools);
    }
}
