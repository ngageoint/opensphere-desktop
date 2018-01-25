package io.opensphere.csvcommon.config.v2;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;

/** The column format for a CSV file. */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "fixedWidthColumnFormat")
public class CSVFixedWidthColumnFormat extends CSVColumnFormat
{
    /** The column divisions. */
    @XmlElement(name = "columnDivisions")
    private int[] myColumnDivisions;

    /**
     * Constructor.
     *
     * @param columnDivisions The indices where column divisions occur.
     */
    public CSVFixedWidthColumnFormat(int[] columnDivisions)
    {
        myColumnDivisions = Utilities.checkNull(columnDivisions, "columnDivisions").clone();
    }

    /** JAXB constructor. */
    protected CSVFixedWidthColumnFormat()
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
        CSVFixedWidthColumnFormat other = (CSVFixedWidthColumnFormat)obj;
        return Arrays.equals(myColumnDivisions, other.myColumnDivisions);
    }

    /**
     * Get the column indices where column divisions occur.
     *
     * @return The indices where column divisions occur.
     */
    public int[] getColumnDivisions()
    {
        return myColumnDivisions.clone();
    }

    @Override
    public int hashCode()
    {
        return 31 + Arrays.hashCode(myColumnDivisions);
    }

    /**
     * Set the column widths.
     *
     * @param columnDivisions The new indices where column divisions occur.
     */
    public void setColumnWidths(int[] columnDivisions)
    {
        myColumnDivisions = Utilities.checkNull(columnDivisions, "columnDivisions").clone();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(64);
        builder.append("CSVFixedWidthColumnFormat [columnDivisions=");
        builder.append(Arrays.toString(myColumnDivisions));
        builder.append(']');
        return builder.toString();
    }

    @Override
    public CSVFixedWidthColumnFormat clone()
    {
        CSVFixedWidthColumnFormat result = (CSVFixedWidthColumnFormat)super.clone();
        result.myColumnDivisions = myColumnDivisions != null ? myColumnDivisions.clone() : null;
        return result;
    }
}
