package io.opensphere.core.common.geospatial;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * This class parses latitudes and longitudes in the same feel as the
 * <code>DecimalFormat</code> and <code>SimpleDateFormat</code> classes.
 * <ul>
 * Describe the format of the incoming Lat or Lon
 * <li>d -> Degree -> 33:25:15N = dd ...</li>
 * <li>m -> Minute -> 33:25:15N = dd:mm ...</li>
 * <li>s -> Second -> 33:25:15N = dd:mm:ss ...</li>
 * <li>h -> Hemisphere -> 33:25:15N = dd:mm:ssh ...</li>
 * </ul>
 */
public class LonLatFormat extends Format
{
    /** The default Serial Version UID. */
    private static final long serialVersionUID = 1L;

    private String pattern;

    private String toConvert;

    /**
     * Constructor.
     */
    public LonLatFormat()
    {
    }

    /**
     * Constructor.
     *
     * @param pattern the longitude/latitude pattern string.
     */
    public LonLatFormat(final String pattern)
    {
        applyPattern(pattern);
    }

    /**
     * Apply the given pattern to this <code>Format</code> object. A pattern is
     * a short-hand specification for the various formatting properties.<br/>
     * <br/>
     * Example "ddd:mm:ssh" -> 123:31:15E
     *
     * @param pattern the new longitude/latitude pattern for this format.
     */
    public void applyPattern(final String pattern)
    {
        // TODO: Why was this included? .replace("'", "")
        setFormat(pattern);
    }

    /**
     * @param toConvert <br>
     *            Wrapper to setToConvert Instance Variable<br>
     *            Nothing fancy going on in here
     */
    private void parse(final String toConvert)
    {
        setToConvert(toConvert);
    }

    /**
     *
     * @return Starting and Ending Position of Character Sequence Passed into
     *         Method <br>
     *         INTEGER[]<br>
     *         <li>int[0] = Beginning Position</li>
     *         <li>int[1] = Ending Position</li>
     */
    private int[] findPositionsOf(final String s)
    {
        final int[] pos = new int[2];
        try
        {
            int cursor = 0;
            int endCursor = 0;
            pos[0] = getFormat().indexOf(s);
            cursor = pos[0];
            for (int i = pos[0]; i < pattern.length(); i++)
            {
                cursor += 1;
                if (pattern.substring(i, cursor).equals(s))
                {
                    endCursor = cursor;
                }
            }
            pos[1] = endCursor;
            return pos;
        }
        catch (final StringIndexOutOfBoundsException e)
        {
            pos[0] = 0;
            pos[1] = 0;
            return pos;
        }
    }

    /**
     * @return the format
     */
    protected String getFormat()
    {
        return pattern;
    }

    /**
     * @param format the format to set
     */
    protected void setFormat(final String format)
    {
        pattern = format;
    }

    /**
     * @return the toConvert
     */
    protected String getToConvert()
    {
        return toConvert;
    }

    /**
     * @param toConvert the toConvert to set
     */
    protected void setToConvert(final String toConvert)
    {
        this.toConvert = toConvert;
    }

    @Override
    public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos)
    {
        throw new UnsupportedOperationException("This method is not yet supported");
    }

    /**
     * @see java.text.Format#parseObject(java.lang.String,
     *      java.text.ParsePosition)
     */
    @Override
    public Object parseObject(final String source, final ParsePosition pos)
    {
        parse(source.substring(pos.getIndex()));
        final int[] degreePos = findPositionsOf("d");
        final int[] minutePos = findPositionsOf("m");
        final int[] secondPos = findPositionsOf("s");
        int[] hemiPos = findPositionsOf("h");
        double degree = 0;
        double minute = 0;
        double second = 0;
        int hemi = 0;
        degree = Double.parseDouble(getToConvert().substring(degreePos[0], degreePos[1]));
        minute = Double.parseDouble(getToConvert().substring(minutePos[0], minutePos[1])) / 60;
        second = Double.parseDouble(getToConvert().substring(secondPos[0], secondPos[1])) / 3600;
        if (hemiPos[1] - hemiPos[0] == 0)
        {
            hemiPos = findPositionsOf("-");
            if (hemiPos[1] - hemiPos[0] == 0)
            {
                hemi = 1;
            }
            else
            {
                hemi = -1;
            }
        }
        else
        {
            if (getToConvert().substring(hemiPos[0], hemiPos[1]).equalsIgnoreCase("N")
                    || getToConvert().substring(hemiPos[0], hemiPos[1]).equalsIgnoreCase("E"))
            {
                hemi = 1;
            }
            else
            {
                hemi = -1;
            }
        }
        final Double result = (degree + minute + second) * hemi;

        // TODO: Correctly update pos with the file parse location/error index.
        pos.setIndex(pos.getIndex() + pattern.length());
        return result;
    }
}
