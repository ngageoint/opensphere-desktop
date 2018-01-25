package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A bind material.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class BindMaterial
{
    /** The technique common. */
    @XmlElement(name = "technique_common")
    private TechniqueCommon myTechniqueCommon;

    /**
     * Gets the techniqueCommon.
     *
     * @return the techniqueCommon
     */
    public TechniqueCommon getTechniqueCommon()
    {
        return myTechniqueCommon;
    }
}
