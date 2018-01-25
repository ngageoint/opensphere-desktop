package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A COLLADA effect.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class InstanceEffect
{
    /** The URL. */
    @XmlAttribute(name = "url")
    private String myUrl;

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl()
    {
        return myUrl;
    }
}
