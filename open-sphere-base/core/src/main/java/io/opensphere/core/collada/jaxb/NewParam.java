package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA new param.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class NewParam
{
    /** The SID. */
    @XmlAttribute(name = "sid")
    private String mySid;

    /** The surface. */
    @XmlElement(name = "surface")
    private Surface mySurface;

    /**
     * Gets the sid.
     *
     * @return the sid
     */
    public String getSid()
    {
        return mySid;
    }

    /**
     * Gets the surface.
     *
     * @return the surface
     */
    public Surface getSurface()
    {
        return mySurface;
    }
}
