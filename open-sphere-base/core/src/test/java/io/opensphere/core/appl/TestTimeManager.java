package io.opensphere.core.appl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;

/** Simple TimeManager for testing purposes. */
public class TestTimeManager extends TimeManagerAdapter
{
    /** The fade. */
    private Fade myFade;

    /** The primary spans. */
    private TimeSpanList myPrimaryActiveTimeSpans;

    /** The secondary spans. */
    private final Map<Object, Collection<? extends TimeSpan>> mySecondaryActiveTimeSpans = new HashMap<>();

    @Override
    public Fade getFade()
    {
        return myFade;
    }

    @Override
    public TimeSpanList getPrimaryActiveTimeSpans()
    {
        return myPrimaryActiveTimeSpans;
    }

    @Override
    public Map<Object, Collection<? extends TimeSpan>> getSecondaryActiveTimeSpans()
    {
        return mySecondaryActiveTimeSpans;
    }

    @Override
    public Collection<? extends TimeSpan> getSecondaryActiveTimeSpans(Object constraintKey)
    {
        return mySecondaryActiveTimeSpans.get(constraintKey);
    }

    @Override
    public void setFade(Fade fade)
    {
        myFade = fade;
    }

    @Override
    public void setPrimaryActiveTimeSpan(TimeSpan span)
    {
        setPrimaryActiveTimeSpans(TimeSpanList.singleton(span));
    }

    @Override
    public void setPrimaryActiveTimeSpans(TimeSpanList span)
    {
        myPrimaryActiveTimeSpans = span;
    }

    @Override
    public void setSecondaryActiveTimeSpans(Object constraintKey, Collection<? extends TimeSpan> spans)
    {
        mySecondaryActiveTimeSpans.put(constraintKey, spans);
    }
}
