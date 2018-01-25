package io.opensphere.core.order.impl;

import java.util.Objects;

import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.util.lang.HashCodeHelper;

/** Abstract base class for order participant keys. */
public class DefaultOrderParticipantKey implements OrderParticipantKey
{
    /** The category of the participant. */
    private final OrderCategory myCategory;

    /** The family of the participant. */
    private final String myFamily;

    /** The ID of the participant. */
    private final String myId;

    /**
     * Constructor.
     *
     * @param family The family of the participant.
     * @param category The category of the participant.
     * @param id The unique id of the participant.
     */
    public DefaultOrderParticipantKey(String family, OrderCategory category, String id)
    {
        myFamily = family;
        myCategory = category;
        myId = id;
    }

    @Override
    public OrderCategory getCategory()
    {
        return myCategory;
    }

    @Override
    public String getFamily()
    {
        return myFamily;
    }

    @Override
    public String getId()
    {
        return myId;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultOrderParticipantKey other = (DefaultOrderParticipantKey)obj;
        //@formatter:off
        return Objects.equals(myId, other.myId)
                && Objects.equals(myCategory, other.myCategory)
                && Objects.equals(myFamily, other.myFamily);
        //@formatter:on
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myId);
        result = prime * result + HashCodeHelper.getHashCode(myCategory);
        result = prime * result + HashCodeHelper.getHashCode(myFamily);
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(32);
        builder.append("Category : ");
        builder.append(myCategory);
        builder.append(" Family : ");
        builder.append(myFamily);
        builder.append(" Id : ");
        builder.append(myId);
        return builder.toString();
    }
}
