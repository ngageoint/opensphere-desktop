package io.opensphere.mantle.data.element.event;

/**
 * The Class DataElementSelectionChangeEvent.
 */
public class DataElementLOBVsibilityChangeEvent extends AbstractDataElementChangeEvent
{
    /** The selected flag. */
    private final boolean myLobVisible;

    /**
     * Instantiates a new data element visible change event.
     *
     * @param regId the registry id
     * @param dtKey the data type key
     * @param lobVisible - true if lob visible, false if not
     * @param source the instigator of the change
     */
    public DataElementLOBVsibilityChangeEvent(long regId, String dtKey, boolean lobVisible, Object source)
    {
        super(regId, dtKey, source);
        myLobVisible = lobVisible;
    }

    @Override
    public String getDescription()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("DataElement ").append(getRegistryId()).append(" type ").append(getDataTypeKey()).append("  LOBVisible: ")
                .append(myLobVisible).append(" by ");
        sb.append(getSource() == null ? "NULL" : getSource().getClass().getName());
        return sb.toString();
    }

    /**
     * Checks if is LOB visible.
     *
     * @return true, if is LOB visible
     */
    public boolean isLOBVisible()
    {
        return myLobVisible;
    }
}
