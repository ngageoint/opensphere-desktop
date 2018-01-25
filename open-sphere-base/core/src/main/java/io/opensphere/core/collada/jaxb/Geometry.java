package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA geometry.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Geometry
{
    /** The id. */
    @XmlAttribute(name = "id")
    private String myId;

    /** The mesh. */
    @XmlElement(name = "mesh")
    private Mesh myMesh;

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
     * Get the mesh.
     *
     * @return The mesh.
     */
    public Mesh getMesh()
    {
        return myMesh;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id)
    {
        myId = id;
    }
}
