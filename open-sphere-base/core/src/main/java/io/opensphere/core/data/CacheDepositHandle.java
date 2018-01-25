package io.opensphere.core.data;

import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.util.Service;

/**
 * Handle for a cache deposit.
 * <p>
 * <b>WARNING:</b> When {@link #close()} is called, all models for the deposit's
 * category will be removed from the data registry, so it is important that this
 * handle has exclusive access to that category.
 */
public class CacheDepositHandle implements Service
{
    /** The data registry. */
    private final DataRegistry myDataRegistry;

    /** The cache deposit. */
    private final CacheDeposit<?> myDeposit;

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry
     * @param deposit The cache deposit
     */
    public CacheDepositHandle(DataRegistry dataRegistry, CacheDeposit<?> deposit)
    {
        myDataRegistry = dataRegistry;
        myDeposit = deposit;
    }

    @Override
    public void open()
    {
        myDataRegistry.addModels(myDeposit);
    }

    @Override
    public void close()
    {
        // Should open() save off the ids and use them to remove the models?
        myDataRegistry.removeModels(myDeposit.getCategory(), false);
    }
}
