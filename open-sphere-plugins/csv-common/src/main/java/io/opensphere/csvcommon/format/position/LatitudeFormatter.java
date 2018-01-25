package io.opensphere.csvcommon.format.position;

import java.text.ParseException;
import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.model.LatLonAltParser;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.units.angle.DegreesMinutesSeconds;
import io.opensphere.csvcommon.common.Constants;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.LocationDetector;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;

/** The formatter used for to format latitude values. */
public class LatitudeFormatter extends BaseLocationFormatter
{
    /** The preferences registry. */
    private final PreferencesRegistry myRegistry;

    /**
     * Constructs a new latitude formatter.
     *
     * @param registry The preferences registry.
     */
    public LatitudeFormatter(PreferencesRegistry registry)
    {
        myRegistry = registry;
    }

    @Override
    public Object formatCell(String val, String fmtString) throws ParseException
    {
        if (val == null || val.isEmpty())
        {
            return null;
        }

        CoordFormat fmt = CoordFormat.fromString(fmtString);
        double latitude = LatLonAltParser.parseLat(val, fmt);
        if (Double.isNaN(latitude))
        {
            return Constants.ERROR_LABEL;
        }
        if (fmt == CoordFormat.DECIMAL)
        {
            return String.valueOf(latitude);
        }
        if (fmt == CoordFormat.DDM)
        {
            return LatLonAlt.latToDdmString(latitude, 3);
        }
        return DegreesMinutesSeconds.getShortLabelString(latitude, 16, 3, 'N', 'S');
    }

    @Override
    public String getFormat(List<String> values)
    {
        LatitudeCellSampler sampler = new LatitudeCellSampler(values);
        LocationDetector detector = new LocationDetector(myRegistry);

        ValuesWithConfidence<LocationResults> valueWithConf = detector.detect(sampler);
        LocationResults results = valueWithConf.getBestValue();
        LatLonColumnResults result = results == null ? null : results.getMostLikelyLatLonColumnPair();
        PotentialLocationColumn column = result == null ? null : result.getLatColumn();

        if (column == null)
        {
            return super.getFormat(values);
        }

        CoordFormat fmt = column.getLatFormat();
        if (fmt != null)
        {
            return fmt.toString();
        }
        return null;
    }
}
