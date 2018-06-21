package io.opensphere.analysis.table.renderers;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import io.opensphere.core.model.GeographicPositionFormat;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.units.angle.DegreesMinutesSeconds;

/** Lat/Lon table cell renderer. */
public class LatLonTableCellRenderer extends DefaultTableCellRenderer
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The format. */
    private final GeographicPositionFormat myFormat;

    /** The coordinate type, Latitude or Longitude. */
    private final CoordType myType;

    /**
     * Constructor.
     *
     * @param format the format
     * @param type the coordinate type
     */
    public LatLonTableCellRenderer(GeographicPositionFormat format, CoordType type)
    {
        myFormat = format;
        myType = type;

        if (myFormat == GeographicPositionFormat.DMSDEG)
        {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        setVerticalAlignment(DefaultTableCellRenderer.TOP);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        return super.getTableCellRendererComponent(table, formatValue(value), isSelected, hasFocus, row, column);
    }

    /**
     * Calculate the formatted value based on the current format.
     *
     * @param val the value to be formatted
     * @return the formatted value
     */
    private Object formatValue(Object val)
    {
        if (!(val instanceof Double))
        {
            return val;
        }
        Double dbl = (Double)val;
        if (myFormat == GeographicPositionFormat.DMSDEG)
        {
            return DegreesMinutesSeconds.getShortLabelString(dbl.doubleValue(), 12, 0, myType.positive, myType.negative);
        }
        if (myFormat == GeographicPositionFormat.DEG_DMIN)
        {
            return toDdmString(dbl.doubleValue(), 3);
        }
        return val;
    }

    /**
     * Convert the decimal latitude/longitude to degrees plus decimal minutes.
     *
     * @param deg decimal degrees
     * @param roundOff digits after the decimal point for minutes
     * @return the requested String
     */
    private String toDdmString(double deg, int roundOff)
    {
        if (myType == CoordType.LAT)
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
