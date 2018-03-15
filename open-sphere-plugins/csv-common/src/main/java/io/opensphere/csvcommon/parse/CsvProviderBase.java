package io.opensphere.csvcommon.parse;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.length.ImbeddedUnitsLengthUtilities;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.QuotingBufferedReader;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.TextDelimitedStringTokenizer;
import io.opensphere.csvcommon.config.v1.CSVColumnInfo;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.ColumnType.Category;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.DataElementProvider;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.util.jts.JTSGeometryToGeometrySupportFactory;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;
import io.opensphere.mantle.util.InputStreamMonitorTaskActivity;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/** Base class for the DataElementProvider of CSV records. */
public abstract class CsvProviderBase implements DataElementProvider
{
    /** The Constant ourLogger. */
    private static final Logger LOGGER = Logger.getLogger(CsvProviderBase.class);

    /** The Constant OUR_DISCARDED_DATAELEMENT. */
    private static DataElement OUR_DISCARDED_DATAELEMENT = new DefaultDataElement(0);

    /** A Counter that helps generate ID's for the geometries. */
    private static AtomicLong ourIDCounter = new AtomicLong(1000000);

    /** The Constant TO_PERCENT. */
    private static final double TO_PERCENT = 100.0;

    /** Saves the config. */
    protected Runnable myConfigSaver;

    /** The current line. */
    protected String myCurrLine;

    /** The Dynamic enumeration registry. */
    protected DynamicEnumerationRegistry myDynamicEnumerationRegistry;

    /** The Error messages. */
    protected List<String> myErrorMessages;

    /** The index of the first row with data in the file. */
    protected int myFirstDataRowNum;

    /** The line index. */
    protected int myLineIndex;

    /** The next element to return. */
    protected DataElement myNextElementToReturn;

    /** The parts. */
    protected String[] myParts;

    /** The reader. */
    protected QuotingBufferedReader myReader;

    /** The special column map. */
    protected TIntObjectHashMap<SpecialColumn> mySpecialColumnMap;

    /** The Task activity. */
    protected InputStreamMonitorTaskActivity myTaskActivity;

    /** The toolbox. */
    protected Toolbox myToolbox;

    /** The type info. */
    protected DefaultDataTypeInfo myTypeInfo;

    /** The Use determined data types. */
    protected boolean myUseDeterminedDataTypes;

    /** The Use dynamic enumerations. */
    protected boolean myUseDynamicEnumerations;

    /** The Warning messages. */
    protected List<String> myWarningMessages;

    /** Support for generating geometries. */
    protected GeomSupportFactory geomFact;

    /** The Column analyzer. */
    private ColumnClassAnalyzer myColumnAnalyzer;

    /** The the location of the column breaks if not delimited. */
    private int[] myColumnBreaks;

    /** The column names. */
    private List<? extends String> myColumnNames;

    /** The Column name to class map. */
    private Map<String, Class<?>> myColumnNameToClassMap;

    /** The discarded line count. */
    private int myDiscardedLineCount;

    /** The Dynamic enum columns. */
    private Map<String, Class<?>> myDynamicEnumColumnsToOrigClassMap;

    /** The filtered set of column names. */
    private Set<String> myFilteredColumns;

    /** The my had error. */
    private boolean myHadError;

    /** The Had warning. */
    private boolean myHadWarning;

    /** Indicates if the warn message has already been shown. */
    private boolean myHasBeenWarned;

    /** The delimited flag, if false means fixed width columns. */
    private boolean myIsDelimited;

    /** The Is quoted. */
    private boolean myIsQuoted;

    /** The number of columns. */
    private int myNumColumns;

    /** The my overall time extent. */
    private TimeSpan myOverallTimeExtent = TimeSpan.TIMELESS;

    /** The delimited text tokenizer. */
    private TextDelimitedStringTokenizer myTokenizer;

    /** The Total line count. */
    private int myTotalLineCount;

    /** The Uses fixed width columns. */
    private boolean myUsesFixedWidthColumns;

    /**
     * Gets the URI of the data, depending on the specific source.
     * @return the URI
     */
    protected abstract URI getSourceUri();

    /**
     * Retrieve parsing parameters for CSV records from the specific source.
     * @return the CSVParseParameters
     */
    protected abstract CSVParseParameters getParseParams();

    /**
     * Get the column filtering info from the specific source.
     * @return a Set of column names
     */
    protected abstract Set<String> getColumnFilter();

    /**
     * Determine the Color to use for visible features.
     * @return the feature Color
     */
    protected abstract Color getLayerColor();

    /**
     * Some applications may choose to suppress warnings selectively.
     * @param parts the parts
     * @return true if and only if a warning is allowed
     */
    protected boolean checkAllowWarning(String[] parts)
    {
        // default to issuing a warning on any perceived problem
        return true;
    }

    /**
     * Apply labels to the CSV features.  By default, this does nothing;
     * override to provide this capability in subclasses.
     * @param dtiKey the type key
     * @param metaDataProvider the feature metadata
     */
    protected void applyLabels(String dtiKey, MetaDataProvider metaDataProvider)
    {
        // nothing
    }

    @Override
    public DataTypeInfo getDataTypeInfo()
    {
        return myTypeInfo;
    }

    /**
     * Gets the discarded line count.
     *
     * @return the discarded line count
     */
    public int getDiscardedLineCount()
    {
        return myDiscardedLineCount;
    }

    @Override
    public List<String> getErrorMessages()
    {
        return myErrorMessages;
    }

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalLineCount()
    {
        return myLineIndex - myFirstDataRowNum;
    }

    @Override
    public List<String> getWarningMessages()
    {
        return myWarningMessages;
    }

    @Override
    public boolean hadError()
    {
        return myHadError;
    }

    @Override
    public boolean hadWarning()
    {
        return myHadWarning;
    }

    @Override
    public boolean hasNext()
    {
        return myNextElementToReturn != null;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates the CSV line reader.
     *
     * @param params the parameters
     * @param streamReader the stream reader
     * @return the CSV line reader
     */
    protected QuotingBufferedReader createCSVLineReader(CSVParseParameters params, InputStreamReader streamReader)
    {
        char[] quotes;
        if (params.getColumnFormat() instanceof CSVDelimitedColumnFormat)
        {
            CSVDelimitedColumnFormat columnFormat = (CSVDelimitedColumnFormat)params.getColumnFormat();
            char quoteChar = StringUtils.isEmpty(columnFormat.getTextDelimiter()) ? '"'
                    : columnFormat.getTextDelimiter().charAt(0);
            quotes = new char[] { quoteChar };
        }
        else
        {
            quotes = new char[0];
        }

        return new QuotingBufferedReader(streamReader, quotes, null);
    }

    @Override
    public DataElement next()
    {
        if (myNextElementToReturn == null)
        {
            myTaskActivity.setComplete(true);
            throw new NoSuchElementException();
        }

        DataElement temp = myNextElementToReturn;
        try
        {
            myNextElementToReturn = getNextDataElement();
            if (myTypeInfo.getTimeExtents() == null)
            {
                myTypeInfo.setTimeExtents(new DefaultTimeExtents(myOverallTimeExtent), DataTypeInfo.NO_EVENT_SOURCE);
            }
            else
            {
                ((DefaultTimeExtents)myTypeInfo.getTimeExtents()).setTimeExtent(myOverallTimeExtent);
            }
            myTypeInfo.setTimeExtents(new DefaultTimeExtents(myOverallTimeExtent), DataTypeInfo.NO_EVENT_SOURCE);
            if (myNextElementToReturn == null)
            {
                if (myDiscardedLineCount > 0)
                {
                    double percent = (double)myDiscardedLineCount / (double)myTotalLineCount * TO_PERCENT;

                    if (myDiscardedLineCount == myTotalLineCount)
                    {
                        myHadError = true;
                        StringBuilder sb = new StringBuilder(64);
                        sb.append("Discarded ").append(String.format("%-6.2f", Double.valueOf(percent))).append("% ")
                                .append(myDiscardedLineCount).append(" of ").append(myTotalLineCount)
                                .append(" lines from the file ").append(getSourceUri());
                        sb.append("\n  See logs for additional details.");
                        myErrorMessages.add(sb.toString());
                    }
                    else
                    {
                        StringBuilder sb = new StringBuilder(128);
                        sb.append("All ").append(myTotalLineCount).append(" lines from file ").append(getSourceUri())
                                .append(" were discarded because of parsing problems.  See logs for details.");
                        myErrorMessages.add(sb.toString());
                    }
                }
                myTaskActivity.setComplete(true);
                if (myColumnAnalyzer != null)
                {
                    myColumnAnalyzer.imprintResults();
                    myConfigSaver.run();
                }
            }
        }
        catch (IOException e)
        {
            myHadError = true;
            StringBuilder sb = new StringBuilder(64);
            sb.append("Encountered a problem reading the CSV File ").append(getSourceUri())
                    .append("\n" + "   MESSAGE: ").append(e.getMessage());
            myErrorMessages.add(sb.toString());

            myNextElementToReturn = null;
            myTaskActivity.setComplete(true);
        }
        catch (RuntimeException e)
        {
            myHadError = true;
            StringBuilder sb = new StringBuilder(64);
            sb.append("Encountered a problem loading the CSV File ").append(getSourceUri())
                    .append("\n" + "   MESSAGE: ").append(e.getMessage());
            myErrorMessages.add(sb.toString());
            myNextElementToReturn = null;
            myTaskActivity.setComplete(true);
        }
        return Utilities.sameInstance(temp, OUR_DISCARDED_DATAELEMENT) ? null : temp;
    }

    /**
     * Gets the next data element.
     *
     * @return the next data element
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public DataElement getNextDataElement() throws IOException
    {
        DataElement de = null;
        if (myReader.ready())
        {
            myCurrLine = myReader.readLine();
            if (myCurrLine != null)
            {
                myTotalLineCount++;
                myParts = generateParts();
                if (myParts == null || myParts.length == 0)
                {
                    return OUR_DISCARDED_DATAELEMENT;
                }

                if (myParts.length != myNumColumns)
                {
                    if (checkAllowWarning(myParts))
                    {
                        String logEntry = "Line " + (myLineIndex + 1) + " in CSV had unexpected number of columns "
                                + Integer.valueOf(myParts.length) + " of " + myNumColumns + ": " + myCurrLine;
                        if (!myHasBeenWarned)
                        {
                            UserMessageEvent.warn(myToolbox.getEventManager(), getSourceUri() + "\n" + logEntry);
                            myHasBeenWarned = true;
                        }
                        LOGGER.info(logEntry);
                    }
                    return OUR_DISCARDED_DATAELEMENT;
                }

                if (myColumnAnalyzer != null)
                {
                    myColumnAnalyzer.considerValues(myParts);
                }
                MDILinkedMetaDataProvider metaDataProvider = new MDILinkedMetaDataProvider(myTypeInfo.getMetaDataInfo());
                PointExtract ptData = extractDataAndUpdateMetaDataProvider(myTypeInfo.getTypeKey(), myLineIndex, myCurrLine,
                        myParts, metaDataProvider);
                if (ptData == null)
                {
                    de = OUR_DISCARDED_DATAELEMENT;
                    myDiscardedLineCount++;
                }
                else
                {
                    TimeSpan ts = determineTimeSpan(ptData);
                    if (ts != null && !ts.isUnboundedStart())
                    {
                        metaDataProvider.setValue("TIME", ts.getStartDate());
                    }
                    if (ts != null)
                    {
                        updateOveralTimeExtent(ts);
                    }
                    de = getDataElement(metaDataProvider, ptData, ts);
                }
                myLineIndex++;
            }
        }
        return de;
    }

    /**
     * Determine column classes.
     *
     * @param dtiKey the dti key
     */
    private void determineColumnClasses(String dtiKey)
    {
        myDynamicEnumColumnsToOrigClassMap = New.map();
        myColumnNameToClassMap = New.map();

        List<CSVColumnInfo> colInfoList = getParseParams().getColumnClasses();
        if (colInfoList == null || colInfoList.isEmpty())
        {
            myColumnAnalyzer = new ColumnClassAnalyzer(getParseParams());
        }
        String columnName = null;
        for (int i = 0; i < myColumnNames.size(); i++)
        {
            columnName = myColumnNames.get(i);
            if (colInfoList == null || colInfoList.isEmpty())
            {
                myColumnNameToClassMap.put(columnName, String.class);
            }
            else
            {
                Class<?> cClass;
                if (i < colInfoList.size())
                {
                    CSVColumnInfo colInfo = colInfoList.get(i);
                    try
                    {
                        cClass = Class.forName(colInfo.getClassName());
                    }
                    catch (ClassNotFoundException e)
                    {
                        cClass = String.class;
                    }
                    if (!isSpecialColumn(i) && myUseDynamicEnumerations && colInfo.isIsEnumCandidate()
                            && colInfo.getNumSamplesConsidered() > 1000 && colInfo.getUniqueValueCount() > 0
                            && colInfo.getUniqueValueCount() < 128)
                    {
                        myDynamicEnumColumnsToOrigClassMap.put(columnName, cClass);
                        myDynamicEnumerationRegistry.createEnumeration(dtiKey, columnName, cClass);
                        cClass = DynamicEnumerationKey.class;
                    }
                }
                else
                {
                    cClass = String.class;
                }
                myColumnNameToClassMap.put(columnName, cClass == null || !myUseDeterminedDataTypes ? String.class : cClass);
            }
        }
    }

    /**
     * Extract data and update meta data provider.
     *
     * @param dtiKey the dti key
     * @param lineIndex the line index
     * @param currLine the curr line
     * @param parts the parts
     * @param metaDataProvider the meta data provider
     * @return the point extract
     */
    private PointExtract extractDataAndUpdateMetaDataProvider(String dtiKey, int lineIndex, String currLine, String[] parts,
            MDILinkedMetaDataProvider metaDataProvider)
    {
        PointExtract ptData = new PointExtract();
        try
        {
            for (int columnIndex = 0; columnIndex < parts.length; columnIndex++)
            {
                String colName = columnIndex < myColumnNames.size() ? myColumnNames.get(columnIndex)
                        : Integer.toString(columnIndex);
                if (isSpecialColumn(columnIndex))
                {
                    extractSpecialKeyValue(parts, metaDataProvider, ptData, columnIndex, colName);
                }
                else
                {
                    if (!myFilteredColumns.contains(colName))
                    {
                        Class<?> colClass = myColumnNameToClassMap.get(colName);
                        boolean isDynamicEnum = myUseDynamicEnumerations
                                && myDynamicEnumColumnsToOrigClassMap.containsKey(colName);

                        // try to extract the value as that type of object.
                        if (StringUtils.isEmpty(parts[columnIndex]) || "null".equals(parts[columnIndex]))
                        {
                            if (isDynamicEnum)
                            {
                                myDynamicEnumerationRegistry.addValue(dtiKey, colName, null);
                                metaDataProvider.setValue(colName, null);
                            }
                            else
                            {
                                metaDataProvider.setValue(colName, null);
                            }
                        }
                        else
                        {
                            if (colClass == null)
                            {
                                metaDataProvider.setValue(colName, parts[columnIndex]);
                            }
                            else
                            {
                                if (isDynamicEnum && colClass == DynamicEnumerationKey.class)
                                {
                                    Object value = extractValueAsType(myDynamicEnumColumnsToOrigClassMap.get(colName),
                                            parts[columnIndex]);
                                    DynamicEnumerationKey key = myDynamicEnumerationRegistry.addValue(dtiKey, colName, value);
                                    metaDataProvider.setValue(colName, key);
                                }
                                else
                                {
                                    Object value = extractValueAsType(colClass, parts[columnIndex]);
                                    metaDataProvider.setValue(colName, (Serializable)value);
                                }
                            }
                        }
                    }
                }
            }
            applyLabels(dtiKey, metaDataProvider);
        }
        catch (CSVParseException e)
        {
            LOGGER.warn("CSVParseException On Line: " + (lineIndex + 1) + " Line Discarded!\n Line[" + currLine + "] "
                    + e.getMessage());
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
            ptData = null;
        }
        catch (RuntimeException e)
        {
            LOGGER.warn("Unknown Problem On Line: " + (lineIndex + 1) + " Line Discarded!\n Line[" + currLine + "]", e);
            ptData = null;
        }
        return ptData;
    }

    /**
     * Finds all necessary information to perform the extraction from the CSV
     * file.
     *
     * @param dtiKey the new up extraction
     */
    protected void setupExtraction(String dtiKey)
    {
        myHasBeenWarned = false;
        CSVParseParameters parseParameters = getParseParams();
        myFirstDataRowNum = parseParameters.getDataStartLine().intValue();
        if (parseParameters.getColumnFormat() instanceof CSVDelimitedColumnFormat)
        {
            CSVDelimitedColumnFormat columnFormat = (CSVDelimitedColumnFormat)parseParameters.getColumnFormat();
            myIsDelimited = true;
            myIsQuoted = StringUtils.isNotEmpty(columnFormat.getTextDelimiter());
            myTokenizer = new TextDelimitedStringTokenizer(columnFormat.getTokenDelimiter(), columnFormat.getTextDelimiter());
        }
        myUsesFixedWidthColumns = parseParameters.getColumnFormat() instanceof CSVFixedWidthColumnFormat;
        myColumnBreaks = myUsesFixedWidthColumns
                ? ((CSVFixedWidthColumnFormat)parseParameters.getColumnFormat()).getColumnDivisions() : null;
        myNumColumns = parseParameters.getColumnNames().size();

        myColumnNames = parseParameters.getColumnNames();
        determineColumnClasses(dtiKey);

        myFilteredColumns = getColumnFilter();
    }

    /**
     * Extract special key value.
     *
     * @param parts the parts
     * @param metaDataProvider the meta data provider
     * @param ptData the pt data
     * @param columnIndex the column index
     * @param colName the col name
     * @throws CSVParseException the parse exception
     */
    private void extractSpecialKeyValue(String[] parts, MDILinkedMetaDataProvider metaDataProvider, PointExtract ptData,
            int columnIndex, String colName)
        throws CSVParseException
    {
        final String cellValue = parts[columnIndex];

        if (StringUtils.isBlank(cellValue))
        {
            return;
        }

        // Update point data and meta data provider
        final SpecialColumn specialColumn = mySpecialColumnMap.get(columnIndex);
        if (specialColumn != null)
        {
            final ColumnType columnType = Utilities.getValue(specialColumn.getColumnType(), ColumnType.OTHER);
            if (columnType.getCategory() == Category.TEMPORAL)
            {
                new CSVTimeExtractor(getParseParams(), myToolbox.getPreferencesRegistry()).extractTime(
                        specialColumn, cellValue, colName, ptData, metaDataProvider, parts);
            }
            else if (columnType.getCategory() == Category.SPATIAL)
            {
                new CSVLocationExtractor(getParseParams()).extractLocation(
                        specialColumn, cellValue, colName, ptData, metaDataProvider);
            }
            else if (!myFilteredColumns.contains(colName))
            {
                if (columnType == ColumnType.OTHER)
                {
                    metaDataProvider.setValue(colName, cellValue);
                }
                else if (columnType == ColumnType.ALT)
                {
                    ptData.setAlt(Double.valueOf(cellValue));
                    metaDataProvider.setValue(colName, cellValue);
                }
                else if (columnType == ColumnType.SEMIMAJOR)
                {
                    // TODO We may have the correct units here if the column has
                    // imbedded units, but we have no way to preserve this.
                    Length len = ImbeddedUnitsLengthUtilities.getLength(cellValue);
                    ptData.setSma(Double.valueOf(len.getMagnitude()));
                    metaDataProvider.setValue(colName, cellValue);
                }
                else if (columnType == ColumnType.SEMIMINOR)
                {
                    // TODO We may have the correct units here if the column has
                    // imbedded units, but we have no way to preserve this.
                    Length len = ImbeddedUnitsLengthUtilities.getLength(cellValue);
                    ptData.setSmi(Double.valueOf(len.getMagnitude()));
                    metaDataProvider.setValue(colName, cellValue);
                }
                else if (columnType == ColumnType.ORIENTATION)
                {
                    ptData.setOrnt(Double.valueOf(cellValue));
                    metaDataProvider.setValue(colName, cellValue);
                }
                else if (columnType == ColumnType.RADIUS)
                {
                    // TODO We may have the correct units here if the column has
                    // imbedded units, but we have no way to preserve this.
                    Length len = ImbeddedUnitsLengthUtilities.getLength(cellValue);
                    ptData.setRadius(Double.valueOf(len.getMagnitude()));
                    metaDataProvider.setValue(colName, cellValue);
                }
                else if (columnType == ColumnType.LOB)
                {
                    ptData.setLob(Double.valueOf(cellValue));
                    metaDataProvider.setValue(colName, cellValue);
                }
                else
                {
                    metaDataProvider.setValue(colName, cellValue);
                }
            }
        }
        else
        {
            LOGGER.error("No special column found for " + colName);
        }
    }

    /**
     * Extract value from a string as a specific class type. Handles the number
     * primitives and boolean types, everything else is left as a string.
     *
     * @param colClass the col class
     * @param value the value
     * @return the object
     */
    private Object extractValueAsType(Class<?> colClass, String value)
    {
        Object result = value;
        try
        {
            if (colClass == String.class)
            {
                result = value;
            }
            else if (colClass == Float.class)
            {
                result = Float.valueOf(StringUtils.isEmpty(value) ? 0.0f : Float.parseFloat(value));
            }
            else if (colClass == Integer.class)
            {
                result = Integer.valueOf(StringUtils.isEmpty(value) ? 0 : Integer.parseInt(value));
            }
            else if (colClass == Double.class)
            {
                result = Double.valueOf(StringUtils.isEmpty(value) ? 0.0 : Double.parseDouble(value));
            }
            else if (colClass == Long.class)
            {
                result = Long.valueOf(StringUtils.isEmpty(value) ? 0L : Long.parseLong(value));
            }
            else if (colClass == Boolean.class)
            {
                result = "true".equalsIgnoreCase(value) ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        catch (RuntimeException e)
        {
            result = value;
        }
        return result;
    }

    /**
     * Checks if is special column.
     *
     * @param colIndex the col index
     * @return true, if is special column
     */
    private boolean isSpecialColumn(int colIndex)
    {
        return mySpecialColumnMap.contains(colIndex);
    }

    /**
     * Determine time span.
     *
     * @param ptData the pt data
     * @return the time span
     */
    private TimeSpan determineTimeSpan(PointExtract ptData)
    {
        Date startDate = ptData.getDate();
        Date endDate = ptData.getDownDate() == null ? startDate : ptData.getDownDate();
        if (startDate != null && endDate != null && startDate.after(endDate))
        {
            Date temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        TimeSpan ts = TimeSpan.get(startDate, endDate);
        return ts;
    }

    /**
     * Generate parts.
     *
     * @return the string[]
     */
    private String[] generateParts()
    {
        String[] parts = null;
        if (myUsesFixedWidthColumns)
        {
            parts = io.opensphere.mantle.util.StringUtils.explode(myCurrLine, myColumnBreaks, true);
        }
        else if (myIsDelimited)
        {
            CSVDelimitedColumnFormat columnFormat = (CSVDelimitedColumnFormat)getParseParams().getColumnFormat();
            if (myIsQuoted)
            {
                parts = New.array(myTokenizer.tokenize(myCurrLine), String.class);
            }
            else
            {
                parts = myCurrLine.split(columnFormat.getTokenDelimiter(), -1);
            }
        }
        return parts;
    }

    /**
     * Create a data element.
     *
     * @param metaDataProvider The metadata provider.
     * @param ptData The point data.
     * @param ts The time span.
     * @return The data element.
     */
    private DataElement getDataElement(MDILinkedMetaDataProvider metaDataProvider, PointExtract ptData, TimeSpan ts)
    {
        DataElement de;
        if (!getParseParams().hasCategory(ColumnType.Category.SPATIAL))
        {
            de = createNonMapElement(metaDataProvider, ts);
        }
        else if (getParseParams().hasType(ColumnType.WKT_GEOMETRY))
        {
            // Build the geometry from WKT.
            MapGeometrySupport geomSupport = JTSGeometryToGeometrySupportFactory
                    .createGeometrySupportFromWKTGeometry(ptData.getWKTGeometry(), getLayerColor());
            if (geomSupport != null)
            {
                geomSupport.setTimeSpan(ts);
                de = new DefaultMapDataElement(ourIDCounter.incrementAndGet(), ts, myTypeInfo, metaDataProvider, geomSupport);
                de.getVisualizationState().setColor(getLayerColor());
            }
            else
            {
                de = createNonMapElement(metaDataProvider, ts);
            }
        }
        else
        {
            MapLocationGeometrySupport geomSupport = geomFact.createGeometrySupport(
                    myTypeInfo.getMapVisualizationInfo(), ptData, getLayerColor());
            geomSupport.setTimeSpan(ts);
            de = new DefaultMapDataElement(ourIDCounter.incrementAndGet(), ts, myTypeInfo, metaDataProvider, geomSupport);
            de.getVisualizationState().setColor(getLayerColor());
        }
        return de;
    }

    /**
     * Creates a {@link DataElement} that does not have a geometry or location.
     *
     * @param metaDataProvider The metadata provider.
     * @param ts The time span.
     * @return The data element.
     */
    private DataElement createNonMapElement(MDILinkedMetaDataProvider metaDataProvider, TimeSpan ts)
    {
        DataElement de = new DefaultDataElement(ourIDCounter.incrementAndGet(), ts, myTypeInfo, metaDataProvider);
        de.getVisualizationState().setColor(getLayerColor());

        return de;
    }

    /**
     * Update overall time extent.
     *
     * @param ts the ts
     */
    private void updateOveralTimeExtent(TimeSpan ts)
    {
        if (!ts.isTimeless())
        {
            if (myOverallTimeExtent.isTimeless())
            {
                myOverallTimeExtent = ts;
            }
            else if (!myOverallTimeExtent.contains(ts))
            {
                myOverallTimeExtent = myOverallTimeExtent.simpleUnion(ts);
            }
        }
    }
}
