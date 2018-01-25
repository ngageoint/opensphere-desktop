package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA technique base class.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class TechniqueBase
{
    /** The diffuse. */
    @XmlElement(name = "diffuse")
    private ColorBase myDiffuse;

    /** The specular. */
    @XmlElement(name = "specular")
    private ColorBase mySpecular;

    /** The transparent. */
    @XmlElement(name = "transparent")
    private ColorBase myTransparent;

    /**
     * Gets the diffuse.
     *
     * @return the diffuse
     */
    public ColorBase getDiffuse()
    {
        return myDiffuse;
    }

    /**
     * Gets the specular.
     *
     * @return the specular
     */
    public ColorBase getSpecular()
    {
        return mySpecular;
    }

    /**
     * Gets the transparent.
     *
     * @return the transparent
     */
    public ColorBase getTransparent()
    {
        return myTransparent;
    }
}
