package io.opensphere.importer.config;

import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.importer.config.ColumnType.Category;

/**
 * Parameters for parsing a document.
 */
@XmlRootElement(name = "ImportParseParameters")
@XmlAccessorType(XmlAccessType.NONE)
public class ImportParseParameters extends Observable implements Cloneable
{
    /** The column names. */
    @XmlElement(name = "name", required = false)
    @XmlElementWrapper(name = "columnNames")
    private List<? extends String> myColumnNames = New.list();

    /** The special columns. */
    @XmlElementRef
    private Set<SpecialColumn> mySpecialColumns = New.set();

    /** The column indices to ignore on import. */
    @XmlElement(name = "columnsToIgnore", required = false)
    @XmlList
    private List<Integer> myColumnsToIgnore = New.list();

    /**
     * Constructor.
     */
    public ImportParseParameters()
    {
        /* intentionally blank */
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
     * Get the definitions for columns that require special handling.
     *
     * @return The special columns.
     */
    public Set<SpecialColumn> getSpecialColumns()
    {
        return mySpecialColumns;
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
     * Determines whether the special keys contain the given column category.
     *
     * @param category the column category
     * @return whether the special keys contain the given column category
     */
    public boolean hasCategory(final Category category)
    {
        return mySpecialColumns.stream().anyMatch(specialColumn -> specialColumn.getColumnType().getCategory() == category);
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
        return mySpecialColumns.stream().anyMatch(specialColumn -> isColumnIncluded(specialColumn, types));
    }

    /**
     * Tests to determine if the supplied special column is included in the
     * supplied array of types.
     *
     * @param specialColumn the column for which to test.
     * @param types the column types in which to search.
     * @return true if the column is in the supplied types, false otherwise.
     */
    public boolean isColumnIncluded(SpecialColumn specialColumn, final ColumnType... types)
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

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        ImportParseParameters other = (ImportParseParameters)obj;
        //@formatter:off
        return Objects.equals(myColumnNames, other.myColumnNames)
                && Objects.equals(mySpecialColumns, other.mySpecialColumns)
                && Objects.equals(myColumnsToIgnore, other.myColumnsToIgnore);
        //@formatter:on
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myColumnNames);
        result = prime * result + HashCodeHelper.getHashCode(mySpecialColumns);
        result = prime * result + HashCodeHelper.getHashCode(myColumnsToIgnore);
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(128);
        builder.append("ImportParseParameters [columnNames=").append(myColumnNames).append(", specialColumns=")
                .append(mySpecialColumns).append(", columnsToIgnore=").append(myColumnsToIgnore).append(']');
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public ImportParseParameters clone()
    {
        try
        {
            ImportParseParameters result = (ImportParseParameters)super.clone();
            result.myColumnNames = New.list(myColumnNames);
            result.mySpecialColumns = New.set(StreamUtilities.map(mySpecialColumns, specialColumn -> specialColumn.clone()));
            result.myColumnsToIgnore = New.list(myColumnsToIgnore);
            return result;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}
