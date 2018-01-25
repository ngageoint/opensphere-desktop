package io.opensphere.csv;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.csv.config.v2.CSVDataSource;
import io.opensphere.csv.parse.CSVDataElementProvider;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoOrderManager;
import io.opensphere.mantle.data.DefaultDataTypeInfoOrderManager;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.LoadEndDispositionEvent;
import io.opensphere.mantle.datasources.impl.AbstractDataSourceController;
import io.opensphere.mantle.datasources.impl.AbstractDataSourceHandler;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class CSVFileHandler.
 */
public class CSVFileHandler extends AbstractDataSourceHandler
{
    /** The Constant ourLogger. */
    private static final Logger LOGGER = Logger.getLogger(CSVFileHandler.class);

    /** The Master group lock. */
    private final ReentrantLock myLoadUnloadLock;

    /** Manager which determines the z-order of the CSV sources. */
    private final DataTypeInfoOrderManager myOrderManager;

    /** The my use determined data types. */
    private final boolean myUseDeterminedDataTypes;

    /** The my use dynamic enumerations. */
    private final boolean myUseDynamicEnumerations;

    /**
     * Instantiates a new cSV file handler.
     *
     * @param controller the controller
     * @param useDeterminedDataTypes the use determined data types
     * @param useDynamicEnumerations the use dynamic enumerations
     */
    public CSVFileHandler(AbstractDataSourceController controller, boolean useDeterminedDataTypes, boolean useDynamicEnumerations)
    {
        super(controller);
        myUseDeterminedDataTypes = useDeterminedDataTypes;
        myUseDynamicEnumerations = useDynamicEnumerations;
        myLoadUnloadLock = new ReentrantLock();

        myOrderManager = new DefaultDataTypeInfoOrderManager(getController().getToolbox().getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, DefaultOrderCategory.FEATURE_CATEGORY));
        myOrderManager.open();
    }

    @Override
    public boolean addDataSource(IDataSource isource)
    {
        final CSVDataSource sourceToLoad = (CSVDataSource)isource;
        sourceToLoad.setBusy(true, this);

        DataTypeInfo dti = null;

        // Rebuild the DTI to make sure it is up to date with the
        // source.
//                DataTypeInfo oldDTI = sourceToLoad.getDataTypeInfo();

        DataTypeInfo dtiNew = CSVTypeInfoGenerator.generateTypeInfo(getController().getToolbox(), sourceToLoad, true, true);
        CSVDataTypeInfo cDti = (CSVDataTypeInfo)sourceToLoad.getDataTypeInfo();
        cDti.setMetaDataInfo(dtiNew.getMetaDataInfo());

        LoadEndDispositionEvent loadEvent = null;
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Do Load Here");
            }

            // This dti will be the one in sourceToLoad if there is
            // anything to load. It will be null otherwise.
            dti = loadSource(sourceToLoad, (CSVFileDataSourceController)getController());

            if (dti != null)
            {
                myOrderManager.activateParticipant(dti);
            }
        }
        catch (FileNotFoundException e)
        {
            displayUserToastMessage(LOGGER, Type.ERROR, "CSV File Not Found: " + sourceToLoad.getSourceUri(), e, true);
            // Administratively disable this source because
            // we can't find its associated shape file.
            sourceToLoad.setActive(false);
            loadEvent = new LoadEndDispositionEvent(true, sourceToLoad, getController(), e,
                    sourceToLoad.getLayerSettings().getLoadsTo().isTimelineEnabled());
        }
        catch (IOException e)
        {
            displayUserToastMessage(LOGGER, Type.ERROR,
                    "An error occurred while trying to read the CSV file: " + sourceToLoad.getSourceUri(), e, true);
            // Administratively disable this source because
            // we can't find its associated shape file.
            sourceToLoad.setActive(false);
            loadEvent = new LoadEndDispositionEvent(true, sourceToLoad, getController(), e,
                    sourceToLoad.getLayerSettings().getLoadsTo().isTimelineEnabled());
        }
        catch (RuntimeException e)
        {
            displayUserToastMessage(LOGGER, Type.ERROR,
                    "The following exception occurred while loading the CSV file: " + sourceToLoad.getSourceUri(), e, true);
            loadEvent = new LoadEndDispositionEvent(true, sourceToLoad, getController(), e,
                    sourceToLoad.getLayerSettings().getLoadsTo().isTimelineEnabled());
        }
        finally
        {
            try
            {
                sourceToLoad.setLoadError(loadEvent != null, this);

                if (loadEvent == null)
                {
                    sourceToLoad.setParticipating(true);
                    loadEvent = new LoadEndDispositionEvent(true, true, sourceToLoad, getController(),
                            sourceToLoad.getLayerSettings().getLoadsTo().isTimelineEnabled());
                }
            }
            finally
            {
                sendLoadEndMessages(loadEvent);
            }
        }

        return loadEvent.wasSuccessful();
    }

    /** Perform any required cleanup before being discarded. */
    public void close()
    {
        myOrderManager.close();
    }

    @Override
    public void removeDataSource(IDataSource source)
    {
        final CSVDataSource src = (CSVDataSource)source;
        src.setBusy(true, this);
        LoadEndDispositionEvent loadEvent = null;
        try
        {
            unloadSource(src);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Unload here");
            }
        }
        catch (RuntimeException e)
        {
            src.setActive(false);
            loadEvent = new LoadEndDispositionEvent(true, src, getController(), e,
                    src.getLayerSettings().getLoadsTo().isTimelineEnabled());
            displayUserToastMessage(LOGGER, Type.ERROR,
                    "A problem was encountered while trying to remove the requested CSV file:\n" + src.getSourceUri(), e, true);
        }
        finally
        {
            src.setParticipating(false);
            loadEvent = new LoadEndDispositionEvent(true, false, src, getController(),
                    src.getLayerSettings().getLoadsTo().isTimelineEnabled());

            DataTypeInfo dti = src.getDataTypeInfo();
            try
            {
                if (dti != null)
                {
                    myOrderManager.deactivateParticipant(dti);
                }
            }
            finally
            {
                sendLoadEndMessages(loadEvent);
            }
        }
    }

    @Override
    public boolean updateDataSource(final IDataSource pSource)
    {
        removeDataSource(pSource);
        return addDataSource(pSource);
    }

    /**
     * Load source.
     *
     * @param csvFileSource the csv file source
     * @param controller the controller
     * @return the data type info
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private DataTypeInfo loadSource(CSVDataSource csvFileSource, CSVFileDataSourceController controller)
        throws FileNotFoundException, IOException
    {
        DataTypeInfo loadResult = null;

        CSVDataElementProvider dep = new CSVDataElementProvider(getController().getToolbox(),
                (DefaultDataTypeInfo)csvFileSource.getDataTypeInfo(), () -> controller.saveConfigState(), csvFileSource,
                myUseDeterminedDataTypes, myUseDynamicEnumerations);
        if (dep.hasNext())
        {
            // This is the same dti as the one in csvFileSource
            loadResult = dep.getDataTypeInfo();
            String source = CSVEnvoy.class.getSimpleName() + ':' + csvFileSource.getSourceUri();
            String category = "CSV/" + csvFileSource.getName();

            myLoadUnloadLock.lock();
            try
            {
                MantleToolboxUtils.getMantleToolbox(getController().getToolbox()).getDataTypeController().addDataType(source,
                        category, loadResult, this);
                MantleToolboxUtils.getMantleToolbox(getController().getToolbox()).getDataTypeController().addDataElements(dep,
                        null, null, this);
                // Ensure that the data elements get the type color
                Color typeColor = loadResult.getBasicVisualizationInfo().getTypeColor();
                if (typeColor != null && typeColor != loadResult.getBasicVisualizationInfo().getDefaultTypeColor())
                {
                    loadResult.fireChangeEvent(new DataTypeInfoColorChangeEvent(loadResult,
                            loadResult.getBasicVisualizationInfo().getTypeColor(), false, source));
                }
            }
            finally
            {
                myLoadUnloadLock.unlock();
            }
        }
        if (dep.hadError())
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Errors encountered loading CSV File ").append(csvFileSource.getSourceUri());
            if (dep.getErrorMessages() != null && !dep.getErrorMessages().isEmpty())
            {
                sb.append("\nMessage:\n  ");
                for (String msg : dep.getErrorMessages())
                {
                    sb.append("  ").append(msg);
                    if (!msg.endsWith("\n"))
                    {
                        sb.append('\n');
                    }
                }
            }
            StringBuilder errors = new StringBuilder();
            errors.append("The following error(s) were encountered while loading the CSV file, ");
            errors.append(csvFileSource.getSourceUri());
            errors.append('\n');
            errors.append(sb.toString());
            displayUserToastMessage(LOGGER, Type.ERROR, errors.toString(), null, true);
            throw new IOException(sb.toString());
        }
        return loadResult;
    }

    /**
     * Unload source.
     *
     * @param source the csv file source
     */
    private void unloadSource(CSVDataSource source)
    {
        if (source != null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("UNLOAD Source: " + source.getSourceUri());
            }
            myLoadUnloadLock.lock();
            try
            {
                if (source.getDataTypeInfo() != null)
                {
                    MantleToolboxUtils.getMantleToolbox(getController().getToolbox()).getDataTypeController()
                            .removeDataType(source.getDataTypeInfo(), this);
                }
            }
            finally
            {
                myLoadUnloadLock.unlock();
            }
        }
    }
}
