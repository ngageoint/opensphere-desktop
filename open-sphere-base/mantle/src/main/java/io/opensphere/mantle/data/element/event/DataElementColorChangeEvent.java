package io.opensphere.mantle.data.element.event;

import java.awt.Color;

/**
 * The Class DataElementSelectionChangeEvent.
 */
public class DataElementColorChangeEvent extends AbstractDataElementChangeEvent
{
    /** The new color. */
    private final Color myColor;

    /**
     * Instantiates a new data element color change event.
     *
     * @param regId the registry id
     * @param dtKey the data type key
     * @param newColor - the new color
     * @param source the instigator of the change
     */
    public DataElementColorChangeEvent(long regId, String dtKey, Color newColor, Object source)
    {
        super(regId, dtKey, source);
        myColor = newColor;
    }

    /**
     * Gets the new color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor;
    }

    @Override
    public String getDescription()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append("DataElement ").append(getRegistryId()).append(" type ").append(getDataTypeKey()).append("  Color: ")
                .append(myColor.toString()).append(" by ");
        sb.append(getSource() == null ? "NULL" : getSource().getClass().getName());
        return sb.toString();
    }
}
