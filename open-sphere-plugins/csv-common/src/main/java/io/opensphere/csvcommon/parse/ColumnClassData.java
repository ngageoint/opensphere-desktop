package io.opensphere.csvcommon.parse;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.collections.New;

/** The Class ColumnClassData. */
public class ColumnClassData
{
    /** The Constant MAX_UNIQUEVALUES_TO_CONSIDER. */
    private static final int MAX_UNIQUEVALUES_TO_CONSIDER = 255;

    /** The Column name. */
    private final String myColumnName;

    /** The Column class. */
    private ColumnClass myColumnClass;

    /** The all doubles. */
    private boolean myAllDoubles = true;

    /** The Double count. */
    private int myDoubleCount;

    /** The all floats. */
    private boolean myAllFloats = true;

    /** The float count. */
    private int myFloatCount;

    /** The all longs. */
    private boolean myAllLongs = true;

    /** The long count. */
    private int myLongCount;

    /** The all integers. */
    private boolean myAllIntegers = true;

    /** The integer count. */
    private int myIntCount;

    /** The All booleans. */
    private boolean myAllBooleans = true;

    /** The integer count. */
    private int myBooleanCount;

    /** The Num values considered. */
    private int myNumValuesConsidered;

    /** The Unique values set. */
    private final Set<String> myUniqueValuesSet;

    /**
     * Instantiates a new column class data.
     *
     * @param columnName the column name
     */
    public ColumnClassData(String columnName)
    {
        myUniqueValuesSet = New.set();
        myColumnName = columnName;
    }

    /**
     * Consider value.
     *
     * @param value the value
     */
    public void considerValue(String value)
    {
        if (!StringUtils.isBlank(value))
        {
            boolean doubleSuccess = false;
            boolean floatSuccess = false;
            boolean longSuccess = false;
            boolean integerSuccess = false;
            Double doubleResult = null;
            Float floatResult = null;

            if (myAllDoubles)
            {
                try
                {
                    doubleResult = Double.valueOf(value);
                    doubleSuccess = true;
                    myDoubleCount++;
                    doubleSuccess = checkDoubleForInfinateAndNan(doubleSuccess, doubleResult);
                }
                catch (NumberFormatException e)
                {
                    myAllDoubles = false;
                }
            }

            if (myAllFloats || doubleSuccess)
            {
                try
                {
                    floatResult = Float.valueOf(value);
                    myFloatCount++;
                    floatSuccess = checkFloatForInfNanAndPrecision(doubleSuccess, doubleResult, floatResult);
                }
                catch (NumberFormatException e)
                {
                    myAllFloats = false;
                }
            }

            if (myAllLongs)
            {
                try
                {
                    Long.parseLong(value);
                    longSuccess = true;
                    myLongCount++;
                }
                catch (NumberFormatException e)
                {
                    myAllLongs = false;
                }
            }

            if (myAllIntegers || longSuccess)
            {
                try
                {
                    Integer.parseInt(value);
                    integerSuccess = true;
                    myIntCount++;
                }
                catch (NumberFormatException e)
                {
                    myAllIntegers = false;
                }
            }

            // Check for Boolean.
            checkForBooleanValue(value, doubleSuccess, floatSuccess, longSuccess, integerSuccess);

            if (myUniqueValuesSet.size() < MAX_UNIQUEVALUES_TO_CONSIDER)
            {
                myUniqueValuesSet.add(value);
            }
            myNumValuesConsidered++;
        }
    }

    /**
     * Gets the column class.
     *
     * @return the column class
     */
    public ColumnClass getColumnClass()
    {
        myColumnClass = ColumnClass.STRING;
        if (myNumValuesConsidered > 0)
        {
            if (myBooleanCount > 0 && myAllBooleans)
            {
                myColumnClass = ColumnClass.BOOLEAN;
            }
            else if (myIntCount > 0 && myAllIntegers)
            {
                myColumnClass = ColumnClass.INTEGER;
            }
            else if (myLongCount > 0 && myAllLongs)
            {
                myColumnClass = ColumnClass.LONG;
            }
            else if (myFloatCount > 0 && myAllFloats)
            {
                myColumnClass = ColumnClass.FLOAT;
            }
            else if (myDoubleCount > 0 && myAllDoubles)
            {
                myColumnClass = ColumnClass.DOUBLE;
            }
        }
        return myColumnClass;
    }

    /**
     * Gets the num values considered.
     *
     * @return the num values considered
     */
    public int getNumValuesConsidered()
    {
        return myNumValuesConsidered;
    }

    /**
     * Gets the unique value count.
     *
     * @return the unique value count
     */
    public int getUniqueValueCount()
    {
        return myUniqueValuesSet.size();
    }

    /**
     * Gets the unique values.
     *
     * @return the unique values
     */
    public Set<String> getUniqueValues()
    {
        return myUniqueValuesSet;
    }

    /**
     * Checks for less than max unique values.
     *
     * @return true, if successful
     */
    public boolean hasLessThanMaxUniqueValues()
    {
        return myUniqueValuesSet.size() < MAX_UNIQUEVALUES_TO_CONSIDER;
    }

    /**
     * Checks if is string.
     *
     * @return true, if is string
     */
    public boolean isString()
    {
        return myColumnClass == ColumnClass.STRING;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Column[").append(myColumnName).append("] Samples[").append(myNumValuesConsidered);
        sb.append("] UQ[").append(hasLessThanMaxUniqueValues() ? Integer.valueOf(getUniqueValueCount()) : "MAX")
                .append("] Class[").append(getColumnClass());
        sb.append("] Double[All=").append(myAllDoubles).append(", #").append(myDoubleCount);
        sb.append("] Float[All=").append(myAllFloats).append(", #").append(myFloatCount);
        sb.append("] Long[All=").append(myAllLongs).append(", #").append(myLongCount);
        sb.append("] Int[All=").append(myAllIntegers).append(", #").append(myIntCount);
        sb.append("] Bool[All=").append(myAllBooleans).append(", #").append(myBooleanCount);
        sb.append("]\n");
        if (isString() && hasLessThanMaxUniqueValues() && getUniqueValueCount() > 0)
        {
            sb.append("   UQVals[");
            for (String val : getUniqueValues())
            {
                sb.append(val).append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("]\n");
        }
        return sb.toString();
    }

    /**
     * Check double for infinite and NaN (Not a number).
     *
     * @param pDoubleSuccess the double success
     * @param doubleResult the double result
     * @return true, if successful
     */
    private boolean checkDoubleForInfinateAndNan(boolean pDoubleSuccess, Double doubleResult)
    {
        boolean doubleSuccess = pDoubleSuccess;
        if (doubleResult.isInfinite() || doubleResult.isNaN())
        {
            doubleSuccess = false;
            myDoubleCount--;
            myAllDoubles = false;
        }
        return doubleSuccess;
    }

    /**
     * Check float for inf nan and precision.
     *
     * @param doubleSuccess the double success
     * @param doubleResult the double result
     * @param floatResult the float result
     * @return true, if successful
     */
    private boolean checkFloatForInfNanAndPrecision(boolean doubleSuccess, Double doubleResult, Float floatResult)
    {
        boolean floatSuccess = true;
        if (floatResult.isInfinite() || floatResult.isNaN()
                || doubleSuccess && doubleResult.floatValue() != floatResult.floatValue())
        {
            floatSuccess = false;
            myFloatCount--;
            myAllFloats = false;
        }
        return floatSuccess;
    }

    /**
     * Check for boolean value.
     *
     * @param value the value
     * @param doubleSuccess the double success
     * @param floatSuccess the float success
     * @param longSuccess the long success
     * @param integerSuccess the integer success
     */
    private void checkForBooleanValue(String value, boolean doubleSuccess, boolean floatSuccess, boolean longSuccess,
            boolean integerSuccess)
    {
        if (!(doubleSuccess || floatSuccess || longSuccess || integerSuccess))
        {
            BooleanDetermination bd = isBoolean(value);
            if (bd.isBoolean())
            {
                myBooleanCount++;
            }
            else
            {
                myAllBooleans = false;
            }
        }
    }

    /**
     * Checks if is boolean.
     *
     * @param value the value
     * @return the boolean determination
     */
    public static BooleanDetermination isBoolean(String value)
    {
        if ("true".equalsIgnoreCase(value))
        {
            return BooleanDetermination.BOOLEAN_TRUE;
        }
        else if ("false".equalsIgnoreCase(value))
        {
            return BooleanDetermination.BOOLEAN_FALSE;
        }
        return BooleanDetermination.UNDETERMINED;
    }
}
