package io.opensphere.mantle.util.columnanalyzer;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * The Class ColumnAnalyzerData.
 */
@XmlRootElement(name = "ColumnAnalyzerData")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.GodClass")
public class ColumnAnalyzerData
{
    /** The Constant DETERMINATION_THRESHOLD. */
    public static final int DETERMINATION_THRESHOLD = 10000;

    /** The Constant MAX_UNIQUE_REACHED. */
    public static final int MAX_UNIQUE_REACHED = 1000;

    /** The All booleans. */
    @XmlAttribute(name = "allBooleans")
    private boolean myAllBooleans = true;

    /** The all doubles. */
    @XmlAttribute(name = "allDoubles")
    private boolean myAllDoubles = true;

    /** The all floats. */
    @XmlAttribute(name = "myAllFloats")
    private boolean myAllFloats = true;

    /** The all integers. */
    @XmlAttribute(name = "allIntegers")
    private boolean myAllIntegers = true;

    /** The all longs. */
    @XmlAttribute(name = "allLongs")
    private boolean myAllLongs = true;

    /** The integer count. */
    @XmlAttribute(name = "booleanCount")
    private int myBooleanCount;

    /** The Column class. */
    @XmlElement(name = "columnClass")
    private ColumnClass myColumnClass = ColumnClass.STRING;

    /** The Column name. */
    @XmlElement(name = "columnName")
    private String myColumnName;

    /** The Double count. */
    @XmlAttribute(name = "doubleCount")
    private int myDoubleCount;

    /** The float count. */
    @XmlAttribute(name = "floatCount")
    private int myFloatCount;

    /** The integer count. */
    @XmlAttribute(name = "intCount")
    private int myIntCount;

    /** The long count. */
    @XmlAttribute(name = "longCount")
    private int myLongCount;

    /** The number of values considered. */
    @XmlElement(name = "numValsConsidered")
    private int myNumValuesConsidered;

    /** The Total values processed. */
    @XmlElement(name = "totalValuesProcessed")
    private int myTotalValuesProcessed;

    /** The Type name. */
    @XmlElement(name = "typeName")
    private String myTypeName;

    /** The Unique value count. */
    @XmlElement(name = "uniqueValueCount")
    private int myUniqueValueCount;

    /** The Unique values set. */
    @XmlElement(name = "uniqueValue", required = false)
    private Set<String> myUniqueValuesSet;

    /**
     * Instantiates a new column analyzer data.
     */
    public ColumnAnalyzerData()
    {
        myUniqueValuesSet = New.set();
    }

    /**
     * Instantiates a new column analyzer data.
     *
     * @param other the other
     */
    public ColumnAnalyzerData(ColumnAnalyzerData other)
    {
        this();
        setEqualTo(other);
    }

    /**
     * Instantiates a new column analyzer data.
     *
     * @param typeName the type name
     * @param columnName the column name
     */
    public ColumnAnalyzerData(String typeName, String columnName)
    {
        this();
        myTypeName = typeName;
        myColumnName = columnName;
    }

    /**
     * Adds the unique value.
     *
     * @param value the value to add
     * @return true, if the value was not already in the unique value set.
     */
    public final boolean addUniqueValue(String value)
    {
        return myUniqueValuesSet.add(value);
    }

    /**
     * Clear unique value set.
     */
    public void clearUniqueValueSet()
    {
        myUniqueValuesSet.clear();
    }

    /**
     * Decrement double count.
     */
    public void decrementDoubleCount()
    {
        myDoubleCount--;
    }

    /**
     * Decrement float count.
     */
    public void decrementFloatCount()
    {
        myFloatCount--;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ColumnAnalyzerData other = (ColumnAnalyzerData)obj;
        return EqualsHelper.equals(myColumnName, other.myColumnName, myTypeName, other.myTypeName)
                && myAllBooleans == other.myAllBooleans && myAllDoubles == other.myAllDoubles && myAllFloats == other.myAllFloats
                && myAllIntegers == other.myAllIntegers && myAllLongs == other.myAllLongs
                && myBooleanCount == other.myBooleanCount && myColumnClass == other.myColumnClass
                && myDoubleCount == other.myDoubleCount && myFloatCount == other.myFloatCount && myIntCount == other.myIntCount
                && myLongCount == other.myLongCount && myNumValuesConsidered == other.myNumValuesConsidered
                && myTotalValuesProcessed == other.myTotalValuesProcessed && myUniqueValueCount == other.myUniqueValueCount
                && EqualsHelper.equals(myUniqueValuesSet, other.myUniqueValuesSet);
    }

    /**
     * Gets the boolean count.
     *
     * @return the boolean count
     */
    public final int getBooleanCount()
    {
        return myBooleanCount;
    }

    /**
     * Gets the column class.
     *
     * @return the column class
     */
    public final ColumnClass getColumnClass()
    {
        return myColumnClass;
    }

    /**
     * Gets the column name.
     *
     * @return the column name
     */
    public final String getColumnName()
    {
        return myColumnName;
    }

    /**
     * Gets the double count.
     *
     * @return the double count
     */
    public final int getDoubleCount()
    {
        return myDoubleCount;
    }

    /**
     * Gets the float count.
     *
     * @return the float count
     */
    public final int getFloatCount()
    {
        return myFloatCount;
    }

    /**
     * Gets the int count.
     *
     * @return the int count
     */
    public final int getIntCount()
    {
        return myIntCount;
    }

    /**
     * Gets the long count.
     *
     * @return the long count
     */
    public final int getLongCount()
    {
        return myLongCount;
    }

    /**
     * Gets the num values considered.
     *
     * @return the num values considered
     */
    public final int getNumValuesConsidered()
    {
        return myNumValuesConsidered;
    }

    /**
     * Gets the total values processed.
     *
     * @return the total values processed
     */
    public final int getTotalValuesProcessed()
    {
        return myTotalValuesProcessed;
    }

    /**
     * Gets the type name.
     *
     * @return the type name
     */
    public final String getTypeName()
    {
        return myTypeName;
    }

    /**
     * Gets the unique value count.
     *
     * @return the unique value count
     */
    public int getUniqueValueCount()
    {
        return myUniqueValueCount;
    }

    /**
     * Gets the unique values set.
     *
     * @return the unique values set
     */
    public final Set<String> getUniqueValuesSet()
    {
        return myUniqueValuesSet;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myAllBooleans);
        result = prime * result + HashCodeHelper.getHashCode(myAllDoubles);
        result = prime * result + HashCodeHelper.getHashCode(myAllFloats);
        result = prime * result + HashCodeHelper.getHashCode(myAllIntegers);
        result = prime * result + HashCodeHelper.getHashCode(myAllLongs);
        result = prime * result + myBooleanCount;
        result = prime * result + HashCodeHelper.getHashCode(myColumnClass);
        result = prime * result + HashCodeHelper.getHashCode(myColumnName);
        result = prime * result + myDoubleCount;
        result = prime * result + myFloatCount;
        result = prime * result + myIntCount;
        result = prime * result + myLongCount;
        result = prime * result + myNumValuesConsidered;
        result = prime * result + myTotalValuesProcessed;
        result = prime * result + myUniqueValueCount;
        result = prime * result + HashCodeHelper.getHashCode(myTypeName);
        result = prime * result + HashCodeHelper.getHashCode(myUniqueValuesSet);
        return result;
    }

    /**
     * Increment boolean count.
     */
    public void incrementBooleanCount()
    {
        myBooleanCount++;
    }

    /**
     * Increment double count.
     */
    public void incrementDoubleCount()
    {
        myDoubleCount++;
    }

    /**
     * Increment float count.
     */
    public void incrementFloatCount()
    {
        myFloatCount++;
    }

    /**
     * Increment int count.
     */
    public void incrementIntCount()
    {
        myIntCount++;
    }

    /**
     * Increment long count.
     */
    public final void incrementLongCount()
    {
        myLongCount++;
    }

    /**
     * Increment num values considered.
     */
    public void incrementNumValuesConsidered()
    {
        myNumValuesConsidered++;
    }

    /**
     * Increment total values processed.
     */
    public void incrementTotalValuesProcessed()
    {
        myTotalValuesProcessed++;
    }

    /**
     * Increment unique value count.
     */
    public void incrementUniqueValueCount()
    {
        myUniqueValueCount++;
    }

    /**
     * Checks if is all booleans.
     *
     * @return true, if is all booleans
     */
    public final boolean isAllBooleans()
    {
        return myAllBooleans;
    }

    /**
     * Checks if is all doubles.
     *
     * @return true, if is all doubles
     */
    public final boolean isAllDoubles()
    {
        return myAllDoubles;
    }

    /**
     * Checks if is all floats.
     *
     * @return true, if is all floats
     */
    public final boolean isAllFloats()
    {
        return myAllFloats;
    }

    /**
     * Checks if is all integers.
     *
     * @return true, if is all integers
     */
    public final boolean isAllIntegers()
    {
        return myAllIntegers;
    }

    /**
     * Checks if is all longs.
     *
     * @return true, if is all longs
     */
    public final boolean isAllLongs()
    {
        return myAllLongs;
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

    /**
     * Must be string if values have been considered and none-were convertible
     * to boolean or any other numeric type.
     *
     * @return true, if must be string.
     */
    public boolean mustBeString()
    {
        return myNumValuesConsidered > 0 && !myAllBooleans && !myAllDoubles && !myAllFloats && !myAllIntegers && !myAllLongs;
    }

    /**
     * Sets the all booleans.
     *
     * @param allBooleans the new all booleans
     */
    public final void setAllBooleans(boolean allBooleans)
    {
        myAllBooleans = allBooleans;
    }

    /**
     * Sets the all doubles.
     *
     * @param allDoubles the new all doubles
     */
    public final void setAllDoubles(boolean allDoubles)
    {
        myAllDoubles = allDoubles;
    }

    /**
     * Sets the all floats.
     *
     * @param allFloats the new all floats
     */
    public final void setAllFloats(boolean allFloats)
    {
        myAllFloats = allFloats;
    }

    /**
     * Sets the all integers.
     *
     * @param allIntegers the new all integers
     */
    public final void setAllIntegers(boolean allIntegers)
    {
        myAllIntegers = allIntegers;
    }

    /**
     * Sets the all longs.
     *
     * @param allLongs the new all longs
     */
    public final void setAllLongs(boolean allLongs)
    {
        myAllLongs = allLongs;
    }

    /**
     * Sets the boolean count.
     *
     * @param booleanCount the new boolean count
     */
    public final void setBooleanCount(int booleanCount)
    {
        myBooleanCount = booleanCount;
    }

    /**
     * Sets the column class.
     *
     * @param columnClass the new column class
     */
    public final void setColumnClass(ColumnClass columnClass)
    {
        myColumnClass = columnClass == null ? ColumnClass.STRING : columnClass;
    }

    /**
     * Sets the column name.
     *
     * @param columnName the new column name
     */
    public final void setColumnName(String columnName)
    {
        myColumnName = columnName;
    }

    /**
     * Sets the double count.
     *
     * @param doubleCount the new double count
     */
    public final void setDoubleCount(int doubleCount)
    {
        myDoubleCount = doubleCount;
    }

    /**
     * Sets the equal to.
     *
     * @param other the new equal to
     */
    public final void setEqualTo(ColumnAnalyzerData other)
    {
        myTypeName = other.myTypeName;
        myColumnName = other.myColumnName;
        myColumnClass = other.myColumnClass;
        myAllDoubles = other.myAllDoubles;
        myDoubleCount = other.myDoubleCount;
        myAllFloats = other.myAllFloats;
        myFloatCount = other.myFloatCount;
        myAllLongs = other.myAllLongs;
        myAllIntegers = other.myAllIntegers;
        myLongCount = other.myLongCount;
        myAllLongs = other.myAllLongs;
        myBooleanCount = other.myBooleanCount;
        myAllBooleans = other.myAllBooleans;
        myNumValuesConsidered = other.myNumValuesConsidered;
        myTotalValuesProcessed = other.myTotalValuesProcessed;
        myUniqueValueCount = other.myUniqueValueCount;
        myUniqueValuesSet = New.set(other.myUniqueValuesSet);
    }

    /**
     * Sets the float count.
     *
     * @param floatCount the new float count
     */
    public final void setFloatCount(int floatCount)
    {
        myFloatCount = floatCount;
    }

    /**
     * Sets the int count.
     *
     * @param intCount the new int count
     */
    public final void setIntCount(int intCount)
    {
        myIntCount = intCount;
    }

    /**
     * Sets the long count.
     *
     * @param longCount the new long count
     */
    public final void setLongCount(int longCount)
    {
        myLongCount = longCount;
    }

    /**
     * Sets the num values considered.
     *
     * @param numValuesConsidered the new num values considered
     */
    public final void setNumValuesConsidered(int numValuesConsidered)
    {
        myNumValuesConsidered = numValuesConsidered;
    }

    /**
     * Sets the total values processed.
     *
     * @param totalValuesProcessed the new total values processed
     */
    public final void setTotalValuesProcessed(int totalValuesProcessed)
    {
        myTotalValuesProcessed = totalValuesProcessed;
    }

    /**
     * Sets the type name.
     *
     * @param typeName the new type name
     */
    public final void setTypeName(String typeName)
    {
        myTypeName = typeName;
    }

    /**
     * Sets the unique value count.
     *
     * @param count the new unique value count
     */
    public void setUniqueValueCount(int count)
    {
        myUniqueValueCount = count;
    }

    /**
     * Sets the unique value set.
     *
     * @param values the new unique value set
     */
    public final void setUniqueValueSet(Set<String> values)
    {
        myUniqueValuesSet = values == null ? New.<String>set() : New.set(values);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Type[").append(myTypeName).append("] Column[").append(myColumnName).append("]\n   Samples Considered[")
                .append(myNumValuesConsidered).append("] Unique[").append(getUniqueValueCount()).append("] Total[")
                .append(myTotalValuesProcessed).append("]\n" + "   Determined Class[").append(getColumnClass())
                .append("]\n" + "   Double: ").append(myAllDoubles ? "ALL" : "NOT_ALL").append(" Count: ").append(myDoubleCount)
                .append("\n" + "   Float : ").append(myAllFloats ? "ALL" : "NOT_ALL").append(" Count: ").append(myFloatCount)
                .append("\n" + "   Long  : ").append(myAllLongs ? "ALL" : "NOT_ALL").append(" Count: ").append(myLongCount)
                .append("\n" + "   Int   : ").append(myAllIntegers ? "ALL" : "NOT_ALL").append(" Count: ").append(myIntCount)
                .append("\n" + "   Bool  : ").append(myAllBooleans ? "ALL" : "NOT_ALL").append(" Count: ").append(myBooleanCount)
                .append('\n');
        uniqueValuesToString(sb);
        return sb.toString();
    }

    /**
     * Unique values to string.
     *
     * @param sb the sb
     */
    private void uniqueValuesToString(StringBuilder sb)
    {
        sb.append("   Unique Values: Count: ").append(
                getUniqueValueCount() == 1000 ? "COUNT EXCEEDS TRACKING THRESHOLD" : Integer.valueOf(getUniqueValueCount()));
        if (getUniqueValueCount() > 0 && getUniqueValueCount() != 1000)
        {
            sb.append('\n');
            StringBuilder sb2 = null;
            for (String val : myUniqueValuesSet)
            {
                if (sb2 == null)
                {
                    sb2 = new StringBuilder();
                    sb2.append("          ");
                }
                sb2.append(val).append(", ");
                if (sb2.length() > 80)
                {
                    sb.append(sb2).append('\n');
                    sb2 = null;
                }
            }
            if (sb2 != null)
            {
                sb2.append('\n');
                sb.append(sb2);
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    /**
     * The Enum BooleanDetermination.
     */
    public enum BooleanDetermination
    {
        /** BOOLEAN_FALSE. */
        BOOLEAN_FALSE,

        /** BOOLEAN_TRUE. */
        BOOLEAN_TRUE,

        /** UNDETERMINED. */
        UNDETERMINED;

        /**
         * Checks if is boolean.
         *
         * @return true, if is boolean
         */
        public boolean isBoolean()
        {
            return this == BOOLEAN_FALSE || this == BOOLEAN_TRUE;
        }
    }

    /**
     * The Enum ColumnClass.
     */
    public enum ColumnClass
    {
        /** The BOOLEAN. */
        BOOLEAN(Boolean.class),

        /** The DOUBLE. */
        DOUBLE(Double.class),

        /** The FLOAT. */
        FLOAT(Float.class),

        /** The INTEGER. */
        INTEGER(Integer.class),

        /** The LONG. */
        LONG(Long.class),

        /** The STRING. */
        STRING(String.class);

        /** The Class. */
        private final Class<?> myClass;

        /**
         * Instantiates a new column class.
         *
         * @param colClass the col class
         */
        ColumnClass(Class<?> colClass)
        {
            myClass = colClass;
        }

        /**
         * Gets the representative class.
         *
         * @return the representative class
         */
        public Class<?> getRepresentativeClass()
        {
            return myClass;
        }
    }
}
