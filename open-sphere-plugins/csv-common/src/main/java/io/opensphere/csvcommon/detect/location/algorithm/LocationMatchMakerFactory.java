package io.opensphere.csvcommon.detect.location.algorithm;

import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * A factory for creating LocationMatchMaker objects.
 */
public final class LocationMatchMakerFactory
{
    /** The Constant ourInstance. */
    private static final LocationMatchMakerFactory ourInstance = new LocationMatchMakerFactory();

    /** Not constructible. */
    private LocationMatchMakerFactory()
    {
    }

    /**
     * Gets the single instance of LocationMatchMakerFactory.
     *
     * @return single instance of LocationMatchMakerFactory
     */
    public static LocationMatchMakerFactory getInstance()
    {
        return ourInstance;
    }

    /**
     * Builds the match makers.
     *
     * @return the list of match makers
     */
    public List<LocationMatchMaker> buildMatchMakers()
    {
        return New.list(new DecimalLatLonMatchMaker(), new DMSMatchMaker(), new PositionMatchMaker(), new MGRSMatchMaker(),
                new WktMatchMaker());
    }
}
