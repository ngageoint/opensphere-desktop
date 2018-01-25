package io.opensphere.geopackage.model;

import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains information about the progress completed or an import or export.
 */
public class ProgressModel extends Observable
{
    /**
     * The property name for completedCount.
     */
    public static final String COMPLETED_COUNT_PROP = "completedImportCount";

    /**
     * The number of elements that have been imported/exported from/to the
     * geopackage file.
     */
    private final AtomicInteger myCompletedCount = new AtomicInteger();

    /**
     * Gets the number of elements that have been imported/exported from/to the
     * geopackage file.
     *
     * @return The number of elements that have been imported/exported from/to
     *         the geopackage file.
     */
    public int getCompletedCount()
    {
        return myCompletedCount.get();
    }

    /**
     * Sets the number of elements that have been imported/exported.
     *
     * @param completedCount The number of elements that have been
     *            imported/exported.
     */
    public void setCompletedCount(int completedCount)
    {
        myCompletedCount.set(completedCount);
        super.setChanged();
        super.notifyObservers(COMPLETED_COUNT_PROP);
    }

    /**
     * Increments the completed count.
     */
    public void incrementCompletedCount()
    {
        myCompletedCount.incrementAndGet();
        super.setChanged();
        super.notifyObservers(COMPLETED_COUNT_PROP);
    }
}
