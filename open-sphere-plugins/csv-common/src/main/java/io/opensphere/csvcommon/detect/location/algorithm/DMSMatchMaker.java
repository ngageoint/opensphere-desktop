package io.opensphere.csvcommon.detect.location.algorithm;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class DMSMatchMaker will try to recognize DMS values in data without a
 * header that specifies column names. If values are found that pass, a set of
 * potential columns with confidence values will be created.
 */
@SuppressWarnings("PMD.GodClass")
public class DMSMatchMaker extends LatLonLocationMatcher
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DMSMatchMaker.class);

    /** Indicates a latitude value may have been found. */
    private boolean myIsPossibleLatitudeValue;

    /** Indicates a longitude value may have been found. */
    private boolean myIsPossibleLongitudeValue;

    /** The confidence that a column contains valid DMS lat/lon values. */
    private float myConfidence;

    /** Some confidence factors. */
    private static final float ourConfFactor1 = .15f;

    /** Some confidence factors. */
    private static final float ourConfFactor2 = .2f;

    /** Some confidence factors. */
    private static final float ourConfFactor3 = .25f;

    /** The confidence value if there is a single result. */
    private static final float ourSingleResultConf = .56f;

    /** The Num columns. */
    private int myNumColumns = -1;

    @Override
    public ValuesWithConfidence<LocationResults> detect(CellSampler sampler)
    {
        LocationResults results = new LocationResults();
        for (List<? extends String> row : sampler.getBeginningSampleCells())
        {
            if (myNumColumns == -1)
            {
                myNumColumns = row.size();
            }
            for (int i = 0; i < row.size(); i++)
            {
                validateValue(i, row.get(i), results);
            }
        }
        consolidateResults(results, myNumColumns);

        if (getAggregateResults().getLatLonResults().size() == 1
                && getAggregateResults().getLatLonResults().get(0).getConfidence() < .5f)
        {
            getAggregateResults().getLatLonResults().get(0).setConfidence(ourSingleResultConf);
        }

        return new ValuesWithConfidence<LocationResults>(getAggregateResults(), Math.max(0f, getMaxConfidence()));
    }

    @Override
    protected void validateValue(int index, String str, LocationResults results)
    {
        myIsPossibleLatitudeValue = false;
        myIsPossibleLongitudeValue = false;
        myConfidence = 0f;

        if (str.contains("°"))
        {
            myConfidence = ourConfFactor3;
        }

        if (!(myIsPossibleLatitudeValue = checkStandardValue(str, "N", "S"))
                && !(myIsPossibleLongitudeValue = checkStandardValue(str, "E", "W")))
        {
            checkNonStandardDMSValue(str);
        }

        if (myConfidence > 1.0f)
        {
            myConfidence = 1.0f;
        }

        if (myIsPossibleLongitudeValue && myConfidence > ourConfFactor1)
        {
            for (Entry<PotentialLocationColumn, Integer> entry : getPotentialColumns().entrySet())
            {
                if (entry.getKey().getColumnIndex() == index && entry.getKey().getColumnName().equals(ColumnType.LAT.toString())
                        && entry.getKey().getLatFormat() != null)
                {
                    // This is actually a longitude column, change applicable
                    // values
                    entry.getKey().setType(ColumnType.LON);
                    entry.getKey().setColumnName(ColumnType.LON.toString());
                    entry.getKey().setLatFormat(null);
                    entry.getKey().setLonFormat(CoordFormat.DECIMAL);
                }
            }

            PotentialLocationColumn plc = createPotentialColumn(index, results, myConfidence, ColumnType.LON);
            if (plc != null)
            {
                plc.setLonFormat(CoordFormat.DMS);
            }
        }
        else if (myIsPossibleLatitudeValue && myConfidence > ourConfFactor1)
        {
            PotentialLocationColumn plc = createPotentialColumn(index, results, myConfidence, ColumnType.LAT);
            if (plc != null)
            {
                plc.setLatFormat(CoordFormat.DMS);
            }
        }
    }

    /**
     * Checks string values that contain a hemisphere designator.
     *
     * @param str the string to parse and check
     * @param dir1 the northing or easting
     * @param dir2 the southing or westing
     * @return true, if successful
     */
    private boolean checkStandardValue(String str, String dir1, String dir2)
    {
        boolean isLatitude = false;
        String locStr = null;

        if ("N".equalsIgnoreCase(dir1) && "S".equalsIgnoreCase(dir2))
        {
            isLatitude = true;
        }

        if (str.startsWith(dir1) || str.startsWith(dir2))
        {
            locStr = str.substring(1);
        }
        else if (str.endsWith(dir1) || str.endsWith(dir2))
        {
            locStr = str.substring(0, str.length() - 1);
        }

        if (locStr == null)
        {
            return false;
        }

        String splitStr = findSeparator(locStr);
        // String has no separators, ie N223300 or E0634300
        if (splitStr == null)
        {
            return parseStandardNoSeparator(isLatitude, locStr);
        }
        else
        {
            return parseStandardWithSeparator(isLatitude, locStr, splitStr);
        }
    }

    /**
     * Determine if there is a separator or not.
     *
     * @param locStr the location string
     * @return the separator
     */
    private String findSeparator(String locStr)
    {
        String splitStr = null;
        for (int i = 0; i < locStr.length(); i++)
        {
            char c = locStr.charAt(i);
            if (!Character.isDigit(c) && !".".equals(String.valueOf(c)))
            {
                splitStr = String.valueOf(c);
                break;
            }
        }
        return splitStr;
    }

    /**
     * Parses a DMS string in the standard format with no separators, ie N223300
     * or E0634300.
     *
     * @param isLatitude true if this has been identified as a latitude value
     * @param locStr the loc str
     * @return true, if successful
     */
    private boolean parseStandardNoSeparator(boolean isLatitude, String locStr)
    {
        boolean isLikelyValue = false;
        String toParse = locStr;
        if (toParse.length() > 5)
        {
            // Check for decimal seconds, ignore if found
            if (toParse.contains("."))
            {
                toParse = toParse.split("\\.")[0];
            }

            try
            {
                if (isLatitude)
                {
                    int locValue = Integer.parseInt(toParse.substring(0, 2));
                    if (locValue >= 0 && locValue <= 90 && validateMinSecValue(toParse.substring(2, 4))
                            && validateMinSecValue(toParse.substring(4, 6)))
                    {
                        isLikelyValue = true;
                        myConfidence = 1.0f;
                    }
                }
                else
                {
                    int locValue = -1;
                    String minStr = null;
                    String secStr = null;
                    if (toParse.length() == 6)
                    {
                        locValue = Integer.parseInt(toParse.substring(0, 2));
                        minStr = toParse.substring(2, 3);
                        secStr = toParse.substring(4);
                    }
                    else if (toParse.length() == 7)
                    {
                        locValue = Integer.parseInt(toParse.substring(0, 3));
                        minStr = toParse.substring(3, 5);
                        secStr = toParse.substring(5);
                    }

                    if (locValue >= 0 && locValue <= 180 && validateMinSecValue(minStr) && validateMinSecValue(secStr))
                    {
                        isLikelyValue = true;
                        myConfidence = 1.0f;
                    }
                }
            }
            catch (NumberFormatException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Error validating standard format DMS string.", e);
                }
            }
            catch (PatternSyntaxException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Error validating standard format DMS pattern.", e);
                }
            }
        }

        return isLikelyValue;
    }

    /**
     * Parses a DMS string in the standard format with a separator, ie
     * 15°07'45.23"S or 144°18'39.54"W.
     *
     * @param isLatitude true if this has been identified as a latitude value
     * @param locStr the string to parse and check
     * @param splitStr the string used to split the location into degrees and
     *            minutes/seconds
     * @return true, if successful
     */
    private boolean parseStandardWithSeparator(boolean isLatitude, String locStr, String splitStr)
    {
        boolean isLikelyValue = false;
        try
        {
            String[] locTok = locStr.split(splitStr);
            if (locTok.length > 1)
            {
                try
                {
                    int locValue = Integer.parseInt(locTok[0]);
                    if (isLatitude)
                    {
                        if (locValue >= 0 && locValue <= 90)
                        {
                            isLikelyValue = true;
                            myConfidence += ourConfFactor2;
                            validateNoDegrees(locTok[1]);
                        }
                    }
                    else
                    {
                        if (locValue >= 0 && locValue <= 180)
                        {
                            isLikelyValue = true;
                            myConfidence += ourConfFactor2;
                            validateNoDegrees(locTok[1]);
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Error validating standard format DMS string.", e);
                    }
                }
                catch (PatternSyntaxException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Pattern error validating standard format DMS string.", e);
                    }
                }
            }
        }
        catch (PatternSyntaxException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Pattern error validating standard format DMS string.", e);
            }
        }

        return isLikelyValue;
    }

    /**
     * Checks a location that does not contain a hemisphere indicator. These
     * strings will contain a '-' character to represent the opposite
     * hemisphere.
     *
     * @param str the string to parse and check
     */
    private void checkNonStandardDMSValue(String str)
    {
        String toCheck = str;
        if (str.length() > 0)
        {
            if ("-".equals(String.valueOf(str.charAt(0))))
            {
                toCheck = str.substring(1);
            }

            String splitChar = null;
            StringBuilder locValueStr = new StringBuilder();
            for (int i = 0; i < toCheck.length(); i++)
            {
                char c = toCheck.charAt(i);
                if (Character.isDigit(c))
                {
                    locValueStr.append(c);
                }
                else
                {
                    splitChar = String.valueOf(c);
                    break;
                }
            }

            if (splitChar != null)
            {
                try
                {
                    int locValue = Integer.parseInt(locValueStr.toString());
                    if (locValue > 90 && locValue <= 180)
                    {
                        myIsPossibleLongitudeValue = true;
                    }
                    /* Since it cannot be determined if this is a lat or lon
                     * when the value is less than 90, assume latitude. */
                    else if (locValue >= 0 && locValue <= 90)
                    {
                        String[] locTok = toCheck.split(splitChar);
                        if (locTok.length > 1)
                        {
                            myIsPossibleLatitudeValue = true;
                            String locNoDegrees = locTok[1];
                            validateNoDegrees(locNoDegrees);
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Error parsing DMS value without hemisphere indicator.", e);
                    }
                }
                catch (PatternSyntaxException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Pattern error parsing DMS value without hemisphere indicator.", e);
                    }
                }
            }
        }
    }

    /**
     * Validates the remainder of the location string without the degrees.
     *
     * @param value the string to parse and check
     */
    private void validateNoDegrees(String value)
    {
        // Make sure we have at least a 1 digit minute, a separator, and a 1
        // digit second.
        if (value.length() >= 3)
        {
            String splitChar = null;
            boolean foundDecimalSeconds = false;
            StringBuilder minutes = new StringBuilder();
            StringBuilder seconds = new StringBuilder();

            for (int i = 0; i < value.length(); i++)
            {
                char c = value.charAt(i);
                if (Character.isDigit(c))
                {
                    if (!foundDecimalSeconds)
                    {
                        if (splitChar == null)
                        {
                            minutes.append(c);
                        }
                        else
                        {
                            seconds.append(c);
                        }
                    }
                }
                else
                {
                    if (".".equals(String.valueOf(c)))
                    {
                        foundDecimalSeconds = true;
                    }
                    else
                    {
                        splitChar = String.valueOf(c);
                    }
                }
            }

            if (minutes.toString() != null && validateMinSecValue(minutes.toString()))
            {
                myConfidence += ourConfFactor1;
            }

            if (seconds.toString() != null && validateMinSecValue(seconds.toString()))
            {
                myConfidence = 1.0f;
            }
        }
    }

    /**
     * Validate that the minutes and seconds fall within the range.
     *
     * @param value the value to convert and check
     * @return true, if successful
     */
    private boolean validateMinSecValue(String value)
    {
        try
        {
            int dmValue = Integer.parseInt(value);
            if (dmValue >= 0 && dmValue < 60)
            {
                return true;
            }
        }
        catch (NumberFormatException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Error validating minute/second value for DMS format.", e);
            }
        }

        return false;
    }
}
