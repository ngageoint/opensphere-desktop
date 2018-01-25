package io.opensphere.geopackage.transformer;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * Notifies the user of the ongoing geopackage queries that are happening.
 */
public class GeoPackageQueryTracker
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
     * The number of total qeuries.
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
    public GeoPackageQueryTracker(UIRegistry uiRegistry, String layerName)
    {
        myUIRegistry = uiRegistry;
        myLayerName = layerName;
    }

    /**
     * Called when a query is done.
     */
    public synchronized void subtractCounter()
    {
        myTotalQueries--;
        if (myTotalQueries == 0)
        {
            myTa.setComplete(true);
            myTa = null;
        }
        else
        {
            myTa.setLabelValue(myLayerName + " Tile Queries " + myTotalQueries);
        }
    }

    /**
     * Called when a querying is occuring.
     */
    public synchronized void upCounter()
    {
        myTotalQueries++;
        if (myTa == null)
        {
            myTa = TaskActivity.createActive(myLayerName + " Tile Queries " + myTotalQueries);
            myUIRegistry.getMenuBarRegistry().addTaskActivity(myTa);
        }
        else
        {
            myTa.setLabelValue(myLayerName + " Tile Queries " + myTotalQueries);
        }
    }
}
