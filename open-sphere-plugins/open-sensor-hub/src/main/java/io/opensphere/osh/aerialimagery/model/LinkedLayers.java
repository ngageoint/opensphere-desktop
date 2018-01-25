package io.opensphere.osh.aerialimagery.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * Contains all the linked layers.
 */
@XmlRootElement(name = "LinkedLayers")
@XmlAccessorType(XmlAccessType.NONE)
public class LinkedLayers
{
    /**
     * The list of linked layers.
     */
    @XmlElement(name = "LinkedLayer")
    private final List<LinkedLayer> myLinkedLayers = New.list();

    /**
     * Gets the list of linked layers.
     *
     * @return The list of linked layers.
     */
    public List<LinkedLayer> getLinkedLayers()
    {
        return myLinkedLayers;
    }
}
