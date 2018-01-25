package io.opensphere.arcgis2.esri;

/** An object IDs response. */
public class ObjectIdsResponse
{
    /** The object IDs. */
    private long[] myObjectIds;

    /**
     * Gets the objectIds.
     *
     * @return the objectIds
     */
    public long[] getObjectIds()
    {
        return myObjectIds;
    }

    /**
     * Sets the objectIds.
     *
     * @param objectIds the objectIds
     */
    public void setObjectIds(long[] objectIds)
    {
        myObjectIds = objectIds;
    }
}
