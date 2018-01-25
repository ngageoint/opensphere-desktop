package io.opensphere.wfs.envoy;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.metrics.impl.DefaultNumberMetricsProvider;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.PhasedChangeArbitrator;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.wfs.layer.WFSDataType;

/**
 * Controller that monitors active WFS downloads and reports status to the map
 * display and events out status for other status displays.
 */
public class WFSDownloadMonitor
{
    /** My set of active downloads. */
    private final Set<DownloadMonitorObject> myActiveDownloads;

    /** My set of completed downloads. */
    private final Set<DownloadMonitorObject> myCompletedDownloads;

    /** The Complete downloads metrics provider. */
    private final DefaultNumberMetricsProvider myCompleteDownloadsMetricsProvider;

    /** The Downloads metrics provider. */
    private final DefaultNumberMetricsProvider myDownloadsMetricsProvider;

    /** My core event manager. */
    private final EventManager myEventManager;

    /** My active task activity. */
    private final DownloadTaskActivity myTaskActivity;

    /**
     * Instantiates a new WFS download monitor.
     *
     * @param toolbox the toolbox
     */
    public WFSDownloadMonitor(Toolbox toolbox)
    {
        myActiveDownloads = Collections.synchronizedSet(New.<DownloadMonitorObject>set());
        myCompletedDownloads = New.set();
        myEventManager = toolbox.getEventManager();

        myDownloadsMetricsProvider = new DefaultNumberMetricsProvider(1, "WFS", "Downloads", "Active");
        myCompleteDownloadsMetricsProvider = new DefaultNumberMetricsProvider(2, "WFS", "Downloads", "Complete");
        toolbox.getMetricsRegistry().addMetricsProvider(myDownloadsMetricsProvider);
        toolbox.getMetricsRegistry().addMetricsProvider(myCompleteDownloadsMetricsProvider);

        myTaskActivity = new DownloadTaskActivity();
        // TODO There is no cleanup mechanism for this monitor, so these are
        // never removed cleanly.
        if (toolbox.getAnimationManager() != null)
        {
            toolbox.getAnimationManager().addPhasedChangeArbitrator(myTaskActivity);
        }
        if (toolbox.getUIRegistry() != null)
        {
            toolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(myTaskActivity);
        }
    }

    /**
     * Adds an active download.
     *
     * @param type the type
     * @param timeSpan the time span
     * @param geometry the geometry
     */
    public void addDownload(WFSDataType type, TimeSpan timeSpan, Geometry geometry)
    {
        myActiveDownloads.add(new DownloadMonitorObject(type, timeSpan, geometry));
        myDownloadsMetricsProvider.setValue(Integer.valueOf(myActiveDownloads.size()));
        updateMapStatus();
        fireChangeEvent();
    }

    /**
     * Gets a list of all downloads, completed and active.
     *
     * @return an unmodifiable list of all downloads
     */
    public List<DownloadMonitorObject> getActiveDownloads()
    {
        List<DownloadMonitorObject> returnList = New.list(myActiveDownloads);
        returnList.addAll(myCompletedDownloads);
        return returnList;
    }

    /**
     * Removes an active download.
     *
     * @param type the type
     * @param timeSpan the time span
     * @param geometry the geometry
     * @param numFeatures the number of features returned by the query
     */
    public void removeDownload(WFSDataType type, TimeSpan timeSpan, Geometry geometry, int numFeatures)
    {
        synchronized (myActiveDownloads)
        {
            for (Iterator<DownloadMonitorObject> iter = myActiveDownloads.iterator(); iter.hasNext();)
            {
                DownloadMonitorObject obj = iter.next();
                if (obj.matches(type, timeSpan, geometry))
                {
                    obj.setQueryCompleteTime(new Date(System.currentTimeMillis()));
                    obj.setNumFeatures(numFeatures);
                    myCompletedDownloads.add(obj);
                    myCompleteDownloadsMetricsProvider.setValue(Integer.valueOf(myCompletedDownloads.size()));
                    iter.remove();
                    break;
                }
            }
            myDownloadsMetricsProvider.setValue(Integer.valueOf(myActiveDownloads.size()));
        }
        updateMapStatus();
        fireChangeEvent();
    }

    /**
     * Fire change event to clients monitoring WFS downloads.
     */
    private void fireChangeEvent()
    {
        myEventManager.publishEvent(new DownloadMonitorChangeEvent());
    }

    /**
     * Update the download status on the map.
     */
    private void updateMapStatus()
    {
        synchronized (myTaskActivity)
        {
            if (!myActiveDownloads.isEmpty())
            {
                if (!myTaskActivity.isActive())
                {
                    myTaskActivity.setActive(true);
                }
                myTaskActivity.setNumDownloads(myActiveDownloads.size());
            }
            else
            {
                myTaskActivity.setActive(false);
            }
        }
    }

    /**
     * The Class DownloadMonitorChangeEvent.
     */
    protected static class DownloadMonitorChangeEvent extends AbstractSingleStateEvent
    {
        @Override
        public String getDescription()
        {
            return "Signals a change in the Download Monitor's active download list.";
        }
    }

    /**
     * The Class DownloadMonitorObject.
     */
    protected static class DownloadMonitorObject
    {
        /** The Constant requestCounter. */
        private static final AtomicInteger ourRequestCounter = new AtomicInteger(0);

        /** My geometry. */
        private final Geometry myGeometry;

        /** The number of features returned from this request. */
        private int myNumFeatures = -1;

        /** The time this query was added to the download monitor. */
        private final Date myQueryAddTime;

        /** The time this query was removed from the download monitor. */
        private Date myQueryCompleteTime;

        /** My unique request number. */
        private final int myRequestNumber;

        /** My time span. */
        private final TimeSpan myTimeSpan;

        /** My type. */
        private final WFSDataType myType;

        /**
         * Format a date using the HMS formatter.
         *
         * @param date The date.
         * @return The string.
         */
        private static synchronized String formatMsTime(Date date)
        {
            return new SimpleDateFormat("HH:mm:ss.SSS").format(date);
        }

        /**
         * Format a date using the HMS formatter.
         *
         * @param date The date.
         * @return The string.
         */
        private static synchronized String formatTime(Date date)
        {
            return new SimpleDateFormat().format(date);
        }

        /**
         * Instantiates a new download monitor object.
         *
         * @param type the type
         * @param timeSpan the time span
         * @param geometry the geometry
         */
        protected DownloadMonitorObject(WFSDataType type, TimeSpan timeSpan, Geometry geometry)
        {
            myType = type;
            myTimeSpan = timeSpan;
            myGeometry = geometry;
            myRequestNumber = ourRequestCounter.incrementAndGet();
            myQueryAddTime = new Date(System.currentTimeMillis());
        }

        /**
         * Matches.
         *
         * @param requestNumber the request number
         * @return true, if successful
         */
        public boolean matches(int requestNumber)
        {
            return requestNumber == myRequestNumber;
        }

        /**
         * Check whether the passed-in fields match my local fields.
         *
         * @param type the type
         * @param timeSpan the time span
         * @param geometry the geometry
         * @return true, if my local variables are the same as those passed in
         */
        public boolean matches(WFSDataType type, TimeSpan timeSpan, Geometry geometry)
        {
            return type.equals(myType) && timeSpan.equals(myTimeSpan) && geometry.equals(myGeometry);
        }

        /* Make toString method "synchronized" because static SimpleDateFormats
         * are used. */
        @Override
        public synchronized String toString()
        {
            StringBuilder output = new StringBuilder(128).append("WFS Query #").append(myRequestNumber)
                    .append(":\n   Data Layer: ").append(myType.getDisplayName()).append(" (").append(myType.getTypeKey())
                    .append(")\n   Features Returned: ").append(myNumFeatures == -1 ? "Unknown" : Integer.toString(myNumFeatures))
                    .append("\n   Query Time: ");
            if (myQueryCompleteTime == null)
            {
                output.append("Pending (Started at ").append(formatMsTime(myQueryAddTime)).append(")\n");
            }
            else
            {
                long deltaNs = (myQueryCompleteTime.getTime() - myQueryAddTime.getTime()) * 1000000;
                output.append(StringUtilities.formatTimingMessage("", deltaNs));
                output.append(" (").append(formatMsTime(myQueryAddTime));
                output.append(" - ").append(formatMsTime(myQueryCompleteTime)).append(")\n");
            }
            output.append("   Request:\n      Times: ");
            if (myTimeSpan.isTimeless())
            {
                output.append("Timeless\n");
            }
            else
            {
                output.append(myTimeSpan.isUnboundedStart() ? "Unbounded" : formatTime(myTimeSpan.getStartDate())).append(" - ");
                output.append(myTimeSpan.isUnboundedEnd() ? "Unbounded" : formatTime(myTimeSpan.getEndDate())).append('\n');
            }
            output.append("      Geometry: ").append(myGeometry.toText());
            return output.toString();
        }

        /**
         * Gets the geometry.
         *
         * @return the geometry
         */
        protected Geometry getGeometry()
        {
            return myGeometry;
        }

        /**
         * Gets the number of features returned by this request.
         *
         * @return the number of features
         */
        protected int getNumFeatures()
        {
            return myNumFeatures;
        }

        /**
         * Gets the query time.
         *
         * @return the query time
         */
        protected Date getQueryTime()
        {
            return myQueryAddTime;
        }

        /**
         * Gets the request number.
         *
         * @return the request number
         */
        protected int getRequestNumber()
        {
            return myRequestNumber;
        }

        /**
         * Gets the time span.
         *
         * @return the time span
         */
        protected TimeSpan getTimeSpan()
        {
            return myTimeSpan;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        protected WFSDataType getType()
        {
            return myType;
        }

        /**
         * Sets the number of features returned by the request.
         *
         * @param numFeatures the number of features
         */
        protected void setNumFeatures(int numFeatures)
        {
            myNumFeatures = numFeatures;
        }

        /**
         * Sets the time the query completed.
         *
         * @param completeTime the new query complete time
         */
        protected void setQueryCompleteTime(Date completeTime)
        {
            myQueryCompleteTime = completeTime;
        }
    }

    /**
     * Display a label in the activity bar which shows the queued downloads.
     * While downloads are in progress, insist on phased commits for animation
     * changes.
     */
    private static class DownloadTaskActivity extends TaskActivity implements PhasedChangeArbitrator
    {
        /** The message that is displayed to the user for queued downloads. */
        private static final String MAP_MESSAGE = "Downloads queued: ";

        @Override
        public boolean isPhasedCommitRequired()
        {
            return isActive();
        }

        /**
         * Sets the number of active downloads to report.
         *
         * @param numDownloads the number of active downloads
         */
        public void setNumDownloads(int numDownloads)
        {
            setLabelValue(MAP_MESSAGE + numDownloads);
        }
    }
}
