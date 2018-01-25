package io.opensphere.mantle.data.element.event;

/**
 * The Class DataElementMetaDataValueChangeEvent.
 */
public class DataElementMetaDataValueChangeEvent extends AbstractDataElementChangeEvent
{
    /**
     * Instantiates a new data element altitude change event.
     *
     * @param regId the registry id
     * @param dtKey the data type key
     * @param source the instigator of the change
     */
    public DataElementMetaDataValueChangeEvent(long regId, String dtKey, Object source)
    {
        super(regId, dtKey, source);
    }

    @Override
    public String getDescription()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append("DataElement ").append(getRegistryId()).append(" type ").append(getDataTypeKey()).append(" by ");
        sb.append(getSource() == null ? "NULL" : getSource().getClass().getName());
        return sb.toString();
    }
}
