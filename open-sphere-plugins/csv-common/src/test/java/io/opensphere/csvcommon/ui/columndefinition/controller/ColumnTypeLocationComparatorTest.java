package io.opensphere.csvcommon.ui.columndefinition.controller;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.controller.ColumnTypeLocationComparator;
import io.opensphere.importer.config.ColumnType;

/**
 * Tests the ColumnTypeLocationComparator class.
 */
public class ColumnTypeLocationComparatorTest
{
    /**
     * Tests the comparator.
     */
    @Test
    public void testCompare()
    {
        List<ColumnType> dataTypes = New.list();

        dataTypes.add(ColumnType.TIMESTAMP);
        dataTypes.add(ColumnType.DATE);
        dataTypes.add(ColumnType.TIME);
        dataTypes.add(ColumnType.DOWN_DATE);
        dataTypes.add(ColumnType.DOWN_TIME);
        dataTypes.add(ColumnType.DOWN_TIMESTAMP);
        dataTypes.add(ColumnType.LAT);
        dataTypes.add(ColumnType.LON);
        dataTypes.add(ColumnType.MGRS);
        dataTypes.add(ColumnType.POSITION);
        dataTypes.add(ColumnType.WKT_GEOMETRY);
        dataTypes.add(ColumnType.SEMIMAJOR);
        dataTypes.add(ColumnType.SEMIMINOR);
        dataTypes.add(ColumnType.ORIENTATION);
        dataTypes.add(ColumnType.RADIUS);
        dataTypes.add(ColumnType.LOB);

        ColumnTypeLocationComparator comparator = new ColumnTypeLocationComparator();

        Collections.sort(dataTypes, comparator);

        assertEquals(ColumnType.LAT, dataTypes.get(0));
        assertEquals(ColumnType.LON, dataTypes.get(1));
        assertEquals(ColumnType.MGRS, dataTypes.get(2));
        assertEquals(ColumnType.POSITION, dataTypes.get(3));
        assertEquals(ColumnType.WKT_GEOMETRY, dataTypes.get(4));
    }
}
