package io.opensphere.core.common.geospatial.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.geospatial.model.interfaces.IDataPoint;

public class CompositeDataPoint extends io.opensphere.core.common.geospatial.model.DataPoint
{

    private static final Log LOGGER = LogFactory.getLog(CompositeDataPoint.class);

    protected List<IDataPoint> subPoints = null;

    public CompositeDataPoint()
    {
        super();
        subPoints = new ArrayList<IDataPoint>();
    }

    public boolean addPoint(IDataPoint point)
    {
        boolean ret = false;
        if (subPoints.size() == 0)
        {
            subPoints.add(point);
            recompute();
        }
        else if (subPoints.size() > 0)
        {
            subPoints.add(point);
            recompute();
            ret = true;
        }

        return ret;
    }

    public List<IDataPoint> getSubPoints()
    {
        return subPoints;
    }

    public void setSubPoints(List<IDataPoint> subPoints)
    {
        this.subPoints = subPoints;
    }

    public void recompute()
    {
        double totalLat = 0.0;
        double totalLon = 0.0;
        long time = 0;
        for (int i = 0; i < subPoints.size(); i++)
        {
            totalLat += subPoints.get(i).getLat();
            totalLon += subPoints.get(i).getLon();
            time += subPoints.get(i).getDate().getTime();
        }
        lat = totalLat / subPoints.size();
        lon = totalLon / subPoints.size();
        time = time / subPoints.size();
        setDate(new Date(time));
    }

}
