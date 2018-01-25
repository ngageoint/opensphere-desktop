package io.opensphere.wps.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.wps.source.WPSFeature;

/**
 * This class holds information pertaining to the results from a wps execute
 * request.
 */
public class WPSProcessResult
{
    /** The collection of wps features. */
    private final Collection<WPSFeature> myFeatures;

    /** The name. */
    private String myName;

    /** The bounding box. */
    // private final GeographicBoundingBox myBoundingBox;

    /** The time-span. */
    private TimeSpan myTimespan;

    /**
     * Constructor.
     *
     * @param name The name of the requestor.
     */
    public WPSProcessResult(String name)
    {
        myName = name;
        myFeatures = new ArrayList<>();
    }

    /**
     * Add a WPSFeature to my collection.
     *
     * @param feature The WPSFeature to add.
     */
    public void addFeature(WPSFeature feature)
    {
        if (feature != null)
        {
            myFeatures.add(feature);
        }
    }

    /**
     * Standard getter.
     *
     * @return The collection of features.
     */
    public Collection<WPSFeature> getFeatures()
    {
        return myFeatures;
    }

    /**
     * Find all the locations associated with all my features.
     *
     * @return A set of LatLonAlt locations.
     */
    public Set<LatLonAlt> getLocations()
    {
        Set<LatLonAlt> locations = new HashSet<>();
        for (WPSFeature feature : myFeatures)
        {
            locations.addAll(feature.getLocations());
        }
        return locations;
    }

    /**
     * Standard getter.
     *
     * @return The name value.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Standard getter.
     *
     * @return The time-span value.
     */
    public TimeSpan getTimespan()
    {
        return myTimespan;
    }

    /**
     * Remove a WPSFeature from my collection.
     *
     * @param feature The WPSFeature to remove.
     */
    public void removeFeature(WPSFeature feature)
    {
        if (feature != null)
        {
            myFeatures.remove(feature);
        }
    }

    /**
     * Standard setter.
     *
     * @param name The name value.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Standard setter.
     *
     * @param timespan The time-span value.
     */
    public void setTimespan(TimeSpan timespan)
    {
        myTimespan = timespan;
    }
}
