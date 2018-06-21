package io.opensphere.mantle.infinity;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.time.TimeSpan;

/** Infinity query parameters. */
public class QueryParameters implements Serializable
{
    /** The {@link PropertyDescriptor} for query parameters. */
    public static final PropertyDescriptor<QueryParameters> PROPERTY_DESCRIPTOR = new PropertyDescriptor<>("QueryParameters",
            QueryParameters.class);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The geometry being queried. */
    private Geometry myGeometry;

    /** The time span being queried. */
    private TimeSpan myTimeSpan;

    /** The field on which to bin, or null. */
    private String myBinField;

    /** The class type of the bin field. */
    private Class<?> myBinFieldType;

    /** The width of the bins.  */
    private Double myBinWidth;

    /** The offset of the bin. */
    private Double myBinOffset;

    /** The min doc count of the bin. This represents the minimum count needed to return a bin. */
    private int myMinDocCount;

    /** The geometry field in the layer. */
    private String myGeomField;

    /** The time (or start time) field in the layer. */
    private String myTimeField;

    /** The end time field in the layer, or null. */
    private String myEndTimeField;

    /** The geometry type. */
    private GeometryType myGeometryType;

    /**
     * Gets the geometry.
     *
     * @return the geometry
     */
    public Geometry getGeometry()
    {
        return myGeometry;
    }

    /**
     * Sets the geometry.
     *
     * @param geometry the geometry
     */
    public void setGeometry(Geometry geometry)
    {
        myGeometry = geometry;
    }

    /**
     * Gets the time span.
     *
     * @return the time span
     */
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }

    /**
     * Sets the time span.
     *
     * @param timeSpan the time span
     */
    public void setTimeSpan(TimeSpan timeSpan)
    {
        myTimeSpan = timeSpan;
    }

    /**
     * Gets the bin field.
     *
     * @return the bin field
     */
    public String getBinField()
    {
        return myBinField;
    }

    /**
     * Sets the bin field.
     *
     * @param binField the bin field
     */
    public void setBinField(String binField)
    {
        myBinField = binField;
    }

    /**
     * Gets the bin field type.
     *
     * @return the bin field type
     */
    public Class<?> getBinFieldType()
    {
        return myBinFieldType;
    }

    /**
     * Sets the bin field type.
     *
     * @param binFieldType the new bin field type
     */
    public void setBinFieldType(Class<?> binFieldType)
    {
        myBinFieldType = binFieldType;
    }


    /**
     * Gets the bin's width
     *
     * @return the binWidth
     */
    public Double getBinWidth()
    {
        return myBinWidth;
    }

    /**
     * Sets the bin's width
     *
     * @param binWidth the binWidth to set
     */
    public void setBinWidth(Double binWidth)
    {
        myBinWidth = binWidth;
    }

    /**
     * Get the bin's offset
     *
     * @return the binOffset
     */
    public Double getBinOffset()
    {
        return myBinOffset;
    }

    /**
     * Set the bin's offset
     *
     * @param binOffset the binOffset to set
     */
    public void setBinOffset(Double binOffset)
    {
        myBinOffset = binOffset;
    }

    /**
     * Gets the geom field.
     *
     * @return the geom field
     */
    public String getGeomField()
    {
        return myGeomField;
    }

    /**
     * Sets the geometry field.
     *
     * @param geomField the geometry field
     */
    public void setGeomField(String geomField)
    {
        myGeomField = geomField;
    }

    /**
     * Gets the time field.
     *
     * @return the time field
     */
    public String getTimeField()
    {
        return myTimeField;
    }

    /**
     * Sets the time field.
     *
     * @param timeField the time field
     */
    public void setTimeField(String timeField)
    {
        myTimeField = timeField;
    }

    /**
     * Gets the end time field.
     *
     * @return the end time field
     */
    public String getEndTimeField()
    {
        return myEndTimeField;
    }

    /**
     * Sets the end time field.
     *
     * @param endTimeField the end time field
     */
    public void setEndTimeField(String endTimeField)
    {
        myEndTimeField = endTimeField;
    }

    /**
     * Gets the geometry type.
     *
     * @return the geometry type
     */
    public GeometryType getGeometryType()
    {
        return myGeometryType;
    }

    /**
     * Sets the geometry type.
     *
     * @param geometryType the geometry type
     */
    public void setGeometryType(GeometryType geometryType)
    {
        myGeometryType = geometryType;
    }

    /**
     * Get the minDocCount.
     *
     * @return the minDocCount
     */
    public int getMinDocCount()
    {
        return myMinDocCount;
    }

    /**
     * Set the minDocCount
     *
     * @param minDocCount the minDocCount to set
     */
    public void setMinDocCount(int minDocCount)
    {
        myMinDocCount = minDocCount;
    }

    /** Geometry type. */
    public enum GeometryType
    {
        /** Point type. */
        POINT,

        /** Shape type. */
        SHAPE;
    }
}
