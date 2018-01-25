package io.opensphere.core.common.geospatial.conversion;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * This is a partial port of GeoTrans 2.4.2. It allows conversion from an MGRS
 * grid coordinate to a Lon/Lat Point. You can also get lower-left and
 * upper-right lon/lat coordinates for the grid by doing the following:
 *
 * MGRS grid = new MGRS(mgrsString); Point2D lowerLeft =
 * grid.convertToGeodetic(); grid.toMaxEastNorth(); Point2D upperRight =
 * grid.convertToGeodetic();
 */

public class MGRS
{
    /** ARRAY INDEX FOR LETTER A */
    private static final int LETTER_A = 0;

    /** ARRAY INDEX FOR LETTER B */
    private static final int LETTER_B = 1;

    /** ARRAY INDEX FOR LETTER C */
    private static final int LETTER_C = 2;

    /** ARRAY INDEX FOR LETTER D */
    private static final int LETTER_D = 3;

    /** ARRAY INDEX FOR LETTER E */
    private static final int LETTER_E = 4;

    /** ARRAY INDEX FOR LETTER F */
    private static final int LETTER_F = 5;

    /** ARRAY INDEX FOR LETTER G */
    private static final int LETTER_G = 6;

    /** ARRAY INDEX FOR LETTER H */
    private static final int LETTER_H = 7;

    /** ARRAY INDEX FOR LETTER I */
    private static final int LETTER_I = 8;

    /** ARRAY INDEX FOR LETTER J */
    private static final int LETTER_J = 9;

    /** ARRAY INDEX FOR LETTER K */
    private static final int LETTER_K = 10;

    /** ARRAY INDEX FOR LETTER L */
    private static final int LETTER_L = 11;

    /** ARRAY INDEX FOR LETTER M */
    private static final int LETTER_M = 12;

    /** ARRAY INDEX FOR LETTER N */
    private static final int LETTER_N = 13;

    /** ARRAY INDEX FOR LETTER O */
    private static final int LETTER_O = 14;

    /** ARRAY INDEX FOR LETTER P */
    private static final int LETTER_P = 15;

    /** ARRAY INDEX FOR LETTER Q */
    private static final int LETTER_Q = 16;

    /** ARRAY INDEX FOR LETTER R */
    private static final int LETTER_R = 17;

    /** ARRAY INDEX FOR LETTER S */
    private static final int LETTER_S = 18;

    /** ARRAY INDEX FOR LETTER T */
    private static final int LETTER_T = 19;

    /** ARRAY INDEX FOR LETTER U */
    private static final int LETTER_U = 20;

    /** ARRAY INDEX FOR LETTER V */
    private static final int LETTER_V = 21;

    /** ARRAY INDEX FOR LETTER W */
    private static final int LETTER_W = 22;

    /** ARRAY INDEX FOR LETTER X */
    private static final int LETTER_X = 23;

    /** ARRAY INDEX FOR LETTER Y */
    private static final int LETTER_Y = 24;

    /** ARRAY INDEX FOR LETTER Z */
    private static final int LETTER_Z = 25;

// /** NUMBER OF LETTERS IN MGRS              */
//    private static final int MGRS_LETTERS = 3;
    /** ONE HUNDRED THOUSAND */
    private static final double ONEHT = 100000.0;

    /** TWO MILLION */
    private static final double TWOMIL = 2000000.0;

    private static final double LETTER_X_MAX_NORTHING = 9328000.0;

//    private static final int MIN_EASTING = 100000;
//    private static final int MAX_EASTING = 900000;
//    private static final int MIN_NORTHING = 0;
//    private static final int MAX_NORTHING = 10000000;
//   /** Maximum precision of easting & northing */
//    private static final int MAX_PRECISION = 5;
//    private static final double MIN_UTM_LAT = Math.toRadians(-80);
//  /** 84 degrees in radians     */
//    private static final double MAX_UTM_LAT = Math.toRadians(84);
//
//    private static final int MIN_EAST_NORTH = 0;
//    private static final int MAX_EAST_NORTH = 4000000;

    /* Ellipsoid parameters, default to WGS 84 */
    /** Semi-major axis of ellipsoid in meters */
    private static final double MGRS_a = 6378137.0;

    /** Flattening of ellipsoid */
    private static final double MGRS_f = 1 / 298.257223563;

    private static final String MGRS_Ellipsoid_Code = "WE";

    private static final String CLARKE_1866 = "CC";

    private static final String CLARKE_1880 = "CD";

    private static final String BESSEL_1841 = "BR";

    private static final String BESSEL_1841_NAMIBIA = "BN";

    /** latitude band lookup table */
    private static final LatitudeBand[] Latitude_Band_Table = new LatitudeBand[] {
        new LatitudeBand(LETTER_C, 1100000.0, -72.0, -80.5, 0.0), new LatitudeBand(LETTER_D, 2000000.0, -64.0, -72.0, 2000000.0),
        new LatitudeBand(LETTER_E, 2800000.0, -56.0, -64.0, 2000000.0),
        new LatitudeBand(LETTER_F, 3700000.0, -48.0, -56.0, 2000000.0),
        new LatitudeBand(LETTER_G, 4600000.0, -40.0, -48.0, 4000000.0),
        new LatitudeBand(LETTER_H, 5500000.0, -32.0, -40.0, 4000000.0),
        new LatitudeBand(LETTER_J, 6400000.0, -24.0, -32.0, 6000000.0),
        new LatitudeBand(LETTER_K, 7300000.0, -16.0, -24.0, 6000000.0),
        new LatitudeBand(LETTER_L, 8200000.0, -8.0, -16.0, 8000000.0),
        new LatitudeBand(LETTER_M, 9100000.0, 0.0, -8.0, 8000000.0), new LatitudeBand(LETTER_N, 0.0, 8.0, 0.0, 0.0),
        new LatitudeBand(LETTER_P, 800000.0, 16.0, 8.0, 0.0), new LatitudeBand(LETTER_Q, 1700000.0, 24.0, 16.0, 0.0),
        new LatitudeBand(LETTER_R, 2600000.0, 32.0, 24.0, 2000000.0),
        new LatitudeBand(LETTER_S, 3500000.0, 40.0, 32.0, 2000000.0),
        new LatitudeBand(LETTER_T, 4400000.0, 48.0, 40.0, 4000000.0),
        new LatitudeBand(LETTER_U, 5300000.0, 56.0, 48.0, 4000000.0),
        new LatitudeBand(LETTER_V, 6200000.0, 64.0, 56.0, 6000000.0),
        new LatitudeBand(LETTER_W, 7000000.0, 72.0, 64.0, 6000000.0),
        new LatitudeBand(LETTER_X, 7900000.0, 84.5, 72.0, 6000000.0) };

//    private static final UPSConstant[] UPS_Constant_Table = new UPSConstant[] {
//        new UPSConstant(LETTER_A, LETTER_J, LETTER_Z, LETTER_Z, 800000.0, 800000.0),
//        new UPSConstant(LETTER_B, LETTER_A, LETTER_R, LETTER_Z, 2000000.0, 800000.0),
//        new UPSConstant(LETTER_Y, LETTER_J, LETTER_Z, LETTER_P, 800000.0, 1300000.0),
//        new UPSConstant(LETTER_Z, LETTER_A, LETTER_J, LETTER_P, 2000000.0, 1300000.0)};

    private int zone;

    private ArrayList<Integer> letters = new ArrayList<>();

    private double easting = 0;

    private double northing = 0;

    private int precision;

    private double maxEasting = 0;

    private double minEasting = 0;

    private double maxNorthing = 0;

    private double minNorthing = 0;

    private boolean validZone = true;

    public MGRS(String mgrs)
    {
        mgrs = mgrs.replaceAll("\\s", "");
        mgrs = mgrs.trim().toUpperCase();
        // check that the string is a valid MGRS string
        if (mgrs.matches("[\\d]{1,2}?[A-Z]{3,}?[0-9]{0,10}?"))
        {
            String zoneStr = "";
            int i = 0;
            while (i < mgrs.length() && isNumeric(mgrs.charAt(i)))
            {
                zoneStr += mgrs.charAt(i);
                i++;
            }

            // get the zone number
            try
            {
                zone = Integer.parseInt(zoneStr);
            }
            catch (NumberFormatException e)
            {
                validZone = false;
            }

            // add the letter indices

            while (i < mgrs.length() && isAlpha(mgrs.charAt(i)))
            {
                letters.add(mgrs.charAt(i) - 'A');
                i++;
            }

            String rest = mgrs.substring(i);

            if (rest.length() > 0)
            {
                if (rest.length() % 2 == 0)
                {
                    precision = rest.length() / 2;
                    easting = Double.parseDouble(rest.substring(0, precision)) * Math.pow(10, 5 - precision);
                    northing = Double.parseDouble(rest.substring(precision)) * Math.pow(10, 5 - precision);
                }
                else
                {
                    throw new RuntimeException("The MGRS string was not valid");
                }
            }
            else
            {
                precision = 0;
                easting = 0;
                northing = 0;
            }

            double max = Math.pow(10, 5 - precision);
            minEasting = easting;
            minNorthing = northing;
            maxEasting = easting + max;
            maxNorthing = northing + max;
        }
        else
        {
            throw new RuntimeException("The MGRS string was not valid");
        }
    }

    /**
     * Converts the MGRS grid coordinate to a (Lon,Lat) point that represents
     * the lower-left corner of the grid. To get the upper-right coordinate, use
     * the toMaxEastNorth() method and then call this method.
     *
     * @returns java.awt.Point2D representing Lon, Lat in degrees
     */
    public Point2D convertToGeodetic()
    {
        if (validZone)
        {
            UTM utm = convertToUTM();
            utm.setParameters(MGRS_a, MGRS_f);

            return utm.convertToLonLat();
            // add the maximum easting and northing for the given precision

        }
        else
        {
            // We aren't supporting UPS coordinates yet
//            UPS ups = convertMGRSToUPS(MGRS);
//            ups.setParameters(MGRS_a, MGRS_f);
//            return ups.convertToGeodetic();
            throw new RuntimeException("Error: UPS conversion not implemented.");
        }
    }

    public void toUpperLeft()
    {
        easting = minEasting;
        northing = maxNorthing;
    }

    public void toUpperRight()
    {
        easting = maxEasting;
        northing = maxNorthing;
    }

    public void toLowerRight()
    {
        easting = maxEasting;
        northing = minNorthing;
    }

    public void toLowerLeft()
    {
        easting = minEasting;
        northing = minNorthing;
    }

    /**
     * Calculates the center point of this MGRS grid/cell
     *
     * @return Point2D representing the center point of this MGRS grid/cell
     */
    public Point2D getCenterPoint()
    {
        // Get opposite corners
        toLowerLeft();
        Point2D lowerLeft = convertToGeodetic();
        toUpperRight();
        Point2D upperRight = convertToGeodetic();

        // Now find the center point of the box
        double lonDist = upperRight.getX() - lowerLeft.getX();
        double latDist = upperRight.getY() - lowerLeft.getY();

        double centerLon = lowerLeft.getX() + lonDist / 2;
        double centerLat = lowerLeft.getY() + latDist / 2;

        return new Point2D.Double(centerLon, centerLat);
    }

    /**
     * Converts this MGRS to UTM
     *
     * @return UTM
     */
    public UTM convertToUTM()
    {
        char hemisphere = '1';

        if (letters.get(0) == LETTER_X && (zone == 32 || zone == 34 || zone == 36))
        {
            throw new RuntimeException("error");
        }
        else
        {
            if (letters.get(0) < LETTER_N)
            {
                hemisphere = 'S';
            }
            else
            {
                hemisphere = 'N';
            }
        }

        int ltr2_low_value = 0, ltr2_high_value = 0;
        double patternOffset;

        int number = zone % 6;

        // false northing on appears to be correct
        boolean falseNorthing = true;

        if (number == 1 || number == 4)
        {
            ltr2_low_value = LETTER_A;
            ltr2_high_value = LETTER_H;
        }
        else if (number == 2 || number == 5)
        {
            ltr2_low_value = LETTER_J;
            ltr2_high_value = LETTER_R;
        }
        else if (number == 3 || number == 0)
        {
            ltr2_low_value = LETTER_S;
            ltr2_high_value = LETTER_Z;
        }

        /* False northing at A for second letter of grid square */
        if (falseNorthing)
        {
            if (number % 2 == 0)
            {
                patternOffset = 500000.0;
            }
            else
            {
                patternOffset = 0.0;
            }
        }
        else
        {
            if (number % 2 == 0)
            {
                patternOffset = 1500000.0;
            }
            else
            {
                patternOffset = 1000000.00;
            }
        }

        /* Check that the second letter of the MGRS string is within the range
         * of valid second letter values. Also check that the third letter is
         * valid */

        if (letters.get(1) < ltr2_low_value || letters.get(1) > ltr2_high_value || letters.get(2) > LETTER_V)
        {
            throw new RuntimeException("Letter2: " + letters.get(1) + " Range: " + ltr2_low_value + " - " + ltr2_high_value
                    + " Letter3: " + letters.get(2) + " Error: Second letter is not in valid range.");
        }

        double row_letter_northing = (double)letters.get(2) * ONEHT;
        double grid_easting = (letters.get(1) - ltr2_low_value + 1) * ONEHT;

        if (ltr2_low_value == LETTER_J && letters.get(1) > LETTER_O)
        {
            grid_easting = grid_easting - ONEHT;
        }

        if (letters.get(2) > LETTER_O)
        {
            row_letter_northing = row_letter_northing - ONEHT;
        }

        if (letters.get(2) > LETTER_I)
        {
            row_letter_northing = row_letter_northing - ONEHT;
        }

        if (row_letter_northing >= TWOMIL)
        {
            row_letter_northing -= TWOMIL;
        }

        LatitudeBand band = getLatitudeBand(letters.get(0));
        double grid_northing = row_letter_northing - patternOffset;
        if (grid_northing < 0)
        {
            grid_northing += TWOMIL;
        }

        grid_northing += band.northingOffset;

        if (grid_northing < band.minNorthing)
        {
            grid_northing += TWOMIL;
        }

        double utmEasting = grid_easting + easting;
        double utmNorthing = grid_northing + northing;

        return new UTM(zone, hemisphere, utmEasting, utmNorthing);
    }

    private static LatitudeBand getLatitudeBand(int letter)
    {
        LatitudeBand value = null;

        if (letter >= LETTER_C && letter <= LETTER_H)
        {
            value = Latitude_Band_Table[letter - 2];
        }
        else if (letter >= LETTER_J && letter <= LETTER_N)
        {
            value = Latitude_Band_Table[letter - 3];
        }
        else if (letter >= LETTER_P && letter <= LETTER_X)
        {
            value = Latitude_Band_Table[letter - 4];
        }

        return value;
    }

    private static boolean isAlpha(char c)
    {
        return c >= 'A' && c <= 'Z';
    }

    private static boolean isNumeric(char c)
    {
        return c >= '0' && c <= '9';
    }

    @Override
    public String toString()
    {
        String l = "";
        for (int i : letters)
        {
            l += i + ", ";
        }
        return "MGRS{zone: " + zone + " letters: " + l + " easting: " + easting + " northing: " + northing + " precision: "
                + precision + "}";
    }

    /**
     * Utility function to get the center point of an MGRS location
     *
     * @param mgrsValue - the MGRS string
     * @return the Lat/Lon in a Point2D
     */
    public static Point2D computeCenterLatLon(String mgrsValue)
    {
        MGRS mgrs = new MGRS(mgrsValue);

        double minLat = mgrs.convertToGeodetic().getY();
        double minLon = mgrs.convertToGeodetic().getX();

        mgrs.toUpperRight();

        double maxLat = mgrs.convertToGeodetic().getY();
        double maxLon = mgrs.convertToGeodetic().getX();

        double ctrLat = (maxLat + minLat) / 2;
        double ctrLon = (maxLon + minLon) / 2;

        return new Point2D.Double(ctrLat, ctrLon);
    }

    /**
     * A simple application that converts MGRS values on the command-line to the
     * latitude and longitude of the center point.
     *
     * @param args the command-line arguments.
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Converts MGRS to the center point.");
            System.out.println(MGRS.class.getName() + " <mgrs1> [<mgrs2> ...]");
            System.exit(1);
        }

        for (String arg : args)
        {
            System.out.println(arg + ": " + computeCenterLatLon(arg));
        }
    }
}
