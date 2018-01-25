package io.opensphere.analysis.table.renderers;

/**
 * Interface to get the key name for the given row and column.
 */
@FunctionalInterface
public interface KeyProvider
{
    /**
     * Gets the key name for the given row and column.
     *
     * @param row the row
     * @param column the column
     * @return the key name
     */
    String getKeyName(int row, int column);
}
