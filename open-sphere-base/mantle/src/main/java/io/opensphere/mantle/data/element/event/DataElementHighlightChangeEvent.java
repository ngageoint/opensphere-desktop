package io.opensphere.mantle.data.element.event;

/**
 * The Class DataElementHighlightChangeEvent.
 */
public class DataElementHighlightChangeEvent extends AbstractDataElementChangeEvent
{
    /** The highlighted flag. */
    private final boolean myHighlighted;

    /**
     * Instantiates a new data element highlight change event.
     *
     * @param regId the registry id
     * @param dtKey the data type key
     * @param highlighted - true if highlighted, false if not
     * @param source the instigator of the change
     */
    public DataElementHighlightChangeEvent(long regId, String dtKey, boolean highlighted, Object source)
    {
        super(regId, dtKey, source);
        myHighlighted = highlighted;
    }

    @Override
    public String getDescription()
    {
        return toString();
    }

    /**
     * Checks if is highlighted.
     *
     * @return true, if is highlighted
     */
    public boolean isHighlighted()
    {
        return myHighlighted;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("DataElement ").append(getRegistryId()).append(" type ").append(getDataTypeKey()).append("  Highlighted: ")
                .append(myHighlighted).append(" by ");
        sb.append(getSource() == null ? "NULL" : getSource().getClass().getName());
        return sb.toString();
    }
}
