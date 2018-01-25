package io.opensphere.wps.streaming.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import io.opensphere.core.util.collections.New;

/**
 * Part of the subscription response from the server, contains information on
 * how to construct the stream url.
 */
public class ConfigurationUrl
{
    /**
     * The base url.
     */
    @XmlAttribute(name = "base")
    private String myBase;

    /**
     * The names of the request parameters to use, first one is the filter Id
     * parameter name.
     */
    @XmlElement(name = "requestParameter")
    private final List<String> myRequestParameters = New.list();

    /**
     * Gets the base url.
     *
     * @return The base url.
     */
    public String getBase()
    {
        return myBase;
    }

    /**
     * Gets the list of parameter names than can be used in the stream request.
     *
     * @return The names of the request parameters to use, first one is the
     *         filter Id parameter name.
     */
    public List<String> getRequestParameters()
    {
        return myRequestParameters;
    }

    /**
     * Sets the base url.
     *
     * @param base The base url.
     */
    public void setBase(String base)
    {
        myBase = base;
    }
}
