package io.opensphere.shapefile;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.LoadEndDispositionEvent;
import io.opensphere.mantle.datasources.impl.AbstractDataSourceController;
import io.opensphere.mantle.datasources.impl.AbstractDataSourceHandler;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * The Class ShapeFileHandler.
 */
public class ShapeFileHandler extends AbstractDataSourceHandler
{
    /** The LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileHandler.class);

    /** The Master group lock. */
    private final ReentrantLock myLoadUnloadLock = new ReentrantLock();

    /**
     * Instantiates a new shape file handler.
     *
     * @param controller the controller
     */
    public ShapeFileHandler(AbstractDataSourceController controller)
    {
        super(controller);
    }

    @Override
    public boolean addDataSource(IDataSource pSource)
    {
        ShapeFileSource source = (ShapeFileSource)pSource;
        source.setBusy(true, this);
        final ShapeFileSource sourceToLoad = source;
        LoadEndDispositionEvent loadEvent = null;
        String error = null;
        Exception exception = null;
        try
        {
            loadSource(sourceToLoad);
        }
        catch (FileNotFoundException e)
        {
            error = "Shape File File Not Found: " + sourceToLoad.getPath();
            exception = e;
        }
        catch (IOException e)
        {
            error = "Shape File File Could Not Be Read: " + sourceToLoad.getPath();
            exception = e;
        }
        catch (RuntimeException e)
        {
            error = "Exception Loading Shape File File: " + sourceToLoad.getPath();
            exception = e;
        }
        catch (InterruptedException e)
        {
            error = "Interrupted While Loading Shape File File: " + sourceToLoad.getPath();
            exception = e;
        }
        finally
        {
            try
            {
                if (error != null)
                {
                    displayUserToastMessage(LOGGER, Type.ERROR, error, exception, true);
                    // Administratively disable this source because
                    // we can't find its associated shape file.
                    sourceToLoad.setEnabled(false);
                    loadEvent = new LoadEndDispositionEvent(true, sourceToLoad, getController(), exception,
                            sourceToLoad.getLoadsTo().isTimelineEnabled());
                }
                sourceToLoad.setLoadError(error != null, this);

                if (loadEvent == null)
                {
                    sourceToLoad.setParticipating(true);
                    loadEvent = new LoadEndDispositionEvent(true, true, sourceToLoad, getController(),
                            sourceToLoad.getLoadsTo().isTimelineEnabled());
                }
            }
            finally
            {
                sendLoadEndMessages(loadEvent);
            }
        }
        return loadEvent.wasSuccessful();
    }

    /**
     * Load source.
     *
     * @param source the shape file source
     * @return the data type info
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException if the load is interupted by the user
     */
    public DataTypeInfo loadSource(ShapeFileSource source) throws FileNotFoundException, IOException, InterruptedException
    {
        DataTypeInfo loadResult = null;
        ShapeFileLoader loader = new ShapeFileLoader();
        LoadResultSet result = loader.loadFile(getController().getToolbox(), source);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Shape File Load Results: " + result.getDataElements().size());
        }

        if (result.getDataElements().size() > 0)
        {
            loadResult = result.getDataTypeInfo();

            String sourceStr = ShapeFileEnvoy.class.getSimpleName() + ':' + source.getShapeFileAbsolutePath();
            String category = "SHP/" + source.getName();
            getMantleToolbox().getDataAnalysisReporter().setAnalyzeStringsOnly(result.getDataTypeInfo().getTypeKey(), false);
            myLoadUnloadLock.lock();
            try
            {
                getMantleToolbox().getDataTypeController().addDataType(sourceStr, category, result.getDataTypeInfo(), this);
                getMantleToolbox().getDataTypeController().addMapDataElements(result.getDataTypeInfo(),
                        result.getBoundingGeometry(), result.getOverallTimespan(), result.getDataElements(), this);
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
        return loadResult;
    }

    @Override
    public void removeDataSource(IDataSource pSource)
    {
        final ShapeFileSource src = (ShapeFileSource)pSource;
        src.setBusy(true, this);
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                LoadEndDispositionEvent loadEvent = null;
                try
                {
                    unloadSource(src);
                }
                catch (RuntimeException e)
                {
                    // Administratively disable this source because
                    // we can't find its associated shape file.
                    src.setActive(false);
                    loadEvent = new LoadEndDispositionEvent(true, src, getController(), e, src.getLoadsTo().isTimelineEnabled());
                    displayUserToastMessage(LOGGER, Type.ERROR,
                            "A problem was encountered while trying to remove the requested ShapeFile file:\n" + src.getPath(),
                            null, true);
                }
                finally
                {
                    src.setParticipating(false);

                    loadEvent = new LoadEndDispositionEvent(true, false, src, getController(),
                            src.getLoadsTo().isTimelineEnabled());

                    sendLoadEndMessages(loadEvent);
                }
            }
        };
        getExecutorService().execute(r);
    }

    /**
     * Unload source.
     *
     * @param source the shape file source
     */
    public void unloadSource(ShapeFileSource source)
    {
        if (source != null && source.getDataTypeInfo() != null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("ULOAD Source: " + source.getPath());
            }
            myLoadUnloadLock.lock();
            try
            {
                getMantleToolbox().getDataTypeController().removeDataType(source.getDataTypeInfo(), this);
            }
            finally
            {
                myLoadUnloadLock.unlock();
            }
        }
    }

    /**
     * Get the data registry key for a DTI.
     *
     * @param type The DTI.
     * @return The data registry key.
     */
    protected String getDataTypeInfoKey(DataTypeInfo type)
    {
        return type.getTypeName() + ":" + type.getTypeKey();
    }

    /**
     * Gets the mantle toolbox.
     *
     * @return the mantle toolbox
     */
    private MantleToolbox getMantleToolbox()
    {
        return MantleToolboxUtils.getMantleToolbox(getController().getToolbox());
    }
}
