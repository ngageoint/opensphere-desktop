package io.opensphere.csvcommon.common;

import java.util.Collection;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.importer.config.SpecialColumn;

/**
 * Utilities.
 */
public final class Utilities
{
    /**
     * Creates a map of column index to CSVSpecialColumn.
     *
     * @param specialColumns the list of CSVSpecialColumn objects
     * @return the map of column index to CSVSpecialColumn
     */
    public static TIntObjectHashMap<SpecialColumn> createSpecialColumnMap(Collection<SpecialColumn> specialColumns)
    {
        TIntObjectHashMap<SpecialColumn> specialColumnMap = new TIntObjectHashMap<>();
        for (SpecialColumn column : specialColumns)
        {
            specialColumnMap.put(column.getColumnIndex(), column);
        }
        return specialColumnMap;
    }

    /**
     * Private constructor.
     */
    private Utilities()
    {
    }
}
