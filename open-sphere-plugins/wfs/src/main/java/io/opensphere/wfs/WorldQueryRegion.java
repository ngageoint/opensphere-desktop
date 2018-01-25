package io.opensphere.wfs;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.wfs.layer.WFSDataType;

/** A special query region that covers the whole world and all time. */
public final class WorldQueryRegion implements QueryRegion
{
    /** The load filter to use for the region. */
    private final DataFilter myFilter;

    /** The data type. */
    private final WFSDataType myType;

    /**
     * Constructor.
     *
     * @param type The data type.
     * @param loadFilter The load filter.
     */
    public WorldQueryRegion(WFSDataType type, DataFilter loadFilter)
    {
        myType = type;
        myFilter = loadFilter;
    }

    @Override
    public boolean appliesToType(String typeKey)
    {
        return myType.getTypeKey().equals(typeKey);
    }

    @Override
    public Collection<? extends PolygonGeometry> getGeometries()
    {
        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<GeographicPosition>();
        builder.setVertices(GeographicBoundingBox.WHOLE_GLOBE.getVertices());
        PolygonRenderProperties renderProperties = new DefaultPolygonRenderProperties(0, false, false);
        PolygonGeometry polygon = new PolygonGeometry(builder, renderProperties, (Constraints)null);
        return Collections.singleton(polygon);
    }

    @Override
    public Collection<? extends String> getTypeKeys()
    {
        return Collections.singleton(myType.getTypeKey());
    }

    @Override
    public Map<? extends String, ? extends DataFilter> getTypeKeyToFilterMap()
    {
        return Collections.singletonMap(myType.getTypeKey(), myFilter);
    }

    @Override
    public Collection<? extends TimeSpan> getValidTimes()
    {
        return TimeSpanList.TIMELESS;
    }

    @Override
    public String getId()
    {
        return "world";
    }
}
