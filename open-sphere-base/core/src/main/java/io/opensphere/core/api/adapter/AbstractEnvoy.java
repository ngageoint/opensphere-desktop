package io.opensphere.core.api.adapter;

import java.util.concurrent.ExecutorService;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.data.DataRegistry;

/**
 * Abstract {@link Envoy} implementation. This provides a reference to the
 * toolbox and a publisher to use for created models.
 */
public abstract class AbstractEnvoy implements Envoy
{
    /** Flag indicating if the envoy has been closed. */
    private volatile boolean myClosed;

    /** An executor to use for ad-hoc envoy tasks. */
    private ExecutorService myExecutor;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public AbstractEnvoy(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public void close()
    {
        myClosed = true;
    }

    /**
     * Get the data registry.
     *
     * @return The data registry.
     */
    public DataRegistry getDataRegistry()
    {
        return getToolbox().getDataRegistry();
    }

    /**
     * Get the toolbox.
     *
     * @return The toolbox.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Get if this envoy is closed.
     *
     * @return If this envoy is closed.
     */
    public boolean isClosed()
    {
        return myClosed;
    }

    /**
     * Begin retrieving data.
     */
    public abstract void open();

    @Override
    public final void open(ExecutorService executor)
    {
        myExecutor = executor;
        open();
    }

    @Override
    public void setFilter(Object filter)
    {
    }

    /**
     * Access to this envoy's executor.
     *
     * @return The executor.
     */
    protected ExecutorService getExecutor()
    {
        return myExecutor;
    }
}
