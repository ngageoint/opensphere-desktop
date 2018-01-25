package io.opensphere.csvcommon.ui.columndefinition.controller;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import io.opensphere.core.util.collections.New;
import io.opensphere.importer.config.ColumnType;

/**
 * Orders the column types in location type order.
 */
public class ColumnTypeLocationComparator implements Comparator<ColumnType>, Serializable
{
    /**
     * The serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Contains the order the locations should be in.
     */
    private final Map<ColumnType, Integer> myLocationOrders = New.map();

    /**
     * Constructs a new comparator.
     */
    public ColumnTypeLocationComparator()
    {
        myLocationOrders.put(ColumnType.LAT, 0);
        myLocationOrders.put(ColumnType.LON, 1);
        myLocationOrders.put(ColumnType.MGRS, 2);
        myLocationOrders.put(ColumnType.POSITION, 3);
        myLocationOrders.put(ColumnType.WKT_GEOMETRY, 4);
    }

    @Override
    public int compare(ColumnType o1, ColumnType o2)
    {
        Integer order1 = getOrder(o1);
        Integer order2 = getOrder(o2);

        return order1.compareTo(order2);
    }

    /**
     * Gets the order the type should be.
     *
     * @param columnType The type to get the order for.
     * @return The order number.
     */
    private Integer getOrder(ColumnType columnType)
    {
        Integer order = myLocationOrders.get(columnType);
        if (order == null)
        {
            order = Integer.MAX_VALUE;
        }

        return order;
    }
}
