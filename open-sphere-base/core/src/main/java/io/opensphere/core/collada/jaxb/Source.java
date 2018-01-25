package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;

import io.opensphere.core.util.Utilities;

/**
 * A data source, which is just an array of floats and an id. This will be
 * referenced from an {@link Input} to give it meaning.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Source
{
    /** The float array. */
    @XmlList
    @XmlElement(name = "float_array")
    private float[] myFloatArray;

    /** The technique common. */
    @XmlElement(name = "technique_common")
    private TechniqueCommon myTechniqueCommon;

    /** The id. */
    @XmlAttribute(name = "id")
    private String myId;

    /**
     * Get the float array.
     *
     * @return The float array.
     */
    public float[] getFloatArray()
    {
        return Utilities.clone(myFloatArray);
    }

    /**
     * Gets the techniqueCommon.
     *
     * @return the techniqueCommon
     */
    public TechniqueCommon getTechniqueCommon()
    {
        return myTechniqueCommon;
    }

    /**
     * Get the id.
     *
     * @return The id.
     */
    public String getId()
    {
        return myId;
    }
}
