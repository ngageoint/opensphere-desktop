package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA mesh, which contains the position/normal/etc of each vertex in the
 * mesh.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Mesh
{
    /** The lines. */
    @XmlElement(name = "lines")
    private List<Lines> myLines;

    /** The sources. */
    @XmlElement(name = "source")
    private List<Source> mySources;

    /** The triangles. */
    @XmlElement(name = "triangles")
    private List<Triangles> myTriangles;

    /** The vertices. */
    @XmlElement(name = "vertices")
    private List<Vertices> myVertices;

    /**
     * Get the lines.
     *
     * @return The lines.
     */
    public List<Lines> getLines()
    {
        return myLines;
    }

    /**
     * Get the sources.
     *
     * @return The sources.
     */
    public List<Source> getSources()
    {
        return mySources;
    }

    /**
     * Get the triangles.
     *
     * @return The triangles.
     */
    public List<Triangles> getTriangles()
    {
        return myTriangles;
    }

    /**
     * Get the vertices.
     *
     * @return The vertices.
     */
    public List<Vertices> getVertices()
    {
        return myVertices;
    }
}
