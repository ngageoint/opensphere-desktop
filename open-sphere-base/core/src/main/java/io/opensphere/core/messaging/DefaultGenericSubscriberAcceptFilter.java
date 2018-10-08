package io.opensphere.core.messaging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A default implementation of the {@link GenericSubscriberAcceptFilter}
 * interface.
 */
public class DefaultGenericSubscriberAcceptFilter implements GenericSubscriberAcceptFilter
{
    /** Set of acceptable source classes. */
    private final Set<Class<?>> myAcceptableSourceClasses;

    /** Set of acceptable sources. */
    private final Set<Object> myAcceptableSources;

    /** Flag to indicate filtering by individual sources. */
    private final boolean myFiltersBySource;

    /** Flag to indicate filtering by source classes. */
    private final boolean myFiltersBySourceClasses;

    /**
     * CTOR with with max of one of each type.
     *
     * @param acceptableSource - the acceptable source ( if null filtersBySource
     *            will be set to false )
     * @param acceptableSourceClass - the acceptable source type ( if null or
     *            empty, filtersBySourceClasses will be set to false )
     */
    public DefaultGenericSubscriberAcceptFilter(Object acceptableSource, Class<?> acceptableSourceClass)
    {
        if (acceptableSource == null)
        {
            myFiltersBySource = false;
            myAcceptableSources = null;
        }
        else
        {
            myFiltersBySource = true;
            myAcceptableSources = new HashSet<>();
            myAcceptableSources.add(acceptableSource);
        }

        if (acceptableSourceClass == null)
        {
            myFiltersBySourceClasses = false;
            myAcceptableSourceClasses = null;
        }
        else
        {
            myFiltersBySourceClasses = true;
            myAcceptableSourceClasses = new HashSet<>();
            myAcceptableSourceClasses.add(acceptableSourceClass);
        }
    }

    /**
     * CTOR with all possible parameters. Note: If a list is null or empty,
     * regardless of what was set for the corresponding flag, the flag will be
     * switched to false.
     *
     * @param acceptableSources - the set of acceptable sources ( if null or
     *            empty, filtersBySource will be set to false )
     * @param acceptableSourceClasses - the set of acceptable source types ( if
     *            null or empty, filtersBySourceClasses will be set to false )
     */
    public DefaultGenericSubscriberAcceptFilter(Set<Object> acceptableSources, Set<Class<?>> acceptableSourceClasses)
    {
        if (acceptableSources == null || acceptableSources.isEmpty())
        {
            myFiltersBySource = false;
            myAcceptableSources = null;
        }
        else
        {
            myFiltersBySource = true;
            myAcceptableSources = new HashSet<>(acceptableSources);
        }

        if (acceptableSourceClasses == null || acceptableSourceClasses.isEmpty())
        {
            myFiltersBySourceClasses = false;
            myAcceptableSourceClasses = null;
        }
        else
        {
            myFiltersBySourceClasses = true;
            myAcceptableSourceClasses = new HashSet<>(acceptableSourceClasses);
        }
    }

    @Override
    public boolean acceptsSource(Object source)
    {
        if (myFiltersBySource)
        {
            return myAcceptableSources.contains(source);
        }
        return true;
    }

    @Override
    public boolean acceptsSourceClass(Class<?> aClass)
    {
        if (myFiltersBySourceClasses)
        {
            boolean found = false;
            for (Class<?> acceptClass : myAcceptableSourceClasses)
            {
                if (acceptClass.isInstance(aClass))
                {
                    found = true;
                    break;
                }
            }
            return found;
        }
        return true;
    }

    @Override
    public boolean filtersBySource()
    {
        return myFiltersBySource;
    }

    @Override
    public boolean filtersBySourceClasses()
    {
        return myFiltersBySourceClasses;
    }

    @Override
    public Set<Class<?>> getAcceptSourceClasses()
    {
        return Collections.unmodifiableSet(myAcceptableSourceClasses);
    }

    @Override
    public Set<Object> getAcceptSources()
    {
        return Collections.unmodifiableSet(myAcceptableSources);
    }
}
