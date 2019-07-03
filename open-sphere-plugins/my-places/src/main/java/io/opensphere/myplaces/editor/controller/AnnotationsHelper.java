package io.opensphere.myplaces.editor.controller;

import java.text.DecimalFormat;
import java.util.Collection;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.viewer.impl.ViewerAnimator;

/**
 * A helper class to do some of the work for the PointPanelController (and
 * lessen it's coupling).
 */
public class AnnotationsHelper
{
    /** Logger used. */
    private static final Logger LOGGER = Logger.getLogger(AnnotationsHelper.class);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The decimal formatter. */
    private static final DecimalFormat ourDecimalFormat = new DecimalFormat("##0.##########");

    /** A listener for changes to the preferred units. */
    private final UnitsChangeListener<Length> myUnitsChangeListener = new UnitsChangeListener<Length>()
    {

        @Override
        public void preferredUnitsChanged(Class<? extends Length> type)
        {
            myPreferredUnits = type;
        }
    };

    /** The currently preferred units. */
    private volatile Class<? extends Length> myPreferredUnits;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public AnnotationsHelper(Toolbox toolbox)
    {
        myToolbox = toolbox;
        UnitsProvider<Length> unitsProvider = myToolbox.getUnitsRegistry().getUnitsProvider(Length.class);
        unitsProvider.addListener(myUnitsChangeListener);
        myPreferredUnits = unitsProvider.getPreferredUnits();
    }

    /**
     * Create a MGRS string for the given position.
     *
     * @param geoPos The geographic position.
     * @return A MGRS string or null if the latitude is out of range (near one
     *         of the poles).
     */
    public String findMGRS(GeographicPosition geoPos)
    {
        MGRSConverter converter = new MGRSConverter();
        UTM utmCoords = new UTM(geoPos);
        return converter.createString(utmCoords);
    }

    /**
     * Create a string representation of altitude with the current units.
     *
     * @param alt The altitude (in meters) to convert (if necessary) and display
     *            with current units.
     * @return The formatted altitude (i.e. "124.12345 km" or "12.345 mi").
     */
    public String formatAltitude(double alt)
    {
        Length elevation = new Meters(alt);
        if (!myPreferredUnits.isInstance(elevation))
        {
            elevation = Length.create(myPreferredUnits, elevation);
        }
        return StringUtilities.concat(formatDecimal(elevation.getMagnitude(), ".#####"), " ", elevation.getShortLabel(false));
    }

    /**
     * Format the given double value according to our desired decimal format.
     *
     * @param value The double value.
     * @return The string representation of the formatted double.
     */
    public String formatDecimal(double value)
    {
        return ourDecimalFormat.format(value);
    }

    /**
     * Format a decimal to the given precision.
     *
     * @param value the value
     * @param precision the precision
     * @return the string
     */
    public String formatDecimal(double value, int precision)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("##0.");
        for (int i = 0; i < precision; i++)
        {
            sb.append('#');
        }
        DecimalFormat df = new DecimalFormat(sb.toString());
        return df.format(value);
    }

    /**
     * Format the given double value with a custom decimal format.
     *
     * @param value The double value.
     * @param customFormat The custom decimal format as string. An example would
     *            be "##.###".
     * @return The string representation of the formatted double.
     */
    public String formatDecimal(double value, String customFormat)
    {
        String str = String.valueOf(value);
        try
        {
            DecimalFormat decFormat = new DecimalFormat(customFormat);
            str = decFormat.format(value);
        }
        catch (RuntimeException e)
        {
            LOGGER.error(e.getMessage());
            return str;
        }
        return str;
    }

    /**
     * Find the altitude of viewer and format into a string with units.
     *
     * @return The formatted altitude ("124.12345 km" for example).
     */
    public String getAltitude()
    {
        return formatAltitude(myToolbox.getMapManager().getStandardViewer().getAltitude());
    }

    /**
     * Return the meters for the given length string.
     *
     * @param lengthStr The string representation of length that contains
     *            magnitude and units. Such as "123.45 m" or "25.2 km" for
     *            example.
     * @return The corresponding length in meters.
     */
    public double getLengthMeters(String lengthStr)
    {
        try
        {
            Length length = myToolbox.getUnitsRegistry().fromShortLabelString(Length.class, lengthStr);
            return length.inMeters();
        }
        catch (RuntimeException e)
        {
            return 100000;
        }
    }

    /**
     * Goto location.
     *
     * @param lat the latitude
     * @param lon the longitude
     * @param alt the altitude
     */
    public void gotoLocation(double lat, double lon, double alt)
    {
        ViewerAnimator viewerAnimator = new ViewerAnimator(myToolbox.getMapManager().getStandardViewer(),
                new GeographicPosition(LatLonAlt.createFromDegreesMeters(lat, lon, alt, Altitude.ReferenceLevel.ELLIPSOID)));
        viewerAnimator.snapToPosition();
    }

    /**
     * Move to the specified location on the globe. If the degree symbol is
     * present at the end of the lat/lon values it will be stripped off.
     *
     * @param lat the latitude
     * @param lon the longitude
     * @param fps The frames per second pan speed.
     */
    public void panToLocation(double lat, double lon, Integer fps)
    {
        try
        {
            Vector3d centerPoint = myToolbox.getMapManager().getProjection()
                    .convertToModel(new GeographicPosition(LatLonAlt.createFromDegrees(lat, lon)), Vector3d.ORIGIN);

            // Or can animate and fly to the model coords location.
            ViewerAnimator viewerAnimator = new ViewerAnimator(myToolbox.getMapManager().getStandardViewer(), centerPoint);
            viewerAnimator.start(fps.intValue());
        }
        catch (NumberFormatException nfe)
        {
            LOGGER.warn("Unable to parse lat/lon values: " + nfe.getMessage());
        }
    }
}
