package io.opensphere.wps.envoy;

import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistry;

/**
 * A receiver class, implemented to deposit WPS data into the data registry.
 */
public class WpsCacheDepositReceiver implements CacheDepositReceiver
{
    /**
     * A handle to the data registry through which data will be inserted.
     */
    private final DataRegistry myDataRegistry;

    /**
     * Creates a new receiver, configured to access and deposit data to the supplied registry.
     *
     * @param pDataRegistry A handle to the data registry through which data will be inserted.
     */
    public WpsCacheDepositReceiver(DataRegistry pDataRegistry)
    {
        myDataRegistry = pDataRegistry;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.CacheDepositReceiver#receive(io.opensphere.core.cache.CacheDeposit)
     */
    @Override
    public <T> long[] receive(CacheDeposit<T> pDeposit) throws CacheException
    {
        return myDataRegistry.addModels(pDeposit);
    }
}
