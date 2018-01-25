package io.opensphere.core.common.geospatial.model;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.common.geospatial.model.interfaces.IDataPoint;

/**
 * Represents a collection of data points Used as a transfer object between the
 * ogc server and any clients that want to acquire feature results in a more
 * optimized way.
 *
 */
public class DataPointCollection extends DataObject
        implements io.opensphere.core.common.geospatial.model.interfaces.IDataPointCollection
{
    private static final long serialVersionUID = 1L;

    /**
     * Grouping of data points
     */
    protected List<IDataPoint> points;

    /**
     * Default constructor needed to be serialization friendly
     */
    public DataPointCollection()
    {
        points = new ArrayList<IDataPoint>();
    }

    @Override
    public List<IDataPoint> getPoints()
    {
        return points;
    }

    @Override
    public void addPoint(IDataPoint point)
    {
        points.add(point);
    }

    @Override
    public void addPoints(List<? extends IDataPoint> points)
    {
        this.points.addAll(points);
    }

    @Override
    public IDataPoint[] getMinimumBoundingRectangle()
    {
        double minLon = Double.POSITIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;

        IDataPoint minLonPoint = new io.opensphere.core.common.geospatial.model.DataPoint();
        IDataPoint minLatPoint = new io.opensphere.core.common.geospatial.model.DataPoint();
        IDataPoint maxLonPoint = new io.opensphere.core.common.geospatial.model.DataPoint();
        IDataPoint maxLatPoint = new io.opensphere.core.common.geospatial.model.DataPoint();

        for (IDataPoint point : points)
        {
            double pointLon = point.getLon();
            double pointLat = point.getLat();

            if (pointLon < minLon)
            {
                minLonPoint = point;
                minLon = pointLon;
            }
            if (pointLat < minLat)
            {
                minLatPoint = point;
                minLat = pointLat;
            }
            if (pointLon > maxLon)
            {
                maxLonPoint = point;
                maxLon = pointLon;
            }
            if (pointLat > maxLat)
            {
                maxLatPoint = point;
                maxLat = pointLat;
            }
        }
        IDataPoint[] mbr = { minLonPoint, minLatPoint, maxLonPoint, maxLatPoint };

        return mbr;
    }
}
