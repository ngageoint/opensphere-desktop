package io.opensphere.core.animationhelper;

import java.util.List;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/**
 * Used for tests.
 */
public class MockTimeSpanGovernor extends TimeSpanGovernor
{
    /**
     * The requested times.
     */
    private final List<TimeSpan> myRequestedTimes = New.list();

    /**
     * Constructor.
     */
    public MockTimeSpanGovernor()
    {
        super(TimeSpan.get(0, Long.MAX_VALUE));
    }

    /**
     * Gets the requested times.
     *
     * @return The times that were queried.
     */
    public List<TimeSpan> getRequestedTimes()
    {
        return myRequestedTimes;
    }

    @Override
    protected boolean performRequest(List<? extends TimeSpan> spans)
    {
        myRequestedTimes.addAll(spans);
        return true;
    }
}
