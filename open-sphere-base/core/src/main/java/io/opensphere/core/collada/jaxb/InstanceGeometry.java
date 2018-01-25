package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A reference to a COLLADA geometry.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class InstanceGeometry
{
    /** The id. */
    @XmlAttribute(name = "id")
    private String myId;

    /** The url for the geometry definition. */
    @XmlAttribute(name = "url")
    private String myUrl;

    /** The bind material. */
    @XmlElement(name = "bind_material")
    private BindMaterial myBindMaterial;

    /**
     * Get the id.
     *
     * @return The id.
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Get the url.
     *
     * @return The url.
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Set the url.
     *
     * @param url The url.
     */
    public void setUrl(String url)
    {
        myUrl = url;
    }

    /**
     * Gets the bindMaterial.
     *
     * @return the bindMaterial
     */
    public BindMaterial getBindMaterial()
    {
        return myBindMaterial;
    }
}
