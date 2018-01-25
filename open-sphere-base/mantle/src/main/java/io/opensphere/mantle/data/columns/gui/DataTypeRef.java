
package io.opensphere.mantle.data.columns.gui;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * DataTypeInfo probably should have an "isActive" method, but it doesn't.
 * This class compensates for the deficiency.
 */
public class DataTypeRef
{
    /** The type. */
    private DataTypeInfo type;
    /** Active flag. */
    private boolean active;

    /**
     * Cons.
     * @param t the type
     * @param a the active flag
     */
    public DataTypeRef(DataTypeInfo t, boolean a)
    {
        type = t;
        active = a;
    }

    /**
     * You guessed it.  Get the type.
     * @return the type
     */
    public DataTypeInfo getType()
    {
        return type;
    }

    /**
     * Right again.  Get the active flag.
     * @return the active flag
     */
    public boolean isActive()
    {
        return active;
    }
}
