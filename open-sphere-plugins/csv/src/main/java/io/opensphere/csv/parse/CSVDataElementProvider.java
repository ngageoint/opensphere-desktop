package io.opensphere.csv.parse;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.csv.config.v2.CSVDataSource;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.parse.CsvProviderBase;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.util.InputStreamMonitorTaskActivity;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.MonitorInputStream;

/** The Class CSVDataElementProvider. */
@SuppressWarnings("PMD.GodClass")
public class CSVDataElementProvider extends CsvProviderBase
{
    /** The Constant ourLogger. */
    private static final Logger LOGGER = Logger.getLogger(CSVDataElementProvider.class);

    /** The my file source. */
    private final CSVDataSource myFileSource;

    /**
     * Instantiates a new cSV data element provider.
     *
     * @param tb the tb
     * @param dti the dti
     * @param configSaver saves the config
     * @param source the source
     * @param useDeterminedDataTypes the use determined data types
     * @param useDynamicEnumerations the use dynamic enumerations
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CSVDataElementProvider(Toolbox tb, DefaultDataTypeInfo dti, Runnable configSaver, CSVDataSource source,
            boolean useDeterminedDataTypes, boolean useDynamicEnumerations)
        throws FileNotFoundException, IOException
    {
        geomFact = new CSVMapLocationGeoemtrySupportFactory();
        myToolbox = tb;
        myFileSource = source;
        myConfigSaver = configSaver;
        myErrorMessages = New.linkedList();
        myWarningMessages = New.linkedList();
        myUseDeterminedDataTypes = useDeterminedDataTypes;
        myUseDynamicEnumerations = useDynamicEnumerations;
        myDynamicEnumerationRegistry = MantleToolboxUtils.getMantleToolbox(tb).getDynamicEnumerationRegistry();
        File aFile = new File(myFileSource.getFileLocalPath(tb));
        myTypeInfo = dti;
        mySpecialColumnMap = io.opensphere.csvcommon.common.Utilities
                .createSpecialColumnMap(getParseParams().getSpecialColumns());
        setupExtraction(myTypeInfo.getTypeKey());

        myTaskActivity = new InputStreamMonitorTaskActivity("Loading CSV File", "Loading CSV File");
        LOGGER.info("Reading CSV file: " + aFile);
        myReader = createCSVLineReader(myFileSource.getParseParameters(), new InputStreamReader(
                new MonitorInputStream(new FileInputStream(aFile), myTaskActivity), StringUtilities.DEFAULT_CHARSET));

        tb.getUIRegistry().getMenuBarRegistry().addTaskActivity(myTaskActivity);
        if (myReader.ready())
        {
            while (myLineIndex < myFirstDataRowNum)
            {
                myCurrLine = myReader.readLine();
                myLineIndex++;
            }
        }

        myNextElementToReturn = getNextDataElement();
    }

    @Override
    protected URI getSourceUri()
    {
        return myFileSource.getSourceUri();
    }

    @Override
    protected Color getLayerColor()
    {
        return myTypeInfo.getBasicVisualizationInfo().getTypeColor();
    }

    @Override
    protected CSVParseParameters getParseParams()
    {
        return myFileSource.getParseParameters();
    }

    @Override
    protected Set<String> getColumnFilter()
    {
        return myFileSource.getColumnFilter();
    }

    /**
     * Some applications may choose to suppress warnings selectively.
     * @param parts the parts
     * @return true if and only if a warning is allowed
     */
    @Override
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
    @Override
    protected void applyLabels(String dtiKey, MetaDataProvider metaDataProvider)
    {
        // nothing
    }
}
