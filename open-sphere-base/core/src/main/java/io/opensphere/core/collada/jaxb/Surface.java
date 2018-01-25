package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA surface.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Surface
{
    /** The image path. */
    @XmlElement(name = "init_from")
    private String myInitFrom;

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
