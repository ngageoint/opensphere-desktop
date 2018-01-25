package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA material.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Material
{
    /** The ID. */
    @XmlAttribute(name = "id")
    private String myId;

    /** The name. */
    @XmlAttribute(name = "name")
    private String myName;

    /** The instance effect. */
    @XmlElement(name = "instance_effect")
    private InstanceEffect myInstanceEffect;

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
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the instanceEffect.
     *
     * @return the instanceEffect
     */
    public InstanceEffect getInstanceEffect()
    {
        return myInstanceEffect;
    }
}
