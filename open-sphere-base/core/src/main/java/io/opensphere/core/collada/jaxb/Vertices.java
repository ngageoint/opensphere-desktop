package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Describes the vertices of a {@link Mesh}.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Vertices
{
    /** The id. */
    @XmlAttribute(name = "id")
    private String myId;

    /** The inputs. */
    @XmlElement(name = "input")
    private List<Input> myInputs;

    /**
     * Get the id.
     *
     * @return The id.
     */
    public String getId()
    {
        return myId;
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
}
