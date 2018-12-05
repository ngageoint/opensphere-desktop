package io.opensphere.csvcommon.detect.location.algorithm.decider;

import java.util.List;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;

/**
 * A factory for creating LocationDecider objects.
 */
public final class LocationDeciderFactory
{
    /** The Constant ourInstance. */
    private static final LocationDeciderFactory ourInstance = new LocationDeciderFactory();

    /** Not constructible. */
    private LocationDeciderFactory()
    {
    }

    /**
     * Gets the single instance of LocationDeciderFactory.
     *
     * @return single instance of LocationDeciderFactory
     */
    public static LocationDeciderFactory getInstance()
    {
        return ourInstance;
    }

    /**
     * Builds the deciders.
     *
     * @param prefsRegistry the preferences registry
     * @return the list
     */
    public List<LocationDecider> buildDeciders(PreferencesRegistry prefsRegistry)
    {
        return New.list(new LatLonDecider(prefsRegistry), new MGRSDecider(prefsRegistry), new PositionDecider(prefsRegistry),
                new WktDecider(prefsRegistry), new ColorDecider(prefsRegistry));
    }
}
