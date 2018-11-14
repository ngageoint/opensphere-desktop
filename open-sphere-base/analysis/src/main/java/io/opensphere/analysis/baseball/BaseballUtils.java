package io.opensphere.analysis.baseball;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.MGRSUtil;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.GeographicPositionFormat;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.units.angle.DegreesMinutesSeconds;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.util.TimeSpanUtility;

public class BaseballUtils
{

    /**
     * Calculate the formatted value based on the current format.
     *
     * @param value the value to be formatted
     * @param format the type of the format
     * @param type the coordtype
     * @return the formatted value
     */
    public static String formatCoordinate(Double value, GeographicPositionFormat format, CoordType type)
    {
        if (format == GeographicPositionFormat.DMSDEG)
        {
            return DegreesMinutesSeconds.getShortLabelString(value.doubleValue(), 12, 0, type.positive, type.negative);
        }
        else if (format == GeographicPositionFormat.DEG_DMIN)
        {
            return toDdmString(value.doubleValue(), 3, type);
        }
        return value.toString();
    }

    public static String formatMGRS(DataElement dataElement)
    {
        MapGeometrySupport geometrySupport = ((MapDataElement)dataElement).getMapGeometrySupport();
        MGRSConverter converter = new MGRSConverter();
        String mgrs = "";
        if (geometrySupport instanceof MapLocationGeometrySupport)
        {
            MapLocationGeometrySupport locationSupport = (MapLocationGeometrySupport)geometrySupport;
            LatLonAlt location = locationSupport.getLocation();
            UTM utmCoords = new UTM(new GeographicPosition(location));
            mgrs = converter.createString(utmCoords);
//            if (myMGRSPrecision != 10)
//            {
//                value = MGRSUtil.reducePrecision(value, myMGRSPrecision);
//            }
        }
        return mgrs;
    }

    public static String formatNumber(Number value)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setGroupingUsed(false);
        formatter.setMaximumFractionDigits(9);
        return formatter.format(value);
    }

    public static String formatDate(Date value, int precision)
    {
    	SimpleDateFormat formatter = ListToolPreferences.getSimpleDateFormatForPrecision(precision);
    	return formatter.format(value);
    }

    public static String formatTimeSpan(TimeSpan value, int precision)
    {
    	SimpleDateFormat formatter = ListToolPreferences.getSimpleDateFormatForPrecision(precision);
    	return TimeSpanUtility.formatTimeSpan(formatter, value);
    }

    /**
     * Convert the decimal latitude/longitude to degrees plus decimal minutes.
     *
     * @param deg decimal degrees
     * @param roundOff digits after the decimal point for minutes
     * @return the requested String
     */
    private static String toDdmString(double deg, int roundOff, CoordType type)
    {
        if (type == CoordType.LAT)
        {
            return LatLonAlt.latToDdmString(deg, roundOff);
        }
        else
        {
            return LatLonAlt.lonToDdmString(deg, roundOff);
        }
    }

    /** Coordinate type. */
    public enum CoordType
    {
        /** Latitude. */
        LAT('N', 'S'),

        /** Longitude. */
        LON('W', 'E');

        /** Positive direction. */
        private char positive;

        /** Negative direction. */
        private char negative;

        /**
         * Constructs coordinate type.
         *
         * @param positive the cardinal direction a positive value represents
         * @param negative the cardinal direction a negative value represents
         */
        private CoordType(char positive, char negative)
        {
            this.positive = positive;
            this.negative = negative;
        }
    }
}
