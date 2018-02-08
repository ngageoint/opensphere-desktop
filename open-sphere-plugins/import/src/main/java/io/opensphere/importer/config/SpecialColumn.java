package io.opensphere.importer.config;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.mantle.data.SpecialKey;

/**
 * Base class for classes that identify columns with special handling.
 *
 * Note: the XML root element has CSV in the name for legacy compatibility.
 */
@XmlRootElement(name = "CSVSpecialColumn")
@XmlAccessorType(XmlAccessType.NONE)
public class SpecialColumn implements Cloneable
{
    /** The column index. */
    @XmlAttribute(name = "column")
    private int myColumnIndex;

    /**
     * The column type.
     */
    @XmlAttribute(name = "columnType")
    private ColumnType myColumnType;

    /**
     * If applicable the format the data is stored within the source.
     */
    @XmlAttribute(name = "format")
    private String myFormat;

    /** The optional special key. */
    @XmlTransient
    private SpecialKey mySpecialKey;

    /**
     * JAXB Constructor.
     */
    public SpecialColumn()
    {
    }

    /**
     * Constructor.
     *
     * @param columnIndex The column index
     * @param columnType The column type
     * @param format If applicable the format the data is stored within the
     *            source
     */
    public SpecialColumn(int columnIndex, ColumnType columnType, String format)
    {
        myColumnIndex = columnIndex;
        myColumnType = columnType;
        myFormat = format;
    }

    /**
     * Gets the column index.
     *
     * @return The column index.
     */
    public int getColumnIndex()
    {
        return myColumnIndex;
    }

    /**
     * Gets the column type.
     *
     * @return The column type.
     */
    public ColumnType getColumnType()
    {
        return myColumnType;
    }

    /**
     * Gets the format.
     *
     * @return The format.
     */
    public String getFormat()
    {
        return myFormat;
    }

    /**
     * Sets the column index.
     *
     * @param columnIndex The column index.
     */
    public void setColumnIndex(int columnIndex)
    {
        myColumnIndex = columnIndex;
    }

    /**
     * Sets the column type.
     *
     * @param columnType The column type.
     */
    public void setColumnType(ColumnType columnType)
    {
        myColumnType = columnType;
    }

    /**
     * Sets the format.
     *
     * @param format The format.
     */
    public void setFormat(String format)
    {
        myFormat = format;
    }

    /**
     * Gets the special key.
     *
     * @return The special key.
     */
    public SpecialKey getSpecialKey()
    {
        return mySpecialKey;
    }

    /**
     * Sets the special key.
     *
     * @param specialKey The special key.
     */
    public void setSpecialKey(SpecialKey specialKey)
    {
        mySpecialKey = specialKey;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myColumnIndex);
        result = prime * result + HashCodeHelper.getHashCode(myColumnType);
        result = prime * result + HashCodeHelper.getHashCode(myFormat);
        return result;
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
        SpecialColumn other = (SpecialColumn)obj;
        //@formatter:off
        return myColumnIndex == other.myColumnIndex
                && Objects.equals(myColumnType, other.myColumnType)
                && Objects.equals(myFormat, other.myFormat);
        //@formatter:on
    }

    @Override
    public SpecialColumn clone()
    {
        try
        {
            return (SpecialColumn)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}
