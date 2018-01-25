package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import io.opensphere.core.util.collections.New;

/**
 * A COLLADA visual scene.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class VisualScene
{
    /** The id. */
    @XmlAttribute(name = "id")
    private String myId;

    /** The nodes. */
    @XmlElement(name = "node")
    private final List<Node> myNodes = New.list();

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
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Get the nodes.
     *
     * @return The nodes.
     */
    public List<Node> getNodes()
    {
        return myNodes;
    }
}
