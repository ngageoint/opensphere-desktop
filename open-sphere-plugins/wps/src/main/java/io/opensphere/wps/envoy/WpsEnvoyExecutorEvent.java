package io.opensphere.wps.envoy;

import java.util.concurrent.ExecutorService;

import io.opensphere.core.event.AbstractMultiStateEvent;

/**
 * An event describing an update for the envoy executor.
 */
public class WpsEnvoyExecutorEvent extends AbstractMultiStateEvent
{
    /**
     * The envoy executor service that was updated to trigger the event.
     */
    private ExecutorService myExecutor;

    /**
     * Creates a new event, configured to describe a change to the supplied
     * executor.
     *
     * @param pExecutor The envoy executor that was updated to trigger the
     *            event.
     */
    public WpsEnvoyExecutorEvent(ExecutorService pExecutor)
    {
        myExecutor = pExecutor;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.event.Event#getDescription()
     */
    @Override
    public String getDescription()
    {
        return "Event indicating that WPS Envoy Executor has changed.";
    }

    /**
     * Gets the value of the {@link #myExecutor} field.
     *
     * @return the value stored in the {@link #myExecutor} field.
     */
    public ExecutorService getEnvoyExecutor()
    {
        return myExecutor;
    }

    /**
     * Sets the value of the {@link #myExecutor} field.
     *
     * @param pExecutor the value to store in the {@link #myExecutor} field.
     */
    public void setEnvoyExecutor(ExecutorService pExecutor)
    {
        myExecutor = pExecutor;
    }
}
