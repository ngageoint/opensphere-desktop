package io.opensphere.core.common.geospatial.model;

/**
 * Represents a point of data. Used as a transfer object between the ogc server
 * and any clients that want to acquire feature results in a more optimized way.
 *
 */
public class DataPoint extends DataObject implements io.opensphere.core.common.geospatial.model.interfaces.IDataPoint
{

    private static final long serialVersionUID = 1L;

    /**
     * Latitude of point
     */
    protected double lat;

    /**
     * Longitude of point
     */
    protected double lon;

    /**
     * Default constructor to support serialization
     */
    public DataPoint()
    {
        super();
    }

    /**
     * Constructor to initialize latitude and longitude
     *
     * @param lat
     * @param lon
     */
    public DataPoint(double lat, double lon)
    {
        super();
        this.lat = lat;
        this.lon = lon;
    }

    // Getters and setters
    @Override
    public double getLat()
    {
        return this.lat;
    }

    @Override
    public void setLat(double deg)
    {
        this.lat = deg;
    }

    @Override
    public double getLon()
    {
        return this.lon;
    }

    @Override
    public void setLon(double deg)
    {
        this.lon = deg;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("DataPoint:\n");
        buf.append("--Id: " + id);
        buf.append("\n--Start: " + startDate);
        buf.append("\n--End: " + endDate);
        buf.append("\n--Lon,Lat: " + lon + "," + lat);
        buf.append("\n--Properties:");
        for (int i = 0; i < keys.size(); i++)
        {
            buf.append("\n----" + keys.get(i) + ": " + getProperty(keys.get(i)));
        }
        return buf.toString();
    }

    @Override
    public DataPoint clone()
    {
        DataPoint newPoint = new DataPoint();
        newPoint.setFeatureId(getFeatureId());
        newPoint.setDate(getDate());
        newPoint.setStartDate(getStartDate());
        newPoint.setEndDate(getEndDate());
        newPoint.setId(getId());
        newPoint.setLat(getLat());
        newPoint.setLon(getLon());
        newPoint.setColor(getColor());
        for (int i = 0; i < keys.size(); i++)
        {
            newPoint.setProperty(keys.get(i), getProperty(keys.get(i)));
        }
        return newPoint;
    }
}
