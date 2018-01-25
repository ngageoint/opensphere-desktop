package io.opensphere.analysis.export.controller;

import java.awt.Color;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

import javax.swing.JTable;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.analysis.export.model.DelegateDataElement;
import io.opensphere.analysis.export.model.DelegateMapDataElement;
import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.export.model.ExtraColumnsMetaDataProvider;
import io.opensphere.analysis.export.model.LatLonFormat;
import io.opensphere.analysis.table.model.MetaColumn;
import io.opensphere.analysis.table.model.MetaColumnsTableModel;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.table.JTableUtilities;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.util.TimeSpanUtility;

/**
 * This class will modify/filter {@link DataElement} based on user inputs
 * contained within {@link ExportOptionsModel}.
 */
public class DataElementProvider
{
    /**
     * The column name for the separated date column.
     */
    private static final String ourDateColumnName = "DATE";

    /**
     * The value to append to any special lat lon columns.
     */
    private static final String ourLatLonAppend = " (DMS)";

    /**
     * The column name for the separated time column.
     */
    private static final String ourTimeColumnName = "TIME OF DAY";

    /**
     * The column name for the WKT values.
     */
    private static final String ourWKTColumnName = "WKT Geometry";

    /**
     * Used to format the color value to a string.
     */
    private final ColorFormatter myColorFormatter;

    /**
     * Contains the user's inputs selected for export.
     */
    private final ExportOptionsModel myExportModel;

    /**
     * Formats the latitude and longitude to the user specified format.
     */
    private final LatLonFormatter myLatLonFormatter;

    /**
     * Gets the wkt values if the user chooses to add those to the export.
     */
    private final WKTValueProvider myWktValueProvider;

    /**
     * Gets the data elements to export from the table.
     *
     * @param tableModel The table model to export.
     * @param table The table to export.
     * @param onlySelected whether to include only selected rows
     * @return the data elements
     */
    public static List<DataElement> getDataElementsToExport(MetaColumnsTableModel tableModel, JTable table, boolean onlySelected)
    {
        return getDataElementsToExport(tableModel, table, onlySelected, false, null);
    }

    /**
     * Create the appropriate {@link DelegateDataElement} or
     * {@link DelegateMapDataElement}.
     *
     * @param element The element to wrap.
     * @param provider The {@link MetaDataProvider} to use.
     * @return The new delegating data element.
     */
    private static DataElement createDelegateElement(DataElement element, MetaDataProvider provider)
    {
        DataElement copied = null;
        if (element instanceof MapDataElement)
        {
            copied = new DelegateMapDataElement((MapDataElement)element, provider);
        }
        else
        {
            copied = new DelegateDataElement(element, provider);
        }

        return copied;
    }

    /**
     * Gets the data element to export for the given row.
     *
     * @param row the view row index
     * @param tableModel The table model to export.
     * @param table The table to export.
     * @param columnNames the column names being displayed
     * @param includeMetaColumns whether to include meta columns
     * @param colorFormatter the color formatter
     * @return the data element
     */
    private static DataElement getDataElement(int row, MetaColumnsTableModel tableModel, JTable table, List<String> columnNames,
            boolean includeMetaColumns, ColorFormatter colorFormatter)
    {
        int modelRow = table.convertRowIndexToModel(row);
        DataElement element = tableModel.getDataAt(modelRow);

        // Get the normal columns
        Map<String, Serializable> metaData = new LinkedHashMap<>();
        for (String columnName : columnNames)
        {
            Object value = element.getMetaData().getValue(columnName);
            if (value instanceof Serializable)
            {
                metaData.put(columnName, (Serializable)value);
            }
        }

        MetaDataProvider provider = new SimpleMetaDataProvider(metaData);

        // Get the meta data columns
        if (includeMetaColumns && !tableModel.getMetaColumns().isEmpty())
        {
            Map<String, Object> extraColumns = New.map();
            for (MetaColumn<?> metaColumn : tableModel.getMetaColumns())
            {
                String columnName = metaColumn.getColumnIdentifier();
                int columnIndex = tableModel.findColumn(columnName);
                Object value = tableModel.getValueAt(modelRow, columnIndex);

                if (value instanceof Color)
                {
                    value = colorFormatter.format(value);
                }

                extraColumns.put(columnName, value);
            }

            provider = new ExtraColumnsMetaDataProvider(provider, extraColumns);
        }

        // Return a copy of the data element with new meta data
        return createDelegateElement(element, provider);
    }

    /**
     * Gets the data elements to export from the table.
     *
     * @param tableModel The table model to export.
     * @param table The table to export.
     * @param onlySelected whether to include only selected rows
     * @param includeMetaColumns whether to include meta columns
     * @param colorFormatter the color formatter
     * @return the data elements
     */
    private static List<DataElement> getDataElementsToExport(MetaColumnsTableModel tableModel, JTable table, boolean onlySelected,
            boolean includeMetaColumns, ColorFormatter colorFormatter)
    {
        int size;
        IntStream rowStream;
        if (onlySelected)
        {
            int[] selRows = table.getSelectedRows();
            size = selRows.length;
            rowStream = Arrays.stream(selRows);
        }
        else
        {
            size = table.getRowCount();
            rowStream = IntStream.range(0, size);
        }

        List<DataElement> elements = New.list(size);
        List<String> columnNames = JTableUtilities.getColumnNames(table);
        for (OfInt iter = rowStream.iterator(); iter.hasNext();)
        {
            int row = iter.nextInt();
            DataElement element = getDataElement(row, tableModel, table, columnNames, includeMetaColumns, colorFormatter);
            elements.add(element);
        }
        return elements;
    }

    /**
     * Constructs a new element modifier.
     *
     * @param exportModel Contains the user's inputs selected for export.
     * @param elementCache Used to get the elements {@link MapGeometrySupport}.
     */
    public DataElementProvider(ExportOptionsModel exportModel, DataElementCache elementCache)
    {
        myExportModel = exportModel;
        myWktValueProvider = new WKTValueProvider(myExportModel);
        myLatLonFormatter = new LatLonFormatter(myExportModel);
        myColorFormatter = new ColorFormatter(myExportModel);
    }

    /**
     * Modifies and/or filters out the elements passed in based on the user
     * inputs contained in the {@link ExportOptionsModel}.
     *
     * @param tableModel The table model to export.
     * @param table The table to export.
     * @param timePrecision The precision to format the time to.
     * @return The elements.
     */
    public List<DataElement> provideElements(MetaColumnsTableModel tableModel, JTable table, int timePrecision)
    {
        List<DataElement> modified = New.list();

        List<DataElement> elements = getDataElementsToExport(tableModel, table, false, myExportModel.isIncludeMetaColumns(),
                myColorFormatter);
        for (DataElement element : elements)
        {
            if (element.getVisualizationState().isSelected() || !myExportModel.isSelectedRowsOnly())
            {
                Map<String, Object> extraValues = New.map();
                addWkt(element, extraValues);
                separateDateAndTime(element, extraValues, timePrecision);
                formatLatLon(element, extraValues);
                if (!extraValues.isEmpty())
                {
                    element = createDelegateElement(element, new ExtraColumnsMetaDataProvider(element.getMetaData(), extraValues));
                }

                modified.add(element);
            }
        }

        return modified;
    }

    /**
     * Adds a wkt value to the data to export if the user chooses to do so.
     *
     * @param element The element to add wkt value for.
     * @param extraValues The map to add the wkt value to.
     */
    private void addWkt(DataElement element, Map<String, Object> extraValues)
    {
        String wktValue = myWktValueProvider.getWKTValue(element);
        if (StringUtils.isNotEmpty(wktValue))
        {
            extraValues.put(ourWKTColumnName, wktValue);
        }
    }

    /**
     * Formats latitude and longitude values to the user specified format.
     *
     * @param element The element to format its latitude and longitude values
     *            for.
     * @param extraValues The map to add the latitude longitude values to.
     */
    private void formatLatLon(DataElement element, Map<String, Object> extraValues)
    {
        if (myExportModel.getSelectedLatLonFormat() != LatLonFormat.DECIMAL)
        {
            String latKey = element.getDataTypeInfo().getMetaDataInfo().getLatitudeKey();
            String lonKey = element.getDataTypeInfo().getMetaDataInfo().getLongitudeKey();

            Object latitude = element.getMetaData().getValue(latKey);
            Object longitude = element.getMetaData().getValue(lonKey);

            latitude = myLatLonFormatter.format(latitude, LatitudeKey.DEFAULT);
            longitude = myLatLonFormatter.format(longitude, LongitudeKey.DEFAULT);

            if (latitude instanceof Serializable)
            {
                extraValues.put(latKey + ourLatLonAppend, latitude);
            }

            if (longitude instanceof Serializable)
            {
                extraValues.put(lonKey + ourLatLonAppend, longitude);
            }
        }
    }

    /**
     * Separates the Date/Time into separate date and time columns if the user
     * chooses to do so.
     *
     * @param element The element to separate the date and time.
     * @param extraValues The map to add the separated date and time values to.
     * @param timePrecision The precision to format the time to.
     */
    private void separateDateAndTime(DataElement element, Map<String, Object> extraValues, int timePrecision)
    {
        if (myExportModel.isSeparateDateTimeColumns())
        {
            TimeSpan val = element.getTimeSpan();

            String date = TimeSpanUtility.formatTimeSpanSingleTimeOnly(new SimpleDateFormat(DateTimeFormats.DATE_FORMAT), val);

            // Add the time portion (added to the list below)

            SimpleDateFormat timeFormatter = ListToolPreferences.getSimpleTimeFormatForPrecision(timePrecision);
            String time = TimeSpanUtility.formatTimeSpanSingleTimeOnly(timeFormatter, val);

            extraValues.put(ourDateColumnName, date);
            extraValues.put(ourTimeColumnName, time);
        }
    }
}
