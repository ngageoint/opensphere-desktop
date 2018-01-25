package io.opensphere.city.model.json;

import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * Json response object returned by the cyber city 3d server.
 */
public class Response
{
    /**
     * The results.
     */
    private List<Result> myResults = New.list();

    /**
     * The return status.
     */
    private int myStatus;

    /**
     * Gets the results.
     *
     * @return the results
     */
    public List<Result> getResults()
    {
        return myResults;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus()
    {
        return myStatus;
    }

    /**
     * Sets the results.
     *
     * @param results the results to set
     */
    public void setResults(List<Result> results)
    {
        myResults = results;
    }

    /**
     * Sets the status.
     *
     * @param status the status to set
     */
    public void setStatus(int status)
    {
        myStatus = status;
    }
}
