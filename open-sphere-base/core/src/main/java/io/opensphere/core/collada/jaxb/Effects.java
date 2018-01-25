package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * COLLADA effects.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Effects
{
    /** The effects. */
    @XmlElement(name = "effect")
    private List<Effect> myEffects;

    /**
     * Gets the effects.
     *
     * @return the effects
     */
    public List<Effect> getEffects()
    {
        return myEffects;
    }
}
