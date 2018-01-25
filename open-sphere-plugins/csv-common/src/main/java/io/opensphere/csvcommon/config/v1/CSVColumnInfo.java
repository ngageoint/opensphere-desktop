package io.opensphere.csvcommon.config.v1;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * The Class ColumnInfo.
 */
@XmlRootElement(name = "ColumnInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class CSVColumnInfo implements Cloneable
{
    /** The Class name. */
    @XmlAttribute(name = "className")
    private String myClassName;

    /** The Unique value count. */
    @XmlAttribute(name = "uniqueValueCount", required = false)
    private int myUniqueValueCount;

    /** The Num samples considered. */
    @XmlAttribute(name = "numSamplesConsidered", required = false)
    private int myNumSamplesConsidered;

    /** The Is enum candidate. */
    @XmlAttribute(name = "isEnumCandidate", required = false)
    private boolean myIsEnumCandidate;

    /**
     * Instantiates a new column info.
     */
    public CSVColumnInfo()
    {
    }

    /**
     * Instantiates a new column info.
     *
     * @param className The Class name
     * @param uniqueValueCount The Unique value count
     * @param numSamplesConsidered The Num samples considered
     * @param isEnumCandidate The Is enum candidate
     */
    public CSVColumnInfo(String className, int uniqueValueCount, int numSamplesConsidered, boolean isEnumCandidate)
    {
        myClassName = className;
        myUniqueValueCount = uniqueValueCount;
        myNumSamplesConsidered = numSamplesConsidered;
        myIsEnumCandidate = isEnumCandidate;
    }

    /**
     * Instantiates a new column info.
     *
     * @param className the class name
     */
    public CSVColumnInfo(String className)
    {
        myClassName = className;
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
        CSVColumnInfo other = (CSVColumnInfo)obj;
        //@formatter:off
        return Objects.equals(myClassName, other.myClassName)
                && myIsEnumCandidate == other.myIsEnumCandidate
                && myNumSamplesConsidered == other.myNumSamplesConsidered
                && myUniqueValueCount == other.myUniqueValueCount;
        //@formatter:on
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    public final String getClassName()
    {
        return myClassName;
    }

    /**
     * Gets the num samples considered.
     *
     * @return the num samples considered
     */
    public final int getNumSamplesConsidered()
    {
        return myNumSamplesConsidered;
    }

    /**
     * Gets the unique value count.
     *
     * @return the unique value count
     */
    public final int getUniqueValueCount()
    {
        return myUniqueValueCount;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myClassName);
        result = prime * result + HashCodeHelper.getHashCode(myIsEnumCandidate);
        result = prime * result + HashCodeHelper.getHashCode(myNumSamplesConsidered);
        result = prime * result + HashCodeHelper.getHashCode(myUniqueValueCount);
        return result;
    }

    /**
     * Checks if is checks if is enum candidate.
     *
     * @return true, if is checks if is enum candidate
     */
    public final boolean isIsEnumCandidate()
    {
        return myIsEnumCandidate;
    }

    /**
     * Sets the class name.
     *
     * @param className the new class name
     */
    public final void setClassName(String className)
    {
        myClassName = className;
    }

    /**
     * Sets the equal to.
     *
     * @param other the new equal to
     */
    public void setEqualTo(CSVColumnInfo other)
    {
        myClassName = other.myClassName;
        myUniqueValueCount = other.myUniqueValueCount;
        myNumSamplesConsidered = other.myNumSamplesConsidered;
        myIsEnumCandidate = other.myIsEnumCandidate;
    }

    /**
     * Sets the checks if is enum candidate.
     *
     * @param isEnumCandidate the new checks if is enum candidate
     */
    public final void setIsEnumCandidate(boolean isEnumCandidate)
    {
        myIsEnumCandidate = isEnumCandidate;
    }

    /**
     * Sets the num samples considered.
     *
     * @param numSamplesConsidered the new num samples considered
     */
    public final void setNumSamplesConsidered(int numSamplesConsidered)
    {
        myNumSamplesConsidered = numSamplesConsidered;
    }

    /**
     * Sets the unique value count.
     *
     * @param uniqueValueCount the new unique value count
     */
    public final void setUniqueValueCount(int uniqueValueCount)
    {
        myUniqueValueCount = uniqueValueCount;
    }

    @Override
    public CSVColumnInfo clone()
    {
        try
        {
            CSVColumnInfo result = (CSVColumnInfo)super.clone();
            return result;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}
