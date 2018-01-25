package io.opensphere.core.common.geospatial.model;

import java.util.List;

import io.opensphere.core.common.geospatial.model.interfaces.IDataPoint;
import io.opensphere.core.common.geospatial.model.interfaces.IDataPolygon;

public class DataPolygon extends DataPointCollection implements IDataPolygon
{

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor needed to be serialization friendly
     */
    public DataPolygon()
    {
        super();
    }

    /**
     * Constructor to initialize with a list of data points
     *
     * @param dataPoints
     */
    public DataPolygon(List<IDataPoint> dataPoints)
    {
        super();
        points.addAll(dataPoints);
    }

    /**
     *
     * @return Polygon with four points {min lon, max lat, max lon, min lat}
     */
    @Override
    public DataPolygon getMinimumBoundingRectangleAsPolygon()
    {
        DataPolygon mbr = new DataPolygon();

        IDataPoint[] mbrPoints = getMinimumBoundingRectangle();

        mbr.addPoint(mbrPoints[0]);
        mbr.addPoint(mbrPoints[3]);
        mbr.addPoint(mbrPoints[2]);
        mbr.addPoint(mbrPoints[1]);
        return mbr;
    }

    @Override
    public IDataPolygon getSimpleConvexHull()
    {
        if (this.getPoints().size() < 4)
        {
            return this;
        }
        else
        {
            return getMinimumBoundingRectangleAsPolygon();
        }
    }
}
