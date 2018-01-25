package io.opensphere.wps.streaming;

import java.net.URL;
import java.util.UUID;

/**
 * Contains certain information pertaining to a subscribed stream.
 */
public class SubscriptionContext
{
    /**
     * The parameter name for the filter id parameter.
     */
    private String myFilterIdParameterName;

    /**
     * The unique id of the subscription.
     */
    private UUID myStreamId;

    /**
     * The url to use to get new data.
     */
    private URL myStreamUrl;

    /**
     * The number of milliseconds an NRT poll occurs for.
     */
    private int myPollInterval;

    /**
     * Gets the number of milliseconds an NRT poll occurs for.
     *
     * @return The number of milliseconds an NRT poll occurs for.
     */
    public int getPollInterval()
    {
        return myPollInterval;
    }

    /**
     * Sets the number of milliseconds an NRT poll occurs for.
     *
     * @param pollInterval The number of milliseconds an NRT poll occurs for.
     */
    public void setPollInterval(int pollInterval)
    {
        myPollInterval = pollInterval;
    }

    /**
     * Gets the parameter name for the filter id parameter.
     *
     * @return The parameter name for the filter id parameter.
     */
    public String getFilterIdParameterName()
    {
        return myFilterIdParameterName;
    }

    /**
     * Gets the unique id of the subscription.
     *
     * @return The unique id of the subscription.
     */
    public UUID getStreamId()
    {
        return myStreamId;
    }

    /**
     * Gets the url to use to get new data.
     *
     * @return The url to use to get new data.
     */
    public URL getStreamUrl()
    {
        return myStreamUrl;
    }

    /**
     * Sets the parameter name for the filter id parameter.
     *
     * @param filterIdParameterName The parameter name for the filter id
     *            parameter.
     */
    public void setFilterIdParameterName(String filterIdParameterName)
    {
        myFilterIdParameterName = filterIdParameterName;
    }

    /**
     * Sets the unique id of the subscription.
     *
     * @param streamId The unique id of the subscription.
     */
    public void setStreamId(UUID streamId)
    {
        myStreamId = streamId;
    }

    /**
     * Sets the url to use to get new data.
     *
     * @param streamUrl The url to use to get new data.
     */
    public void setStreamUrl(URL streamUrl)
    {
        myStreamUrl = streamUrl;
    }
}
