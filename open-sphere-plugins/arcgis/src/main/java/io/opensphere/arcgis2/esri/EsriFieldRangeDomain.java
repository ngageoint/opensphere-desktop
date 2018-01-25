package io.opensphere.arcgis2.esri;

import java.util.Arrays;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Range domain specifies a range of valid values for a field. The
 * <code>type</code> property for range domains is <code>range</code>.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriFieldRangeDomain extends EsriFieldDomain
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My name. */
    @JsonProperty("name")
    private String myName;

    /** My range. */
    @JsonProperty("range")
    private final int[] myRange = new int[2];

    /**
     * Gets the maximum value.
     *
     * @return the maximum value
     */
    public int getMaxValue()
    {
        return myRange[1];
    }

    /**
     * Gets the minimum value.
     *
     * @return the minimum value
     */
    public int getMinValue()
    {
        return myRange[0];
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the range.
     *
     * @return the range
     */
    public int[] getRange()
    {
        return Arrays.copyOf(myRange, myRange.length);
    }

    /**
     * Sets the maximum value.
     *
     * @param maxValue the new maximum value
     */
    public void setMaxValue(int maxValue)
    {
        myRange[1] = maxValue;
    }

    /**
     * Sets the minimum value.
     *
     * @param minValue the new minimum value
     */
    public void setMinValue(int minValue)
    {
        myRange[0] = minValue;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Sets the range.
     *
     * @param range the new range
     */
    public void setRange(int[] range)
    {
        if (range != null)
        {
            if (range.length > 0)
            {
                setMinValue(range[0]);
            }
            if (range.length > 1)
            {
                setMaxValue(range[1]);
            }
        }
    }
}
