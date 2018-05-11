package io.opensphere.mantle.util.columnanalyzer;

import java.util.Set;

import io.opensphere.mantle.util.columnanalyzer.ColumnAnalyzerData.BooleanDetermination;
import io.opensphere.mantle.util.columnanalyzer.ColumnAnalyzerData.ColumnClass;

/**
 * The Class ColumnDataAnalyzer.
 */
@SuppressWarnings("PMD.GodClass")
public class ColumnDataAnalyzer
{
    /** The Constant MAX_UNIQUEVALUES_TO_CONSIDER. */
    private static final int MAX_UNIQUEVALUES_TO_CONSIDER = 50;

    /** The Data. */
    private ColumnAnalyzerData myData;

    /**
     * Checks if is boolean.
     *
     * @param value the value
     * @return the boolean determination
     */
    private static BooleanDetermination isBoolean(String value)
    {
        BooleanDetermination result = BooleanDetermination.UNDETERMINED;
        if (value == null || value.isEmpty())
        {
            if ("true".equalsIgnoreCase(value))
            {
                result = BooleanDetermination.BOOLEAN_TRUE;
            }
            else if ("false".equalsIgnoreCase(value))
            {
                result = BooleanDetermination.BOOLEAN_FALSE;
            }
        }
        return result;
    }

    /**
     * Instantiates a new column data analyzer.
     *
     * @param data the data
     */
    public ColumnDataAnalyzer(ColumnAnalyzerData data)
    {
        myData = data;
    }

    /**
     * Instantiates a new column class data.
     *
     * @param typeName the type name
     * @param columnName the column name
     */
    public ColumnDataAnalyzer(String typeName, String columnName)
    {
        myData = new ColumnAnalyzerData(typeName, columnName);
    }

    /**
     * Consider value.
     *
     * @param obj the object to consider.
     */
    public void considerValue(Object obj)
    {
        if (obj != null)
        {
            String value = obj.toString();
            if (!value.isEmpty())
            {
                boolean doubleSuccess = false;
                boolean floatSuccess = false;
                boolean longSuccess = false;
                boolean integerSuccess = false;
                Double doubleResult = null;
                Float floatResult = null;

                if (myData.isAllDoubles())
                {
                    try
                    {
                        doubleResult = Double.valueOf(value);
                        doubleSuccess = true;
                        myData.incrementDoubleCount();
                        doubleSuccess = checkDoubleForInfinateAndNan(doubleSuccess, doubleResult);
                    }
                    catch (NumberFormatException e)
                    {
                        myData.setAllDoubles(false);
                    }
                }

                if (myData.isAllFloats() || doubleSuccess)
                {
                    try
                    {
                        floatResult = Float.valueOf(value);
                        myData.incrementFloatCount();
                        floatSuccess = checkFloatForInfNanAndPrecision(doubleSuccess, doubleResult, floatResult);
                    }
                    catch (NumberFormatException e)
                    {
                        myData.setAllFloats(false);
                    }
                }

                if (myData.isAllLongs())
                {
                    try
                    {
                        Long.parseLong(value);
                        longSuccess = true;
                        myData.incrementLongCount();
                    }
                    catch (NumberFormatException e)
                    {
                        myData.setAllLongs(false);
                    }
                }

                if (myData.isAllIntegers() || longSuccess)
                {
                    try
                    {
                        Integer.parseInt(value);
                        integerSuccess = true;
                        myData.incrementIntCount();
                    }
                    catch (NumberFormatException e)
                    {
                        myData.setAllIntegers(false);
                    }
                }

                // Check for Boolean.
                checkForBooleanValue(value, doubleSuccess, floatSuccess, longSuccess, integerSuccess);

                considerForUniqueValues(value);
                myData.incrementNumValuesConsidered();
            }
        }
        myData.incrementTotalValuesProcessed();
    }

    /**
     * Gets the column class.
     *
     * @return the column class
     */
    public ColumnClass determineColumnClassFromData()
    {
        myData.setColumnClass(ColumnClass.STRING);
        if (myData.getNumValuesConsidered() > 0)
        {
            if (myData.getBooleanCount() > 0 && myData.isAllBooleans())
            {
                myData.setColumnClass(ColumnClass.BOOLEAN);
            }
            else if (myData.getIntCount() > 0 && myData.isAllIntegers())
            {
                myData.setColumnClass(ColumnClass.INTEGER);
            }
            else if (myData.getLongCount() > 0 && myData.isAllLongs())
            {
                myData.setColumnClass(ColumnClass.LONG);
            }
            else if (myData.getFloatCount() > 0 && myData.isAllFloats())
            {
                myData.setColumnClass(ColumnClass.FLOAT);
            }
            else if (myData.getDoubleCount() > 0 && myData.isAllDoubles())
            {
                myData.setColumnClass(ColumnClass.DOUBLE);
            }
        }
        return myData.getColumnClass();
    }

    /**
     * Gets the column name.
     *
     * @return the column name
     */
    public String getColumnName()
    {
        return myData.getColumnName();
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public ColumnAnalyzerData getData()
    {
        return myData;
    }

    /**
     * Gets the num values considered.
     *
     * @return the num values considered
     */
    public int getNumValuesConsidered()
    {
        return myData.getNumValuesConsidered();
    }

    /**
     * Gets the type name.
     *
     * @return the type name
     */
    public String getTypeName()
    {
        return myData.getTypeName();
    }

    /**
     * Gets the unique value count.
     *
     * @return the unique value count
     */
    public int getUniqueValueCount()
    {
        return myData.getUniqueValueCount();
    }

    /**
     * Gets the unique values.
     *
     * @return the unique values
     */
    public Set<String> getUniqueValues()
    {
        return myData.getUniqueValuesSet();
    }

    /**
     * Checks for less than max unique values.
     *
     * @return true, if successful
     */
    public boolean hasLessThanMaxUniqueValues()
    {
        return myData.getUniqueValueCount() < MAX_UNIQUEVALUES_TO_CONSIDER;
    }

    /**
     * Checks if is string.
     *
     * @return true, if is string
     */
    public boolean isString()
    {
        return myData.getColumnClass() == ColumnClass.STRING;
    }

    /**
     * Sets the data.
     *
     * @param data the new data
     */
    public void setData(ColumnAnalyzerData data)
    {
        myData = data;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(myData.toString());
        return sb.toString();
    }

    /**
     * Check double for infinity and NaN (Not a number).
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
            myData.decrementDoubleCount();
            myData.setAllDoubles(false);
        }
        return doubleSuccess;
    }

    /**
     * Check float for infinity and NaN and precision.
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
            myData.decrementFloatCount();
            myData.setAllFloats(false);
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
                myData.incrementBooleanCount();
            }
            else
            {
                myData.setAllBooleans(false);
            }
        }
    }

    /**
     * Consider for unique values.
     *
     * @param value the value
     */
    private void considerForUniqueValues(String value)
    {
        if (myData.getUniqueValueCount() < MAX_UNIQUEVALUES_TO_CONSIDER)
        {
            if (myData.addUniqueValue(value))
            {
                myData.incrementUniqueValueCount();
            }
        }
        else
        {
            myData.setUniqueValueCount(ColumnAnalyzerData.MAX_UNIQUE_REACHED);
            myData.clearUniqueValueSet();
        }
    }
}
