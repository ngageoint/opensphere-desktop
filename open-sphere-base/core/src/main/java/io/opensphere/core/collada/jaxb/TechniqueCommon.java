package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA technique common.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class TechniqueCommon
{
    /** The accessor. */
    @XmlElement(name = "accessor")
    private Accessor myAccessor;

    /** The instance materials. */
    @XmlElement(name = "instance_material")
    private List<InstanceMaterial> myInstanceMaterials;

    /**
     * Gets the accessor.
     *
     * @return the accessor
     */
    public Accessor getAccessor()
    {
        return myAccessor;
    }

    /**
     * Gets the instanceMaterial.
     *
     * @return the instanceMaterial
     */
    public List<InstanceMaterial> getInstanceMaterials()
    {
        return myInstanceMaterials;
    }
}
