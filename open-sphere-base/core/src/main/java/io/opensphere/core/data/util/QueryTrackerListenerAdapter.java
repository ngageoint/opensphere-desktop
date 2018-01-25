package io.opensphere.core.data.util;

import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.data.util.QueryTracker.QueryTrackerListener;

/** Adapter for {@link QueryTrackerListener}. */
public class QueryTrackerListenerAdapter implements QueryTrackerListener
{
    @Override
    public void fractionCompleteChanged(QueryTracker tracker, float fractionComplete)
    {
    }

    @Override
    public void statusChanged(QueryTracker tracker, QueryStatus status)
    {
    }
}
