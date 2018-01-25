package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * An instance material.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class InstanceMaterial
{
    /** The symbol. */
    @XmlAttribute(name = "symbol")
    private String mySymbol;

    /** The target. */
    @XmlAttribute(name = "target")
    private String myTarget;

    /**
     * Gets the symbol.
     *
     * @return the symbol
     */
    public String getSymbol()
    {
        return mySymbol;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget()
    {
        return myTarget;
    }
}
