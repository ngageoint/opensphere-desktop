package io.opensphere.core.animationhelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * A {@link TimeSpanGovernorManager} that can clear data based on a {@link Pair}
 * object whose null values represent wildcards.
 *
 * @param <T> The type of the first object in the pair context.
 * @param <Q> The type of the second object in the pair context.
 */
public class PairGovernorManager<T, Q> extends TimeSpanGovernorManager<Pair<T, Q>>
{
    /**
     * Constructs a new governor.
     *
     * @param generator The function to generate new governors.
     */
    public PairGovernorManager(Function<Pair<T, Q>, TimeSpanGovernor> generator)
    {
        super(generator);
    }

    @Override
    public void clearData(Pair<T, Q> context)
    {
        List<TimeSpanGovernor> governors = getGovernors(context, false, true);
        for (TimeSpanGovernor governor : governors)
        {
            governor.clearData();
        }
    }

    @Override
    public void clearData(Pair<T, Q> context, Collection<? extends TimeSpan> timeSpans)
    {
        List<TimeSpanGovernor> governors = getGovernors(context, false, false);
        for (TimeSpanGovernor governor : governors)
        {
            governor.clearData(timeSpans);
        }
    }

    @Override
    public void requestData(Pair<T, Q> context, Collection<? extends TimeSpan> timeSpans)
    {
        List<TimeSpanGovernor> governors = getGovernors(context, true, false);

        for (TimeSpan span : timeSpans)
        {
            for (TimeSpanGovernor governor : governors)
            {
                governor.requestData(span);
            }
        }
    }

    /**
     * Gets all governors that match context.
     *
     * @param context The key to key governors, either object in the pair can be
     *            null for wild card.
     * @param isCreate True if we should create a new governor if there isn't
     *            one in the map.
     * @param isRemove True if we should remove the found governors, false if we
     *            should leave them be.
     * @return The governors that match the context.
     */
    private List<TimeSpanGovernor> getGovernors(Pair<T, Q> context, boolean isCreate, boolean isRemove)
    {
        List<TimeSpanGovernor> matchingGovernors = New.list();
        Map<Pair<T, Q>, TimeSpanGovernor> governors = getTimeSpanGovernors();
        synchronized (governors)
        {
            // Find wildcards
            if (context.getFirstObject() == null || context.getSecondObject() == null)
            {
                List<Pair<T, Q>> collectedKeys = New.list();
                for (Map.Entry<Pair<T, Q>, TimeSpanGovernor> entry : governors.entrySet())
                {
                    boolean matches = (context.getFirstObject() == null
                            || context.getFirstObject().equals(entry.getKey().getFirstObject()))
                            && (context.getSecondObject() == null
                            || context.getSecondObject().equals(entry.getKey().getSecondObject()));
                    if (matches)
                    {
                        TimeSpanGovernor governor = entry.getValue();
                        if (governor != null)
                        {
                            collectedKeys.add(entry.getKey());
                            matchingGovernors.add(governor);
                        }
                    }
                }

                if (isRemove)
                {
                    for (Pair<T, Q> key : collectedKeys)
                    {
                        governors.remove(key);
                    }
                }
            }
            // Direct lookup in map
            else
            {
                TimeSpanGovernor governor = null;
                if (isCreate)
                {
                    governor = super.getGovernor(context);
                }
                else if (isRemove)
                {
                    governor = governors.remove(context);
                }
                else
                {
                    governor = governors.get(context);
                }

                if (governor != null)
                {
                    matchingGovernors.add(governor);
                }
            }
        }

        return matchingGovernors;
    }
}
