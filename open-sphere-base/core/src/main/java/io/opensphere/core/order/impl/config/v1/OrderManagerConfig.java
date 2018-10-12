package io.opensphere.core.order.impl.config.v1;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gnu.trove.map.TObjectIntMap;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.util.collections.New;

/** Configuration for an order manager. */
@XmlRootElement(name = "OrderManager")
@XmlAccessorType(XmlAccessType.NONE)
public class OrderManagerConfig
{
    /** The category for this manager. */
    @XmlElement(name = "Category")
    private OrderCategoryConfig myCategory;

    /** The family for this manager. */
    @XmlAttribute(name = "family")
    private String myFamily;

    /** The orders and their respective participants. */
    @XmlElement(name = "Entry")
    private List<OrderManagerParticipant> myParticipants;

    /**
     * Constructor.
     *
     * @param categoryConfig The category config.
     * @param family The order family.
     * @param participantMap map of participant order to id.
     */
    public OrderManagerConfig(OrderCategoryConfig categoryConfig, String family,
            TObjectIntMap<OrderParticipantKey> participantMap)
    {
        myCategory = categoryConfig;
        myFamily = family;

        if (participantMap != null && !participantMap.isEmpty())
        {
            myParticipants = New.list(participantMap.size());
            participantMap.forEachEntry((key, order) ->
            {
                myParticipants.add(new OrderManagerParticipant(key.getId(), order));
                return true;
            });
            Collections.sort(myParticipants, OrderManagerParticipant.ourReverseCompareByOrder);
        }
        else
        {
            myParticipants = New.list();
        }
    }

    /** JAXB constructor. */
    protected OrderManagerConfig()
    {
    }

    /**
     * Get the category.
     *
     * @return The category.
     */
    public OrderCategoryConfig getCategory()
    {
        return myCategory;
    }

    /**
     * Get the family.
     *
     * @return The family.
     */
    public String getFamily()
    {
        return myFamily;
    }

    /**
     * Get the participants.
     *
     * @return The participants.
     */
    public List<OrderManagerParticipant> getParticipants()
    {
        return myParticipants;
    }
}
