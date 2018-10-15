package io.opensphere.csvcommon.config.v2;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Set;
import java.util.function.Predicate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.csvcommon.config.v1.CSVColumnInfo;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.ColumnType.Category;
import io.opensphere.importer.config.SpecialColumn;

/**
 * Parameters for parsing a CSV document.
 */
@XmlRootElement(name = "CSVParseParameters")
@XmlAccessorType(XmlAccessType.NONE)
@SuppressWarnings("PMD.GodClass")
@XmlSeeAlso({CSVDelimitedColumnFormat.class, CSVFixedWidthColumnFormat.class})
public class CSVParseParameters extends Observable implements Cloneable
{
    /** The format of the columns. */
    @XmlElementRef
    private CSVColumnFormat myColumnFormat;

    /** The column names. */
    @XmlElement(name = "name", required = false)
    @XmlElementWrapper(name = "columnNames")
    private List<? extends String> myColumnNames = New.list();

    /** A string signaling a line to be ignored. */
    @XmlElement(name = "commentIndicator", required = false)
    private String myCommentIndicator = "#";

    /** The index of the line containing the first row of data. */
    @XmlElement(name = "dataStartLine")
    private Integer myDataStartLine;

    /** The column header line number. */
    @XmlElement(name = "headerLine")
    private Integer myHeaderLine;

    /** The special columns. */
    @XmlElementRef
    private Set<SpecialColumn> mySpecialColumns = New.set();

    /** The column indices to ignore on import. */
    @XmlElement(name = "columnsToIgnore", required = false)
    @XmlList
    private List<Integer> myColumnsToIgnore = New.list();

    /** The column names. */
    @XmlElement(name = "ColumnInfo", required = false)
    private List<CSVColumnInfo> myColumnInfo = New.list();

    /** The Constant COLUMNFORMAT. */
    @XmlTransient
    public static final String COLUMNFORMAT = "COLUMNFORMAT";

    /** The Constant COMMENTINDICATOR. */
    @XmlTransient
    public static final String COMMENTINDICATOR = "COMMENTINDICATOR";

    /** The Constant DATASTARTLINE. */
    @XmlTransient
    public static final String DATASTARTLINE = "DATASTARTLINE";

    /** The Constant HEADERLINE. */
    @XmlTransient
    public static final String HEADERLINE = "HEADERLINE";

    /** The Fire updates. */
    @XmlTransient
    private boolean myFireUpdates = true;

    /**
     * Constructor.
     */
    public CSVParseParameters()
    {
    }

    /**
     * Copy constructor.
     *
     * @param parameters the parameters from which to copy
     */
    public CSVParseParameters(CSVParseParameters parameters)
    {
        super();
        myColumnFormat = parameters.myColumnFormat == null ? null : parameters.myColumnFormat.clone();
        myColumnNames = New.list(parameters.myColumnNames);
        myCommentIndicator = parameters.myCommentIndicator;
        myDataStartLine = parameters.myDataStartLine;
        myHeaderLine = parameters.myHeaderLine;
        mySpecialColumns = New.set(StreamUtilities.map(parameters.mySpecialColumns, specialColumn -> specialColumn.clone()));
        myColumnsToIgnore = New.list(parameters.myColumnsToIgnore);
        myColumnInfo = StreamUtilities.map(parameters.myColumnInfo, columnInfo -> columnInfo.clone());
    }

    /**
     * Get the column format for the file.
     *
     * @return The column format.
     */
    public CSVColumnFormat getColumnFormat()
    {
        return myColumnFormat;
    }

    /**
     * Gets the column names.
     *
     * @return the column names
     */
    public List<? extends String> getColumnNames()
    {
        return myColumnNames;
    }

    /**
     * Gets the comment indicator.
     *
     * @return the comment indicator
     */
    public String getCommentIndicator()
    {
        return myCommentIndicator;
    }

    /**
     * Gets the data start line.
     *
     * @return the data start line
     */
    public Integer getDataStartLine()
    {
        return myDataStartLine;
    }

    /**
     * Gets the header line.
     *
     * @return the header line
     */
    public Integer getHeaderLine()
    {
        return myHeaderLine;
    }

    /**
     * Get the definitions for columns that require special handling.
     *
     * @return The special columns.
     */
    public Set<SpecialColumn> getSpecialColumns()
    {
        return mySpecialColumns;
    }

    /**
     * Set the format for the columns in the CSV file.
     *
     * @param columnFormat The column format.
     */
    public void setColumnFormat(CSVColumnFormat columnFormat)
    {
        CSVColumnFormat previousFormat = myColumnFormat;
        myColumnFormat = columnFormat;
        if (myFireUpdates && hasColumnFormatChanged(previousFormat))
        {
            setChanged();
            this.notifyObservers(COLUMNFORMAT);
        }
    }

    /**
     * Checks to see if the format, or the column delimiter, or the text
     * delimiter has changed.
     *
     * @param columnFormat The changed column format.
     * @return True if the column format is changed, false otherwise.
     */
    private boolean hasColumnFormatChanged(CSVColumnFormat columnFormat)
    {
        boolean hasChanged = false;

        if (myColumnFormat instanceof CSVDelimitedColumnFormat && columnFormat instanceof CSVDelimitedColumnFormat)
        {
            CSVDelimitedColumnFormat currentDelimitedFormat = (CSVDelimitedColumnFormat)myColumnFormat;
            CSVDelimitedColumnFormat newDelimitedFormat = (CSVDelimitedColumnFormat)columnFormat;

            String currentTextDelimiter = currentDelimitedFormat.getTextDelimiter();
            String newTextDelimiter = newDelimitedFormat.getTextDelimiter();

            String currentTokenDelimiter = currentDelimitedFormat.getTokenDelimiter();
            String newTokenDelimiter = newDelimitedFormat.getTokenDelimiter();

            if (!Objects.equals(currentTextDelimiter, newTextDelimiter)
                    || !Objects.equals(currentTokenDelimiter, newTokenDelimiter))
            {
                hasChanged = true;
            }
        }
        else if (myColumnFormat instanceof CSVFixedWidthColumnFormat && columnFormat instanceof CSVFixedWidthColumnFormat)
        {
            CSVFixedWidthColumnFormat currentFixedFormat = (CSVFixedWidthColumnFormat)myColumnFormat;
            CSVFixedWidthColumnFormat newFixedFormat = (CSVFixedWidthColumnFormat)columnFormat;

            if (!Arrays.equals(currentFixedFormat.getColumnDivisions(), newFixedFormat.getColumnDivisions()))
            {
                hasChanged = true;
            }
        }
        else
        {
            hasChanged = true;
        }

        return hasChanged;
    }

    /**
     * Sets the column names.
     *
     * @param columnNames the new column names
     */
    public void setColumnNames(List<? extends String> columnNames)
    {
        myColumnNames = columnNames;
    }

    /**
     * Sets the comment indicator.
     *
     * @param commentIndicator the new comment indicator
     */
    public void setCommentIndicator(String commentIndicator)
    {
        if (!Objects.equals(myCommentIndicator, commentIndicator))
        {
            myCommentIndicator = commentIndicator;
            if (myFireUpdates)
            {
                setChanged();
                this.notifyObservers(COMMENTINDICATOR);
            }
        }
    }

    /**
     * Sets the data start line.
     *
     * @param dataStartLine the new data start line
     */
    public void setDataStartLine(Integer dataStartLine)
    {
        if (!Objects.equals(myDataStartLine, dataStartLine))
        {
            myDataStartLine = dataStartLine;
            if (myFireUpdates)
            {
                setChanged();
                this.notifyObservers(DATASTARTLINE);
            }
        }
    }

    /**
     * Sets the header line.
     *
     * @param headerLine the new header line
     */
    public void setHeaderLine(Integer headerLine)
    {
        if (!Objects.equals(myHeaderLine, headerLine))
        {
            myHeaderLine = headerLine;
            if (myFireUpdates)
            {
                setChanged();
                this.notifyObservers(HEADERLINE);
            }
        }
    }

    /**
     * Gets the column indices to ignore on import.
     *
     * @return The column indices to ignore on import.
     */
    public List<Integer> getColumnsToIgnore()
    {
        return myColumnsToIgnore;
    }

    /**
     * Sets the column indices to ignore on import.
     *
     * @param columnsToIgnore The column indices to ignore on import.
     */
    public void setColumnsToIgnore(List<Integer> columnsToIgnore)
    {
        myColumnsToIgnore = columnsToIgnore;
    }

    /**
     * Gets the column classes.
     *
     * @return the column classes
     */
    public List<CSVColumnInfo> getColumnClasses()
    {
        return myColumnInfo;
    }

    /**
     * Sets the column classes.
     *
     * @param columnClasses the new column classes
     */
    public void setColumnClasses(List<CSVColumnInfo> columnClasses)
    {
        if (columnClasses == null)
        {
            throw new IllegalArgumentException("Column Classes Cannot Be Null!");
        }
        myColumnInfo = columnClasses;
    }

    /**
     * Sets the fires updates.
     *
     * @param update true if updates are to be fired
     */
    public void setFiresUpdates(boolean update)
    {
        myFireUpdates = update;
    }

    /**
     * Determines whether the special keys contain the given column category.
     *
     * @param category the column category
     * @return whether the special keys contain the given column category
     */
    public boolean hasCategory(final Category category)
    {
        return mySpecialColumns.stream().anyMatch(specialColumn -> specialColumn.getColumnType() != null
                && specialColumn.getColumnType().getCategory() == category);
    }

    /**
     * Determines whether the special columns contain the given column type. The
     * column is not counted if it is in the columns to ignore.
     *
     * @param type the column type
     * @return whether the special columns contain the given column type
     */
    public boolean hasType(final ColumnType type)
    {
        return mySpecialColumns.stream().anyMatch(specialColumn -> specialColumn.getColumnType() == type
                && !getColumnsToIgnore().contains(Integer.valueOf(specialColumn.getColumnIndex())));
    }

    /**
     * Determines whether the special columns contain the given column type. The
     * column is not counted if it is in the columns to ignore.
     *
     * @param types the column types
     * @return whether the special columns contain the given column type
     */
    public boolean hasType(final ColumnType... types)
    {
        return mySpecialColumns.stream().anyMatch(new Predicate<SpecialColumn>()
        {
            @Override
            public boolean test(SpecialColumn specialColumn)
            {
                for (ColumnType type : types)
                {
                    if (specialColumn.getColumnType() == type
                            && !getColumnsToIgnore().contains(Integer.valueOf(specialColumn.getColumnIndex())))
                    {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Gets the special column for the given column type.
     *
     * @param columnType the column type
     * @return the special column, or null
     */
    public SpecialColumn getSpecialColumn(ColumnType columnType)
    {
        SpecialColumn specialColumn = null;
        for (SpecialColumn column : mySpecialColumns)
        {
            if (column.getColumnType() == columnType)
            {
                specialColumn = column;
                break;
            }
        }
        return specialColumn;
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
        CSVParseParameters other = (CSVParseParameters)obj;
        return EqualsHelper.equals(getColumnFormat(), other.getColumnFormat(), myColumnNames, other.myColumnNames,
                myCommentIndicator, other.myCommentIndicator, myDataStartLine, other.myDataStartLine, myHeaderLine,
                other.myHeaderLine, mySpecialColumns, other.mySpecialColumns, myColumnsToIgnore, other.myColumnsToIgnore,
                myColumnInfo, other.myColumnInfo);
    }

    @Override
    public int hashCode()
    {
        return HashCodeHelper.getHashCode(1, 31, getColumnFormat(), myColumnNames, myCommentIndicator, myDataStartLine,
                myHeaderLine, mySpecialColumns, myColumnsToIgnore, myColumnInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(256);
        builder.append("CSVParseParameters [columnFormat=").append(getColumnFormat()).append(", columnNames=")
                .append(myColumnNames).append(", commentIndicator=").append(myCommentIndicator).append(", dataStartLine=")
                .append(myDataStartLine).append(", headerLine=").append(myHeaderLine).append(", specialColumns=")
                .append(mySpecialColumns).append(", columnsToIgnore=").append(myColumnsToIgnore).append(", columnInfo=")
                .append(myColumnInfo).append(']');
        return builder.toString();
    }

    @Override
    public CSVParseParameters clone()
    {
        try
        {
            CSVParseParameters result = (CSVParseParameters)super.clone();
            result.myColumnNames = New.list(myColumnNames);
            result.mySpecialColumns = New.set(StreamUtilities.map(mySpecialColumns, specialColumn -> specialColumn.clone()));
            result.myColumnsToIgnore = New.list(myColumnsToIgnore);
            result.myColumnInfo = StreamUtilities.map(myColumnInfo, columnInfo -> columnInfo.clone());
            return result;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}
