package io.opensphere.controlpanels.columnlabels.model;

import java.io.Serializable;
import java.util.Observable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * Represents one column of column label options within the label options.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class ColumnLabel extends Observable implements Serializable
{
    /**
     * The column property.
     */
    public static final String COLUMN_PROP = "column";

    /**
     * The enabled property.
     */
    public static final String SHOW_COLUMN_NAME_PROP = "enabled";

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The available columns to choose from.
     */
    private transient ObservableList<String> myAvailableColumns;

    /**
     * The column whose value should be included in the label.
     */
    @XmlAttribute(name = "column")
    private String myColumn;

    /**
     * Indicates if this columns value should be included in the label.
     */
    @XmlAttribute(name = "showColumnName")
    private boolean myShowColumnName;

    /**
     * Gets the available columns.
     *
     * @return the availableColumns
     */
    public ObservableList<String> getAvailableColumns()
    {
        if (myAvailableColumns == null)
        {
            myAvailableColumns = FXCollections.observableArrayList();
        }

        return myAvailableColumns;
    }

    /**
     * Gets The column whose value should be included in the label.
     *
     * @return the column
     */
    public String getColumn()
    {
        return myColumn;
    }

    /**
     * Indicates if this columns name should be included in the label.
     *
     * @return the enabled
     */
    public boolean isShowColumnName()
    {
        return myShowColumnName;
    }

    /**
     * Sets The column whose value should be included in the label.
     *
     * @param column the column to set
     */
    public void setColumn(String column)
    {
        myColumn = column;
        setChanged();
        notifyObservers(COLUMN_PROP);
    }

    /**
     * Sets if this columns name should be included in the label.
     *
     * @param showColumnName the enabled to set
     */
    public void setShowColumnName(boolean showColumnName)
    {
        myShowColumnName = showColumnName;
        setChanged();
        notifyObservers(SHOW_COLUMN_NAME_PROP);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(myColumn, Boolean.valueOf(myShowColumnName));
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
        ColumnLabel other = (ColumnLabel)obj;
        return Objects.equal(myColumn, other.myColumn) && myShowColumnName == other.myShowColumnName;
    }
}
