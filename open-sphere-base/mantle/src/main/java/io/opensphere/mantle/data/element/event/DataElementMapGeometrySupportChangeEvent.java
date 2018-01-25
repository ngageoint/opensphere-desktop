package io.opensphere.mantle.data.element.event;

import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * The Class DataElementMapGeometrySupportChangeEvent.
 */
public class DataElementMapGeometrySupportChangeEvent extends AbstractDataElementChangeEvent
{
    /** The map geometry support. */
    private final MapGeometrySupport myMapGeometrySupport;

    /**
     * Instantiates a new data element map geometry support change event.
     *
     * @param regId the registry id
     * @param dtKey the data type key
     * @param mgs the {@link MapGeometrySupport}
     * @param source the instigator of the change
     */
    public DataElementMapGeometrySupportChangeEvent(long regId, String dtKey, MapGeometrySupport mgs, Object source)
    {
        super(regId, dtKey, source);
        myMapGeometrySupport = mgs;
    }

    @Override
    public String getDescription()
    {
        return toString();
    }

    /**
     * Gets the {@link MapGeometrySupport}.
     *
     * @return the map geometry support
     */
    public MapGeometrySupport getMapGeometrySupport()
    {
        return myMapGeometrySupport;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("DataElement ").append(getRegistryId()).append(" type ").append(getDataTypeKey())
                .append("  MapGeometrySupport Changed by ");
        sb.append(getSource() == null ? "NULL" : getSource().getClass().getName());
        return sb.toString();
    }
}
