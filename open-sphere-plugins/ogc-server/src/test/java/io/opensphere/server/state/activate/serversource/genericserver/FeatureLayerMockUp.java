package io.opensphere.server.state.activate.serversource.genericserver;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A mock up jaxb object used to help the WfsUrlRetrieverTest.
 *
 */
@XmlRootElement(name = "layer")
@XmlAccessorType(XmlAccessType.FIELD)
public class FeatureLayerMockUp
{
    /**
     * The layer type.
     */
    @XmlAttribute(name = "type")
    private String myType = "wfs";

    /** The server url. */
    @XmlElement(name = "url")
    private String myUrl;

    /**
     * Gets the layer type.
     *
     * @return The layer type.
     */
    public String getType()
    {
        return myType;
    }

    /**
     * Gets the url.
     *
     * @return The url.
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Sets the layer type.
     *
     * @param type The layer type.
     */
    public void setType(String type)
    {
        myType = type;
    }

    /**
     * Sets the url.
     *
     * @param url The url.
     */
    public void setUrl(String url)
    {
        myUrl = url;
    }
}
