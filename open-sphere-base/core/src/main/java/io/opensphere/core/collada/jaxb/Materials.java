package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * COLLADA materials.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Materials
{
    /** The materials. */
    @XmlElement(name = "material")
    private List<Material> myMaterials;

    /**
     * Gets the materials.
     *
     * @return the materials
     */
    public List<Material> getMaterials()
    {
        return myMaterials;
    }
}
