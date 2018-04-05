package io.opensphere.core.common.geospatial.model;

import java.awt.geom.Point2D;

import io.opensphere.core.common.coordinate.math.strategy.Haversine;
import io.opensphere.core.common.geospatial.model.interfaces.IDataEllipse;
import io.opensphere.core.common.math.Matrix;

/**
 * Represents an ellipse around a data point. Used as a transfer object between
 * the ogc server and any clients that want to acquire feature results in a more
 * optimzed way. This class is used on both client and server side.
 *
 */
public class DataEllipse extends DataPoint implements IDataEllipse
{

    private static final long serialVersionUID = 1L;

    /**
     * Semi-Major Axis, nautical miles
     */
    protected double major;

    /**
     * Semi-Minor Axis, nautical miles
     */
    protected double minor;

    /**
     * Orientation angle, degrees
     */
    protected double orient;

    /**
     * Covariance matrix. May be null.
     */
    private Matrix covariance;

    /**
     * Default constructor to support serialization
     */
    public DataEllipse()
    {
        super();
        this.covariance = null;
    }

    /**
     * @param maj, nm
     * @param min, nm
     * @param orient, degs
     * @param lon, degs
     * @param lat, degs
     *
     */
    public DataEllipse(double maj, double min, double orient, double lon, double lat)
    {
        super(lat, lon);
        this.major = maj;
        this.minor = min;
        this.orient = orient;
        this.covariance = null;
    }

    /**
     * Set the semi-major axis
     *
     * @param val, nm
     */
    @Override
    public void setSemiMajorAxis(double val)
    {
        this.major = val;
    }

    /**
     * Get the semi-major axis.
     *
     * @return nm
     */
    @Override
    public double getSemiMajorAxis()
    {
        return this.major;
    }

    /**
     * Set the semi-minor axis
     *
     * @param val, nm
     */
    @Override
    public void setSemiMinorAxis(double val)
    {
        this.minor = val;
    }

    /**
     * Get the semi-minor axis.
     *
     * @return nm
     */
    @Override
    public double getSemiMinorAxis()
    {
        return this.minor;
    }

    /**
     * Set the orientation angle in degrees from North
     *
     * @param val, degs
     */
    @Override
    public void setOrientation(double val)
    {
        this.orient = val;
    }

    /**
     * Get the orientation angle in degrees from North
     *
     * @return degs
     */
    @Override
    public double getOrientation()
    {
        return this.orient;
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
        buf.append("\n--Semi-Major Axis: " + major);
        buf.append("\n--Semi-Minor Axis: " + minor);
        buf.append("\n--Orientation: " + orient);
        buf.append("\n--Properties:");
        for (int i = 0; i < keys.size(); i++)
        {
            buf.append("\n----" + keys.get(i) + ": " + getProperty(keys.get(i)));
        }
        return buf.toString();
    }

    @Override
    public DataEllipse clone()
    {
        DataEllipse newPoint = new DataEllipse();
        newPoint.setFeatureId(getFeatureId());
        newPoint.setDate(getDate());
        newPoint.setStartDate(getStartDate());
        newPoint.setEndDate(getEndDate());
        newPoint.setId(getId());
        newPoint.setLat(getLat());
        newPoint.setLon(getLon());
        newPoint.setSemiMajorAxis(getSemiMajorAxis());
        newPoint.setSemiMinorAxis(getSemiMinorAxis());
        newPoint.setOrientation(getOrientation());
        newPoint.setColor(getColor());
        for (int i = 0; i < keys.size(); i++)
        {
            newPoint.setProperty(keys.get(i), getProperty(keys.get(i)));
        }
        return newPoint;
    }

    /**
     * Calculates the minimum, axis-aligned bounding box for this ellipse.
     * Populates lowerLeft and upperRight.
     */
    public void getBoundingBox(Point2D.Double lowerLeft, Point2D.Double upperRight)
    {
        Matrix cov = getCovarianceMatrix();

        double dLat = Math.sqrt(cov.get(0, 0)) * 1852.0;
        double dLon = Math.sqrt(cov.get(1, 1)) * 1852.0;

        double scaleFactor = Math.cos(Math.toRadians(lat));

        double metersPerDegreeAtEquator = (2.0 * Math.PI * Haversine.R) / 360.0;
        double metersPerDegreeAtLatitude = scaleFactor * metersPerDegreeAtEquator;

        double d = dLon / metersPerDegreeAtLatitude;
        double a = dLat / metersPerDegreeAtEquator;

        if (lon + d > 180.0)
        {
            upperRight.x = (lon + d) - 360.0;
        }
        else
        {
            upperRight.x = lon + d;
        }

        if (lon - d <= -180.0)
        {
            lowerLeft.x = (lon - d) + 360.0;
        }
        else
        {
            lowerLeft.x = lon - d;
        }

        upperRight.y = Math.min(lat + a, 90.0);
        lowerLeft.y = Math.max(lat - a, -90.0);
    }

    public Matrix getCovarianceMatrix()
    {
        if (covariance == null)
        {
            double sinTheta = Math.sin(Math.toRadians(orient));
            double cosTheta = Math.cos(Math.toRadians(orient));

            Matrix covarianceMatrix = new Matrix(2, 2);
            covarianceMatrix.set(0, 0, major * major * cosTheta * cosTheta + minor * minor * sinTheta * sinTheta);
            covarianceMatrix.set(0, 1, (major * major - minor * minor) * cosTheta * sinTheta);
            covarianceMatrix.set(1, 0, (major * major - minor * minor) * cosTheta * sinTheta);
            covarianceMatrix.set(1, 1, major * major * sinTheta * sinTheta + minor * minor * cosTheta * cosTheta);

            covariance = covarianceMatrix;
        }

        return covariance;
    }
}
