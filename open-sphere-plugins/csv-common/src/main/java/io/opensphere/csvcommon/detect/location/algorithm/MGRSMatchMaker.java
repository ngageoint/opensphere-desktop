package io.opensphere.csvcommon.detect.location.algorithm;

import org.apache.log4j.Logger;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class MGRSMatchMaker will attempt to locate values in a row that are
 * recognized as MGRS values, assign a confidence, and store the results.
 */
public class MGRSMatchMaker extends LocationMatchMaker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MGRSMatchMaker.class);

    /** The Constant ourConfidenceFactor1. */
    private static final float ourConfidenceFactor1 = .1f;

    /** The Constant ourConfidenceFactor2. */
    private static final float ourConfidenceFactor2 = .18f;

    /** The Constant ourConfidenceFactor3. */
    private static final float ourConfidenceFactor3 = .25f;

    /** Separator characters. */
    private static final String SEP = "[\\s'\",:\\._-]";

    /**
     * Validate a MGRS string by determining if this string starts with 1 or 2
     * numeric digits between 1 and 60 followed by a letter designator c-x
     * omitting i and o(the grid zone). The grid zone is followed by a 100,000
     * meter square row letter(a-z omitting i and o) followed by a column
     * letter(a-v omitting i and 0). The 100,000 meter square is followed by up
     * to 10 digits. As the cell value is checked, it is given a confidence
     * based on how many of the above match as well as how many of the sampled
     * data out of the total are validated.
     *
     * @param index the column index
     * @param cell a cell value from a row of data
     * @param results the results
     */
    @Override
    protected void validateValue(int index, String cell, LocationResults results)
    {
        String str = cell;
        // If the string's length is less than 2 or contains a separator don't
        // parse it
        String[] strTok = str.split(SEP);
        if (str.length() < 2 || strTok.length > 1)
        {
            // If the string is divided in 3 parts, it may be MGRS
            if (strTok.length == 3)
            {
                str = strTok[0] + strTok[1] + strTok[2];
                // If the string's length is still less than 2, it isn't valid
                if (str.length() < 2)
                {
                    return;
                }
            }
            else
            {
                return;
            }
        }
        float confidence = 0;
        int zone = -1;
        char c = str.charAt(0);
        if (Character.isDigit(c) && isValidGrid(str.substring(0, 1)))
        {
            zone = 1;
            confidence = ourConfidenceFactor1;
        }

        c = str.charAt(1);
        if (Character.isDigit(c) && isValidGrid(str.substring(0, 2)))
        {
            zone = 2;
            confidence = ourConfidenceFactor2;
        }

        if (zone > 0)
        {
            confidence = validateNoZoneString(str, confidence, zone);
        }

        if (confidence > 0 && confidence <= ourConfidenceFactor2 && str.length() > 2)
        {
            confidence = 0f;
        }

        createPotentialMGRSColumn(index, results, confidence);
    }

    /**
     * Creates the potential mgrs column.
     *
     * @param index the index
     * @param results the results
     * @param confidence the confidence
     */
    private void createPotentialMGRSColumn(int index, LocationResults results, float confidence)
    {
        PotentialLocationColumn plc = createPotentialColumn(index, results, confidence, ColumnType.MGRS);
        if (plc != null)
        {
            plc.setLocationFormat(CoordFormat.MGRS);
        }
    }

    /**
     * Validates the remainder of the MGRS string without the zone.
     *
     * @param str the string without the zone number
     * @param conf the confidence
     * @param zone where to start the substring without the zone
     * @return the adjusted confidence value
     */
    private float validateNoZoneString(String str, float conf, int zone)
    {
        float confidence = conf;
        String noZoneStr = str.substring(zone);
        if (noZoneStr != null && noZoneStr.length() > 2 && isValidGridZone(noZoneStr.charAt(0)))
        {
            confidence += ourConfidenceFactor3;
            if (isValidSquare(noZoneStr.substring(1, 3)))
            {
                confidence += ourConfidenceFactor3;
                String locationStr = noZoneStr.substring(3);
                if (locationStr.length() % 2 == 0)
                {
                    confidence += ourConfidenceFactor3;
                    boolean isDigit = true;
                    for (int i = 0; i < locationStr.length(); i++)
                    {
                        if (!Character.isDigit(locationStr.charAt(i)))
                        {
                            isDigit = false;
                            break;
                        }
                    }
                    if (isDigit)
                    {
                        confidence = 1.0f;
                    }
                }
            }
        }
        return confidence;
    }

    /**
     * Checks if this grid is valid - between 1 and 60.
     *
     * @param grid the grid
     * @return true, if is valid grid
     */
    private boolean isValidGrid(String grid)
    {
        try
        {
            int gridZoneValue = Integer.parseInt(grid);
            if (gridZoneValue >= 1 && gridZoneValue <= 60)
            {
                return true;
            }
        }
        catch (NumberFormatException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Error validating MGRS string.", e);
            }
        }

        return false;
    }

    /**
     * Checks if this character is a valid grid zone.
     *
     * @param zone the zone
     * @return true, if is valid grid zone
     */
    private boolean isValidGridZone(char zone)
    {
        String gridZoneMatch = "[C-Hc-hJ-Nj-nP-Xp-x]";
        return String.valueOf(zone).matches(gridZoneMatch);
    }

    /**
     * Checks if this string is a valid 100,000 meter square.
     *
     * @param str the value to check
     * @return true, if is valid square
     */
    private boolean isValidSquare(String str)
    {
        String squareColMatch = "[A-Ha-hJ-Nj-nP-Zp-z]";
        String squareRowMatch = "[A-Ha-hJ-Nj-nP-Vp-v]";
        return String.valueOf(str.charAt(0)).matches(squareColMatch) && String.valueOf(str.charAt(0)).matches(squareRowMatch);
    }
}
