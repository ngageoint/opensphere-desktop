package io.opensphere.mantle.infinity;

import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/** An abstract controller that listens to view and time changes. */
public abstract class AbstractViewTimeController extends EventListenerService
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** Procrastinating executor. */
    private final ProcrastinatingExecutor myProcrastinatingExecutor = new ProcrastinatingExecutor(getClass().getSimpleName(),
            1000);

    /** The last active time span. */
    @ThreadConfined("AbstractViewTimeController-0")
    private TimeSpan myLastActiveTime;

    /** The last visible bounding box. */
    @ThreadConfined("AbstractViewTimeController-0")
    private GeographicBoundingBox myLastBoundingBox;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public AbstractViewTimeController(Toolbox toolbox)
    {
        super(toolbox.getEventManager());
        myToolbox = toolbox;
        addService(toolbox.getMapManager().getViewChangeSupport().getViewChangeListenerService(this::handleViewChanged));
        addService(toolbox.getTimeManager().getPrimaryTimeSpanListenerService(new PrimaryTimeSpanChangeListener()
        {
            @Override
            public void primaryTimeSpansChanged(TimeSpanList spans)
            {
                handleTimeChanged(spans);
            }

            @Override
            public void primaryTimeSpansCleared()
            {
            }
        }));
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    protected Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Triggers a change without resetting the viewer box or time.
     */
    protected void triggerChange()
    {
        myProcrastinatingExecutor.execute(() -> handleChange(null));
    }

    /**
     * Handle time changed.
     *
     * @param spans the spans
     */
    void handleTimeChanged(TimeSpanList spans)
    {
        myProcrastinatingExecutor.execute(() ->
        {
            myLastActiveTime = null;
            handleChange(spans.get(0));
        });
    }

    /**
     * Handle view changed.
     *
     * @param viewer the viewer
     * @param type the change type
     */
    private void handleViewChanged(Viewer viewer, ViewChangeSupport.ViewChangeType type)
    {
        myProcrastinatingExecutor.execute(() ->
        {
            myLastBoundingBox = null;
            handleChange(null);
        });
    }

    /**
     * Handles a view or time change.
     *
     * @param activeSpan the optional active span
     */
    private void handleChange(TimeSpan activeSpan)
    {
        if (myLastActiveTime == null || myLastActiveTime.isZero())
        {
            myLastActiveTime = activeSpan != null ? activeSpan : myToolbox.getTimeManager().getPrimaryActiveTimeSpans().get(0);
        }
        if (myLastBoundingBox == null)
        {
            myLastBoundingBox = myToolbox.getMapManager().getVisibleBoundingBox();
        }

        handleChange(myLastActiveTime, myLastBoundingBox);
    }

    /**
     * Handles a view or time change.
     *
     * @param activeSpan the active span
     * @param boundingBox the bounding box
     */
    protected abstract void handleChange(TimeSpan activeSpan, GeographicBoundingBox boundingBox);
}
