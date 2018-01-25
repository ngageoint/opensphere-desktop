package io.opensphere.csvcommon.ui;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.TextDelimitedStringTokenizer;
import io.opensphere.core.util.swing.table.AbstractObjectTableModel;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.common.Utilities;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.format.CellFormatter;
import io.opensphere.csvcommon.format.factory.CellFormatterFactory;
import io.opensphere.csvcommon.ui.CsvUiUtilities;
import io.opensphere.importer.config.SpecialColumn;

/**
 * Table model for LineSampler.
 */
@SuppressWarnings("PMD.GodClass")
public class CsvLineTableModel extends AbstractObjectTableModel<String>
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(CsvLineTableModel.class);

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The cell formatter factory. */
    private final transient CellFormatterFactory myCellFormatterFactory = new CellFormatterFactory();

    /** The sampler. */
    private transient LineSampler mySampler;

    /** The preferences registry. */
    private transient PreferencesRegistry myPreferencesRegistry;

    /** Whether the do cell formatting, hide columns, etc. */
    private boolean myDoFormatting;

    /** The current selected parameters. */
    private transient CSVParseParameters mySelectedParameters;

    /** The special column map. */
    private TIntObjectHashMap<SpecialColumn> mySpecialColumnMap;

    /** The tokenizer. */
    private transient StringTokenizer myTokenizer;

    /**
     * Constructor. This version will not do formatting.
     *
     * @param sampler the line sampler
     */
    public CsvLineTableModel(LineSampler sampler)
    {
        super();
        mySampler = sampler;
    }

    /**
     * Constructor. This version will do formatting.
     *
     * @param sampler the line sampler
     * @param preferencesRegistry the preferences registry
     */
    public CsvLineTableModel(LineSampler sampler, PreferencesRegistry preferencesRegistry)
    {
        super();
        mySampler = sampler;
        myPreferencesRegistry = preferencesRegistry;
        myDoFormatting = preferencesRegistry != null;
    }

    /**
     * Get the sampler used to determine the table elements.
     *
     * @param sampler the sampler used to determine the table elements.
     */
    public void setSampler(LineSampler sampler)
    {
        mySampler = sampler;
    }

    /**
     * Sets the state of the model based on CSVParseParameters.
     *
     * @param selectedParameters the selected parameters
     * @param calculateColumns whether to calculate the columns
     */
    public void setSelectedParameters(CSVParseParameters selectedParameters, boolean calculateColumns)
    {
        mySelectedParameters = selectedParameters;
        mySpecialColumnMap = Utilities.createSpecialColumnMap(mySelectedParameters.getSpecialColumns());

        updateTokenizer();
        updateModel(calculateColumns);
    }

    @Override
    protected List<?> getRowValues(String dataObject)
    {
        List<String> row;

        // Fixed width
        if (mySelectedParameters != null && mySelectedParameters.getColumnFormat() instanceof CSVFixedWidthColumnFormat)
        {
            int[] columnDivisionIndices = ((CSVFixedWidthColumnFormat)mySelectedParameters.getColumnFormat())
                    .getColumnDivisions();
            row = parseColumns(dataObject, columnDivisionIndices);
        }
        // Delimited
        else if (myTokenizer != null)
        {
            row = myTokenizer.tokenize(dataObject);
        }
        else
        {
            row = New.list(dataObject);
        }

        // Ensure there are enough columns
        if (row.size() < getColumnCount())
        {
            int toAdd = getColumnCount() - row.size();
            for (int i = 0; i < toAdd; i++)
            {
                row.add("");
            }
        }

        if (myDoFormatting)
        {
            // Format the row
            formatRow(row);

            // Filter only the columns that are included
            row = filterIncludedColumns(row);
        }

        return row;
    }

    /**
     * Filters the list of complete columns down to only the included columns.
     *
     * @param row the row of columns
     * @return the list of included columns
     */
    private List<String> filterIncludedColumns(List<String> row)
    {
        List<String> filtered = row;
        if (!mySelectedParameters.getColumnsToIgnore().isEmpty())
        {
            filtered = New.list(row.size());
            for (int columnIndex = 0; columnIndex < row.size(); ++columnIndex)
            {
                if (!mySelectedParameters.getColumnsToIgnore().contains(Integer.valueOf(columnIndex)))
                {
                    filtered.add(row.get(columnIndex));
                }
            }
        }
        return filtered;
    }

    /**
     * Formats the cells in the row.
     *
     * @param row the row
     */
    private void formatRow(List<String> row)
    {
        if (!mySpecialColumnMap.isEmpty())
        {
            for (int columnIndex = 0; columnIndex < row.size(); ++columnIndex)
            {
                SpecialColumn specialColumn = mySpecialColumnMap.get(columnIndex);
                if (specialColumn != null && specialColumn.getFormat() != null)
                {
                    CellFormatter formatter = myCellFormatterFactory.getFormatter(specialColumn.getColumnType(),
                            myPreferencesRegistry);
                    if (formatter != null)
                    {
                        String cellValue = row.get(columnIndex);
                        try
                        {
                            Object cellObject = formatter.formatCell(cellValue, specialColumn.getFormat());
                            String systemFormat = myCellFormatterFactory.getSystemFormat(specialColumn.getColumnType(),
                                    myPreferencesRegistry);
                            String formattedValue = formatter.fromObjectValue(cellObject, systemFormat);
                            row.set(columnIndex, formattedValue);
                        }
                        catch (ParseException e)
                        {
                            if (LOGGER.isDebugEnabled())
                            {
                                LOGGER.debug(e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates column identifiers for when there are no headers.
     *
     * @return the column identifiers
     */
    private List<String> generateColumnIdentifiers()
    {
        // Get the column count
        int columnCount = 1;
        if (mySelectedParameters.getColumnFormat() instanceof CSVFixedWidthColumnFormat)
        {
            int[] columnWidths = ((CSVFixedWidthColumnFormat)mySelectedParameters.getColumnFormat()).getColumnDivisions();
            columnCount = columnWidths.length + 1;
        }
        else if (myTokenizer != null)
        {
            for (String line : mySampler.getBeginningSampleLines())
            {
                columnCount = Math.max(columnCount, myTokenizer.tokenize(line).size());
            }
        }

        // Create fake column names
        return CsvUiUtilities.generateDefaultColumnIdentifiers(columnCount);
    }

    /**
     * Gets the column identifiers.
     *
     * @param calculateColumns whether to calculate the columns
     * @return the column identifiers
     */
    private List<String> getColumnIdentifiers(boolean calculateColumns)
    {
        List<String> columnIdentifiers;

        // Calculate our own columns
        if (calculateColumns || !CollectionUtilities.hasContent(mySelectedParameters.getColumnNames()))
        {
            if (mySampler instanceof CellSampler)
            {
                List<? extends String> headerCells = ((CellSampler)mySampler).getHeaderCells();
                columnIdentifiers = headerCells != null ? New.list(headerCells) : generateColumnIdentifiers();
            }
            else
            {
                boolean hasHeader = mySelectedParameters.getHeaderLine() != null
                        && mySelectedParameters.getHeaderLine().intValue() != -1;
                if (hasHeader)
                {
                    int headerLine = mySelectedParameters.getHeaderLine().intValue();
                    if (headerLine >= 0 && headerLine < mySampler.getBeginningSampleLines().size())
                    {
                        String headerData = mySampler.getBeginningSampleLines().get(headerLine);
                        if (mySelectedParameters.getColumnFormat() instanceof CSVFixedWidthColumnFormat)
                        {
                            int[] columnWidths = ((CSVFixedWidthColumnFormat)mySelectedParameters.getColumnFormat())
                                    .getColumnDivisions();
                            columnIdentifiers = parseColumns(headerData, columnWidths);
                        }
                        else if (myTokenizer != null)
                        {
                            columnIdentifiers = myTokenizer.tokenize(headerData);
                        }
                        else
                        {
                            columnIdentifiers = Collections.singletonList(headerData);
                        }
                    }
                    else
                    {
                        columnIdentifiers = Collections.emptyList();
                    }
                }
                else
                {
                    columnIdentifiers = generateColumnIdentifiers();
                }
            }

            // Update the selected parameters with the new column identifiers
            mySelectedParameters.setColumnNames(columnIdentifiers);
            mySelectedParameters.getColumnsToIgnore().clear();
        }
        // Just use what we were told to
        else
        {
            columnIdentifiers = New.list(mySelectedParameters.getColumnNames());
        }

        if (myDoFormatting)
        {
            // Filter only the columns that are included
            columnIdentifiers = filterIncludedColumns(columnIdentifiers);
        }

        return columnIdentifiers;
    }

    /**
     * Gets the data objects.
     *
     * @return the data objects
     */
    private List<? extends String> getDataObjects()
    {
        List<? extends String> data;
        int firstDataLine = mySelectedParameters.getDataStartLine() != null ? mySelectedParameters.getDataStartLine().intValue()
                : 1;

        // CellSampler starts from the first line after the header, so adjust
        // the first data line to account for this
        if (mySampler instanceof CellSampler)
        {
            boolean hasHeader = mySelectedParameters.getHeaderLine() != null
                    && mySelectedParameters.getHeaderLine().intValue() != -1;
            if (hasHeader)
            {
                firstDataLine -= mySelectedParameters.getHeaderLine().intValue() + 1;
            }
        }

        if (firstDataLine >= 0 && firstDataLine < mySampler.getBeginningSampleLines().size())
        {
            data = mySampler.getBeginningSampleLines().subList(firstDataLine, mySampler.getBeginningSampleLines().size());

            // Filter out commented lines
            final Character commentCharacter = mySelectedParameters.getCommentIndicator() != null
                    ? Character.valueOf(mySelectedParameters.getCommentIndicator().charAt(0)) : null;
            if (commentCharacter != null)
            {
                data = StreamUtilities.filter(data, line -> !StringUtilities.startsWith(line, commentCharacter.charValue()));
            }
        }
        else
        {
            data = Collections.emptyList();
        }

        return data;
    }

    /**
     * Updates the model from the sampler and other data.
     *
     * @param calculateColumns whether to calculate the columns
     */
    private void updateModel(boolean calculateColumns)
    {
        setColumnIdentifiers(getColumnIdentifiers(calculateColumns));
        setColumnClasses(getColumnClasses(getColumnCount()));
        setData(getDataObjects());
    }

    /**
     * Updates the tokenizer based on the current state.
     */
    private void updateTokenizer()
    {
        if (mySelectedParameters.getColumnFormat() instanceof CSVDelimitedColumnFormat)
        {
            CSVDelimitedColumnFormat delimitedFormat = (CSVDelimitedColumnFormat)mySelectedParameters.getColumnFormat();

            myTokenizer = StringUtils.isEmpty(delimitedFormat.getTokenDelimiter()) ? null
                    : new TextDelimitedStringTokenizer(delimitedFormat.getTokenDelimiter(), delimitedFormat.getTextDelimiter());
        }
    }

    /**
     * Gets the specified number of String column classes.
     *
     * @param count the count
     * @return the classes
     */
    private static Class<?>[] getColumnClasses(int count)
    {
        Class<?>[] columnClasses = new Class<?>[count];
        Arrays.fill(columnClasses, String.class);
        return columnClasses;
    }

    /**
     * Parses the given string into tokens based on the column widths.
     *
     * @param text the text
     * @param columnWidths the column widths
     * @return the tokens
     */
    private static List<String> parseColumns(String text, int[] columnWidths)
    {
        List<String> tokens = New.list(columnWidths.length + 1);
        int beginIndex = 0;
        int endIndex;
        for (int width : columnWidths)
        {
            endIndex = Math.min(width, text.length());
            tokens.add(text.substring(beginIndex, endIndex));
            beginIndex = endIndex;
        }
        tokens.add(text.substring(beginIndex, text.length()));
        return tokens;
    }
}
