package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA asset.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Asset
{
    /** The unit. */
    @XmlElement(name = "unit")
    private Unit myUnit;

    /** The up axis. */
    @XmlElement(name = "up_axis")
    private String myUpAxis;

    /**
     * Gets the unit.
     *
     * @return the unit
     */
    public Unit getUnit()
    {
        return myUnit;
    }

    /**
     * Gets the up axis.
     *
     * @return The up axis.
     */
    public String getUpAxis()
    {
        return myUpAxis;
    }
}
