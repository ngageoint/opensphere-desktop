package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * A collection of query entries, for XML convenience.
 */
@XmlRootElement(name = "queryEntries")
@XmlAccessorType(XmlAccessType.NONE)
public class QueryEntries
{
    /** The query entries. */
    @XmlElement(name = "queryEntry")
    private Collection<QueryEntry> myQueryEntries = New.collection();

    /**
     * Get the query entries.
     *
     * @return The query entries.
     */
    public Collection<QueryEntry> getQueryEntries()
    {
        return myQueryEntries;
    }

    /**
     * Set the query entries.
     *
     * @param queryEntries The query entries.
     */
    public void setQueryEntries(Collection<QueryEntry> queryEntries)
    {
        myQueryEntries = queryEntries;
    }
}
