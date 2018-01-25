package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * A collection of query areas, for XML convenience.
 */
@XmlRootElement(name = "queryAreas")
@XmlAccessorType(XmlAccessType.NONE)
public class QueryAreas
{
    /** The query areas. */
    @XmlElement(name = "queryArea")
    private Collection<QueryArea> myQueryAreas = New.collection();

    /**
     * Get the query areas.
     *
     * @return The query areas.
     */
    public Collection<QueryArea> getQueryAreas()
    {
        return myQueryAreas;
    }

    /**
     * Set the query areas.
     *
     * @param queryAreas The query areas.
     */
    public void setQueryAreas(Collection<QueryArea> queryAreas)
    {
        myQueryAreas = queryAreas;
    }
}
