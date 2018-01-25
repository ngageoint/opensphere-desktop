package io.opensphere.csvcommon.ui.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.QuotingBufferedReader;
import io.opensphere.core.util.StrongObservableValue;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.IntegerModel;
import io.opensphere.core.util.swing.input.model.NameModel;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;
import io.opensphere.core.util.swing.input.model.TextModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.ColumnFormat;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.config.v2.CSVColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.DelimitedColumnFormatParameters;
import io.opensphere.csvcommon.detect.controller.DetectedParameters;
import io.opensphere.csvcommon.detect.controller.ReaderLineSampler;
import io.opensphere.csvcommon.ui.CsvLineTableModel;
import io.opensphere.csvcommon.ui.format.DisplayableCharacter;
import io.opensphere.importer.config.LayerSettings;

/**
 * The UI model for the FormatPanel.
 */
@SuppressWarnings("PMD.GodClass")
public class FormatPanelModel extends WrappedModel<CSVParseParameters>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FormatPanelModel.class);

    /**
     * Whether to calculate columns (true), or use what's in the parameters
     * (false).
     */
    private boolean myCalculateColumns;

    /** The Change listener. */
    private PropertyChangeListener myChangeListener;

    /** The column delimiter. */
    private final ChoiceModel<DisplayableCharacter> myColumnDelimiter = new ChoiceModel<DisplayableCharacter>(
            new DisplayableCharacter[] { new DisplayableCharacter(','), new DisplayableCharacter(';'),
                new DisplayableCharacter(':'), new DisplayableCharacter('|'), new DisplayableCharacter('/'),
                new DisplayableCharacter('\t', "Tab"), new DisplayableCharacter(' ', "Space(s)"), DisplayableCharacter.CUSTOM, });

    /** The indices where column divisions occur. */
    private final StrongObservableValue<int[]> myColumnDivisions = new StrongObservableValue<>();

    /** The column format. */
    private final ChoiceModel<ColumnFormat> myColumnFormat = new ChoiceModel<>(ColumnFormat.values());

    /** The comment character. */
    private final ChoiceModel<DisplayableCharacter> myCommentCharacter = new ChoiceModel<DisplayableCharacter>(
            new DisplayableCharacter[] { DisplayableCharacter.NONE, new DisplayableCharacter('#'), new DisplayableCharacter('!'),
                new DisplayableCharacter('$'), new DisplayableCharacter('%'), });

    /** The custom delimiter. */
    private final NameModel myCustomDelimiter = new NameModel();

    /** The model of detected parameters. */
    private DetectedParameters myDetectedParameters;

    /** The file which is being loaded. */
    private final File myFile;

    /** The first data row. 1-based counting. */
    private final IntegerModel myFirstDataRow = new IntegerModel(2, 99);

    /** Whether the data has a header. */
    private final BooleanModel myHasHeader = new BooleanModel();

    /** The header row. 1-based counting. */
    private final IntegerModel myHeaderRow = new IntegerModel(1, 98);

    /** Whether the model is completely initialized. */
    private final boolean myInitialized;

    /** The quote character. */
    private final ChoiceModel<DisplayableCharacter> myQuoteCharacter = new ChoiceModel<DisplayableCharacter>(
            new DisplayableCharacter[] { new DisplayableCharacter('"'), new DisplayableCharacter('\''),
                DisplayableCharacter.NONE, });

    /** The line sampler. */
    private LineSampler mySampler;

    /** The table model. */
    private final CsvLineTableModel myTableModel;

    /**
     * Helper method to add options to a DisplayableCharacter model.
     *
     * @param model the model
     * @param options the options
     */
    private static void addOptions(ChoiceModel<DisplayableCharacter> model, List<Character> options)
    {
        List<DisplayableCharacter> toAdd = New.list(options.size());
        for (Character option : options)
        {
            if (option != null)
            {
                DisplayableCharacter character = new DisplayableCharacter(option.charValue());
                if (!model.containsOption(character))
                {
                    toAdd.add(character);
                }
            }
        }
        if (!toAdd.isEmpty())
        {
            model.addOptions(toAdd.toArray(new DisplayableCharacter[toAdd.size()]));
        }
    }

    /**
     * Constructor.
     *
     * @param file The file which is being loaded.
     * @param selectedParameters The parameters for parsing the file.
     * @param detectedParameters The detected parameters.
     * @param sampler the line sampler
     */
    public FormatPanelModel(File file, final CSVParseParameters selectedParameters, DetectedParameters detectedParameters,
            LineSampler sampler)
    {
        myFile = file;
        myDetectedParameters = detectedParameters;
        mySampler = sampler;

        myHasHeader.setNameAndDescription("Data Has Header", "Whether the data has a header row");
        myHeaderRow.setNameAndDescription("Header Row", "The header row");
        myFirstDataRow.setNameAndDescription("First Data Row", "The first data row");
        myCommentCharacter.setNameAndDescription("Comment Character", "The comment character");
        myColumnFormat.setNameAndDescription("Column Format", "The column format");
        myColumnDelimiter.setNameAndDescription("Column Delimiter", "The column delimiter");
        myCustomDelimiter.setNameAndDescription("Custom Delimiter", "The custom delimiter");
        myQuoteCharacter.setNameAndDescription("Text Delimiter", "The text delimiter (quote character)");

        addModel(myHasHeader);
        addModel(myHeaderRow);
        addModel(myFirstDataRow);
        addModel(myCommentCharacter);
        addModel(myColumnFormat);
        addModel(myColumnDelimiter);
        addModel(myCustomDelimiter);
        addModel(myQuoteCharacter);
        myColumnDivisions.addListener(getChangeListener());

        myTableModel = new CsvLineTableModel(sampler);

        setupBehavior();
        set(selectedParameters);

        setupLineSampler(selectedParameters);
        myTableModel.setSelectedParameters(selectedParameters, myCalculateColumns);
        addPropertyChangeListener(getParamChangeListener());

        myInitialized = true;
    }

    /**
     * Gets the columnDelimiter.
     *
     * @return the columnDelimiter
     */
    public ChoiceModel<DisplayableCharacter> getColumnDelimiter()
    {
        return myColumnDelimiter;
    }

    /**
     * Gets the indices where column divisions occur.
     *
     * @return the indices where column divisions occur.
     */
    public int[] getColumnDivisions()
    {
        return myColumnDivisions.get() == null ? null : myColumnDivisions.get().clone();
    }

    /**
     * Gets the columnFormat.
     *
     * @return the columnFormat
     */
    public ChoiceModel<ColumnFormat> getColumnFormat()
    {
        return myColumnFormat;
    }

    /**
     * Gets the commentCharacter.
     *
     * @return the commentCharacter
     */
    public ChoiceModel<DisplayableCharacter> getCommentCharacter()
    {
        return myCommentCharacter;
    }

    /**
     * Gets the customDelimiter.
     *
     * @return the customDelimiter
     */
    public TextModel getCustomDelimiter()
    {
        return myCustomDelimiter;
    }

    /**
     * Gets the firstDataRow.
     *
     * @return the firstDataRow
     */
    public IntegerModel getFirstDataRow()
    {
        return myFirstDataRow;
    }

    /**
     * Gets the hasHeader.
     *
     * @return the hasHeader
     */
    public BooleanModel getHasHeader()
    {
        return myHasHeader;
    }

    /**
     * Gets the headerRow.
     *
     * @return the headerRow
     */
    public IntegerModel getHeaderRow()
    {
        return myHeaderRow;
    }

    /**
     * Gets the quoteCharacter.
     *
     * @return the quoteCharacter
     */
    public ChoiceModel<DisplayableCharacter> getQuoteCharacter()
    {
        return myQuoteCharacter;
    }

    /**
     * Gets the sampler.
     *
     * @return the sampler
     */
    public LineSampler getSampler()
    {
        return mySampler;
    }

    /**
     * Gets the tableModel.
     *
     * @return the tableModel
     */
    public CsvLineTableModel getTableModel()
    {
        return myTableModel;
    }

    /**
     * Sets the indices where column divisions occur.
     *
     * @param columnDivisions the indices where column divisions occur.
     */
    public void setColumnDivisions(int[] columnDivisions)
    {
        myColumnDivisions.set(columnDivisions == null ? null : columnDivisions.clone());
    }

    /**
     * Sets the detected parameters.
     *
     * @param detectedParameters the new detected parameters
     */
    public void setDetectedParameters(DetectedParameters detectedParameters)
    {
        myDetectedParameters = detectedParameters;
    }

    /**
     * Update models.
     *
     * @param parse the parse
     * @param layerSettings the layer settings
     * @param detected the detected
     * @param cellSampler the cell sampler
     */
    public void updateModels(CSVParseParameters parse, LayerSettings layerSettings, DetectedParameters detected,
            CellSampler cellSampler)
    {
        removePropertyChangeListener(getParamChangeListener());
        setDetectedParameters(detected);
        set(parse);
        setupLineSampler(parse);
        myTableModel.setSelectedParameters(parse, myCalculateColumns);
        addPropertyChangeListener(getParamChangeListener());
    }

    @Override
    protected final void updateDomainModel(CSVParseParameters selectedParameters)
    {
        int headerLine = myHasHeader.get().booleanValue() ? myHeaderRow.get().intValue() - 1 : -1;
        selectedParameters.setHeaderLine(Integer.valueOf(headerLine));
        selectedParameters.setDataStartLine(Integer.valueOf(myFirstDataRow.get().intValue() - 1));
        Character commentCharacter = myCommentCharacter.get().toCharacter();
        selectedParameters.setCommentIndicator(commentCharacter == null ? null : commentCharacter.toString());

        CSVParseParameters parametersCopy = new CSVParseParameters(selectedParameters);
        parametersCopy.setColumnFormat(getCsvColumnFormat());
        setupLineSampler(parametersCopy);

        // Update the table model with the updated selections
        myTableModel.setSelectedParameters(parametersCopy, myCalculateColumns);

        // Set these at the end in order to get updated values from the table
        // model
        selectedParameters.setColumnFormat(getCsvColumnFormat());
        selectedParameters.setColumnNames(parametersCopy.getColumnNames());
        selectedParameters.setColumnsToIgnore(parametersCopy.getColumnsToIgnore());
    }

    @Override
    protected void updateViewModel(CSVParseParameters selectedParameters)
    {
        // Set header row
        int headerLine = getHeaderLine(selectedParameters);
        myHeaderRow.set(Integer.valueOf(headerLine));

        // Set first data line
        int firstDataLine = getFirstDataLine(selectedParameters);
        myFirstDataRow.set(Integer.valueOf(firstDataLine));

        // Set has header
        boolean hasHeader = isHeaderPresent(selectedParameters);
        myHasHeader.set(Boolean.valueOf(hasHeader));

        // Set comment character
        DisplayableCharacter commentChar = getCommentChar(selectedParameters);
        if (myDetectedParameters.getCommentParameter() != null)
        {
            List<Character> options = StreamUtilities.map(myDetectedParameters.getCommentParameter().getValues(),
                    new Function<ValueWithConfidence<? extends Character>, Character>()
                    {
                        @Override
                        public Character apply(ValueWithConfidence<? extends Character> value)
                        {
                            return value.getValue();
                        }
                    });
            addOptions(myCommentCharacter, options);
        }
        myCommentCharacter.set(commentChar);

        // Set column format
        myColumnFormat.set(getColumnFormat(selectedParameters));

        // Set column delimiter
        // Set custom delimiter
        // Set quote character
        DisplayableCharacter columnDelimiter = new DisplayableCharacter(',');
        String customDelimiter = String.valueOf(',');
        DisplayableCharacter quoteCharacter = new DisplayableCharacter('"');
        if (selectedParameters.getColumnFormat() instanceof CSVDelimitedColumnFormat)
        {
            CSVDelimitedColumnFormat delimitedFormat = (CSVDelimitedColumnFormat)selectedParameters.getColumnFormat();

            if (myDetectedParameters.getColumnFormatParameter() != null)
            {
                List<Character> tokenOptions = New.list();
                List<Character> textOptions = New.list();
                for (ValueWithConfidence<? extends ColumnFormatParameters> value : myDetectedParameters.getColumnFormatParameter()
                        .getValues())
                {
                    if (value.getValue() instanceof DelimitedColumnFormatParameters)
                    {
                        tokenOptions.add(((DelimitedColumnFormatParameters)value.getValue()).getTokenDelimiter());
                        if (((DelimitedColumnFormatParameters)value.getValue()).getTextDelimiter() != null)
                        {
                            textOptions.add(((DelimitedColumnFormatParameters)value.getValue()).getTextDelimiter());
                        }
                    }
                }
                addOptions(myColumnDelimiter, tokenOptions);
                addOptions(myQuoteCharacter, textOptions);
            }

            columnDelimiter = new DisplayableCharacter(delimitedFormat.getTokenDelimiter().charAt(0));
            if (delimitedFormat.getTokenDelimiter().length() != 1 || !myColumnDelimiter.getOptions().contains(columnDelimiter))
            {
                columnDelimiter = DisplayableCharacter.CUSTOM;
                customDelimiter = delimitedFormat.getTokenDelimiter();
            }

            quoteCharacter = delimitedFormat.getTextDelimiter() != null
                    ? new DisplayableCharacter(delimitedFormat.getTextDelimiter().charAt(0)) : DisplayableCharacter.NONE;
        }
        myColumnDelimiter.set(columnDelimiter);
        myCustomDelimiter.set(customDelimiter);
        myQuoteCharacter.set(quoteCharacter);

        // Set column widths
        int[] columnDivisions = new int[0];
        if (selectedParameters.getColumnFormat() instanceof CSVFixedWidthColumnFormat)
        {
            CSVFixedWidthColumnFormat fixedWidthFormat = (CSVFixedWidthColumnFormat)selectedParameters.getColumnFormat();
            columnDivisions = fixedWidthFormat.getColumnDivisions();
        }
        myColumnDivisions.set(columnDivisions);
    }

    /**
     * Get the column format from the selected parameters.
     *
     * @param selectedParameters The selected parameters.
     * @return The column format.
     */
    private ColumnFormat getColumnFormat(CSVParseParameters selectedParameters)
    {
        ColumnFormat columnFormat;
        if (selectedParameters.getColumnFormat() instanceof CSVDelimitedColumnFormat)
        {
            columnFormat = ColumnFormat.DELIMITED;
        }
        else if (selectedParameters.getColumnFormat() instanceof CSVFixedWidthColumnFormat)
        {
            columnFormat = ColumnFormat.FIXED_WIDTH;
        }
        else
        {
            columnFormat = myColumnFormat.get();
        }
        return columnFormat;
    }

    /**
     * Get the comment character from the selected parameters.
     *
     * @param selectedParameters The selected parameters.
     * @return The comment character.
     */
    private DisplayableCharacter getCommentChar(CSVParseParameters selectedParameters)
    {
        DisplayableCharacter commentChar = DisplayableCharacter.NONE;
        if (selectedParameters.getCommentIndicator() != null)
        {
            commentChar = new DisplayableCharacter(selectedParameters.getCommentIndicator().charAt(0));
        }
        return commentChar;
    }

    /**
     * Gets the CSVColumnFormat from the current UI model state.
     *
     * @return the CSVColumnFormat
     */
    private CSVColumnFormat getCsvColumnFormat()
    {
        return myColumnFormat.get() == ColumnFormat.DELIMITED ? getDelimitedColumnFormat() : getFixedWidthColumnFormat();
    }

    /**
     * Gets the CSVDelimitedColumnFormat from the current UI model state.
     *
     * @return the CSVDelimitedColumnFormat
     */
    private CSVDelimitedColumnFormat getDelimitedColumnFormat()
    {
        String tokenDelimiter = myColumnDelimiter.get() == DisplayableCharacter.CUSTOM ? myCustomDelimiter.get()
                : String.valueOf(myColumnDelimiter.get().charValue());
        String textDelimiter = myQuoteCharacter.get() == DisplayableCharacter.NONE ? null
                : String.valueOf(myQuoteCharacter.get().charValue());
        int columnCount = myTableModel.getColumnCount();
        return new CSVDelimitedColumnFormat(tokenDelimiter, textDelimiter, columnCount);
    }

    /**
     * Get the delimited column format, if applicable.
     *
     * @param selectedParameters The parse parameters.
     * @return The delimited column format, or {@code null}.
     */
    private CSVDelimitedColumnFormat getDelimitedFormat(CSVParseParameters selectedParameters)
    {
        CSVDelimitedColumnFormat delimFormat = null;
        if (selectedParameters.getColumnFormat() instanceof CSVDelimitedColumnFormat
                && StringUtils.isNotEmpty(((CSVDelimitedColumnFormat)selectedParameters.getColumnFormat()).getTextDelimiter()))
        {
            delimFormat = (CSVDelimitedColumnFormat)selectedParameters.getColumnFormat();
        }
        return delimFormat;
    }

    /**
     * Get the selected first data line number.
     *
     * @param selectedParameters The selected parameters.
     * @return The first data line number.
     */
    private int getFirstDataLine(CSVParseParameters selectedParameters)
    {
        return selectedParameters.getDataStartLine() != null ? selectedParameters.getDataStartLine().intValue() + 1
                : getHeaderLine(selectedParameters) + 1;
    }

    /**
     * Gets the CSVFixedWidthColumnFormat from the current UI model state.
     *
     * @return the CSVFixedWidthColumnFormat
     */
    private CSVFixedWidthColumnFormat getFixedWidthColumnFormat()
    {
        return new CSVFixedWidthColumnFormat(myColumnDivisions.get() == null ? new int[0] : myColumnDivisions.get());
    }

    /**
     * Get the selected header line number.
     *
     * @param selectedParameters The selected parameters.
     * @return The header line number.
     */

    private int getHeaderLine(CSVParseParameters selectedParameters)
    {
        return selectedParameters.getHeaderLine() != null ? selectedParameters.getHeaderLine().intValue() + 1 : 1;
    }

    /**
     * Gets the param change listener.
     *
     * @return the param change listener
     */
    private PropertyChangeListener getParamChangeListener()
    {
        if (myChangeListener == null)
        {
            myChangeListener = new PropertyChangeListener()
            {
                @Override
                public void stateChanged(PropertyChangeEvent e)
                {
                    if (e.getProperty() == PropertyChangeEvent.Property.WRAPPED_VALUE_CHANGED)
                    {
                        /* We want to use the columns that are in the selected
                         * parameters as much as possible, except when the user
                         * changes something that affects the columns. */
                        myCalculateColumns = myInitialized && Utilities.notSameInstance(e.getSource(), myFirstDataRow)
                                && Utilities.notSameInstance(e.getSource(), myCommentCharacter);

                        applyChanges();
                    }
                }
            };
        }
        return myChangeListener;
    }

    /**
     * Determine if a header row is present.
     *
     * @param selectedParameters The selected parameters.
     * @return {@code true} if a header row is present.
     */
    private boolean isHeaderPresent(CSVParseParameters selectedParameters)
    {
        return selectedParameters.getHeaderLine() != null && selectedParameters.getHeaderLine().intValue() != -1;
    }

    /**
     * Sets up the behavior of this model.
     */
    private void setupBehavior()
    {
        myHasHeader.addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                boolean hasHeader = myHasHeader.get().booleanValue();
                myHeaderRow.setEnabled(hasHeader);
                myHeaderRow.setMin(hasHeader ? 1 : 0);
                myFirstDataRow.setMin(hasHeader ? myHeaderRow.get().intValue() + 1 : 1);
            }
        });
        myHeaderRow.addListener(new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)
            {
                myFirstDataRow.setMin(myHeaderRow.get().intValue() + 1);
            }
        });
        myColumnFormat.addListener(new ChangeListener<ColumnFormat>()
        {
            @Override
            public void changed(ObservableValue<? extends ColumnFormat> observable, ColumnFormat oldValue, ColumnFormat newValue)
            {
                myColumnDelimiter.setEnabled(myColumnFormat.get() == ColumnFormat.DELIMITED);
                myQuoteCharacter.setEnabled(myColumnFormat.get() == ColumnFormat.DELIMITED);
            }
        });
        myColumnDelimiter.addListener(new ChangeListener<DisplayableCharacter>()
        {
            @Override
            public void changed(ObservableValue<? extends DisplayableCharacter> observable, DisplayableCharacter oldValue,
                    DisplayableCharacter newValue)
            {
                myCustomDelimiter.setVisible(myColumnDelimiter.get() == DisplayableCharacter.CUSTOM);
            }
        });
    }

    /**
     * Determine whether the current sampler needs to be replaced based on the
     * parse parameters.
     *
     * @param selectedParameters The new parse parameters
     */
    private void setupLineSampler(CSVParseParameters selectedParameters)
    {
        if (myFile == null)
        {
            return;
        }

        CSVDelimitedColumnFormat delimFormat = getDelimitedFormat(selectedParameters);

        boolean needsNewSampler = false;
        if (mySampler instanceof ReaderLineSampler)
        {
            if (delimFormat == null)
            {
                // The current sampler uses a text delimiter, but we are
                // switching to fixed size columns
                needsNewSampler = true;
            }
            else
            {
                char[] quotes = ((ReaderLineSampler)mySampler).getReader().getQuotes();
                if (quotes == null || quotes.length == 0 || quotes[0] != delimFormat.getTextDelimiter().charAt(0))
                {
                    // The current sampler uses a text delimiter, but the
                    // delimiter has changed
                    needsNewSampler = true;
                }
            }
        }
        else if (mySampler == null || delimFormat != null)
        {
            // The current sampler does not use a text delimiter, but it should
            needsNewSampler = true;
        }

        if (needsNewSampler)
        {
            Reader reader = null;
            QuotingBufferedReader quotingReader = null;
            try
            {
                reader = new InputStreamReader(new FileInputStream(myFile), StringUtilities.DEFAULT_CHARSET);
                if (delimFormat != null)
                {
                    quotingReader = new QuotingBufferedReader(reader, new char[] { delimFormat.getTextDelimiter().charAt(0) },
                            null);
                    mySampler = new ReaderLineSampler(quotingReader, 100, 20);
                }
                else
                {
                    mySampler = new ReaderLineSampler(reader, 100, 20);
                }
                myTableModel.setSampler(mySampler);
            }
            catch (FileNotFoundException e)
            {
                LOGGER.error("Could not read file." + e, e);
            }
            finally
            {
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to close file reader: " + e, e);
                }
                finally
                {
                    try
                    {
                        if (quotingReader != null)
                        {
                            quotingReader.close();
                        }
                    }
                    catch (IOException e)
                    {
                        LOGGER.error("Failed to close file reader: " + e, e);
                    }
                }
            }
        }
    }
}
