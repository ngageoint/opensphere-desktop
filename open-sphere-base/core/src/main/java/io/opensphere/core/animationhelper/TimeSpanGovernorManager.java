package io.opensphere.core.animationhelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.concurrent.GuardedBy;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/**
 * Manages time span governors for multiple contexts.
 *
 * @param <T> the context type
 */
public class TimeSpanGovernorManager<T>
{
    /** The function that generates new governors. */
    private final Function<T, TimeSpanGovernor> myGenerator;

    /** The governors. */
    @GuardedBy("myTimeSpanGovernors")
    private final Map<T, TimeSpanGovernor> myTimeSpanGovernors = New.map();

    /**
     * Constructor.
     *
     * @param generator The function that generates new governors
     */
    public TimeSpanGovernorManager(Function<T, TimeSpanGovernor> generator)
    {
        myGenerator = generator;
    }

    /**
     * Gets the governor for the context, creating it if necessary.
     *
     * @param context the context
     * @return the governor
     */
    public TimeSpanGovernor getGovernor(T context)
    {
        TimeSpanGovernor governor;
        synchronized (myTimeSpanGovernors)
        {
            governor = myTimeSpanGovernors.get(context);
            if (governor == null)
            {
                governor = myGenerator.apply(context);
                myTimeSpanGovernors.put(context, governor);
            }
        }
        return governor;
    }

    /**
     * Finds the governors that match the predicate.
     *
     * @param predicate the predicate
     * @return the matching governors
     */
    public List<TimeSpanGovernor> findGovernors(Predicate<? super T> predicate)
    {
        List<TimeSpanGovernor> governors;
        synchronized (myTimeSpanGovernors)
        {
            governors = myTimeSpanGovernors.entrySet().stream().filter(e -> predicate.test(e.getKey())).map(e -> e.getValue())
                    .collect(Collectors.toList());
        }
        return governors;
    }

    /**
     * Requests data for the time spans and contexts.
     *
     * @param context the context
     * @param timeSpans the time spans
     */
    public void requestData(T context, Collection<? extends TimeSpan> timeSpans)
    {
        TimeSpanGovernor governor = getGovernor(context);
        for (TimeSpan timeSpan : timeSpans)
        {
            governor.requestData(timeSpan);
        }
    }

    /**
     * Clears data for the given context.
     *
     * @param context the context
     */
    public void clearData(T context)
    {
        TimeSpanGovernor governor;
        synchronized (myTimeSpanGovernors)
        {
            governor = myTimeSpanGovernors.remove(context);
        }
        if (governor != null)
        {
            governor.clearData();
        }
    }

    /**
     * Clears the data for the given context and given time spans.
     *
     * @param context The context.
     * @param timeSpans The time spans.
     */
    public void clearData(T context, Collection<? extends TimeSpan> timeSpans)
    {
        TimeSpanGovernor governor;
        synchronized (myTimeSpanGovernors)
        {
            governor = myTimeSpanGovernors.remove(context);
        }
        if (governor != null)
        {
            governor.clearData(timeSpans);
        }
    }

    /**
     * Gets the governors map. Any access to the map needs to be synchronized on
     * the map.
     *
     * @return the governors map
     */
    protected Map<T, TimeSpanGovernor> getTimeSpanGovernors()
    {
        return myTimeSpanGovernors;
    }
}
