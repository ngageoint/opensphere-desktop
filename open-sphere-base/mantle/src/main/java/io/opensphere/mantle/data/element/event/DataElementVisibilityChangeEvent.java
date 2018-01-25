package io.opensphere.mantle.data.element.event;

/**
 * The Class DataElementSelectionChangeEvent.
 */
public class DataElementVisibilityChangeEvent extends AbstractDataElementChangeEvent
{
    /** The selected flag. */
    private final boolean myVisible;

    /**
     * Instantiates a new data element visible change event.
     *
     * @param regId the registry id
     * @param dtKey the data type key
     * @param visible - true if visible, false if not
     * @param source the instigator of the change
     */
    public DataElementVisibilityChangeEvent(long regId, String dtKey, boolean visible, Object source)
    {
        super(regId, dtKey, source);
        myVisible = visible;
    }

    @Override
    public String getDescription()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("DataElement ").append(getRegistryId()).append(" type ").append(getDataTypeKey()).append("  Visible: ")
                .append(myVisible).append(" by ");
        sb.append(getSource() == null ? "NULL" : getSource().getClass().getName());
        return sb.toString();
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    public boolean isVisible()
    {
        return myVisible;
    }
}
