package io.opensphere.mantle.plugin.queryregion.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A model for an association of query areas and filters in a state file.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class QueryEntry
{
    /** The id of the query area. */
    @XmlAttribute(name = "areaId")
    private String myAreaId;

    /** The filter that applies to the query area. */
    @XmlAttribute(name = "filterId")
    private String myFilterId;

    /** If this area is treated as inclusive. */
    @XmlAttribute(name = "includeArea")
    private boolean myIncludeArea;

    /** The layer that the query area applies to. */
    @XmlAttribute(name = "layerId")
    private String myLayerId;

    /**
     * Get the id.
     *
     * @return The id.
     */
    public String getAreaId()
    {
        return myAreaId;
    }

    /**
     * Accessor for the filterId.
     *
     * @return The filterId.
     */
    public String getFilterId()
    {
        return myFilterId;
    }

    /**
     * Accessor for the layerId.
     *
     * @return The layerId.
     */
    public String getLayerId()
    {
        return myLayerId;
    }

    /**
     * Get the layers.
     *
     * @return The layers.
     */
    public String getLayers()
    {
        return myLayerId;
    }

    /**
     * Get if this is an inclusive area (otherwise exclusive).
     *
     * @return {@code true} if inclusive, {@code false} if exclusive.
     */
    public boolean isIncludeArea()
    {
        return myIncludeArea;
    }

    /**
     * Mutator for the areaId.
     *
     * @param areaId The areaId to set.
     */
    public void setAreaId(String areaId)
    {
        myAreaId = areaId;
    }

    /**
     * Mutator for the filterId.
     *
     * @param filterId The filterId to set.
     */
    public void setFilterId(String filterId)
    {
        myFilterId = filterId;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id)
    {
        myAreaId = id;
    }

    /**
     * Set if this area is treated as inclusive.
     *
     * @param includeArea The include area.
     */
    public void setIncludeArea(boolean includeArea)
    {
        myIncludeArea = includeArea;
    }

    /**
     * Set the layer id.
     *
     * @param layerId The layer id.
     */
    public void setLayerId(String layerId)
    {
        myLayerId = layerId;
    }
}
