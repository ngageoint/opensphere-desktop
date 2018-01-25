package io.opensphere.core.util.taskactivity;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;

/**
 * A {@link TaskActivity} that contains a settable boolean to indicate if the
 * activity should be cancelled.
 */
public class CancellableTaskActivity extends TaskActivity
{
    /**
     * Indicates if the activity should be cancelled.
     */
    private final BooleanProperty myCancelledProperty = new ConcurrentBooleanProperty();

    /**
     * The progress of the task.
     */
    private final SimpleObjectProperty<Double> myProgress = new SimpleObjectProperty<Double>(Double.valueOf(0));

    /**
     * Creates an active task activity with the given label.
     *
     * @param label the label
     * @return the task activity
     */
    public static CancellableTaskActivity createActive(String label)
    {
        CancellableTaskActivity taskActivity = new CancellableTaskActivity();
        taskActivity.setLabelValue(label);
        taskActivity.setActive(true);
        return taskActivity;
    }

    /**
     * Get the cancelled property.
     *
     * @return The cancelled property.
     */
    public BooleanProperty cancelledProperty()
    {
        checkFXThread();
        return myCancelledProperty;
    }

    /**
     * Gets the progress of the task.
     *
     * @return The progress of the task (0.0 - 1.0).
     */
    public double getProgress()
    {
        return myProgress.get().doubleValue();
    }

    /**
     * Gets if this activity should stop what it's doing.
     *
     * @return True if the activity is cancelled false otherwise.
     */
    public boolean isCancelled()
    {
        return myCancelledProperty.get();
    }

    /**
     * Access the progress property.
     *
     * @return The progress property.
     */
    public ObservableValue<Double> progressProperty()
    {
        checkFXThread();
        return myProgress;
    }

    /**
     * Sets if this activity should stop what it's doing.
     *
     * @param cancelled True if the activity is cancelled false otherwise.
     */
    public void setCancelled(boolean cancelled)
    {
        runOnFXThreadIfNecessary(() -> myCancelledProperty.set(cancelled));
        setComplete(true);
    }

    /**
     * Sets the progress of the task.
     *
     * @param progress The progress, 0.0 - 1.0.
     */
    public void setProgress(double progress)
    {
        runOnFXThreadIfNecessary(() -> myProgress.set(Double.valueOf(progress)));
    }
}
