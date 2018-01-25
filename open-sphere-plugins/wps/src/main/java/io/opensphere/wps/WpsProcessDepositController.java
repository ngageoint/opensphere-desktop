package io.opensphere.wps;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.SimplePersistentCacheDeposit;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wps.envoy.WpsCacheDepositReceiver;
import io.opensphere.wps.envoy.WpsPropertyDescriptors;
import io.opensphere.wps.request.WpsProcessConfiguration;

/**
 * A controller used to deposit WPS process configurations into the data registry, to allow persistence between sessions.
 */
public class WpsProcessDepositController
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(WpsProcessDepositController.class);

    /**
     * The toolbox through which application interaction is performed.
     */
    private final Toolbox myToolbox;

    /**
     * The data registry into which configurations are deposited.
     */
    private final DataRegistry myDataRegistry;

    /**
     * Creates a new deposit controller, configured with the supplied toolbox.
     *
     * @param pToolbox the toolbox with which the controller is configured.
     */
    public WpsProcessDepositController(Toolbox pToolbox)
    {
        myToolbox = pToolbox;
        myDataRegistry = myToolbox.getDataRegistry();
    }

    /**
     * Saves the supplied configuration to the data registry for later reuse.
     *
     * @param pServerId the unique identifier of the server with which the configuration is bound.
     * @param pConfiguration the configuration object to store.
     */
    protected void deposit(String pServerId, WpsProcessConfiguration pConfiguration)
    {
        WpsCacheDepositReceiver receiver = new WpsCacheDepositReceiver(myDataRegistry);
        DataModelCategory category = new DataModelCategory(pConfiguration.getServerId(), OGCServerSource.WPS_SERVICE,
                "Saved Processes");

        LOG.info("Depositing data to data registry from " + this.getClass().getName());
        SimplePersistentCacheDeposit<WpsProcessConfiguration> deposit = new SimplePersistentCacheDeposit<>(category,
                WpsPropertyDescriptors.WPS_SAVE_PROCESS_CONFIGURATION, Collections.singleton(pConfiguration), new Date(Long.MAX_VALUE));

        try
        {
            long[] values = receiver.receive(deposit);
            LOG.info("Recieved IDs from registry: " + Arrays.toString(values) + "(" + this.getClass().getName() + ")");
        }
        catch (CacheException e)
        {
            // TODO
            LOG.error("Unable to write to cache.", e);
        }
    }
}
