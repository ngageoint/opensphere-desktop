package io.opensphere.core.geometry;

import java.util.concurrent.ExecutorService;

import io.opensphere.core.util.concurrent.InlineExecutorService;

/**
 * A package-protected agent that handles the data retriever executor for a
 * {@link DataRequestingGeometry}.
 */
public final class DataRequestAgent
{
    /** The executor to use for retrieving data. */
    private transient volatile ExecutorService myDataRetrieverExecutor;

    /**
     * Get the executor for retrieving data.
     *
     * @return The executor.
     */
    ExecutorService getDataRetrieverExecutor()
    {
        return myDataRetrieverExecutor == null ? new InlineExecutorService() : myDataRetrieverExecutor;
    }

    /**
     * Set the executor for retrieving data.
     *
     * @param executor The executor.
     */
    void setDataRetrieverExecutor(ExecutorService executor)
    {
        myDataRetrieverExecutor = executor;
    }
}
