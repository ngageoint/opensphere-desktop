package io.opensphere.analysis.heatmap;

import java.util.List;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/** Mantle geometry plus other stuff. */
public class GeometryInfo
{
    /** The geometry. */
    private final MapGeometrySupport myGeometry;

    /** The data type key. */
    private final String myDataTypeKey;

    /** The meta data. */
    private final List<Object> myMetaData;

    /** The time span in which the data is valid. */
    private final TimeSpan myTimeSpan;

    /**
     * Constructor.
     *
     * @param geometry the geometry
     * @param dataTypeKey the data type key
     * @param metaData the meta data
     * @param timeSpan the time span in which the data is valid.
     */
    public GeometryInfo(MapGeometrySupport geometry, String dataTypeKey, List<Object> metaData, TimeSpan timeSpan)
    {
        myGeometry = geometry;
        myDataTypeKey = dataTypeKey;
        myMetaData = metaData;
        myTimeSpan = timeSpan;
    }

    /**
     * Gets the value of the {@link #myTimeSpan} field.
     *
     * @return the value stored in the {@link #myTimeSpan} field.
     */
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }

    /**
     * Gets the geometry.
     *
     * @return the geometry
     */
    public MapGeometrySupport getGeometry()
    {
        return myGeometry;
    }

    /**
     * Gets the dataTypeKey.
     *
     * @return the dataTypeKey
     */
    public String getDataTypeKey()
    {
        return myDataTypeKey;
    }

    /**
     * Gets the metaData.
     *
     * @return the metaData
     */
    public List<Object> getMetaData()
    {
        return myMetaData;
    }
}
