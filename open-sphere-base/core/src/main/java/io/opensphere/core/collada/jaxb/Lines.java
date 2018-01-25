package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;

import io.opensphere.core.util.Utilities;

/**
 * Line segments.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Lines
{
    /** The material. */
    @XmlAttribute(name = "material")
    private String myMaterial;

    /** The line count. */
    @XmlAttribute(name = "count")
    private int myCount;

    /** The inputs that describe the vertices of the lines. */
    @XmlElement(name = "input")
    private List<Input> myInputs;

    /**
     * The primitives, which are indices into the {@link Source}s referenced by
     * the {@link Input}s. These are mapped to the lines using the offsets in
     * the {@link Input}s.
     */
    @XmlList
    @XmlElement(name = "p")
    private int[] myPrimitives;

    /**
     * Gets the material.
     *
     * @return the material
     */
    public String getMaterial()
    {
        return myMaterial;
    }

    /**
     * Get the count.
     *
     * @return The count.
     */
    public int getCount()
    {
        return myCount;
    }

    /**
     * Get the inputs.
     *
     * @return The inputs.
     */
    public List<Input> getInputs()
    {
        return myInputs;
    }

    /**
     * Get the primitives.
     *
     * @return The primitives.
     */
    public int[] getPrimitives()
    {
        return Utilities.clone(myPrimitives);
    }
}
