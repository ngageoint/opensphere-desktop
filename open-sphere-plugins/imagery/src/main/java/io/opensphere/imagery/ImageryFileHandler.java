package io.opensphere.imagery;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.imagery.util.ImageryTileLoadTracker;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.LoadEndDispositionEvent;
import io.opensphere.mantle.datasources.impl.AbstractDataSourceController;
import io.opensphere.mantle.datasources.impl.AbstractDataSourceHandler;

/**
 * The Class ImageryFileHandler.
 */
public class ImageryFileHandler extends AbstractDataSourceHandler
{
    /** The Constant ourLogger. */
    private static final Logger LOGGER = Logger.getLogger(ImageryFileHandler.class);

    /** The Group to envoy map. */
    private final Map<String, ImageryEnvoy> myGroupToEnvoyMap;

    /** The Group to envoy map lock. */
    private final ReentrantLock myGroupToEnvoyMapLock;

    /** The Metrics tracker. */
    private final ImageryTileLoadTracker myMetricsTracker;

    /**
     * Instantiates a new advanced image file handler.
     *
     * @param controller the controller
     */
    public ImageryFileHandler(AbstractDataSourceController controller)
    {
        super(controller);
        myGroupToEnvoyMapLock = new ReentrantLock();
        myGroupToEnvoyMap = New.map();
        myMetricsTracker = new ImageryTileLoadTracker(controller.getToolbox());
    }

    @Override
    public boolean addDataSource(IDataSource isource)
    {
        isource.setBusy(true, this);
        final ImagerySourceGroup sourceToLoad = (ImagerySourceGroup)isource;

        ImageryFileSourceController controller = (ImageryFileSourceController)getController();
        DataGroupInfo dgi = sourceToLoad.getDataGroupInfo();
        LoadEndDispositionEvent loadEvent = null;
        try
        {
            sourceToLoad.updateSourceGroupReferences();

            ImageryEnvoy envoy = retrieveOrCreateEnvoy(sourceToLoad);
            sourceToLoad.setImageryEnvoy(envoy);
            envoy.loadSource();
            if (sourceToLoad.isShowBoundaryOnLoad())
            {
                sourceToLoad.showGroupBounds(controller.getToolbox(), true);
            }
        }
        catch (RuntimeException | InterruptedException e)
        {
            displayUserToastMessage(LOGGER, Type.ERROR,
                    "The following exception occurred while loading the image file group: " + sourceToLoad.getName(), e, true);
            // Administratively disable this source because
            // we can't find its associated shape file.
            sourceToLoad.setLoadError(true, this);
            sourceToLoad.setEnabled(false);
            loadEvent = new LoadEndDispositionEvent(true, sourceToLoad, getController(), e, false);
        }
        finally
        {
            sourceToLoad.setLoadError(loadEvent != null, this);

            if (loadEvent == null)
            {
                sourceToLoad.setDataGroupInfo(dgi);
                loadEvent = new LoadEndDispositionEvent(true, true, sourceToLoad, getController(), false);
            }
        }
        return loadEvent.wasSuccessful();
    }

    @Override
    public void removeDataSource(IDataSource isource)
    {
        isource.setBusy(true, this);
        final ImagerySourceGroup src = (ImagerySourceGroup)isource;
        ImageryFileSourceController controller = (ImageryFileSourceController)getController();
        try
        {
            closeEnvoy(src);
        }
        finally
        {
            src.setImageryEnvoy(null);
            src.showGroupBounds(controller.getToolbox(), false);
        }
    }

    /**
     * Close envoy.
     *
     * @param sourceToClose the source to close
     */
    private void closeEnvoy(ImagerySourceGroup sourceToClose)
    {
        myGroupToEnvoyMapLock.lock();
        try
        {
            ImageryEnvoy envoy = myGroupToEnvoyMap.remove(sourceToClose.getId());
            if (envoy != null)
            {
                envoy.close();
                getController().getToolbox().getEnvoyRegistry().removeObjectsForSource(this, Collections.singleton(envoy));
            }
        }
        finally
        {
            myGroupToEnvoyMapLock.unlock();
        }
    }

    /**
     * Retrieve or create envoy.
     *
     * @param sourceToLoad the source to load
     * @return the imagery envoy
     */
    private ImageryEnvoy retrieveOrCreateEnvoy(ImagerySourceGroup sourceToLoad)
    {
        ImageryEnvoy envoy = null;
        myGroupToEnvoyMapLock.lock();
        try
        {
            envoy = myGroupToEnvoyMap.get(sourceToLoad.getId());
            if (envoy == null)
            {
                envoy = new ImageryEnvoy(getController().getToolbox(), myMetricsTracker, sourceToLoad);
                myGroupToEnvoyMap.put(sourceToLoad.getId(), envoy);
                getController().getToolbox().getEnvoyRegistry().addObjectsForSource(this, Collections.singleton(envoy));
            }
        }
        finally
        {
            myGroupToEnvoyMapLock.unlock();
        }
        return envoy;
    }
}
