package io.opensphere.csvcommon.format.position;

import java.text.ParseException;
import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.units.angle.DegreesMinutesSeconds;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.LocationDetector;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;

/** Formats position values which contain both latitude and longitude values. */
public class PositionFormatter extends BaseLocationFormatter
{
    /** The preferences registry. */
    private final PreferencesRegistry myRegistry;

    /**
     * Constructs a new latitude formatter.
     *
     * @param registry The preferences registry.
     */
    public PositionFormatter(PreferencesRegistry registry)
    {
        myRegistry = registry;
    }

    @Override
    public Object formatCell(String cellValue, String fmtString) throws ParseException
    {
        LatLonAlt value = LatLonAlt.parse(cellValue);
        if (value == null)
        {
            return null;
        }

        CoordFormat fmt = CoordFormat.fromString(fmtString);
        double latitude = value.getLatD();
        double longitude = value.getLonD();
        if (fmt == CoordFormat.DMS)
        {
            return degToDms(latitude, 'N', 'S') + " " + degToDms(longitude, 'E', 'W');
        }
        else if (fmt == CoordFormat.DDM)
        {
            return LatLonAlt.latToDdmString(latitude, 3) + " "
                    + LatLonAlt.lonToDdmString(longitude, 3);
        }
        else
        {
            return latitude + " " + longitude;
        }
    }

    /**
     * Convenience method for converting to DMS format.
     * @param deg double degrees
     * @param pos positive indicator
     * @param neg negative indicator
     * @return the formatted String
     */
    private static String degToDms(double deg, char pos, char neg)
    {
        return DegreesMinutesSeconds.getShortLabelString(deg, 16, 3, pos, neg);
    }

    @Override
    public String getFormat(List<String> values)
    {
        PositionCellSampler sampler = new PositionCellSampler(values);
        LocationDetector detector = new LocationDetector(myRegistry);

        ValuesWithConfidence<LocationResults> valueWithConf = detector.detect(sampler);
        LocationResults results = valueWithConf.getBestValue();
        PotentialLocationColumn column = results == null ? null :  results.getMostLikelyLocationColumn();
        CoordFormat fmt = column == null ? null :  column.getLatFormat();
        if (fmt != null)
        {
            return fmt.toString();
        }
        return null;
    }
}
