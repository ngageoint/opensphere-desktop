package io.opensphere.csv.ui.controller;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.QuotingBufferedReader;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.wizard.model.WizardPanelModel;
import io.opensphere.csv.ui.CSVWizardPanel;
import io.opensphere.csv.ui.columndefinition.ui.ColumnDefinitionPanel;
import io.opensphere.csv.ui.format.FormatPanel;
import io.opensphere.csv.ui.summary.SummaryPanel;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.controller.CellSamplerImpl;
import io.opensphere.csvcommon.detect.controller.DetectedParameters;
import io.opensphere.csvcommon.detect.controller.ReaderLineSampler;
import io.opensphere.csvcommon.detect.controller.TokenizerFactoryImpl;
import io.opensphere.importer.config.LayerSettings;

/** The wizard panel model for importing CSV content. */
public class CSVWizardPanelModel implements WizardPanelModel
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CSVWizardPanelModel.class);

    /** The file which is being imported. */
    private final File myCSVFile;

    /** The parameters as detected by the auto-detect methods. */
    private DetectedParameters myDetectedParameters;

    /** The settings for the layer. */
    private LayerSettings myLayerSettings;

    /**
     * The parse parameters which were either detected or set by the user, these
     * will be updated as selections are made in the wizard.
     */
    private CSVParseParameters myParseParameters;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The names in use. */
    private final Set<String> myNamesInUse;

    /** The Cell sampler. */
    private CellSampler myCellSampler;

    /** The Panels. */
    private final Map<String, CSVWizardPanel> myPanels;

    /**
     * Constructor.
     *
     * @param file The file which is being imported.
     * @param toolbox The toolbox.
     * @param parseParameters The parse parameters which were either detected or
     *            set by the user, these will be updated as selections are made
     *            in the wizard.
     * @param detected The parameters as detected by the auto-detect methods.
     * @param layerSettings The settings for the layer.
     * @param namesInUse the names in use
     */
    public CSVWizardPanelModel(File file, Toolbox toolbox, CSVParseParameters parseParameters, DetectedParameters detected,
            LayerSettings layerSettings, Set<String> namesInUse)
    {
        myToolbox = toolbox;
        myParseParameters = parseParameters;
        myDetectedParameters = detected;
        myLayerSettings = layerSettings;
        myCSVFile = file;
        myNamesInUse = namesInUse;
        myPanels = New.map();
    }

    @Override
    public Component getWizardPanel(String stepTitle)
    {
        Reader reader = null;
        QuotingBufferedReader quotingReader = null;
        try
        {
            reader = new InputStreamReader(new FileInputStream(myCSVFile), StringUtilities.DEFAULT_CHARSET);

            StringTokenizer tokenizer = new TokenizerFactoryImpl().getTokenizer(myParseParameters.getColumnFormat(),
                    myDetectedParameters.getColumnFormatParameter().getBestValue());
            int headerLine = NumberUtilities.intValue(myParseParameters.getHeaderLine(), -1);

            char[] quote = null;
            if (myParseParameters.getColumnFormat() instanceof CSVDelimitedColumnFormat && !StringUtils
                    .isEmpty(((CSVDelimitedColumnFormat)myParseParameters.getColumnFormat()).getTextDelimiter()))
            {
                CSVDelimitedColumnFormat format = (CSVDelimitedColumnFormat)myParseParameters.getColumnFormat();
                quote = new char[] { format.getTextDelimiter().charAt(0) };
            }
            quotingReader = new QuotingBufferedReader(reader, quote, null);
            LineSampler lineSampler = new ReaderLineSampler(quotingReader, 100, 20);
            myCellSampler = new CellSamplerImpl(lineSampler, tokenizer, headerLine);

            if (WizardStep.FORMAT.toString().equals(stepTitle))
            {
                if (myPanels.get(WizardStep.FORMAT.toString()) == null)
                {
                    myPanels.put(WizardStep.FORMAT.toString(),
                            new FormatPanel(myCSVFile, myParseParameters, myDetectedParameters, lineSampler));
                }
                else
                {
                    updateAllModels();
                }
                return myPanels.get(WizardStep.FORMAT.toString());
            }
            else if (WizardStep.COLUMNS.toString().equals(stepTitle))
            {
                if (myPanels.get(WizardStep.COLUMNS.toString()) == null)
                {
                    myPanels.put(WizardStep.COLUMNS.toString(), new ColumnDefinitionPanel(myToolbox.getPreferencesRegistry(),
                            myParseParameters, myDetectedParameters, myCellSampler));
                }
                else
                {
                    updateAllModels();
                }
                return myPanels.get(WizardStep.COLUMNS.toString());
            }
            else if (WizardStep.SUMMARY.toString().equals(stepTitle))
            {
                if (myPanels.get(WizardStep.SUMMARY.toString()) == null)
                {
                    myPanels.put(WizardStep.SUMMARY.toString(),
                            new SummaryPanel(myToolbox, myParseParameters, myLayerSettings, myCellSampler, myNamesInUse));
                }
                else
                {
                    updateAllModels();
                }
                return myPanels.get(WizardStep.SUMMARY.toString());
            }
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

        return null;
    }

    /**
     * Update the CSV wizard panel models.
     *
     * @param parse the parse
     * @param layerSettings the layer settings
     * @param detected the detected
     * @param cellSampler the cell sampler
     */
    public void updateModels(CSVParseParameters parse, LayerSettings layerSettings, DetectedParameters detected,
            CellSampler cellSampler)
    {
        myParseParameters = parse;
        myLayerSettings = layerSettings;
        myDetectedParameters = detected;
        myCellSampler = cellSampler;
        updateAllModels();
    }

    /**
     * Updates all CSV wizard panel models.
     */
    private void updateAllModels()
    {
        for (Entry<String, CSVWizardPanel> entry : myPanels.entrySet())
        {
            entry.getValue().updateModel(myParseParameters, myLayerSettings, myDetectedParameters, myCellSampler);
        }
    }

    /** The valid steps for this wizard. */
    public enum WizardStep
    {
        /** Step for setting the special columns. */
        COLUMNS("2)  Columns"),

        /** Step for setting the format information for the file. */
        FORMAT("1)  Format"),

        /** Step for reviewing the settings. */
        SUMMARY("3)  Summary"),

        ;

        /** The display name. */
        private final String myDisplayName;

        /**
         * Constructor.
         *
         * @param displayName the display name
         */
        WizardStep(String displayName)
        {
            myDisplayName = displayName;
        }

        @Override
        public String toString()
        {
            return myDisplayName;
        }
    }
}
