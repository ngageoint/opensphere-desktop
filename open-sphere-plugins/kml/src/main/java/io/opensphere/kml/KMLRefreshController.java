package io.opensphere.kml;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Overlay;
import de.micromata.opengis.kml.v_2_2_0.RefreshMode;
import de.micromata.opengis.kml.v_2_2_0.ViewRefreshMode;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.kml.common.model.KMLController;
import io.opensphere.kml.common.model.KMLDataEvent;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLDataSourceUtils;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Refresh Controller.
 */
@ThreadSafe
public class KMLRefreshController implements KMLController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(KMLRefreshController.class);

    /** The number of failures to allow before stopping refresh. */
    private static final int MAX_FAILURE_COUNT = NumberUtilities.parseInt(System.getProperty("opensphere.kml.maxFailureCount", "5"), 5);

    /** Map of data sources to futures for refresh. */
    @GuardedBy("this")
    private final Map<KMLDataSource, Future<?>> myDataSourceToRefreshFutureMap = New.map();

    /** Map of data sources to futures for view refresh. */
    @GuardedBy("this")
    private final Map<KMLDataSource, Future<?>> myDataSourceToViewRefreshFutureMap = New.map();

    /** The executor used to schedule refreshes. */
    private final ScheduledExecutorService myExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("KML-refresh"));

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The data sources to be refreshed after the view changes. */
    private final Set<KMLDataSource> myViewRefreshDataSources = New.set();

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public KMLRefreshController(final Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public synchronized void addData(KMLDataEvent dataEvent, boolean reload)
    {
        final KMLDataSource dataSource = dataEvent.getDataSource();

        startDataSourceRefresh(dataSource);

        for (final KMLFeature feature : dataEvent.getData().getAllFeatures())
        {
            final KMLDataSource resultingDataSource = feature.getResultingDataSource();
            if (resultingDataSource != null)
            {
                if (feature.getFeature() instanceof NetworkLink)
                {
                    if (feature.isVisibility().booleanValue() && feature.getRegion() == null)
                    {
                        // Do the initial load for network links.
                        KMLDataRegistryHelper.queryAndActivate(myToolbox.getDataRegistry(), resultingDataSource,
                                resultingDataSource.getPath(), Nulls.STRING);
                    }
                }
                else if (feature.getFeature() instanceof Overlay)
                {
                    // Overlay data sources do not get put into KMLDataEvents,
                    // so they need to be scheduled when their parent data
                    // sources are added.
                    startDataSourceRefresh(resultingDataSource);
                }
            }
        }
    }

    @Override
    public synchronized void removeData(KMLDataSource dataSource)
    {
        cancelViewRefresh(dataSource);
        cancelPeriodicRefresh(dataSource);
        myViewRefreshDataSources.remove(dataSource);
    }

    /**
     * Handle view changed.
     */
    public synchronized void handleViewChanged()
    {
        for (final KMLDataSource dataSource : myViewRefreshDataSources)
        {
            scheduleViewRefresh(dataSource);
        }
    }

    /**
     * Set up a data source for refreshing if necessary.
     *
     * @param dataSource The data source.
     */
    private void startDataSourceRefresh(KMLDataSource dataSource)
    {
        final RefreshMode refreshMode = KMLDataSourceUtils.getRefreshMode(dataSource);
        final ViewRefreshMode viewRefreshMode = KMLDataSourceUtils.getViewRefreshMode(dataSource);

        if (refreshMode == RefreshMode.ON_INTERVAL || refreshMode == RefreshMode.ON_EXPIRE)
        {
            schedulePeriodicRefresh(dataSource);
        }
        if (viewRefreshMode == ViewRefreshMode.ON_STOP && dataSource.getCreatingFeature() instanceof NetworkLink)
        {
            myViewRefreshDataSources.add(dataSource);
        }
    }

    /**
     * Schedule a data source to be refreshed periodically.
     *
     * @param dataSource The data source to be refreshed.
     */
    private synchronized void schedulePeriodicRefresh(final KMLDataSource dataSource)
    {
        cancelPeriodicRefresh(dataSource);

        final int refreshSeconds = KMLDataSourceUtils.getRefreshInterval(dataSource);
        if (refreshSeconds > 0)
        {
            final ScheduledFuture<?> future = myExecutor.scheduleAtFixedRate(new Refresher(dataSource), refreshSeconds, refreshSeconds,
                    TimeUnit.SECONDS);
            myDataSourceToRefreshFutureMap.put(dataSource, future);
            log();
        }
    }

    /**
     * Schedule a data source to be refreshed after a view change.
     *
     * @param dataSource The data source to be refreshed.
     */
    private synchronized void scheduleViewRefresh(final KMLDataSource dataSource)
    {
        cancelViewRefresh(dataSource);

        final int delaySeconds = KMLDataSourceUtils.getRefreshInterval(dataSource);
        if (delaySeconds > 0)
        {
            final ScheduledFuture<?> future = myExecutor.schedule(new Refresher(dataSource), delaySeconds, TimeUnit.SECONDS);
            myDataSourceToViewRefreshFutureMap.put(dataSource, future);
            log();
        }
    }

    /**
     * Cancel pending non-view refreshes for a data source.
     *
     * @param dataSource The data source.
     */
    private void cancelPeriodicRefresh(KMLDataSource dataSource)
    {
        final Future<?> future = myDataSourceToRefreshFutureMap.remove(dataSource);
        if (future != null)
        {
            future.cancel(false);
            log();
        }
    }

    /**
     * Cancel pending view refreshes for a data source.
     *
     * @param dataSource The data source.
     */
    private void cancelViewRefresh(KMLDataSource dataSource)
    {
        final Future<?> future = myDataSourceToViewRefreshFutureMap.remove(dataSource);
        if (future != null)
        {
            future.cancel(false);
            log();
        }
    }

    /**
     * Logs the contents of this class.
     */
    private void log()
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("KMLRefreshController.ViewRefresh.size = " + myDataSourceToViewRefreshFutureMap.size());
            LOGGER.debug("KMLRefreshController.Refresh.size = " + myDataSourceToRefreshFutureMap.size());
        }
    }

    /**
     * A runnable that refreshes some KML data.
     */
    private final class Refresher implements Runnable
    {
        /** The data source being refreshed. */
        private final KMLDataSource myDataSource;

        /**
         * Constructor.
         *
         * @param dataSource The data source to be refreshed.
         */
        public Refresher(KMLDataSource dataSource)
        {
            myDataSource = dataSource;
        }

        @Override
        public void run()
        {
            if (myDataSource.getOutcomeTracker().getSuccessCount() > 0
                    && myDataSource.getOutcomeTracker().getConsecutiveFailureCount() < MAX_FAILURE_COUNT)
            {
                boolean isVisible = false;
                for (final DataTypeInfo dataType : myDataSource.getRootDataSource().getDataGroupInfo().getMembers(false))
                {
                    isVisible |= dataType.isVisible();
                }

                if (isVisible)
                {
                    if (myDataSource.getCreatingFeature() instanceof Overlay)
                    {
                        // For Overlays, just tell the tile to refresh itself.
                        KMLDataRegistryHelper.clearData(myToolbox.getDataRegistry(), myDataSource);
                        myDataSource.getCreatingKMLFeature().getTile().requestImageData();
                    }
                    else
                    {
                        // For KMLs and Network Links, reload the data source.
                        KMLDataRegistryHelper.reloadData(myToolbox.getDataRegistry(), myDataSource);
                    }
                }
            }
        }
    }
}
