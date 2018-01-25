package io.opensphere.csv;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.csv.config.v2.CSVDataSource;
import io.opensphere.csv.config.v2.CSVDataSources;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoOrderManager;
import io.opensphere.mantle.data.DefaultDataTypeInfoOrderManager;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceCreator;
import io.opensphere.mantle.datasources.impl.AbstractDataSourceController;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class CSVFileDataSourceController.
 */
@SuppressWarnings("PMD.GodClass")
public class CSVFileDataSourceController extends AbstractDataSourceController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CSVFileDataSourceController.class);

    /** The configuration manager. */
    private final CSVConfigurationManager myConfigurationManager;

    /** The config. */
    private final CSVDataSources myConfig;

    /** Control concurrent access to the configuration. */
    private final Lock myConfigLock = new ReentrantLock();

    /** The my csv data group info assistant. */
    private final CSVDataGroupInfoAssistant myCSVDataGroupInfoAssistant;

    /** The csv file handler. */
    private final CSVFileHandler myCSVFileHandler;

    /** The file importer. */
    private final CSVFileImporter myFileImporter;

    /** The base data group info. */
    private final DefaultDataGroupInfo myMasterGroup;

    /** Listener for changes to the order of CSV data sources. */
    private final DataTypeInfoOrderManager myOrderManager;

    /** The source type. */
    private final DataSourceType mySourceType = DataSourceType.FILE;

    /** The activation listener. */
    private final ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public void handleDeactivating(DataGroupInfo dgi)
        {
            if (dgi.hasMembers(false))
            {
                for (DataTypeInfo dti : dgi.getMembers(false))
                {
                    if (dti instanceof CSVDataTypeInfo)
                    {
                        CSVDataSource fileSource = ((CSVDataTypeInfo)dti).getFileSource();
                        LOGGER.info("Deactivate  " + dgi.getId());
                        if (fileSource.isParticipating())
                        {
                            myCSVFileHandler.removeDataSource(fileSource);
                        }
                        else
                        {
                            // Set us to not busy in this case since
                            // we will have been set to busy before.
                            fileSource.setBusy(false, this);
                        }
                    }
                }
            }
        }

        @Override
        public boolean handleActivating(DataGroupInfo dgi, io.opensphere.core.util.lang.PhasedTaskCanceller canceller)
            throws io.opensphere.mantle.data.DataGroupActivationException, InterruptedException
        {
            if (dgi.hasMembers(false))
            {
                for (DataTypeInfo dti : dgi.getMembers(false))
                {
                    if (dti instanceof CSVDataTypeInfo)
                    {
                        CSVDataSource fileSource = ((CSVDataTypeInfo)dti).getFileSource();
                        LOGGER.info("Activate  " + dgi.getId());
                        if (fileSource.isParticipating())
                        {
                            if (!myCSVFileHandler.updateDataSource(fileSource))
                            {
                                return false;
                            }
                        }
                        else
                        {
                            if (!myCSVFileHandler.addDataSource(fileSource))
                            {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
    };

    /**
     * Instantiates a new cSV file data source controller.
     *
     * @param tb the tb
     * @param pluginProperties the plugin properties
     */
    public CSVFileDataSourceController(Toolbox tb, Properties pluginProperties)
    {
        super(tb);
        myMasterGroup = new DefaultDataGroupInfo(true, tb, "CSV", CSVEnvoy.class.getName(), "CSV Files");
        myFileImporter = new CSVFileImporter(this);
        myCSVDataGroupInfoAssistant = new CSVDataGroupInfoAssistant(this);
        boolean useDeterminedDataTypes = pluginProperties.getProperty("useDeterminedDataTypes", "false").equalsIgnoreCase("true");
        boolean useDynamicEnumerations = pluginProperties.getProperty("useDynamicEnumerations", "false").equalsIgnoreCase("true");
        myCSVFileHandler = new CSVFileHandler(this, useDeterminedDataTypes, useDynamicEnumerations);
        myConfigurationManager = new CSVConfigurationManager(tb.getPreferencesRegistry().getPreferences(CSVPlugin.class));
        myConfig = myConfigurationManager.getConfig();
        tb.getImporterRegistry().addImporter(myFileImporter);

        myOrderManager = new DefaultDataTypeInfoOrderManager(getToolbox().getOrderManagerRegistry());
        myOrderManager.open();
    }

    @Override
    public void activateSource(IDataSource source)
    {
        CSVDataSource aSource = (CSVDataSource)source;
        if (aSource.isParticipating())
        {
            myCSVFileHandler.updateDataSource(source);
        }
        else
        {
            myCSVFileHandler.addDataSource(source);
        }
    }

    @Override
    public void addSource(IDataSource source)
    {
        CSVDataSource csvSource = (CSVDataSource)source;

        myConfigLock.lock();
        try
        {
            // Update and save the config
            myConfig.addSource(csvSource);
            saveConfigInternal();

            DataTypeInfo dti = CSVTypeInfoGenerator.generateTypeInfo(getToolbox(), csvSource, true, true);
            csvSource.setDataTypeInfo(dti);
            dti.setVisible(csvSource.isVisible(), this);
            String category = csvSource.getName();
            DefaultDataGroupInfo dgi = new DefaultDataGroupInfo(false, getToolbox(), "CSV", dti.getTypeKey(), category);

            myOrderManager.activateParticipant(dti);

            csvSource.setDataGroupInfo(dgi);
            dgi.setAssistant(myCSVDataGroupInfoAssistant);
            dgi.activationProperty().addListener(myActivationListener);
            dgi.addMember(dti, DataGroupInfo.NO_EVENT_SOURCE);
            myMasterGroup.addChild(dgi, this);

            if (csvSource.isActive())
            {
                csvSource.getDataGroupInfo().activationProperty().setActive(true);
            }
        }
        finally
        {
            myConfigLock.unlock();
        }
    }

    /** Perform any required cleanup before being discarded. */
    public void close()
    {
        myCSVFileHandler.close();
        myOrderManager.close();
    }

    @Override
    public void createSource(Container parent, DataSourceType type, List<File> chosenFiles, Set<IDataSource> sourcesInUse,
            IDataSourceCreator caller)
    {
        throw new AssertionError("createSource was called!");
    }

    @Override
    public void deactivateSource(IDataSource source)
    {
        CSVDataSource aSource = (CSVDataSource)source;
        if (aSource.isParticipating())
        {
            myCSVFileHandler.removeDataSource(source);
        }
        else
        {
            // Set us to not busy in this case since
            // we will have been set to busy before.
            aSource.setBusy(false, aSource);
        }
    }

    /**
     * File importer.
     *
     * @return the cSV file importer
     */
    public CSVFileImporter getFileImporter()
    {
        return myFileImporter;
    }

    @Override
    public List<Class<? extends IDataSource>> getSourceClasses()
    {
        return Collections.<Class<? extends IDataSource>>singletonList(CSVDataSource.class);
    }

    /**
     * Gets the sources as CSV sources.
     *
     * @return the CSV sources (already)
     */
    public List<CSVDataSource> getCsvSourcesAlready()
    {
        myConfigLock.lock();
        try
        {
            return myConfig.getCSVSourceList();
        }
        finally
        {
            myConfigLock.unlock();
        }
    }

    @Override
    public List<IDataSource> getSourceList()
    {
        myConfigLock.lock();
        try
        {
            return myConfig.getSourceList();
        }
        finally
        {
            myConfigLock.unlock();
        }
    }

    @Override
    public DataSourceType getSourceType()
    {
        return mySourceType;
    }

    @Override
    public String[] getTypeExtensions()
    {
        return new String[] { "csv", "CSV", "TXT", "txt" };
    }

    @Override
    public String getTypeName()
    {
        return "CSV";
    }

    @Override
    public void initialize()
    {
        myConfigLock.lock();
        try
        {
            MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController().addRootDataGroupInfo(myMasterGroup, this);
            for (CSVDataSource source : myConfig.getCSVSourceList())
            {
                DataTypeInfo dti = CSVTypeInfoGenerator.generateTypeInfo(getToolbox(), source, true, true);
                source.setDataTypeInfo(dti);
                String category = source.getName();
                DefaultDataGroupInfo dgi = new DefaultDataGroupInfo(false, getToolbox(), "CSV", dti.getTypeKey(), category);

                myOrderManager.activateParticipant(dti);

                source.setDataGroupInfo(dgi);
                dgi.setAssistant(myCSVDataGroupInfoAssistant);
                dgi.activationProperty().addListener(myActivationListener);
                dgi.addMember(dti, this);
                myMasterGroup.addChild(dgi, this);
            }
        }
        finally
        {
            myConfigLock.unlock();
        }
    }

    @Override
    public boolean removeSource(IDataSource source, boolean cleanup, Component parent)
    {
        myConfigLock.lock();
        try
        {
            boolean removed = myConfig.removeSource(source);
            saveConfigInternal();
            if (source.isActive())
            {
                deactivateSource(source);
            }

            if (cleanup)
            {
                CSVDataSource csvSource = (CSVDataSource)source;
                String typeKey = csvSource.generateTypeKey();
                DefaultMetaDataInfo.clearPreferencesRegistryEntryForNumericCache(getToolbox(), typeKey, this);
                MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataTypeInfoPreferenceAssistant().removePreferences(typeKey);

                DataGroupInfo dgi = csvSource.getDataGroupInfo();
                if (dgi != null)
                {
                    for (DataTypeInfo dataTypeInfo : dgi.getMembers(false))
                    {
                        myOrderManager.deactivateParticipant(dataTypeInfo);
                    }
                    myMasterGroup.removeChild(dgi, this);
                    MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController().cleanUpGroup(dgi);
                }
            }

            return removed;
        }
        finally
        {
            myConfigLock.unlock();
        }
    }

    @Override
    public void setExecutorService(ExecutorService execService)
    {
        myCSVFileHandler.setExecutorService(execService);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getTypeName()).append(" File [ ");
        for (String str : getTypeExtensions())
        {
            sb.append("*.").append(str).append(", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" ]");
        return sb.toString();
    }

    @Override
    public void updateSource(IDataSource source)
    {
        myConfigLock.lock();
        try
        {
            myConfig.updateSource(source);
            saveConfigInternal();
        }
        finally
        {
            myConfigLock.unlock();
        }
    }

    /**
     * Determines whether the controller has the given source. The source
     * doesn't have to match 100%, just have the same name.
     *
     * @param source the data source
     * @return whether the controller has the data source
     */
    public boolean hasSource(final CSVDataSource source)
    {
        return myConfig.getCSVSourceList().stream().anyMatch(dataSource -> dataSource.getName().equals(source.getName()));
    }

    @Override
    protected void handleLoadsToChanged(DataTypeInfoLoadsToChangeEvent event)
    {
        if (event.getDataTypeInfo() instanceof CSVDataTypeInfo)
        {
            CSVDataSource fileSource = ((CSVDataTypeInfo)event.getDataTypeInfo()).getFileSource();
            fileSource.getLayerSettings().setLoadsTo(event.getLoadsTo());
            updateSource(fileSource);
        }
    }

    /**
     * Save config state.
     */
    protected final void saveConfigState()
    {
        myConfigLock.lock();
        try
        {
            saveConfigInternal();
        }
        finally
        {
            myConfigLock.unlock();
        }
    }

    /**
     * Save config state (without synchronization).
     */
    private void saveConfigInternal()
    {
        myConfigurationManager.saveConfig(myConfig.clone());
    }
}
