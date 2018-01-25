package io.opensphere.core.terrain.util;

import java.util.List;

import io.opensphere.core.model.GeographicPolygon;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.collections.New;

/** Abstract base for elevation providers. */
public abstract class AbstractElevationProvider implements AbsoluteElevationProvider
{
    /**
     * Regions for which this provider provided elevations. If a region
     * straddles -180/180 longitude it should be divided into multiple regions.
     */
    private final List<? extends GeographicPolygon> myRegions;

    /**
     * Constructor.
     *
     * @param regions Regions for which this provider can provided elevations.
     */
    public AbstractElevationProvider(List<GeographicPolygon> regions)
    {
        myRegions = New.unmodifiableList(regions);
    }

    @Override
    public List<? extends GeographicPolygon> getRegions()
    {
        return myRegions;
    }

    @Override
    public boolean overlaps(GeographicPolygon polygon)
    {
        for (GeographicPolygon region : myRegions)
        {
            if (region.overlaps(polygon, 0.))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean providesForPosition(GeographicPosition position)
    {
        for (GeographicPolygon region : myRegions)
        {
            if (region.contains(position, 0.))
            {
                return true;
            }
        }
        return false;
    }
}
