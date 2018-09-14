package io.opensphere.server.customization;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.DurationUnitsProvider;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.lang.StringUtilities;
import net.opengis.wfs._110.WFSCapabilitiesType;

/**
 * Default implementation of {@link ServerCustomization}.
 */
public class DefaultCustomization implements ServerCustomization
{
    /** Default date format string. */
    private static String ourDefaultDateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /** The server type. */
    private final String myServerType;

    /**
     * Get a value in the largest unit type that can represent it as a whole
     * number, including converting 28-31 days to 1 month.
     *
     * @param input The input duration.
     * @return The duration converted to the largest whole unit type.
     */
    public static Duration getLargestIntegerUnitType(Duration input)
    {
        Duration dur = new DurationUnitsProvider().getLargestIntegerUnitType(input);
        if (!dur.isOne() && dur.compareTo(new Days(28)) >= 0 && dur.compareTo(new Days(31)) <= 0)
        {
            dur = Months.ONE;
        }
        return dur;
    }

    /**
     * Constructor.
     */
    public DefaultCustomization()
    {
        this(ServerCustomization.DEFAULT_TYPE);
    }

    /**
     * Constructor.
     *
     * @param serverType the server type
     */
    public DefaultCustomization(String serverType)
    {
        myServerType = serverType;
    }

    /**
     * Default server implementation of WMS time should return the ISO-8601
     * formatted time, truncated to the highest order of precision. For example,
     * the month of April, 2005 should be represented as "2005-04." Week
     * durations will have to be represented as the full duration with start and
     * stop time separated by a "/".
     *
     * This method will have to be overridden for servers that don't support the
     * time format truncation or require the fully qualified start/stop
     * duration.
     *
     * {@inheritDoc}
     */
    @Override
    public String getFormattedWMSTime(TimeSpan span)
    {
        Duration dur = getLargestIntegerUnitType(span.getDuration());
        if (!dur.isOne())
        {
            throw new IllegalArgumentException("Non-unit durations are not supported: " + dur);
        }

        SimpleDateFormat format = getFormat(dur.getClass());
        return StringUtilities.concat(format.format(span.getStartDate()), "/", format.format(span.getEndDate()));
    }

    @Override
    public LatLonOrder getLatLonOrder(WFSCapabilitiesType wfsCap)
    {
        return LatLonOrder.LONLAT;
    }

    @Override
    public String getServerType()
    {
        return myServerType;
    }

    @Override
    public String getSrsName()
    {
        return "EPSG:4326";
    }

    /**
     * Gets the default format for SimpleDateFormats.
     *
     * @return the default date format
     */
    protected String getDefaultDateFormat()
    {
        return ourDefaultDateFormat;
    }

    /**
     * Get the formatter for the given duration type.
     *
     * @param durationType The duration type.
     * @return The formatter.
     */
    protected SimpleDateFormat getFormat(Class<? extends Duration> durationType)
    {
        SimpleDateFormat format;
        if (durationType.equals(Months.class))
        {
            format = new SimpleDateFormat("yyyy-MM");
        }
        else if (durationType.equals(Days.class) || durationType.equals(Weeks.class))
        {
            format = new SimpleDateFormat("yyyy-MM-dd");
        }
        else
        {
            format = new SimpleDateFormat(ourDefaultDateFormat);
        }
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        return format;
    }
}
