package io.opensphere.kml.common.model;

import io.opensphere.core.event.AbstractMultiStateEvent;

/**
 * An event for KML data being ready.
 */
public class KMLDataEvent extends AbstractMultiStateEvent
{
    /** The data source. */
    private final KMLDataSource myDataSource;

    /** The KMLFeature. */
    private final KMLFeature myData;

    /** The old KMLFeature. */
    private KMLFeature myOldData;

    /**
     * Constructor.
     *
     * @param kmlDataSource The KML data source
     * @param data The data object
     */
    public KMLDataEvent(KMLDataSource kmlDataSource, KMLFeature data)
    {
        myDataSource = kmlDataSource;
        myData = data;
    }

    /**
     * Getter for data.
     *
     * @return the data
     */
    public KMLFeature getData()
    {
        return myData;
    }

    /**
     * Getter for dataSource.
     *
     * @return the dataSource
     */
    public KMLDataSource getDataSource()
    {
        return myDataSource;
    }

    @Override
    public String getDescription()
    {
        return myDataSource.getName();
    }

    /**
     * Gets the old data.
     *
     * @return the old data
     */
    public KMLFeature getOldData()
    {
        return myOldData;
    }

    /**
     * Sets the old data.
     *
     * @param oldData the new old data
     */
    public void setOldData(KMLFeature oldData)
    {
        myOldData = oldData;
    }

    @Override
    public String toString()
    {
        return "KMLDataEvent [myDataSource=" + myDataSource + ", myData=" + myData + "]";
    }
}
