package io.opensphere.filterbuilder2.manager;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.util.collections.New;
import io.opensphere.filterbuilder.filter.v1.Filter;

/**
 * A group of filters for a data type simply for display purposes.
 */
class FilterGroup
{
    /** The list of filters. */
    private final List<Filter> myFilters = New.list();

    /** The layer name. */
    private final String myLayerName;

    /** The spatial filter. */
    private Geometry mySpatialFilter;

    /** The type key. */
    private final String myTypeKey;

    /**
     * Constructor.
     *
     * @param typeKey The type key
     * @param layerName The layer name
     */
    public FilterGroup(String typeKey, String layerName)
    {
        myTypeKey = typeKey;
        myLayerName = layerName;
    }

    /**
     * Gets the filters.
     *
     * @return the filters
     */
    public List<Filter> getFilters()
    {
        return myFilters;
    }

    /**
     * Gets the layerName.
     *
     * @return the layerName
     */
    public String getLayerName()
    {
        return myLayerName;
    }

    /**
     * Gets the spatialFilter.
     *
     * @return the spatialFilter
     */
    public Geometry getSpatialFilter()
    {
        return mySpatialFilter;
    }

    /**
     * Gets the typeKey.
     *
     * @return the typeKey
     */
    public String getTypeKey()
    {
        return myTypeKey;
    }

    /**
     * Sets the spatialFilter.
     *
     * @param spatialFilter the spatialFilter
     */
    public void setSpatialFilter(Geometry spatialFilter)
    {
        mySpatialFilter = spatialFilter;
    }
}
