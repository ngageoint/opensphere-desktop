package io.opensphere.csvcommon.format.factory;

import java.util.Map;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.CellFormatter;
import io.opensphere.csvcommon.format.datetime.DateFormatter;
import io.opensphere.csvcommon.format.datetime.DateTimeFormatter;
import io.opensphere.csvcommon.format.datetime.TimeFormatter;
import io.opensphere.csvcommon.format.position.LatitudeFormatter;
import io.opensphere.csvcommon.format.position.LongitudeFormatter;
import io.opensphere.csvcommon.format.position.PositionFormatter;
import io.opensphere.importer.config.ColumnType;

/**
 * Gets the formatters for a given type.
 *
 */
public final class CellFormatterFactory
{
    /** The map of column type to cell formatter. */
    private final Map<ColumnType, CellFormatter> myFormatterMap = New.map();

    /** The map of column type to system format. */
    private final Map<ColumnType, String> mySystemFormatMap = New.map();

    /**
     * Gets the formatter based on the column result.
     *
     * @param columnType The column type.
     * @param preferencesRegistry The preferences registry.
     * @return The cell formatter.
     */
    public CellFormatter getFormatter(ColumnType columnType, PreferencesRegistry preferencesRegistry)
    {
        CellFormatter formatter = myFormatterMap.get(columnType);
        if (formatter == null)
        {
            if (columnType == ColumnType.DOWN_TIMESTAMP || columnType == ColumnType.TIMESTAMP)
            {
                formatter = new DateTimeFormatter(preferencesRegistry);
            }
            else if (columnType == ColumnType.DATE || columnType == ColumnType.DOWN_DATE)
            {
                formatter = new DateFormatter(preferencesRegistry);
            }
            else if (columnType == ColumnType.TIME || columnType == ColumnType.DOWN_TIME)
            {
                formatter = new TimeFormatter(preferencesRegistry);
            }
            else if (columnType == ColumnType.LAT)
            {
                formatter = new LatitudeFormatter(preferencesRegistry);
            }
            else if (columnType == ColumnType.LON)
            {
                formatter = new LongitudeFormatter(preferencesRegistry);
            }
            else if (columnType == ColumnType.POSITION)
            {
                formatter = new PositionFormatter(preferencesRegistry);
            }

            if (formatter != null)
            {
                myFormatterMap.put(columnType, formatter);
            }
        }
        return formatter;
    }

    /**
     * Gets the system format for the given column type.
     *
     * @param columnType The column type.
     * @param preferencesRegistry The preferences registry.
     * @return The system format.
     */
    public String getSystemFormat(ColumnType columnType, PreferencesRegistry preferencesRegistry)
    {
        String systemFormat = mySystemFormatMap.get(columnType);
        if (systemFormat == null)
        {
            CellFormatter formatter = getFormatter(columnType, preferencesRegistry);
            if (formatter != null)
            {
                systemFormat = formatter.getSystemFormat();
                mySystemFormatMap.put(columnType, systemFormat);
            }
        }
        return systemFormat;
    }
}
