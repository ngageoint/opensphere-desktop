package io.opensphere.csvcommon.detect.location.model;

import java.util.Objects;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.importer.config.ColumnType;

/**
 * The PotentialLocationColumn class identifies columns that are potentially
 * location columnms and attempts to store a confidence value for each also.
 */
public class PotentialLocationColumn
{
    /** The column name. */
    private String myColumnName;

    /** The column type. */
    private ColumnType myType;

    /** The matched column name prefix. */
    private final String myPrefix;

    /** The matched column name suffix. */
    private final String mySuffix;

    /**
     * If true, indicated that this is most likely not an abbreviated column
     * name .
     */
    private final boolean myIsLongName;

    /** The index of the identified column in the set of header columns. */
    private final int myColumnIndex;

    /** The format of the latitude data associated with this column. */
    private CoordFormat myLatFormat;

    /** The format of the longitude data associated with this column. */
    private CoordFormat myLonFormat;

    /**
     * If this column is not a latitutde or longitude column use location
     * format.
     */
    private CoordFormat myLocationFormat;

    /** The Confidence value. 1.0 is the highest. */
    private float myConfidence;

    /**
     * Instantiates a new potential location column.
     *
     * @param colName the col name
     * @param type the type
     * @param prefix the prefix and suffix are used to determine the confidence
     *            of columns where a well known name appears within a column
     *            name.
     * @param suffix the suffix
     * @param isLongName the long name is a non abbreviated name, ie LATITUDE
     *            vs. LAT
     * @param index the index
     */
    public PotentialLocationColumn(String colName, ColumnType type, String prefix, String suffix, boolean isLongName, int index)
    {
        myColumnName = colName;
        myType = type;
        myPrefix = prefix;
        mySuffix = suffix;
        myIsLongName = isLongName;
        myColumnIndex = index;
    }

    /**
     * Gets the column name.
     *
     * @return the column name
     */
    public String getColumnName()
    {
        return myColumnName;
    }

    /**
     * Sets the column name.
     *
     * @param columnName the new column name
     */
    public void setColumnName(String columnName)
    {
        myColumnName = columnName;
    }

    /**
     * Gets the column type.
     *
     * @return the type
     */
    public ColumnType getType()
    {
        return myType;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(ColumnType type)
    {
        myType = type;
    }

    /**
     * Gets the matched column name prefix.
     *
     * @return the prefix
     */
    public String getPrefix()
    {
        return myPrefix;
    }

    /**
     * Gets the matched column name suffix.
     *
     * @return the suffix
     */
    public String getSuffix()
    {
        return mySuffix;
    }

    /**
     * Checks if this column name is abbreviated.
     *
     * @return true, if it is not an abbreviation
     */
    public boolean isLongName()
    {
        return myIsLongName;
    }

    /**
     * Gets the column index.
     *
     * @return the column index
     */
    public int getColumnIndex()
    {
        return myColumnIndex;
    }

    /**
     * Sets the format of the column's latitude data.
     *
     * @param format the new format
     */
    public void setLatFormat(CoordFormat format)
    {
        if (format == null || !format.equals(myLatFormat))
        {
            myLatFormat = format;
        }
    }

    /**
     * Gets the format of the column's latitude data.
     *
     * @return the format
     */
    public CoordFormat getLatFormat()
    {
        return myLatFormat;
    }

    /**
     * Sets the format of the column's longitude data.
     *
     * @param format the new format
     */
    public void setLonFormat(CoordFormat format)
    {
        if (format == null || !format.equals(myLonFormat))
        {
            myLonFormat = format;
        }
    }

    /**
     * Sets the location format.
     *
     * @param format the new location format
     */
    public void setLocationFormat(CoordFormat format)
    {
        myLocationFormat = format;
    }

    /**
     * Gets the format of the column's latitude data.
     *
     * @return the format
     */
    public CoordFormat getLonFormat()
    {
        return myLonFormat;
    }

    /**
     * Gets the location format.
     *
     * @return the location format
     */
    public CoordFormat getLocationFormat()
    {
        return myLocationFormat;
    }

    /**
     * Gets the confidence.
     *
     * @return the confidence
     */
    public float getConfidence()
    {
        return myConfidence;
    }

    /**
     * Sets the confidence.
     *
     * @param confidence the new confidence
     */
    public void setConfidence(float confidence)
    {
        myConfidence = confidence;
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
        PotentialLocationColumn other = (PotentialLocationColumn)obj;
        //@formatter:off
        return Objects.equals(myColumnName, other.myColumnName)
                && myIsLongName == other.myIsLongName
                && Objects.equals(myType, other.myType)
                && Objects.equals(myPrefix, other.myPrefix)
                && Objects.equals(mySuffix, other.mySuffix)
                && myColumnIndex == other.myColumnIndex
                && Objects.equals(myLatFormat, other.myLatFormat)
                && Objects.equals(myLonFormat, other.myLonFormat);
        //@formatter:on
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myColumnName);
        result = prime * result + HashCodeHelper.getHashCode(myIsLongName);
        result = prime * result + HashCodeHelper.getHashCode(myType);
        result = prime * result + HashCodeHelper.getHashCode(myPrefix);
        result = prime * result + HashCodeHelper.getHashCode(mySuffix);
        result = prime * result + HashCodeHelper.getHashCode(myColumnIndex);
        result = prime * result + HashCodeHelper.getHashCode(myLatFormat);
        result = prime * result + HashCodeHelper.getHashCode(myLonFormat);
        return result;
    }
}
