package io.opensphere.core.util.taskactivity;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

import net.jcip.annotations.ThreadSafe;

import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;

/**
 * A task activity for display in a TaskActivityPanel.
 */
@ThreadSafe
public class TaskActivity implements AutoCloseable
{
    /**
     * Flag indicating if task activities need to run property changes on the FX
     * thread.
     */
    private static final AtomicBoolean ourFXThreadConstrained = new AtomicBoolean();

    /** Active flag for the TaskActivity. */
    private final BooleanProperty myActive = new ConcurrentBooleanProperty();

    /** Complete flag for the TaskActivity. */
    private final BooleanProperty myComplete = new ConcurrentBooleanProperty();

    /** The activity message. */
    private final StringProperty myLabel = new ConcurrentStringProperty();

    /**
     * Creates an active task activity with the given label.
     *
     * @param label the label
     * @return the task activity
     */
    public static TaskActivity createActive(String label)
    {
        TaskActivity taskActivity = new TaskActivity();
        taskActivity.setLabelValue(label);
        taskActivity.setActive(true);
        return taskActivity;
    }

    /**
     * If the current thread is the FX thread, make sure the
     * {@link #ourFXThreadConstrained} flag is set.
     */
    protected static void checkFXThread()
    {
        if (!ourFXThreadConstrained.get() && Platform.isFxApplicationThread())
        {
            ourFXThreadConstrained.set(true);
        }
    }

    /**
     * If properties have been accessed from the FX thread and this is invoked
     * from a non-FX thread, send the runnable to the FX thread.
     *
     * @param r The runnable.
     */
    protected static void runOnFXThreadIfNecessary(Runnable r)
    {
        if (!ourFXThreadConstrained.get() || Platform.isFxApplicationThread())
        {
            r.run();
        }
        else
        {
            Platform.runLater(r);
        }
    }

    /**
     * Active property.
     *
     * @return the boolean property
     */
    public BooleanProperty activeProperty()
    {
        checkFXThread();
        return myActive;
    }

    /**
     * Complete property.
     *
     * @return the boolean property
     */
    public BooleanProperty completeProperty()
    {
        checkFXThread();
        return myComplete;
    }

    /**
     * Gets the message label that is to be displayed by the TaskActivityPanel.
     *
     * @return the message.
     */
    public String getLabelValue()
    {
        return myLabel.get();
    }

    /**
     * Returns true if this TaskActivity is Active( i.e. displayed ) a task
     * activity will be held by a TaskActivity panel so long as it is not
     * complete, but if it is not active it is not shown. The Active flag is a
     * good way for a long running activity that will sometime need to show the
     * user a message to remain in the panels activity list.
     *
     * @return true if active, false if not
     */
    public boolean isActive()
    {
        return myActive.get();
    }

    /**
     * Returns true if this TaskActivity is complete, this lets the TaskActivity
     * panel know that this TaskActivity should be removed from the held list.
     *
     * @return true if complete, false if not
     */
    public boolean isComplete()
    {
        return myComplete.get();
    }

    /**
     * Label property.
     *
     * @return the string property
     */
    public StringProperty labelProperty()
    {
        checkFXThread();
        return myLabel;
    }

    /**
     * Sets if this TaskActivity is Active( i.e. displayed ) a task activity
     * will be held by a TaskActivity panel so long as it is not complete, but
     * if it is not active it is not shown. The Active flag is a good way for a
     * long running activity that will sometime need to show the user a message
     * to remain in the panels activity list.
     *
     * @param active - true if active ( to be displayed ) false if not
     */
    public void setActive(boolean active)
    {
        runOnFXThreadIfNecessary(() -> myActive.set(active));
    }

    /**
     * Sets the state of the complete flag for this task activity. Once complete
     * the task activity will be removed from the list of tasks in the
     * {@link TaskActivityPanel}, once this is done any changes to message or
     * active will do nothing.
     *
     * @param complete - true when complete, false if not complete.
     */
    public void setComplete(boolean complete)
    {
        runOnFXThreadIfNecessary(() -> myComplete.set(complete));
    }

    /**
     * Sets the value of the message label to be displayed, this will be update
     * cycle in the TaskActivityPanel display at the next update cycle.
     *
     * @param value - the message to display
     */
    public void setLabelValue(String value)
    {
        runOnFXThreadIfNecessary(() -> myLabel.set(value));
    }

    @Override
    public void close()
    {
        setComplete(true);
    }
}
