package io.opensphere.osh.aerialimagery.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains information of two OSH layers linked together.
 */
@XmlRootElement(name = "LinkedLayer")
@XmlAccessorType(XmlAccessType.NONE)
public class LinkedLayer
{
    /**
     * The type key of one of the linked layers.
     */
    @XmlElement(name = "linkedLayersTypeKey")
    private String myLinkedLayersTypeKey;

    /**
     * The type key of the other linked layer.
     */
    @XmlElement(name = "otherLinkedLayersTypeKey")
    private String myOtherLinkedLayersTypeKey;

    /**
     * Gets the type key of one of the linked layers.
     *
     * @return The type key of one of the linked layers.
     */
    public String getLinkedLayersTypeKey()
    {
        return myLinkedLayersTypeKey;
    }

    /**
     * Gets the type key of the other linked layer.
     *
     * @return The type key of the other linked layer.
     */
    public String getOtherLinkedLayersTypeKey()
    {
        return myOtherLinkedLayersTypeKey;
    }

    /**
     * Sets the type key of one of the linked layers.
     *
     * @param linkedLayersTypeKey The type key of one of the linked layers.
     */
    public void setLinkedLayersTypeKey(String linkedLayersTypeKey)
    {
        myLinkedLayersTypeKey = linkedLayersTypeKey;
    }

    /**
     * Sets the type key of the other linked layer.
     *
     * @param otherLinkedLayersTypeKey The type key of the other linked layer.
     */
    public void setOtherLinkedLayersTypeKey(String otherLinkedLayersTypeKey)
    {
        myOtherLinkedLayersTypeKey = otherLinkedLayersTypeKey;
    }
}
