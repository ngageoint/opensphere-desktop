package io.opensphere.csvcommon.config.v2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.EqualsHelper;

/** Format for delimited columns in a CSV file. */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "delimitedColumnFormat")
public class CSVDelimitedColumnFormat extends CSVColumnFormat
{
    /** The number of columns. */
    @XmlElement(name = "columnCount")
    private int myColumnCount;

    /** The text delimiter. */
    @XmlElement(name = "textDelimiter")
    private String myTextDelimiter;

    /** The token delimiter. */
    @XmlElement(name = "tokenDelimiter")
    private String myTokenDelimiter;

    /**
     * Constructor.
     *
     * @param tokenDelimiter The token delimiter.
     * @param textDelimiter The text delimiter.
     * @param columnCount The number of columns.
     */
    public CSVDelimitedColumnFormat(String tokenDelimiter, String textDelimiter, int columnCount)
    {
        myTokenDelimiter = tokenDelimiter;
        myTextDelimiter = textDelimiter;
        myColumnCount = columnCount;
    }

    /** JAXB constructor. */
    protected CSVDelimitedColumnFormat()
    {
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
        CSVDelimitedColumnFormat other = (CSVDelimitedColumnFormat)obj;
        if (myColumnCount != other.myColumnCount)
        {
            return false;
        }
        return EqualsHelper.equals(myTextDelimiter, other.myTextDelimiter, myTokenDelimiter, other.myTokenDelimiter);
    }

    /**
     * Gets the column count.
     *
     * @return The number of columns.
     */
    public int getColumnCount()
    {
        return myColumnCount;
    }

    /**
     * Gets the text delimiter.
     *
     * @return The text delimiter.
     */
    public String getTextDelimiter()
    {
        return myTextDelimiter;
    }

    /**
     * Gets the token delimiter.
     *
     * @return The token delimiter.
     */
    public String getTokenDelimiter()
    {
        return myTokenDelimiter;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myColumnCount;
        result = prime * result + (myTextDelimiter == null ? 0 : myTextDelimiter.hashCode());
        result = prime * result + (myTokenDelimiter == null ? 0 : myTokenDelimiter.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(128);
        builder.append("CSVDelimitedColumnFormat [columnCount=");
        builder.append(myColumnCount);
        builder.append(", textDelimiter=");
        builder.append(myTextDelimiter);
        builder.append(", tokenDelimiter=");
        builder.append(myTokenDelimiter);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public CSVDelimitedColumnFormat clone()
    {
        return (CSVDelimitedColumnFormat)super.clone();
    }
}
