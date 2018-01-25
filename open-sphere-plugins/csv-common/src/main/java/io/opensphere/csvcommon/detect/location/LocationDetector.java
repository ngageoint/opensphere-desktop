package io.opensphere.csvcommon.detect.location;

import java.util.List;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.CellDetector;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.algorithm.decider.LocationDecider;
import io.opensphere.csvcommon.detect.location.algorithm.decider.LocationDeciderFactory;
import io.opensphere.csvcommon.detect.location.format.LocationFormatDetector;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;

/**
 * The LocationDetector will determine if a header row contains a column
 * identified as a location type. The types include latitude/longitude pairs, a
 * single location/position column, MGRS columns, or WKT columns. The results
 * from each decider are aggregated into a single set of results and returned.
 */
public class LocationDetector implements CellDetector<LocationResults>
{
    /** The preferences registry. */
    private final PreferencesRegistry myPrefsRegistry;

    /**
     * Instantiates a new location detector.
     *
     * @param prefsRegistry the preferences registry
     */
    public LocationDetector(PreferencesRegistry prefsRegistry)
    {
        myPrefsRegistry = prefsRegistry;
    }

    @Override
    public ValuesWithConfidence<LocationResults> detect(CellSampler sampler)
    {
        LocationResults aggregateResults = new LocationResults();
        List<LocationDecider> deciders = LocationDeciderFactory.getInstance().buildDeciders(myPrefsRegistry);
        for (LocationDecider decider : deciders)
        {
            LocationResults results = decider.determineLocationColumns(sampler);
            List<LatLonColumnResults> latLonResults = results.getLatLonResults();
            if (latLonResults != null && !latLonResults.isEmpty())
            {
                for (LatLonColumnResults latLonResult : latLonResults)
                {
                    if (latLonResult.getConfidence() > 0)
                    {
                        aggregateResults.addResult(latLonResult);
                    }
                }
            }

            List<PotentialLocationColumn> locResults = results.getLocationResults();
            if (locResults != null && !locResults.isEmpty())
            {
                for (PotentialLocationColumn locResult : locResults)
                {
                    if (locResult.getConfidence() > 0)
                    {
                        aggregateResults.addResult(locResult);
                    }
                }
            }
        }

        LocationFormatDetector lfd = new LocationFormatDetector();
        lfd.detectLocationColumnFormats(aggregateResults, sampler.getBeginningSampleCells());

        return new ValuesWithConfidence<LocationResults>(aggregateResults, Math.max(0f, aggregateResults.getConfidence()));
    }
}
