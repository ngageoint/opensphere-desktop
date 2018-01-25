package io.opensphere.csvcommon.ui.columndefinition.model;

/**
 * Represents a single row within the Before/After table.
 *
 */
public class BeforeAfterRow
{
    /**
     * The after value.
     */
    private String myAfterValue;

    /**
     * The before value.
     */
    private String myBeforeValue;

    /**
     * Gets the after value.
     *
     * @return The after value.
     */
    public String getAfterValue()
    {
        return myAfterValue;
    }

    /**
     * Gets the before value.
     *
     * @return The before value.
     */
    public String getBeforeValue()
    {
        return myBeforeValue;
    }

    /**
     * Sets the after value.
     *
     * @param afterValue The after value.
     */
    public void setAfterValue(String afterValue)
    {
        myAfterValue = afterValue;
    }

    /**
     * Sets the before value.
     *
     * @param beforeValue The before value.
     */
    public void setBeforeValue(String beforeValue)
    {
        myBeforeValue = beforeValue;
    }
}
