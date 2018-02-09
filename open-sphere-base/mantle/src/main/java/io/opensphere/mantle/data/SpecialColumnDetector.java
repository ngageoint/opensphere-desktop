package io.opensphere.mantle.data;

/** A functional interface used to detect and mark special columns. */
public interface SpecialColumnDetector
{
    /**
     * Tests to determine if the supplied column is a some kind of special
     * column, and if so, modifies the supplied metadata object to mark it as
     * such. If the column is special, returns true, otherwise, returns false.
     *
     * @param layerInfo the layer metadata to update if the supplied column
     *            represents a special key.
     * @param columnName the <code>column</code> to test and possibly mark.
     * @return true if the supplied <code>column</code> is a special column,
     *         false otherwise.
     */
    boolean markSpecialColumn(MetaDataInfo layerInfo, String columnName);

    /**
     * Tests to determine if the supplied column is a some kind of special
     * column, and if so returns the special key type.
     *
     * @param columnName the <code>column</code> to test and possibly mark.
     * @return the special key or null if it wasn't detected
     */
    SpecialKey detectColumn(String columnName);
}
