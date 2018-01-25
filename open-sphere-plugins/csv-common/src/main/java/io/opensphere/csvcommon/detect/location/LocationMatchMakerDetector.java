package io.opensphere.csvcommon.detect.location;

import java.util.List;

import io.opensphere.core.util.MathUtil;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.CellDetector;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.algorithm.LocationMatchMaker;
import io.opensphere.csvcommon.detect.location.algorithm.LocationMatchMakerFactory;
import io.opensphere.csvcommon.detect.location.format.LocationFormatDetector;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;

/**
 * The LocationMatchMakerDetector will create available match makers and attempt
 * to detect location data without knowing the header names.
 */
public class LocationMatchMakerDetector implements CellDetector<LocationResults>
{
    @Override
    public ValuesWithConfidence<LocationResults> detect(CellSampler sampler)
    {
        LocationResults aggregateResults = new LocationResults();
        List<LocationMatchMaker> matchMakers = LocationMatchMakerFactory.getInstance().buildMatchMakers();
        for (LocationMatchMaker mm : matchMakers)
        {
            ValuesWithConfidence<LocationResults> results = mm.detect(sampler);
            LocationResults locRes = results.getBestValue();
            List<LatLonColumnResults> llColRes = locRes.getLatLonResults();
            List<PotentialLocationColumn> locationRes = locRes.getLocationResults();

            if (llColRes != null && !llColRes.isEmpty())
            {
                for (LatLonColumnResults latLonRes : llColRes)
                {
                    aggregateResults.addResult(latLonRes);
                }
            }

            if (locationRes != null && !locationRes.isEmpty())
            {
                for (PotentialLocationColumn plc : locationRes)
                {
                    aggregateResults.addResult(plc);
                }
            }
        }

        LocationFormatDetector lfd = new LocationFormatDetector();
        lfd.detectLocationColumnFormats(aggregateResults, sampler.getBeginningSampleCells());

        return new ValuesWithConfidence<LocationResults>(aggregateResults,
                MathUtil.clamp(aggregateResults.getConfidence(), 0f, 1f));
    }
}
