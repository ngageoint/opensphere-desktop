package io.opensphere.csvcommon.parse;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.format.CellFormatter;
import io.opensphere.csvcommon.format.factory.CellFormatterFactory;
import io.opensphere.csvcommon.parse.PointExtract;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;

/** Extracts time fields from a CSV row. */
public class CSVTimeExtractor
{
    /** The date format. */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DateTimeFormats.DATE_FORMAT);

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CSVTimeExtractor.class);

    /** The time format. */
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /** The cell formatter factory. */
    private final transient CellFormatterFactory myCellFormatterFactory = new CellFormatterFactory();

    /** The file parameters source. */
    private final CSVParseParameters params;

    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Constructor.
     *
     * @param fileSource Supplier of parse parameters
     * @param preferencesRegistry The preferences registry.
     */
    public CSVTimeExtractor(CSVParseParameters fileSource, PreferencesRegistry preferencesRegistry)
    {
        params = fileSource;
        myPreferencesRegistry = preferencesRegistry;
    }

    /**
     * Extracts time fields.
     *
     * @param specialColumn the special column
     * @param cellValue the cell value
     * @param colName the col name
     * @param ptData the pt data
     * @param metaDataProvider the meta data provider
     * @param parts the parts
     */
    public void extractTime(SpecialColumn specialColumn, String cellValue, String colName, PointExtract ptData,
            MDILinkedMetaDataProvider metaDataProvider, String[] parts)
    {
        final ColumnType columnType = specialColumn.getColumnType();
        if (columnType == ColumnType.TIMESTAMP)
        {
            extractTimestamp(specialColumn, cellValue, colName, ptData, parts);
        }
        else if (columnType == ColumnType.DATE)
        {
            extractDate(specialColumn, cellValue, colName, ptData, parts);
        }
        else if (columnType == ColumnType.DOWN_TIMESTAMP)
        {
            extractDownTimestamp(specialColumn, cellValue, colName, ptData, metaDataProvider);
        }
        else if (columnType == ColumnType.DOWN_DATE)
        {
            extractDownDate(specialColumn, cellValue, colName, ptData, metaDataProvider, parts);
        }
        else if (columnType == ColumnType.DOWN_TIME)
        {
            extractDownTime(specialColumn, cellValue, colName, metaDataProvider);
        }
    }

    /**
     * Extracts date column.
     *
     * @param specialColumn the special column
     * @param cellValue the cell value
     * @param colName the col name
     * @param ptData The pt data.
     * @param parts The parts.
     */
    private void extractDate(SpecialColumn specialColumn, String cellValue, String colName, PointExtract ptData, String[] parts)
    {
        SpecialColumn timeColumn = params.getSpecialColumn(ColumnType.TIME);
        // Date/Time
        if (timeColumn != null)
        {
            {
                String combinedValue = StringUtilities.concat(cellValue, " ", parts[timeColumn.getColumnIndex()]);
                String combinedFormat = StringUtilities.concat(specialColumn.getFormat(), " ", timeColumn.getFormat());
                SpecialColumn combinedColumn = new SpecialColumn(-1, ColumnType.TIMESTAMP, combinedFormat);
                Serializable formattedValue = formatCell(combinedValue, combinedColumn, "date/time");
                if (formattedValue instanceof Date)
                {
                    ptData.setDate((Date)formattedValue);
                }
            }

            // Special case for single date with start and end time
            if (!params.hasType(ColumnType.DOWN_DATE)
                    && params.hasType(ColumnType.DOWN_TIME))
            {
                SpecialColumn downTimeColumn = params.getSpecialColumn(ColumnType.DOWN_TIME);
                String combinedValue = StringUtilities.concat(cellValue, " ", parts[downTimeColumn.getColumnIndex()]);
                String combinedFormat = StringUtilities.concat(specialColumn.getFormat(), " ", downTimeColumn.getFormat());
                SpecialColumn combinedColumn = new SpecialColumn(-1, ColumnType.TIMESTAMP, combinedFormat);
                Serializable formattedValue = formatCell(combinedValue, combinedColumn, "down date/time");
                if (formattedValue instanceof Date)
                {
                    ptData.setDownDate((Date)formattedValue);
                }
            }
        }
        // Date(s) only - use whole day range
        else
        {
            String format = StringUtilities.concat(specialColumn.getFormat(), " HHmmss");

            Serializable formattedStart = formatCell(StringUtilities.concat(cellValue, " 000000"),
                    new SpecialColumn(-1, ColumnType.TIMESTAMP, format), colName);
            if (formattedStart instanceof Date)
            {
                ptData.setDate((Date)formattedStart);
            }

            SpecialColumn downDateColumn = params.getSpecialColumn(ColumnType.DOWN_DATE);
            String downCellValue = downDateColumn != null ? parts[downDateColumn.getColumnIndex()] : cellValue;
            Serializable formattedDown = formatCell(StringUtilities.concat(downCellValue, " 235959"),
                    new SpecialColumn(-1, ColumnType.TIMESTAMP, format), colName);
            if (formattedDown instanceof Date)
            {
                ptData.setDownDate((Date)formattedDown);
            }
        }
    }

    /**
     * Extracts down date column.
     *
     * @param specialColumn the special column
     * @param cellValue the cell value
     * @param colName the col name
     * @param ptData The pt data.
     * @param metaDataProvider the meta data provider
     * @param parts The parts.
     */
    private void extractDownDate(SpecialColumn specialColumn, String cellValue, String colName, PointExtract ptData,
            MDILinkedMetaDataProvider metaDataProvider, String[] parts)
    {
        SpecialColumn timeColumn = params.getSpecialColumn(ColumnType.DOWN_TIME);
        // Date/Time
        if (timeColumn != null)
        {
            String combinedValue = StringUtilities.concat(cellValue, " ", parts[timeColumn.getColumnIndex()]);
            String combinedFormat = StringUtilities.concat(specialColumn.getFormat(), " ", timeColumn.getFormat());
            SpecialColumn combinedColumn = new SpecialColumn(-1, ColumnType.DOWN_TIMESTAMP, combinedFormat);
            Serializable formattedValue = formatCell(combinedValue, combinedColumn, "down date/time");
            if (formattedValue instanceof Date)
            {
                ptData.setDownDate((Date)formattedValue);
            }
        }

        Serializable formattedValue = formatCell(cellValue, specialColumn, colName);
        if (formattedValue instanceof Date)
        {
            String value;
            synchronized (DATE_FORMAT)
            {
                value = DATE_FORMAT.format((Date)formattedValue);
            }
            metaDataProvider.setValue(colName, value);
        }
    }

    /**
     * Extracts down time column.
     *
     * @param specialColumn the special column
     * @param cellValue the cell value
     * @param colName the col name
     * @param metaDataProvider the meta data provider
     */
    private void extractDownTime(SpecialColumn specialColumn, String cellValue, String colName,
            MDILinkedMetaDataProvider metaDataProvider)
    {
        Serializable formattedValue = formatCell(cellValue, specialColumn, colName);
        if (formattedValue instanceof Date)
        {
            String value;
            synchronized (TIME_FORMAT)
            {
                value = TIME_FORMAT.format((Date)formattedValue);
            }
            metaDataProvider.setValue(colName, value);
        }
    }

    /**
     * Extracts down timestamp column.
     *
     * @param specialColumn the special column
     * @param cellValue the cell value
     * @param colName the col name
     * @param ptData The pt data.
     * @param metaDataProvider the meta data provider
     */
    private void extractDownTimestamp(SpecialColumn specialColumn, String cellValue, String colName, PointExtract ptData,
            MDILinkedMetaDataProvider metaDataProvider)
    {
        Serializable formattedValue = formatCell(cellValue, specialColumn, colName);
        if (formattedValue instanceof Date)
        {
            ptData.setDownDate((Date)formattedValue);
            metaDataProvider.setValue(colName, formattedValue);
        }
    }

    /**
     * Extracts timestamp column.
     *
     * @param specialColumn the special column
     * @param cellValue the cell value
     * @param colName the col name
     * @param ptData The pt data.
     * @param parts The parts.
     */
    private void extractTimestamp(SpecialColumn specialColumn, String cellValue, String colName, PointExtract ptData,
            String[] parts)
    {
        {
            Serializable formattedValue = formatCell(cellValue, specialColumn, colName);
            if (formattedValue instanceof Date)
            {
                ptData.setDate((Date)formattedValue);
            }
        }

        // Special case for start timestamp and end time
        if (!params.hasType(ColumnType.DOWN_DATE)
                && params.hasType(ColumnType.DOWN_TIME))
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            SpecialColumn downTimeColumn = params.getSpecialColumn(ColumnType.DOWN_TIME);
            String combinedValue = StringUtilities.concat(formatter.format(ptData.getDate()), " ",
                    parts[downTimeColumn.getColumnIndex()]);
            String combinedFormat = StringUtilities.concat(formatter.toPattern(), " ", downTimeColumn.getFormat());
            SpecialColumn combinedColumn = new SpecialColumn(-1, ColumnType.TIMESTAMP, combinedFormat);
            Serializable formattedValue = formatCell(combinedValue, combinedColumn, "down date/time");
            if (formattedValue instanceof Date)
            {
                ptData.setDownDate((Date)formattedValue);
            }
        }
    }

    /**
     * Formats the cell value using the default formatter for the column type.
     *
     * @param cellValue the cell value
     * @param specialColumn the special column
     * @param colName the column name
     * @return the formatted value
     */
    private Serializable formatCell(String cellValue, SpecialColumn specialColumn, String colName)
    {
        Serializable formattedValue = cellValue;
        if (specialColumn.getFormat() != null)
        {
            CellFormatter formatter = myCellFormatterFactory.getFormatter(specialColumn.getColumnType(), myPreferencesRegistry);
            if (formatter != null)
            {
                try
                {
                    formattedValue = (Serializable)formatter.formatCell(cellValue, specialColumn.getFormat());
                }
                catch (ParseException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            else
            {
                LOGGER.error("No formatter found for " + colName);
            }
        }
        else
        {
            LOGGER.error("No format specified for " + colName);
        }
        return formattedValue;
    }
}
