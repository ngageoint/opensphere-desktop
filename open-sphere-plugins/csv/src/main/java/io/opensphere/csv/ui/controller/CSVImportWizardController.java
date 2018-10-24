package io.opensphere.csv.ui.controller;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.util.QuotingBufferedReader;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.wizard.WizardCallback;
import io.opensphere.core.util.swing.wizard.WizardController;
import io.opensphere.core.util.swing.wizard.model.DefaultWizardRules;
import io.opensphere.core.util.swing.wizard.model.DefaultWizardStepListModel;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;
import io.opensphere.core.util.swing.wizard.view.WizardDialog;
import io.opensphere.csv.config.v2.CSVDataSource;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.config.v2.CSVColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.DelimitedColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.FixedWidthColumnFormatParameters;
import io.opensphere.csvcommon.detect.controller.CellSamplerImpl;
import io.opensphere.csvcommon.detect.controller.DetectedParameters;
import io.opensphere.csvcommon.detect.controller.DetectionControllerImpl;
import io.opensphere.csvcommon.detect.controller.LineSamplerFactoryImpl;
import io.opensphere.csvcommon.detect.controller.ReaderLineSampler;
import io.opensphere.csvcommon.detect.controller.TokenizerFactoryImpl;
import io.opensphere.csvcommon.detect.lob.model.LineOfBearingColumn;
import io.opensphere.csvcommon.detect.lob.model.LobColumnResults;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.csvcommon.ui.CsvUiUtilities;
import io.opensphere.csvcommon.ui.controller.ImportableColumnController;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.LayerSettings;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.data.ColumnTypeDetector;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.ProgressDialog;

/** Controller for the CSV import wizard. */
@SuppressWarnings("PMD.GodClass")
public class CSVImportWizardController implements Observer
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CSVImportWizardController.class);

    /**
     * The minimum confidence required from a detector in order to consider the
     * the setting valid.
     */
    private static final double ourConfidenceThreshold = .5;

    /** The Data source. */
    private CSVDataSource myDataSource;

    /** The Detection controller. */
    private DetectionControllerImpl myDetectionController;

    /** The CSV wizard panel model. */
    private CSVWizardPanelModel myCSVWizardPanelModel;

    /** The Wizard step list model. */
    private WizardStepListModel myWizardStepListModel;

    /** The Cell sampler. */
    private CellSampler myCellSampler;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The wizard window. */
    private Window myWizardWindow;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public CSVImportWizardController(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Start the wizard to import a CSV document.
     *
     * @param parent The parent component.
     * @param dataSource The data source
     * @param namesInUse the names in use
     * @param wizardCallback The callback to be called from the EDT when the
     *            wizard is finished
     * @throws FileNotFoundException If the file cannot be opened.
     */
    public void startWizard(final Window parent, CSVDataSource dataSource, final Set<String> namesInUse,
            final WizardCallback wizardCallback) throws FileNotFoundException
    {
        assert EventQueue.isDispatchThread();

        myDataSource = dataSource;
        ColumnTypeDetector columnTypeDetector = MantleToolboxUtils.getMantleToolbox(myToolbox).getColumnTypeDetector();
        myDetectionController = new DetectionControllerImpl(myToolbox.getPreferencesRegistry(), columnTypeDetector);
        final File file = new File(dataSource.getSourceUri());

        final ProgressDialog progressDialog = new ProgressDialog(parent, "Import CSV File", false, true, false, null, 0);
        progressDialog.setVisible(true, 100);

        Callable<DetectedParameters> task = () ->
        {
            progressDialog.setMessage("Detecting CSV parameters");
            return setupParams(file, false, null);
        };
        Consumer<? super DetectedParameters> resultConsumer = detected ->
        {
            try
            {
                progressDialog.setMessage("Launching CSV wizard");
                launchWizard(parent, namesInUse, wizardCallback, file, detected);
            }
            finally
            {
                progressDialog.dispose();
            }
        };
        Consumer<Exception> errorHandler = e ->
        {
            LOGGER.error(e, e);
            progressDialog.dispose();
        };
        EventQueueUtilities.runInBackgroundAndReturnResult(null, task, resultConsumer, errorHandler);
    }

    /**
     * Setup the detected parameters.
     *
     * @param file the file
     * @param redetect if these parameters have been redetected
     * @param changed the changed parameter
     * @return the detected parameters
     * @throws FileNotFoundException the file not found exception
     */
    private DetectedParameters setupParams(File file, boolean redetect, String changed) throws FileNotFoundException
    {
        assert !EventQueue.isDispatchThread();

        DetectedParameters detected = null;

        try (InputStreamReader lineSamplerStreamReader = new InputStreamReader(new FileInputStream(file),
                StringUtilities.DEFAULT_CHARSET);
                InputStreamReader quotingBufferedStreamReader = new InputStreamReader(new FileInputStream(file),
                        StringUtilities.DEFAULT_CHARSET))
        {
            LineSampler lineSampler = new ReaderLineSampler(lineSamplerStreamReader, 100, 20);
            detected = myDetectionController.detectParameters(myDataSource.getParseParameters(), lineSampler,
                    new LineSamplerFactoryImpl(file), changed);
            CSVColumnFormat columnFormat = myDataSource.getParseParameters().getColumnFormat();
            StringTokenizer tokenizer = new TokenizerFactoryImpl().getTokenizer(columnFormat,
                    detected.getColumnFormatParameter().getBestValue());

            if (columnFormat instanceof CSVDelimitedColumnFormat)
            {
                // create a line sampler with the correct quote type.

                String textDelimiterStr = ((CSVDelimitedColumnFormat)columnFormat).getTextDelimiter();
                Character textDelimiter = textDelimiterStr != null ? Character.valueOf(textDelimiterStr.charAt(0)) : null;
                if (textDelimiter != null)
                {
                    char[] quote = new char[] { textDelimiter.charValue() };
                    QuotingBufferedReader quotingReader = new QuotingBufferedReader(quotingBufferedStreamReader, quote, null);
                    lineSampler = new ReaderLineSampler(quotingReader, 100, 20);
                }
            }

            int headerLine = -1;
            if (myDataSource.getParseParameters().getHeaderLine() != null
                    && myDataSource.getParseParameters().getHeaderLine().intValue() > -1)
            {
                headerLine = myDataSource.getParseParameters().getHeaderLine().intValue();
            }
            else if (detected.getHeaderLineParameter() != null
                    && detected.getHeaderLineParameter().getBestConfidence() > ourConfidenceThreshold)
            {
                Integer value = detected.getHeaderLineParameter().getBestValue();
                headerLine = value == null ? -1 : value.intValue();
            }

            myCellSampler = new CellSamplerImpl(lineSampler, tokenizer, headerLine);
            copyDetectedParametersToParseParameters(myCellSampler, detected, myDataSource.getParseParameters());
            initializeLayerSettings(myDataSource.getLayerSettings(), detected, myDataSource.getParseParameters());
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to close file: " + e, e);
        }
        return detected;
    }

    /**
     * Actually launches the wizard.
     *
     * @param parent The parent component.
     * @param namesInUse the names in use
     * @param wizardCallback The callback to be called when the wizard is
     *            finished
     * @param file The file
     * @param detected The detected parameters
     */
    private void launchWizard(Window parent, Set<String> namesInUse, WizardCallback wizardCallback, File file,
            DetectedParameters detected)
    {
        myWizardStepListModel = new DefaultWizardStepListModel(Arrays.asList(CSVWizardPanelModel.WizardStep.FORMAT.toString(),
                CSVWizardPanelModel.WizardStep.COLUMNS.toString(), CSVWizardPanelModel.WizardStep.SUMMARY.toString()),
                CSVWizardPanelModel.WizardStep.FORMAT.toString());

        myDataSource.getParseParameters().addObserver(this);

        WizardDialog dialog = new WizardDialog(parent, "CSV Import Wizard - " + myDataSource.getName(),
                Dialog.ModalityType.MODELESS);
        // Save a copy of the window in order to display dialogs off of it
        myWizardWindow = (Window)dialog.getComponent();

        myCSVWizardPanelModel = new CSVWizardPanelModel(file, myToolbox, myDataSource.getParseParameters(), detected,
                myDataSource.getLayerSettings(), namesInUse);
        dialog.setModels(myWizardStepListModel, myCSVWizardPanelModel);

        // Skip to the summary step if all other steps are valid.
        String skipToStep = null;
        for (String step : myWizardStepListModel.getStepTitles())
        {
            if (myWizardStepListModel.getStepState(step) != StepState.VALID)
            {
                skipToStep = step;
                break;
            }
        }
        if (StringUtils.isNotEmpty(skipToStep))
        {
            myWizardStepListModel.setCurrentStep(skipToStep);
        }
        else
        {
            myWizardStepListModel.setCurrentStep(CSVWizardPanelModel.WizardStep.SUMMARY.toString());
        }

        WizardController wizardController = new WizardController(dialog);
        wizardController.setRules(new DefaultWizardRules(myWizardStepListModel));
        wizardController.setFinishAction(wizardCallback);
        wizardController.setMinimumSize(600, 480);
        wizardController.startWizard();
    }

    /**
     * Adds a date column to the parse parameters.
     *
     * @param col the column to check
     * @param parse the csv parameters
     * @param isDownTime the is down time flag
     */
    private void addDateColumn(DateColumn col, CSVParseParameters parse, boolean isDownTime)
    {
        SpecialColumn dateColumn = new SpecialColumn();
        dateColumn.setColumnIndex(col.getPrimaryColumnIndex());
        ColumnType cType = null;

        if (!isDownTime)
        {
            switch (col.getDateColumnType())
            {
                case DATE:
                    cType = ColumnType.DATE;
                    break;
                case TIME:
                    cType = ColumnType.TIME;
                    break;
                case TIMESTAMP:
                    cType = ColumnType.TIMESTAMP;
                    break;

                default:
                    break;
            }
        }
        else
        {
            switch (col.getDateColumnType())
            {
                case DATE:
                    cType = ColumnType.DOWN_DATE;
                    break;
                case TIME:
                    cType = ColumnType.DOWN_TIME;
                    break;
                case TIMESTAMP:
                    cType = ColumnType.DOWN_TIMESTAMP;
                    break;

                default:
                    break;
            }
        }

        dateColumn.setColumnType(cType);
        dateColumn.setFormat(col.getPrimaryColumnFormat());

        if (col.getSecondaryColumnIndex() > -1)
        {
            SpecialColumn timeColumn = new SpecialColumn();
            timeColumn.setColumnIndex(col.getSecondaryColumnIndex());
            timeColumn.setColumnType(ColumnType.TIME);
            if (isDownTime)
            {
                timeColumn.setColumnType(ColumnType.DOWN_TIME);
            }
            timeColumn.setFormat(col.getSecondaryColumnFormat());
            parse.getSpecialColumns().add(timeColumn);
            dateColumn.setColumnType(ColumnType.DATE);
        }

        parse.getSpecialColumns().add(dateColumn);
    }

    /**
     * Adds the latitude/longitude column pair.
     *
     * @param parse the csv parameters
     * @param llColRes the lat/lon results
     */
    private void addLatLonColumns(CSVParseParameters parse, LatLonColumnResults llColRes)
    {
        PotentialLocationColumn latColumn = llColRes.getLatColumn();
        PotentialLocationColumn lonColumn = llColRes.getLonColumn();

        if (llColRes.getColumnType() != null && llColRes.getColumnType().equals(ColumnType.POSITION))
        {
            SpecialColumn locSpecialColumn = new SpecialColumn();
            locSpecialColumn.setColumnIndex(latColumn.getColumnIndex());
            locSpecialColumn.setColumnType(llColRes.getColumnType());
            locSpecialColumn.setFormat(latColumn.getLatFormat().toString());
            parse.getSpecialColumns().add(locSpecialColumn);
        }
        else
        {
            addLocationColumn(parse, latColumn);
            addLocationColumn(parse, lonColumn);
        }
    }

    /**
     * Adds a location column.
     *
     * @param parse the csv parameters
     * @param locColumn the location column
     */
    private void addLocationColumn(CSVParseParameters parse, PotentialLocationColumn locColumn)
    {
        SpecialColumn locSpecialColumn = new SpecialColumn();
        locSpecialColumn.setColumnIndex(locColumn.getColumnIndex());
        locSpecialColumn.setColumnType(locColumn.getType());
        if (locColumn.getType().equals(ColumnType.LAT))
        {
            locSpecialColumn.setFormat(locColumn.getLatFormat().toString());
        }
        else if (locColumn.getType().equals(ColumnType.LON))
        {
            locSpecialColumn.setFormat(locColumn.getLonFormat().toString());
        }
        else if (locColumn.getLatFormat() != null)
        {
            locSpecialColumn.setFormat(locColumn.getLatFormat().toString());
        }
        parse.getSpecialColumns().add(locSpecialColumn);
    }

    /**
     * Adds the location results for known location column types except specific
     * latitude and longitude columns.
     *
     * @param parse the csv parameters
     * @param locResults the set of results to check for the different column
     *            types
     */
    private void addLocationResults(CSVParseParameters parse, LocationResults locResults)
    {
        if (locResults.getLocationResults() != null && !locResults.getLocationResults().isEmpty())
        {
            PotentialLocationColumn mgrsCol = locResults.getMostLikelyLocationColumn(ColumnType.MGRS);
            if (mgrsCol != null)
            {
                addLocationColumn(parse, mgrsCol);
            }

            PotentialLocationColumn wktCol = locResults.getMostLikelyLocationColumn(ColumnType.WKT_GEOMETRY);
            if (wktCol != null)
            {
                addLocationColumn(parse, wktCol);
            }

            PotentialLocationColumn posCol = locResults.getMostLikelyLocationColumn(ColumnType.POSITION);
            if (posCol != null)
            {
                addLocationColumn(parse, posCol);
            }
        }
    }

    /**
     * This is a helper method which copies the detected column format
     * information into the parse parameters.
     *
     * @param detected The detected format.
     * @param parse The parse parameters which requires population.
     * @param confidenceThreshold The required confidence of the detected values
     *            in order for those values to be used.
     */
    private void copyDetectedColumnFormat(DetectedParameters detected, CSVParseParameters parse, final double confidenceThreshold)
    {
        ColumnFormatParameters best = detected.getColumnFormatParameter().getBestValue();
        if (best instanceof DelimitedColumnFormatParameters)
        {
            DelimitedColumnFormatParameters bestDelim = (DelimitedColumnFormatParameters)best;
            String tokenDelimiter = String.valueOf(bestDelim.getTokenDelimiter().charValue());
            String textDelimiter = bestDelim.getTextDelimiter() == null ? null
                    : String.valueOf(bestDelim.getTextDelimiter().charValue());
            parse.setFiresUpdates(false);
            parse.setColumnFormat(new CSVDelimitedColumnFormat(tokenDelimiter, textDelimiter, bestDelim.getColumnCount()));
            parse.setFiresUpdates(true);
        }
        else if (detected.getColumnFormatParameter().getBestValue() instanceof FixedWidthColumnFormatParameters)
        {
            FixedWidthColumnFormatParameters bestFixed = (FixedWidthColumnFormatParameters)best;
            parse.setFiresUpdates(false);
            parse.setColumnFormat(new CSVFixedWidthColumnFormat(bestFixed.getColumnDivisions()));
            parse.setFiresUpdates(true);
        }
    }

    /**
     * Copies detected date time parameters to the parse parameters.
     *
     * @param detected the detected parameters
     * @param parse the csv parameters
     * @param confidenceThreshold the confidence threshhold
     */
    private void copyDetectedDateTimeParametersToParseParameters(DetectedParameters detected, CSVParseParameters parse,
            double confidenceThreshold)
    {
        if (detected.getDateColumnParameter() != null
                && detected.getDateColumnParameter().getBestConfidence() > confidenceThreshold)
        {
            DateColumnResults dateColumnResults = detected.getDateColumnParameter().getBestValue();
            DateColumn upTimeColumn = dateColumnResults.getUpTimeColumn();
            if (upTimeColumn != null)
            {
                addDateColumn(upTimeColumn, parse, false);
            }

            DateColumn downTimeColumn = dateColumnResults.getDownTimeColumn();
            if (downTimeColumn != null)
            {
                addDateColumn(downTimeColumn, parse, true);
            }
        }
    }

    /**
     * Copies the detected line of bearing parameters to the parse parameters.
     *
     * @param detected the detected parameters
     * @param parse the csv parameters
     * @param confidenceThreshold the confidence threshold
     */
    private void copyDetectedLOBParametersToParseParameters(DetectedParameters detected, CSVParseParameters parse,
            double confidenceThreshold)
    {
        if (detected.getLOBParameter() != null && detected.getLOBParameter().getBestConfidence() > confidenceThreshold)
        {
            LobColumnResults lcr = detected.getLOBParameter().getBestValue();
            if (lcr.getLineOfBearingColumn() != null)
            {
                SpecialColumn lobSpecialColumn = new SpecialColumn();
                LineOfBearingColumn lobColumn = lcr.getLineOfBearingColumn();
                lobSpecialColumn.setColumnIndex(lobColumn.getColumnIndex());
                lobSpecialColumn.setColumnType(lobColumn.getType());
                parse.getSpecialColumns().add(lobSpecialColumn);
            }
        }
    }

    /**
     * Copies detected location parameters to the parse parameters.
     *
     * @param detected the detected parameters
     * @param parse the csv parameters
     * @param confidenceThreshold the confidence threshhold
     */
    private void copyDetectedLocationParametersToParseParameters(DetectedParameters detected, CSVParseParameters parse,
            double confidenceThreshold)
    {
        if (detected.getLocationParameter() != null && detected.getLocationParameter().getBestConfidence() >= confidenceThreshold)
        {
            boolean useLocationOnly = false;
            LocationResults locResults = detected.getLocationParameter().getBestValue();
            LatLonColumnResults llColRes = locResults.getMostLikelyLatLonColumnPair();
            /* Make sure that if there is a position result whose confidence is
             * 1 and lat/lon columns whose confidence is less than 1 with
             * unknown formats that they aren't included in the results. */
            if (locResults.getConfidence() == 1.0 && llColRes != null && llColRes.getConfidence() < 1.0)
            {
                PotentialLocationColumn latCol = llColRes.getLatColumn();
                PotentialLocationColumn lonCol = llColRes.getLonColumn();

                if (latCol.getLatFormat() != null && latCol.getLatFormat().equals(CoordFormat.UNKNOWN)
                        && lonCol.getLonFormat() != null && lonCol.getLonFormat().equals(CoordFormat.UNKNOWN))
                {
                    useLocationOnly = true;
                    addLocationResults(parse, locResults);
                }
            }

            if (!useLocationOnly)
            {
                if (llColRes != null)
                {
                    addLatLonColumns(parse, llColRes);
                }

                addLocationResults(parse, locResults);
            }
        }
    }

    /**
     * Copies the detected altitude parameters to the parse parameters.
     *
     * @param detected the detected parameters
     * @param parse the csv parameters
     * @param confidenceThreshold the confidence threshold
     */
    private void copyDetectedAltitudeParametersToParseParameters(DetectedParameters detected, CSVParseParameters parse,
            double confidenceThreshold)
    {
        if (detected.getAltitudeParameter() != null && detected.getAltitudeParameter().getBestConfidence() > confidenceThreshold)
        {
            parse.getSpecialColumns().add(detected.getAltitudeParameter().getBestValue());
        }
    }

    /**
     * Copies the detected color parameters to the parse parameters.
     *
     * @param detected the detected parameters
     * @param parse the CSV parameters
     * @param confidenceThreshold the confidence threshold at which the column
     *            was detected.
     */
    private void copyDetectedColorParametersToParseParameters(DetectedParameters detected, CSVParseParameters parse,
            double confidenceThreshold)
    {
        if (detected.getColorParameter() != null && detected.getColorParameter().getBestConfidence() > confidenceThreshold)
        {
            SpecialColumn colorColumn = detected.getColorParameter().getBestValue();
            colorColumn.setFormat("color");
            parse.getSpecialColumns().add(colorColumn);
        }
    }

    /**
     * Copy the detected parameters into the parse parameters.
     *
     * @param cellSampler The cell sampler used to detect parameters.
     * @param detected The detected parameters.
     * @param parse The parse parameters that will be used to actually parse the
     *            file.
     */
    private void copyDetectedParametersToParseParameters(CellSampler cellSampler, DetectedParameters detected,
            CSVParseParameters parse)
    {
        if (detected.getCommentParameter().getBestConfidence() > ourConfidenceThreshold)
        {
            parse.setFiresUpdates(false);
            parse.setCommentIndicator(detected.getCommentParameter().getBestValue() == null ? null
                    : detected.getCommentParameter().getBestValue().toString());
            parse.setFiresUpdates(true);
        }

        // The header line parameter may have been set to null if a user
        // de-selects the header checkbox.
        if (detected.getHeaderLineParameter().getBestConfidence() > ourConfidenceThreshold)
        {
            List<? extends String> headerCells = cellSampler.getHeaderCells();
            parse.setFiresUpdates(false);
            parse.setHeaderLine(detected.getHeaderLineParameter().getBestValue());
            parse.setColumnNames(headerCells);

            ImportableColumnController importable = new ImportableColumnController();
            importable.detectNonImportableColumns(parse, myToolbox.getPreferencesRegistry());

            parse.setFiresUpdates(true);
        }
        else if (parse.getHeaderLine() == null || parse.getHeaderLine().intValue() < 0
                && detected.getHeaderLineParameter().getBestConfidence() < ourConfidenceThreshold)
        {
            List<? extends List<? extends String>> sampleCells = cellSampler.getBeginningSampleCells();
            if (CollectionUtilities.hasContent(sampleCells))
            {
                int index = 0;
                if (detected.getDataLinesParameter().getBestConfidence() > ourConfidenceThreshold)
                {
                    index = detected.getDataLinesParameter().getBestValue().getMin().intValue();
                }

                List<String> headerCells = null;
                if (index > -1 && index < sampleCells.size() && CollectionUtilities.hasContent(sampleCells.get(index)))
                {
                    int numberOfColumns = sampleCells.get(index).size();
                    headerCells = CsvUiUtilities.generateDefaultColumnIdentifiers(numberOfColumns);
                }
                parse.setFiresUpdates(false);
                parse.setColumnNames(headerCells);
                parse.setFiresUpdates(true);
            }
        }

        if (detected.getDataLinesParameter().getBestConfidence() > ourConfidenceThreshold)
        {
            parse.setFiresUpdates(false);
            parse.setDataStartLine(Integer.valueOf(
                    cellSampler.sampleLineToAbsoluteLine(detected.getDataLinesParameter().getBestValue().getMin().intValue())));
            parse.setFiresUpdates(true);
        }
        else if (parse.getHeaderLine() != null
                && (parse.getDataStartLine() == null || parse.getDataStartLine().intValue() <= parse.getHeaderLine().intValue()))
        {
            parse.setFiresUpdates(false);
            parse.setDataStartLine(Integer.valueOf(parse.getHeaderLine().intValue() + 1));
            parse.setFiresUpdates(true);
        }

        verifyColumns(parse, cellSampler);
        copyDetectedDateTimeParametersToParseParameters(detected, parse, ourConfidenceThreshold);
        copyDetectedLocationParametersToParseParameters(detected, parse, ourConfidenceThreshold);
        copyDetectedAltitudeParametersToParseParameters(detected, parse, ourConfidenceThreshold);
        copyDetectedLOBParametersToParseParameters(detected, parse, ourConfidenceThreshold);
        copyDetectedColorParametersToParseParameters(detected, parse, ourConfidenceThreshold);
        copyDetectedColumnFormat(detected, parse, ourConfidenceThreshold);
        parse.getSpecialColumns().addAll(detected.getOtherColumns());
    }

    /**
     * Makes sure that if there are more columns in the data than header names
     * that header names are added until the two match. This could happen when
     * the last character in a row is the delimiter.
     *
     * @param parse the parse
     * @param cellSampler the cell sampler
     */
    private void verifyColumns(CSVParseParameters parse, CellSampler cellSampler)
    {
        if (parse.getHeaderLine() != null && parse.getHeaderLine().intValue() > -1 && parse.getColumnNames() != null
                && !parse.getColumnNames().isEmpty())
        {
            int numCols = parse.getColumnNames().size();

            if (!cellSampler.getBeginningSampleCells().isEmpty())
            {
                int dataCols = cellSampler.getBeginningSampleCells().get(0).size();

                if (dataCols > numCols)
                {
                    int colsToAdd = dataCols - numCols;
                    List<String> colNames = New.list(parse.getColumnNames());
                    for (int i = 0; i < colsToAdd; i++)
                    {
                        colNames.add("UNKNOWN" + Integer.toString(i + 1));
                    }
                    parse.setFiresUpdates(false);
                    parse.setColumnNames(colNames);
                    parse.setFiresUpdates(true);
                }
            }
        }
    }

    /**
     * Initializes the layer settings.
     *
     * @param layerSettings the layer settings.
     * @param detected The detected parameters.
     * @param selectedParameters The parameters for parsing the file.
     */
    private void initializeLayerSettings(LayerSettings layerSettings, DetectedParameters detected,
            CSVParseParameters selectedParameters)
    {
        if (!layerSettings.isFieldSetByUser(LayerSettings.LOADS_TO))
        {
            layerSettings
                    .setLoadsTo(selectedParameters.hasCategory(ColumnType.Category.TEMPORAL) ? LoadsTo.TIMELINE : LoadsTo.STATIC);
        }
    }

    @Override
    public void update(Observable o, final Object arg)
    {
        if (o instanceof CSVParseParameters)
        {
            CSVParseParameters originalParams = (CSVParseParameters)o;
            originalParams.deleteObserver(this);

            final CSVParseParameters params = originalParams.clone();
            params.addObserver(this);
            params.getSpecialColumns().clear();
            myDataSource.setParseParameters(params);

            final ProgressDialog progressDialog = new ProgressDialog(myWizardWindow, "Import CSV File", false, true, false, null,
                    0);
            progressDialog.setVisible(true, 100);

            Callable<DetectedParameters> task = () ->
            {
                progressDialog.setMessage("Detecting CSV parameters");
                return setupParams(new File(myDataSource.getSourceUri()), true, arg.toString());
            };
            Consumer<? super DetectedParameters> resultConsumer = detected ->
            {
                try
                {
                    myCSVWizardPanelModel.updateModels(myDataSource.getParseParameters(), myDataSource.getLayerSettings(),
                            detected, myCellSampler);
                }
                finally
                {
                    progressDialog.dispose();
                }
            };
            Consumer<? super Exception> errorHandler = e ->
            {
                LOGGER.error(e, e);
                progressDialog.dispose();
            };
            EventQueueUtilities.runInBackgroundAndReturnResult(null, task, resultConsumer, errorHandler);
        }
    }
}
