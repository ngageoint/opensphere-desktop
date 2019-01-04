package io.opensphere.core.util.fx.tabpane.inputmap;

/**
 * A tri-state boolean used with OSKeyBinding.
 */
public enum OSOptionalBoolean
{
    /** The binding value for <code>true</code>. */
    TRUE,

    /** The binding value for <code>false</code>. */
    FALSE,

    /** The binding value for either <code>true</code> or <code>false</code>. */
    ANY;

    /**
     * Tests to determine if the supplied value is equal to the instance. If
     * supplied with <code>true</code>, matches either {@link #TRUE} or
     * {@link #ANY}. If supplied with <code>false</code>, matches either
     * {@link #FALSE} or {@link #ANY}.
     *
     * @param value the value to test for equality against this instance.
     * @return true if the supplied value matches according to the defined
     *         rules.
     */
    public boolean equals(boolean value)
    {
        if (this == ANY)
        {
            return true;
        }
        if (value && this == TRUE)
        {
            return true;
        }
        if (!value && this == FALSE)
        {
            return true;
        }
        return false;
    }
}
