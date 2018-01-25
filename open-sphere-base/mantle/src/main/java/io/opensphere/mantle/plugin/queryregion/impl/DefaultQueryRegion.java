package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;

/**
 * Default implementation for {@link QueryRegion}.
 */
public class DefaultQueryRegion implements QueryRegion
{
    /** The geometries. */
    private final Collection<? extends PolygonGeometry> myGeometries;

    /**
     * Mapping of type keys for layers associated with the query to filters for
     * those layers.
     */
    private final Map<? extends String, ? extends DataFilter> myTypeKeyToFilterMap;

    /** The times that the query region is valid. */
    private final Collection<? extends TimeSpan> myValidTimes;

    /** The ID of the query region. */
    private volatile String myId;

    /**
     * Constructor.
     *
     * @param geometries The geometries that compose the query region.
     * @param validTimes The times that the query region is valid.
     * @param typeKeyToFilterMap Mapping of type keys for layers associated with
     *            the query to filters for those layers.
     */
    public DefaultQueryRegion(Collection<? extends PolygonGeometry> geometries, Collection<? extends TimeSpan> validTimes,
            Map<? extends String, ? extends DataFilter> typeKeyToFilterMap)
    {
        myGeometries = New.unmodifiableCollection(Utilities.checkNull(geometries, "geometries"));
        myValidTimes = New.unmodifiableCollection(Utilities.checkNull(validTimes, "validTimes"));
        myTypeKeyToFilterMap = New.unmodifiableMap(Utilities.checkNull(typeKeyToFilterMap, "typeKeyToFilterMap"));
    }

    @Override
    public boolean appliesToType(String typeKey)
    {
        return myTypeKeyToFilterMap.isEmpty() || myTypeKeyToFilterMap.containsKey(typeKey);
    }

    @Override
    public Map<? extends String, ? extends DataFilter> getTypeKeyToFilterMap()
    {
        return myTypeKeyToFilterMap;
    }

    @Override
    public Collection<? extends PolygonGeometry> getGeometries()
    {
        return myGeometries;
    }

    @Override
    public Collection<? extends String> getTypeKeys()
    {
        return myTypeKeyToFilterMap.keySet();
    }

    @Override
    public Collection<? extends TimeSpan> getValidTimes()
    {
        return myValidTimes;
    }

    @Override
    public String getId()
    {
        return myId;
    }

    /**
     * Sets the id.
     *
     * @param id the id
     */
    public void setId(String id)
    {
        myId = id;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(getClass().getSimpleName()).append(" [");
        for (PolygonGeometry geom : getGeometries())
        {
            if (geom.getVertices().size() < 10)
            {
                sb.append(geom).append(' ');
            }
            else
            {
                sb.append(geom.getClass().getSimpleName()).append(" [").append(geom.getVertices().size()).append(" vertices] ");
            }
        }
        for (TimeSpan timeSpan : getValidTimes())
        {
            sb.append(timeSpan).append(' ');
        }
        sb.append(myTypeKeyToFilterMap);
        return sb.toString();
    }
}
