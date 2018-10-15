package io.opensphere.mantle.data.impl;

import java.util.Objects;

import io.opensphere.mantle.data.ActiveGroupEntry;

/**
 * The Class DefaultActiveGroupEntry.
 */
public class DefaultActiveGroupEntry implements ActiveGroupEntry
{
    /** The active group id. */
    private final String myActiveGroupId;

    /** The active group name. */
    private final String myActiveGroupName;

    /**
     * Instantiates a new default active group entry.
     *
     * @param other the other
     */
    public DefaultActiveGroupEntry(ActiveGroupEntry other)
    {
        myActiveGroupName = other.getName();
        myActiveGroupId = other.getId();
    }

    /**
     * Instantiates a new default active group entry.
     *
     * @param activeGroupName the active group name
     * @param activeGroupId the active group id
     */
    public DefaultActiveGroupEntry(String activeGroupName, String activeGroupId)
    {
        myActiveGroupName = activeGroupName;
        myActiveGroupId = activeGroupId;
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
        DefaultActiveGroupEntry other = (DefaultActiveGroupEntry)obj;
        return Objects.equals(myActiveGroupId, other.myActiveGroupId);
    }

    @Override
    public String getId()
    {
        return myActiveGroupId;
    }

    @Override
    public String getName()
    {
        return myActiveGroupName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myActiveGroupId == null ? 0 : myActiveGroupId.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return myActiveGroupName + " [" + myActiveGroupId + "]\n";
    }
}
