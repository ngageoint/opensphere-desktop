package io.opensphere.mantle.data.element.event;

/**
 * The Class DataElementSelectionChangeEvent.
 */
public class DataElementSelectionChangeEvent extends AbstractDataElementChangeEvent
{
    /** The selected flag. */
    private final boolean mySelected;

    /**
     * Instantiates a new data element selected change event.
     *
     * @param regId the registry id
     * @param dtKey the data type key
     * @param selected - true if selected, false if not
     * @param source the instigator of the change
     */
    public DataElementSelectionChangeEvent(long regId, String dtKey, boolean selected, Object source)
    {
        super(regId, dtKey, source);
        mySelected = selected;
    }

    @Override
    public String getDescription()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("DataElement ").append(getRegistryId()).append(" type ").append(getDataTypeKey()).append("  Selected: ")
                .append(mySelected).append(" by ");
        sb.append(getSource() == null ? "NULL" : getSource().getClass().getName());
        return sb.toString();
    }

    /**
     * Checks if is selected.
     *
     * @return true, if is selected
     */
    public boolean isSelected()
    {
        return mySelected;
    }
}
