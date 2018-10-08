package io.opensphere.core.mgrs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.mgrs.UTM.Hemisphere;
import io.opensphere.core.model.GeographicPosition;

/**
 * This class handles conversions of MGRS strings to lat/lon point and vice
 * versa.
 */
@SuppressWarnings("PMD.GodClass")
public class MGRSConverter
{
    /** Used to determine letter from an index (and vice versa). */
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** ARRAY INDEX FOR LETTER A. */
    public static final int LETTER_A = 0;

    /** ARRAY INDEX FOR LETTER B. */
    public static final int LETTER_B = 1;

    /** ARRAY INDEX FOR LETTER C. */
    public static final int LETTER_C = 2;

    /** ARRAY INDEX FOR LETTER D. */
    public static final int LETTER_D = 3;

    /** ARRAY INDEX FOR LETTER E. */
    public static final int LETTER_E = 4;

    /** ARRAY INDEX FOR LETTER F. */
    public static final int LETTER_F = 5;

    /** ARRAY INDEX FOR LETTER G. */
    public static final int LETTER_G = 6;

    /** ARRAY INDEX FOR LETTER H. */
    public static final int LETTER_H = 7;

    /** ARRAY INDEX FOR LETTER I. */
    public static final int LETTER_I = 8;

    /** ARRAY INDEX FOR LETTER J. */
    public static final int LETTER_J = 9;

    /** ARRAY INDEX FOR LETTER K. */
    public static final int LETTER_K = 10;

    /** ARRAY INDEX FOR LETTER L. */
    public static final int LETTER_L = 11;

    /** ARRAY INDEX FOR LETTER M. */
    public static final int LETTER_M = 12;

    /** ARRAY INDEX FOR LETTER N. */
    public static final int LETTER_N = 13;

    /** ARRAY INDEX FOR LETTER O. */
    public static final int LETTER_O = 14;

    /** ARRAY INDEX FOR LETTER P. */
    public static final int LETTER_P = 15;

    /** ARRAY INDEX FOR LETTER Q. */
    public static final int LETTER_Q = 16;

    /** ARRAY INDEX FOR LETTER R. */
    public static final int LETTER_R = 17;

    /** ARRAY INDEX FOR LETTER S. */
    public static final int LETTER_S = 18;

    /** ARRAY INDEX FOR LETTER T. */
    public static final int LETTER_T = 19;

    /** ARRAY INDEX FOR LETTER U. */
    public static final int LETTER_U = 20;

    /** ARRAY INDEX FOR LETTER V. */
    public static final int LETTER_V = 21;

    /** ARRAY INDEX FOR LETTER W. */
    public static final int LETTER_W = 22;

    /** ARRAY INDEX FOR LETTER X. */
    public static final int LETTER_X = 23;

    /** ARRAY INDEX FOR LETTER Y. */
    public static final int LETTER_Y = 24;

    /** ARRAY INDEX FOR LETTER Z. */
    public static final int LETTER_Z = 25;

    /** The lower latitude limit for MGRS zones. */
    public static final double LOWER_LAT_LIMIT = -80.5;

    /** One hundred thousand. */
    public static final double ONEHT = 100000.0;

    /** Two million. */
    public static final double TWOMIL = 2000000.0;

    /** The upper latitude limit for MGRS zones. */
    public static final double UPPER_LAT_LIMIT = 84.0;

    /** Logger used. */
    private static final Logger LOGGER = Logger.getLogger(MGRSConverter.class);

    /** Collection of latitude bands and their properties. */
    private final List<LatitudeBandData> myLatitudeBands = new ArrayList<>();

    /**
     * Default constructor.
     */
    public MGRSConverter()
    {
        init();
    }

    /**
     * Construct a MGRS string from the given parameters.
     *
     * @param zone The int zone.
     * @param latBand The char latitude band.
     * @param square1 The char square1 label.
     * @param square2 The char square2 label.
     * @param precision The int describing precision (1 - 5).
     * @param easting The easting.
     * @param northing The northing.
     * @return String A MGRS string.
     */
    public String constructMGRSString(int zone, char latBand, char square1, char square2, int precision, int easting,
            int northing)
    {
        StringBuilder str = new StringBuilder();

        int divisor = (int)Math.pow(10, 5 - precision);
        int modifiedEasting = easting / divisor;
        int modifiedNorthing = northing / divisor;

        StringBuilder strEasting = new StringBuilder(Integer.toString(modifiedEasting));
        StringBuilder strNorthing = new StringBuilder(Integer.toString(modifiedNorthing));

        if (strEasting.length() > precision)
        {
            strEasting.delete(precision, strEasting.length());
        }
        else if (strEasting.length() < precision)
        {
            StringBuilder buff = new StringBuilder();
            for (int i = 0; i < precision - strEasting.length(); ++i)
            {
                buff.append('0');
            }
            strEasting.insert(0, buff);
        }

        if (strNorthing.length() > precision)
        {
            strNorthing.delete(precision, strNorthing.length());
        }
        else if (strNorthing.length() < precision)
        {
            StringBuilder buff = new StringBuilder();
            for (int i = 0; i < precision - strNorthing.length(); ++i)
            {
                buff.append('0');
            }
            strNorthing.insert(0, buff);
        }

        // If single digit zone, add a '0' to the beginning.
        if (String.valueOf(zone).length() == 1)
        {
            str.append('0');
        }
        str.append(zone);
        str.append(latBand);
        str.append(square1);
        str.append(square2);
        str.append(strEasting);
        str.append(strNorthing);

        return str.toString();
    }

    /**
     * Given a MGRS string, convert to geographic position.
     *
     * @param mgrsString The MGRS formatted string.
     * @return The GeographicPosition that contains lat/lon.
     */
    public GeographicPosition convertToLatLon(String mgrsString)
    {
        UTM utm = convertToUTM(mgrsString);
        if (utm != null)
        {
            return utm.convertToLatLon();
        }
        return null;
    }

    /**
     * Parse the MGRS string.
     *
     * @param input The MGRS string.
     * @return UTM position.
     */
    public UTM convertToUTM(String input)
    {
        String mgrsString = input.replaceAll("\\s+", "");
        // Only a valid mgrs string if we have an odd number in the range of 3
        // to 15
        if (!validateMGRSString(mgrsString))
        {
            return null;
        }

        try
        {
            int zone = Integer.parseInt(mgrsString.substring(0, 2));
            char latitudeBand = mgrsString.charAt(2);

            Character squareLetter1 = null;
            Character squareLetter2 = null;
            String remainingDigits = null;

            if (mgrsString.length() > 4)
            {
                squareLetter1 = Character.valueOf(mgrsString.charAt(3));
                squareLetter2 = Character.valueOf(mgrsString.charAt(4));

                remainingDigits = mgrsString.substring(5);
            }

            // Default values of 0 will be lower left. Values of 50000 will be
            // in middle (which could put us outside of UTM zone when we are
            // clipped on the border).
            int easting = 50000;
            int northing = 50000;

            // Check to see we have northing/easting values. If not use default
            // values.
            if (remainingDigits != null && !remainingDigits.isEmpty())
            {
                int precision = remainingDigits.length() / 2;

                // easting and northing in (meters). Precision 1 = 10km, 2 =
                // 1km, 3 = 100m, 4 = 10m, 5 = 1m
                easting = Integer.parseInt(remainingDigits.substring(0, precision)) * (int)Math.pow(10, 5 - precision);
                northing = Integer.parseInt(remainingDigits.substring(precision, remainingDigits.length()))
                        * (int)Math.pow(10, 5 - precision);
            }

            // Now convert to UTM
            // Check for special zones that shouldn't exist.
            if (latitudeBand == 'X' && (zone == 32 || zone == 34 || zone == 36))
            {
                LOGGER.error("Unable to process zone X" + zone + " (shouldn't exist).");
                return null;
            }

            // Now find the letter range for mgrs grid letter.
            int sqLetter1LowValue = findLowValueIndex(zone);
            int sqLetter1HighValue = findHighValueIndex(zone);

            // Find false northing at A for second letter of grid square
            double falseNorthing = 0.;
            if (zone % 2 == 0)
            {
                final double onePointFiveMil = 1500000.0;
                falseNorthing = onePointFiveMil;
            }

            if (squareLetter1 != null && squareLetter2 != null
                    && (ALPHABET.indexOf(squareLetter1.charValue()) < sqLetter1LowValue
                            || ALPHABET.indexOf(squareLetter1.charValue()) > sqLetter1HighValue
                            || ALPHABET.indexOf(squareLetter2.charValue()) > LETTER_V))
            {
                LOGGER.error("Out of Range Error - Square Letter 1: " + squareLetter1 + " Range: " + sqLetter1LowValue + " - "
                        + sqLetter1HighValue);
                LOGGER.error("                     Square Letter 2: " + squareLetter2);
                return null;
            }

            LatitudeBandData bandData = getLatitudeBand(ALPHABET.indexOf(latitudeBand));
            if (bandData == null)
            {
                return null;
            }

            // Determine the hemisphere
            Hemisphere hemisphere = determineHemisphere(latitudeBand);

            // Find grid easting and grid northing values
            double gridEasting = findGridEasting(squareLetter1, squareLetter2, sqLetter1LowValue, sqLetter1HighValue);
            double gridNorthing = findGridNorthing(squareLetter1, squareLetter2, bandData, falseNorthing);

            double utmEasting = gridEasting + easting;
            double utmNorthing = gridNorthing + northing;

            return new UTM(zone, hemisphere, utmEasting, utmNorthing);
        }
        catch (NumberFormatException nfe)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Exception : " + nfe.getMessage());
            }
            return null;
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Exception : " + ioobe.getMessage());
            }
            return null;
        }
    }

    /**
     * Test that an MGRS string is the right length.
     *
     * @param mgrsString The MGRS string.
     * @return {@code true} if the string is valid.
     */
    private boolean validateMGRSString(String mgrsString)
    {
        if (mgrsString != null && mgrsString.length() % 2 != 0 && mgrsString.length() >= 3 && mgrsString.length() <= 15)
        {
            return true;
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("MGRS string format incorrect, unable to parse.");
        }
        return false;
    }

    /**
     * Creates the string with the default precision.
     *
     * @param utm the utm
     * @return the string
     */
    public String createString(UTM utm)
    {
        return createString(utm, 5);
    }

    /**
     * Converts UTM position into a MGRS string.
     *
     * @param utm The UTM position to use.
     * @param precision the precision for the Grid
     * @return The MGRS formatted string.
     */
    public String createString(UTM utm, int precision)
    {
        GeographicPosition geoPos = utm.convertToLatLon();

        char latBand = getLatBandDesignator(geoPos.getLatLonAlt().getLatD());
        if (latBand == ' ')
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Unable to construct MGRS string: latitude band out of range");
            }
            return null;
        }

        int latBandIndex = ALPHABET.indexOf(latBand);

        int number = utm.getZone() % 6;

        // Find false northing at A for second letter of grid square
        double falseNorthing = 0.;
        if (number % 2 == 0)
        {
            final double onePointFiveMil = 1500000.0;
            falseNorthing = onePointFiveMil;
        }

        // Round easting and northing values.
        double divisor = Math.pow(10, 5 - precision);
        int easting = (int)(Math.round(utm.getEasting() / divisor) * divisor);
        int northing = (int)(Math.round(utm.getNorthing() / divisor) * divisor);

        char sqLetter1 = find2ndMGRSLetter(easting, latBandIndex, utm.getZone());
        int squareLetter1Index = ALPHABET.indexOf(sqLetter1);

        char sqLetter2 = find3rdMGRSLetter(northing, falseNorthing);
        int squareLetter2Index = ALPHABET.indexOf(sqLetter2);

        // Now go back and calculate grid Easting and grid Northing
        // to find original easting and northing values.
        LatitudeBandData bandData = getLatitudeBand(latBandIndex);
        double gridNorthing = findGridNorthing(squareLetter2Index, falseNorthing, bandData.getMinNorthing());

        int lowValueIndex = findLowValueIndex(utm.getZone());

        // Find grid easting
        double gridEasting = findGridEasting(squareLetter1Index, lowValueIndex);

        int mgrsEast = (int)Math.round(easting - gridEasting);
        int mgrsNorth = (int)Math.round(northing - gridNorthing);

        return constructMGRSString(utm.getZone(), latBand, sqLetter1, sqLetter2, precision, mgrsEast, mgrsNorth);
    }

    /**
     * Determine the second mgrs letter (Useful when converting from utm to
     * mgrs).
     *
     * @param easting The easting value.
     * @param latBandIndex The letter index (int) of latitude band.
     * @param zone The zone.
     * @return The
     */
    public char find2ndMGRSLetter(double easting, int latBandIndex, int zone)
    {
        // First find lower letter range index for this location.
        int lowValueIndex = findLowValueIndex(zone);

        // Use easting to find 2nd letter of MGRS.
        double gEasting = easting;
        final double specialEasting = 500000.0;
        if (latBandIndex == LETTER_V && zone == 31 && gEasting == specialEasting)
        {
            // Subtract 1 meter.
            gEasting = gEasting - 1.0;
        }

        int secondMGRSLetterIndex = lowValueIndex + (int)(gEasting / ONEHT) - 1;
        if (lowValueIndex == LETTER_J && secondMGRSLetterIndex > LETTER_N)
        {
            secondMGRSLetterIndex = secondMGRSLetterIndex + 1;
        }

        return ALPHABET.charAt(secondMGRSLetterIndex);
    }

    /**
     * Determine the third mgrs letter (Useful when converting from utm to
     * mgrs).
     *
     * @param northing The northing value.
     * @param falseNorthing The false northing value.
     * @return char of the third mgrs letter.
     */
    public char find3rdMGRSLetter(double northing, double falseNorthing)
    {
        // Use northing to find 3rd letter of MGRS.
        double gNorthing = northing;
        final double nValue = 1.e7;
        if (gNorthing == nValue)
        {
            gNorthing = gNorthing - 1.0;
        }

        while (gNorthing >= TWOMIL)
        {
            gNorthing = gNorthing - TWOMIL;
        }
        gNorthing = gNorthing - falseNorthing;

        if (gNorthing < 0.0)
        {
            gNorthing = gNorthing + TWOMIL;
        }

        int thirdMGRSLetterIndex = (int)(gNorthing / ONEHT);
        if (thirdMGRSLetterIndex > LETTER_H)
        {
            thirdMGRSLetterIndex = thirdMGRSLetterIndex + 1;
        }

        if (thirdMGRSLetterIndex > LETTER_N)
        {
            thirdMGRSLetterIndex = thirdMGRSLetterIndex + 1;
        }

        return ALPHABET.charAt(thirdMGRSLetterIndex);
    }

    /**
     * Find the grid easting value.
     *
     * @param letterIndex The index of the second mgrs letter.
     * @param lowValueIndex The index of the lower range letter.
     * @return double The grid easting value.
     */
    public double findGridEasting(int letterIndex, int lowValueIndex)
    {
        double gridEasting = (letterIndex - lowValueIndex + 1) * ONEHT;
        if (lowValueIndex == LETTER_J && letterIndex > LETTER_O)
        {
            gridEasting = gridEasting - ONEHT;
        }
        return gridEasting;
    }

    /**
     * Find the grid northing value.
     *
     * @param index The third MGRS letter index.
     * @param falseNorthing The false northing.
     * @param minNorthing The minimum northing.
     * @return double The calculated grid northing value.
     */
    public double findGridNorthing(int index, double falseNorthing, double minNorthing)
    {
        double gridNorthing = index * ONEHT + falseNorthing;
        if (index > LETTER_O)
        {
            gridNorthing = gridNorthing - ONEHT;
        }

        if (index > LETTER_I)
        {
            gridNorthing = gridNorthing - ONEHT;
        }

        if (gridNorthing >= TWOMIL)
        {
            gridNorthing = gridNorthing - TWOMIL;
        }

        double scaledMinNorthing = minNorthing;
        while (scaledMinNorthing >= TWOMIL)
        {
            scaledMinNorthing -= TWOMIL;
        }
        gridNorthing -= scaledMinNorthing;

        if (gridNorthing < 0.)
        {
            gridNorthing += TWOMIL;
        }
        gridNorthing += minNorthing;

        return gridNorthing;
    }

    /**
     * Find the higher letter range index for 2nd mgrs grid letter.
     *
     * @param zone The utm zone.
     * @return int Index value of letter.
     */
    public int findHighValueIndex(int zone)
    {
        int number = zone % 6;
        int higherRangeIndex = -1;

        if (number == 1 || number == 4)
        {
            higherRangeIndex = LETTER_H;
        }
        else if (number == 2 || number == 5)
        {
            higherRangeIndex = LETTER_R;
        }
        else if (number == 3 || number == 0)
        {
            higherRangeIndex = LETTER_Z;
        }
        return higherRangeIndex;
    }

    /**
     * Find the lower letter range index for 2nd mgrs grid letter.
     *
     * @param zone The utm zone.
     * @return int Index value of letter.
     */
    public int findLowValueIndex(int zone)
    {
        int number = zone % 6;
        int lowerRangeIndex = -1;

        if (number == 1 || number == 4)
        {
            lowerRangeIndex = LETTER_A;
        }
        else if (number == 2 || number == 5)
        {
            lowerRangeIndex = LETTER_J;
        }
        else if (number == 3 || number == 0)
        {
            lowerRangeIndex = LETTER_S;
        }
        return lowerRangeIndex;
    }

    /**
     * Given the latitude, determine the utm letter designator.
     *
     * @param lat The latitude to use.
     * @return The utm latitude band designator.
     */
    public char getLatBandDesignator(double lat)
    {
        char letter = ' ';
        if (lat < 0.)
        {
            letter = getSouthernLatBandDesignator(lat);
        }
        else
        {
            letter = getNorthernLatBandDesignator(lat);
        }
        return letter;
    }

    /**
     * Standard accessor.
     *
     * @return The latitude band data.
     */
    public List<LatitudeBandData> getLatitudeBands()
    {
        return myLatitudeBands;
    }

    /**
     * Determine if we are in the Southern or Northern hemisphere.
     *
     * @param latitudeBand The latitude band to check.
     * @return char ('S' or 'N') representing hemisphere.
     */
    private Hemisphere determineHemisphere(char latitudeBand)
    {
        // Determine the hemisphere
        Hemisphere hemisphere;

        if (ALPHABET.indexOf(latitudeBand) < LETTER_N)
        {
            hemisphere = Hemisphere.SOUTH;
        }
        else
        {
            hemisphere = Hemisphere.NORTH;
        }

        return hemisphere;
    }

    /**
     * Helper method to find the grid easting values. This assumes that the
     * square letter values are within the valid range.
     *
     * @param squareLetter1 The first square letter value.
     * @param squareLetter2 The second square letter value.
     * @param sqLetter1LowValue The lower boundary for the first square letter.
     * @param sqLetter1HighValue The upper boundary for the first square letter.
     * @return The double value of grid easting.
     */
    private double findGridEasting(Character squareLetter1, Character squareLetter2, int sqLetter1LowValue,
            int sqLetter1HighValue)
    {
        double gridEasting = 0.;

        // Check to see if we have mgrs grid letters in string, if not then
        // approximate center of UTM zone.
        if (squareLetter1 != null && squareLetter2 != null)
        {
            // Find grid easting
            int sqLetter1Index = ALPHABET.indexOf(squareLetter1.charValue());
            gridEasting = findGridEasting(sqLetter1Index, sqLetter1LowValue);
        }
        else
        {
            // Approximate the center of UTM zone
            int middle = (sqLetter1HighValue + sqLetter1LowValue) / 2;
            gridEasting = findGridEasting(middle, sqLetter1LowValue);
        }

        return gridEasting;
    }

    /**
     * Helper method to find the grid northing values. This assumes that the
     * square letter values are within the valid range.
     *
     * @param squareLetter1 The first square letter value.
     * @param squareLetter2 The second square letter value.
     * @param bandData The latitude band properties.
     * @param falseNorthing The false northing value.
     * @return The double value of grid northing.
     */
    private double findGridNorthing(Character squareLetter1, Character squareLetter2, LatitudeBandData bandData,
            double falseNorthing)
    {
        if (bandData == null)
        {
            throw new IllegalArgumentException("The band data is null");
        }

        double gridNorthing = 0.;

        // Check to see if we have mgrs grid letters in string, if not then
        // approximate center of UTM zone.
        if (squareLetter1 != null && squareLetter2 != null)
        {
            // Find grid northing
            int sqLetter2Index = ALPHABET.indexOf(squareLetter2.charValue());
            gridNorthing = findGridNorthing(sqLetter2Index, falseNorthing, bandData.getMinNorthing());
        }
        else
        {
            // Approximate the center of UTM zone
            gridNorthing = bandData.getMinNorthing() + 400000;
        }
        return gridNorthing;
    }

    /**
     * Find latitude band properties for passed in zone.
     *
     * @param index The zone number.
     * @return LatitudeBandData Info for particular zone.
     */
    private LatitudeBandData getLatitudeBand(int index)
    {
        LatitudeBandData value = null;

        if (index >= LETTER_C && index <= LETTER_H)
        {
            value = myLatitudeBands.get(index - 2);
        }
        else if (index >= LETTER_J && index <= LETTER_N)
        {
            value = myLatitudeBands.get(index - 3);
        }
        else if (index >= LETTER_P && index <= LETTER_X)
        {
            value = myLatitudeBands.get(index - 4);
        }

        return value;
    }

    /**
     * For the given latitude (that must be in northern hemisphere) find the
     * corresponding MGRS latitude band designator.
     *
     * @param lat The latitude to check.
     * @return The latitude band designator.
     */
    private char getNorthernLatBandDesignator(double lat)
    {
        char letter = ' ';
        if (lat < 8 && lat >= 0)
        {
            letter = 'N';
        }
        else if (lat < 16 && lat >= 8)
        {
            letter = 'P';
        }
        else if (lat < 24 && lat >= 16)
        {
            letter = 'Q';
        }
        else if (lat < 32 && lat >= 24)
        {
            letter = 'R';
        }
        else if (lat < 40 && lat >= 32)
        {
            letter = 'S';
        }
        else if (lat < 48 && lat >= 40)
        {
            letter = 'T';
        }
        else if (lat < 56 && lat >= 48)
        {
            letter = 'U';
        }
        else if (lat < 64 && lat >= 56)
        {
            letter = 'V';
        }
        else if (lat < 72 && lat >= 64)
        {
            letter = 'W';
        }
        else if (lat <= UPPER_LAT_LIMIT + 1 && lat >= 72)
        {
            letter = 'X';
        }
        return letter;
    }

    /**
     * For the given latitude (that must be in southern hemisphere) find the
     * corresponding MGRS latitude band designator.
     *
     * @param lat The latitude to check.
     * @return The MGRS latitude band designator.
     */
    private char getSouthernLatBandDesignator(double lat)
    {
        char letter = ' ';
        if (lat < -72 && lat >= LOWER_LAT_LIMIT)
        {
            letter = 'C';
        }
        else if (lat < -64 && lat >= -72)
        {
            letter = 'D';
        }
        else if (lat < -56 && lat >= -64)
        {
            letter = 'E';
        }
        else if (lat < -48 && lat >= -56)
        {
            letter = 'F';
        }
        else if (lat < -40 && lat >= -48)
        {
            letter = 'G';
        }
        else if (lat < -32 && lat >= -40)
        {
            letter = 'H';
        }
        else if (lat < -24 && lat >= -32)
        {
            letter = 'J';
        }
        else if (lat < -16 && lat >= -24)
        {
            letter = 'K';
        }
        else if (lat < -8 && lat >= -16)
        {
            letter = 'L';
        }
        else if (lat < 0 && lat >= -8)
        {
            letter = 'M';
        }

        return letter;
    }

    /**
     * Initialize my latitude bands.
     */
    private void init()
    {
        final double southC = -80.5;
        // 'I' and 'O' are not used.
        myLatitudeBands.add(new LatitudeBandData('C', 1100000, -72, southC, 0));
        myLatitudeBands.add(new LatitudeBandData('D', 2000000, -64, -72, 2000000));
        myLatitudeBands.add(new LatitudeBandData('E', 2800000, -56, -64, 2000000));
        myLatitudeBands.add(new LatitudeBandData('F', 3700000, -48, -56, 2000000));
        myLatitudeBands.add(new LatitudeBandData('G', 4600000, -40, -48, 4000000));
        myLatitudeBands.add(new LatitudeBandData('H', 5500000, -32, -40, 4000000));
        myLatitudeBands.add(new LatitudeBandData('J', 6400000, -24, -32, 6000000));
        myLatitudeBands.add(new LatitudeBandData('K', 7300000, -16, -24, 6000000));
        myLatitudeBands.add(new LatitudeBandData('L', 8200000, -8, -16, 8000000));
        myLatitudeBands.add(new LatitudeBandData('M', 9100000, 0.0, -8, 8000000));
        myLatitudeBands.add(new LatitudeBandData('N', 0, 8, 0, 0));
        myLatitudeBands.add(new LatitudeBandData('P', 800000, 16, 8, 0));
        myLatitudeBands.add(new LatitudeBandData('Q', 1700000, 24, 16, 0));
        myLatitudeBands.add(new LatitudeBandData('R', 2600000, 32, 24, 2000000));
        myLatitudeBands.add(new LatitudeBandData('S', 3500000, 40, 32, 2000000));
        myLatitudeBands.add(new LatitudeBandData('T', 4400000, 48, 40, 4000000));
        myLatitudeBands.add(new LatitudeBandData('U', 5300000, 56, 48, 4000000));
        myLatitudeBands.add(new LatitudeBandData('V', 6200000, 64, 56, 6000000));
        myLatitudeBands.add(new LatitudeBandData('W', 7000000, 72, 64, 6000000));
        myLatitudeBands.add(new LatitudeBandData('X', 7900000, 84, 72, 6000000));
    }

    /** Class that holds latitude band information. */
    public static final class LatitudeBandData
    {
        /** hemisphere latitude band is in. */
        private final Hemisphere myHemisphere;

        /** The latitude band letter. */
        private final char myLatitudeBand;

        /** minimum northing for latitude band. */
        private final double myMinNorthing;

        /** upper latitude for latitude band (degrees). */
        private final double myNorth;

        /** latitude band northing offset. */
        private final double myNorthingOffset;

        /** lower latitude for latitude band (degrees). */
        private final double mySouth;

        /**
         * Constructor.
         *
         * @param latBand The latitude band letter.
         * @param mn The minimum northing.
         * @param n The north value.
         * @param s The south value.
         * @param no the northing offset.
         */
        public LatitudeBandData(char latBand, int mn, double n, double s, int no)
        {
            myLatitudeBand = latBand;
            myMinNorthing = mn;
            myNorth = n;
            mySouth = s;
            myNorthingOffset = no;
            if (ALPHABET.indexOf(latBand) > ALPHABET.indexOf('M'))
            {
                myHemisphere = Hemisphere.NORTH;
            }
            else
            {
                myHemisphere = Hemisphere.SOUTH;
            }
        }

        /**
         * Standard getter.
         *
         * @return The hemisphere.
         */
        public Hemisphere getHemisphere()
        {
            return myHemisphere;
        }

        /**
         * Standard getter.
         *
         * @return char The latitude band letter.
         */
        public char getLatitudeBand()
        {
            return myLatitudeBand;
        }

        /**
         * Standard getter.
         *
         * @return The minimum northing.
         */
        public double getMinNorthing()
        {
            return myMinNorthing;
        }

        /**
         * Standard getter.
         *
         * @return North value for latitude band (degrees).
         */
        public double getNorth()
        {
            return myNorth;
        }

        /**
         * Standard getter.
         *
         * @return The northing offset value.
         */
        public double getNorthingOffset()
        {
            return myNorthingOffset;
        }

        /**
         * Standard getter.
         *
         * @return South value for latitude band (degrees).
         */
        public double getSouth()
        {
            return mySouth;
        }
    }
}
