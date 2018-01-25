package io.opensphere.xyztile.transformer;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.geometry.ImageManager.RequestObserver;
import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * Informs the user how many current tile downloads are running.
 */
public class XYZQueryTracker implements RequestObserver
{
    /**
     * The layer name to notify of the queries.
     */
    private final String myLayerName;

    /**
     * The current task activity.
     */
    private TaskActivity myTa;

    /**
     * The number of total queries.
     */
    private int myTotalQueries;

    /**
     * The ui registry.
     */
    private final UIRegistry myUIRegistry;

    /**
     * Constructs a new query tracker.
     *
     * @param uiRegistry The ui registry.
     * @param layerName The layer name to notify of the queries.
     */
    public XYZQueryTracker(UIRegistry uiRegistry, String layerName)
    {
        myUIRegistry = uiRegistry;
        myLayerName = layerName;
    }

    @Override
    public synchronized void requestComplete()
    {
        myTotalQueries--;
        if (myTotalQueries == 0)
        {
            myTa.setComplete(true);
            myTa = null;
        }
        else
        {
            myTa.setLabelValue(myLayerName + " Tile Downloads " + myTotalQueries);
        }
    }

    @Override
    public synchronized void requestStarted()
    {
        myTotalQueries++;
        if (myTa == null)
        {
            myTa = TaskActivity.createActive(myLayerName + " Tile Downloads " + myTotalQueries);
            myUIRegistry.getMenuBarRegistry().addTaskActivity(myTa);
        }
        else
        {
            myTa.setLabelValue(myLayerName + " Tile Downloads " + myTotalQueries);
        }
    }
}
