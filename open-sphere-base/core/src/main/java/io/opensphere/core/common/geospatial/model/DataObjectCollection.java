package io.opensphere.core.common.geospatial.model;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.common.geospatial.model.interfaces.IDataObject;
import io.opensphere.core.common.geospatial.model.interfaces.IDataPoint;

/**
 * Represents a collection of data objects (geometries)
 */
public class DataObjectCollection extends DataObject
        implements io.opensphere.core.common.geospatial.model.interfaces.IDataObjectCollection
{
    private static final long serialVersionUID = 1L;

    /**
     * Grouping of geometries
     */
    protected List<IDataObject> geometries;

    /**
     * Default constructor needed to be serialization friendly
     */
    public DataObjectCollection()
    {
        geometries = new ArrayList<IDataObject>();
    }

    @Override
    public List<IDataObject> getGeometries()
    {
        return geometries;
    }

    @Override
    public void addGeometry(IDataObject geometry)
    {
        geometries.add(geometry);
    }

    @Override
    public void addGeometries(List<? extends IDataObject> geometries)
    {
        this.geometries.addAll(geometries);
    }

    public int getSize()
    {
        return this.geometries.size();
    }

    /**
     * The following are IDataPointCollection methods These should be removed
     * once IDataObjectCollection completely replaces IDataPointCollection
     */

    public List<IDataPoint> getPoints()
    {
        List<IDataPoint> points = new ArrayList<IDataPoint>();
        for (IDataObject geom : geometries)
        {
            if (geom instanceof IDataPoint)
            {
                points.add((IDataPoint)geom);
            }
        }
        return points;
    }

    public void addPoint(IDataPoint point)
    {
        geometries.add(point);
    }

    public void addPoints(List<? extends IDataPoint> points)
    {
        this.geometries.addAll(points);
    }
}
