package io.opensphere.controlpanels.animation.controller;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.TimeManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ObservableList;

/**
 * Convenience implementation of {@link TimeManager}.
 */
public class TimeManagerAdapter implements TimeManager
{
    @Override
    public void addActiveTimeSpanChangeListener(ActiveTimeSpanChangeListener listener)
    {
    }

    @Override
    public void addDataLoadDurationChangeListener(DataLoadDurationChangeListener listener)
    {
    }

    @Override
    public void addPrimaryTimeSpanChangeListener(PrimaryTimeSpanChangeListener listener)
    {
    }

    @Override
    public void addRequestedDataDurationsChangeListener(RequestedDataDurationsChangeListener listener)
    {
    }

    @Override
    public void clearActiveTimeSpans()
    {
    }

    @Override
    public ActiveTimeSpans getActiveTimeSpans()
    {
        return null;
    }

    @Override
    public Duration getDataLoadDuration()
    {
        return null;
    }

    @Override
    public ObservableList<TimeSpan> getLoadTimeSpans()
    {
        return null;
    }

    @Override
    public Fade getFade()
    {
        return null;
    }

    @Override
    public TimeSpanList getPrimaryActiveTimeSpans()
    {
        return null;
    }

    @Override
    public Set<? extends Duration> getRequestedDataDurations()
    {
        return null;
    }

    @Override
    public Map<Object, Collection<? extends TimeSpan>> getSecondaryActiveTimeSpans()
    {
        return null;
    }

    @Override
    public Collection<? extends TimeSpan> getSecondaryActiveTimeSpans(Object constraintKey)
    {
        return null;
    }

    @Override
    public void releaseDataDurationRequest(Object source)
    {
    }

    @Override
    public void removeActiveTimeSpanChangeListener(ActiveTimeSpanChangeListener listener)
    {
    }

    @Override
    public void removeDataLoadDurationChangeListener(DataLoadDurationChangeListener listener)
    {
    }

    @Override
    public void removePrimaryTimeSpanChangeListener(PrimaryTimeSpanChangeListener listener)
    {
    }

    @Override
    public void removeRequestedDataDurationsChangeListener(RequestedDataDurationsChangeListener listener)
    {
    }

    @Override
    public void removeSecondaryActiveTimeSpan(TimeSpan span)
    {
    }

    @Override
    public void removeSecondaryActiveTimeSpansByConstraint(Object constraintKey)
    {
    }

    @Override
    public void requestDataDurations(Object source, Collection<? extends Duration> durations)
    {
    }

    @Override
    public void setFade(Fade fade)
    {
    }

    @Override
    public void setPrimaryActiveTimeSpan(TimeSpan span)
    {
    }

    @Override
    public void setPrimaryActiveTimeSpans(TimeSpanList span)
    {
    }

    @Override
    public void setSecondaryActiveTimeSpans(Object constraintKey, Collection<? extends TimeSpan> spans)
    {
    }
}
