package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A COLLADA accessor.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Accessor
{
    /** The stride. */
    @XmlAttribute(name = "stride")
    private int myStride;

    /**
     * Gets the stride.
     *
     * @return the stride
     */
    public int getStride()
    {
        return myStride;
    }
}
