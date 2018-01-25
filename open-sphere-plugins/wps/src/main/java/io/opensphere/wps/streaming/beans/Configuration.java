package io.opensphere.wps.streaming.beans;

import javax.xml.bind.annotation.XmlElement;

/**
 * The object returned by the server from a subscription request.
 */
public class Configuration
{
    /**
     * The id to use in the stream url.
     */
    @XmlElement(name = "filterId")
    private String myFilterId;

    /**
     * Contains information pertaining to constructing the stream url.
     */
    @XmlElement(name = "url")
    private ConfigurationUrl myUrl;

    /**
     * Gets the id to use in the stream url.
     *
     * @return The id to use in the stream url.
     */
    public String getFilterId()
    {
        return myFilterId;
    }

    /**
     * Gets information pertaining to constructing the stream url.
     *
     * @return Contains information pertaining to constructing the stream url.
     */
    public ConfigurationUrl getUrl()
    {
        return myUrl;
    }

    /**
     * Sets the id to use in the stream url.
     *
     * @param filterId The id to use in the stream url.
     */
    public void setFilterId(String filterId)
    {
        myFilterId = filterId;
    }

    /**
     * Sets information pertaining to constructing the stream url.
     *
     * @param url Contains information pertaining to constructing the stream
     *            url.
     */
    public void setUrl(ConfigurationUrl url)
    {
        myUrl = url;
    }
}
