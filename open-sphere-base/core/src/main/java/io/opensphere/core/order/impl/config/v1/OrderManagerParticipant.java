package io.opensphere.core.order.impl.config.v1;

import java.util.Comparator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/** Configuration for an order participant. */
@XmlRootElement(name = "Participant")
@XmlAccessorType(XmlAccessType.NONE)
public class OrderManagerParticipant
{
    /** The Constant ourCompareByOrder. */
    public static final Comparator<OrderManagerParticipant> ourCompareByOrder = (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder());

    /** The Constant ourReverseCompareByOrder. */
    public static final Comparator<OrderManagerParticipant> ourReverseCompareByOrder = (o1, o2) -> Integer.compare(o2.getOrder(), o1.getOrder());

    /** The family for this manager. */
    @XmlAttribute(name = "order")
    private int myOrder;

    /** The category for this manager. */
    @XmlAttribute(name = "val")
    private String myId;

    /**
     * Constructor.
     *
     * @param id the id
     * @param order the order
     */
    public OrderManagerParticipant(String id, int order)
    {
        myId = id;
        myOrder = order;
    }

    /** JAXB constructor. */
    protected OrderManagerParticipant()
    {
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public int getOrder()
    {
        return myOrder;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Sets the order.
     *
     * @param order the new order
     */
    public void setOrder(int order)
    {
        myOrder = order;
    }
}
