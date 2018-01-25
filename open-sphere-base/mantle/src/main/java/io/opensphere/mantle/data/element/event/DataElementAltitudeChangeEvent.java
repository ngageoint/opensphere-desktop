package io.opensphere.mantle.data.element.event;

/**
 * The Class DataElementSelectionChangeEvent.
 */
public class DataElementAltitudeChangeEvent extends AbstractDataElementChangeEvent
{
    /** The new color. */
    private final float myAltitude;

    /**
     * Instantiates a new data element altitude change event.
     *
     * @param regId the registry id
     * @param dtKey the data type key
     * @param newAltitude - the new altitude
     * @param source the instigator of the change
     */
    public DataElementAltitudeChangeEvent(long regId, String dtKey, float newAltitude, Object source)
    {
        super(regId, dtKey, source);
        myAltitude = newAltitude;
    }

    /**
     * Gets the new altitude.
     *
     * @return the altitude
     */
    public float getAltitude()
    {
        return myAltitude;
    }

    @Override
    public String getDescription()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("DataElement ").append(getRegistryId()).append(" type ").append(getDataTypeKey()).append("  Altitude: ")
                .append(myAltitude).append(" by ");
        sb.append(getSource() == null ? "NULL" : getSource().getClass().getName());
        return sb.toString();
    }
}
