package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * A COLLADA node, which identifies a point of interest in a scene.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class Node
{
    /** The id of the node. */
    @XmlAttribute(name = "id")
    private String myId;

    /** The transformation matrix for this node. */
    @XmlElement(name = "matrix")
    @XmlList
    private double[] myMatrix;

    /** The geometries that are part of this node. */
    @XmlElement(name = "instance_geometry")
    private final List<InstanceGeometry> myInstanceGeometries = New.list();

    /** The child nodes that are part of this node. */
    @XmlElement(name = "node")
    private final List<Node> myNodes = New.list();

    /** The child nodes that are part of this node. */
    @XmlElement(name = "instance_node")
    private final List<InstanceNode> myInstanceNodes = New.list();

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
     * Get the instance geometries.
     *
     * @return The instance geometries.
     */
    public List<InstanceGeometry> getInstanceGeometries()
    {
        return myInstanceGeometries;
    }

    /**
     * Get the instance nodes.
     *
     * @return The instance nodes.
     */
    public List<InstanceNode> getInstanceNodes()
    {
        return myInstanceNodes;
    }

    /**
     * Get the matrix.
     *
     * @return The matrix.
     */
    public double[] getMatrix()
    {
        return myMatrix == null ? null : myMatrix.clone();
    }

    /**
     * Get the child nodes.
     *
     * @return The nodes.
     */
    public List<Node> getNodes()
    {
        return myNodes;
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

    /**
     * Set the matrix.
     *
     * @param matrix The matrix.
     */
    public void setMatrix(double[] matrix)
    {
        myMatrix = matrix == null ? null : matrix.clone();
    }
}
