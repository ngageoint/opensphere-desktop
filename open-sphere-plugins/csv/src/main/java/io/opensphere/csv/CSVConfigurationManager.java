package io.opensphere.csv;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.SupplierX;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.csv.config.v2.CSVDataSource;
import io.opensphere.csv.config.v2.CSVDataSources;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;

/** Manages the configuration file. */
public class CSVConfigurationManager
{
    /** Packages containing the config classes. */
    private static final Package[] CSV_DATA_PACKAGES =
        {CSVDataSources.class.getPackage(), CSVParseParameters.class.getPackage()};

    /**
     * Supplier for the JAXB context for marshalling/unmarshalling the
     * preferences.
     */
    private static final SupplierX<JAXBContext, JAXBException> CONTEXT_SUPPLIER = new SupplierX<JAXBContext, JAXBException>()
    {
        @Override
        public JAXBContext get() throws JAXBException
        {
            return JAXBContextHelper.getCachedContext(CSV_DATA_PACKAGES);
        }
    };

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CSVConfigurationManager.class);

    /** Config preferences key (version 2). */
    private static final String PREFERENCES_KEY_V2 = "config.v2";

    /** The CSV preferences. */
    private final Preferences myCsvPreferences;

    /**
     * Gets the data source from the given node and id.
     *
     * @param sourceNode the data source node
     * @return the data source, or null if it couldn't be read
     */
    static CSVDataSource getDataSource(Node sourceNode)
    {
        CSVDataSource dataSource = null;

        String nodeName = sourceNode.getNodeName();

        // New format
        if (CSVDataSource.class.getSimpleName().equals(nodeName))
        {
            try
            {
                dataSource = XMLUtilities.readXMLObject(sourceNode, CONTEXT_SUPPLIER.get(), CSVDataSource.class);
            }
            catch (JAXBException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return dataSource;
    }

    /**
     * Constructor.
     *
     * @param csvPreferences the CSV preferences
     */
    public CSVConfigurationManager(Preferences csvPreferences)
    {
        myCsvPreferences = csvPreferences;
    }

    /**
     * Gets the configuration, migrating if necessary.
     *
     * @return The config object
     */
    public CSVDataSources getConfig()
    {
        CSVDataSources config = myCsvPreferences.getJAXBObject(
                CSVDataSources.class, PREFERENCES_KEY_V2, CONTEXT_SUPPLIER, new CSVDataSources());
        revertTabPlaceholders(config);
        return config;
    }

    /**
     * Saves the given config to the preferences.
     *
     * @param config the config object
     */
    public void saveConfig(CSVDataSources config)
    {
        insertTabPlaceholders(config);
        myCsvPreferences.putJAXBObject(PREFERENCES_KEY_V2, config, false, CONTEXT_SUPPLIER, this);
    }

    /**
     * Inserts the placeholder delimiter "TAB" wherever the token 
     * delimiter is "\t".
     *
     * @param sources collection of data sources to change delimiter
     */
    private void insertTabPlaceholders(CSVDataSources sources)
    {
        sources.getCSVSourceList().forEach(e ->
        {
            CSVDelimitedColumnFormat columnFormat = (CSVDelimitedColumnFormat)e.getParseParameters().getColumnFormat();
            if (columnFormat.getTokenDelimiter().equals("\t"))
            {
                EventQueueUtilities.runOnEDTAndWait(() -> columnFormat.setTokenDelimiter("TAB"));
            }
        });
    }

    /**
     * Reverts the placeholder delimiter "TAB" to the normal
     * delimiter "\t".
     *
     * @param sources collection of data sources to change delimiter
     */
    private void revertTabPlaceholders(CSVDataSources sources)
    {
        sources.getCSVSourceList().forEach(e ->
        {
            CSVDelimitedColumnFormat columnFormat = (CSVDelimitedColumnFormat)e.getParseParameters().getColumnFormat();
            if (columnFormat.getTokenDelimiter().equals("TAB"))
            {
                EventQueueUtilities.runOnEDTAndWait(() -> columnFormat.setTokenDelimiter("\t"));
            }
        });
    }
}
