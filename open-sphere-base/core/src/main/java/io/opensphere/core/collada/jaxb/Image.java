package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA image.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Image
{
    /** The ID. */
    @XmlAttribute(name = "id")
    private String myId;

    /** The image path. */
    @XmlElement(name = "init_from")
    private String myInitFrom;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the initFrom.
     *
     * @return the initFrom
     */
    public String getInitFrom()
    {
        return myInitFrom;
    }
}
