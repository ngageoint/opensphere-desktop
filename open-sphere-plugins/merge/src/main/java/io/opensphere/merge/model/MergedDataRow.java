package io.opensphere.merge.model;

import java.io.Serializable;
import java.util.Map;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * Contains a row of the merged data.
 */
public class MergedDataRow implements Serializable
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The table data.
     */
    private final Map<String, Serializable> myData;

    /**
     * The geometry for the row.
     */
    private final MapGeometrySupport myGeometry;

    /**
     * The timespan for the row.
     */
    private final TimeSpan myTimeSpan;

    /**
     * Constructs a new row of merged data.
     *
     * @param data The merged table data.
     * @param geometry The geometry for the row.
     * @param timeSpan The time span for the row.
     */
    public MergedDataRow(Map<String, Serializable> data, MapGeometrySupport geometry, TimeSpan timeSpan)
    {
        myData = data;
        myGeometry = geometry;
        myTimeSpan = timeSpan;
    }

    /**
     * Gets the merged table data.
     *
     * @return the data.
     */
    public Map<String, Serializable> getData()
    {
        return myData;
    }

    /**
     * Gets the geometry.
     *
     * @return the geometry.
     */
    public MapGeometrySupport getGeometry()
    {
        return myGeometry;
    }

    /**
     * Gets the time span for the row.
     *
     * @return The timespan.
     */
    public TimeSpan getTimespan()
    {
        return myTimeSpan;
    }
}
