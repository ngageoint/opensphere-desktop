package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A COLLADA unit.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Unit
{
    /** The name. */
    @XmlAttribute(name = "name")
    private String myName;

    /** The meter. */
    @XmlAttribute(name = "meter")
    private double myMeter;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the meter.
     *
     * @return the meter
     */
    public double getMeter()
    {
        return myMeter;
    }
}
