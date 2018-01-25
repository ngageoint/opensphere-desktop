package io.opensphere.featureactions.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The action that allows the user to add new column values to a row that
 * matches the feature actions filter.
 */
@XmlRootElement
public class CustomColumnAction extends Action
{
    /**
     * The name of the new custom column.
     */
    private final StringProperty myColumn = new SimpleStringProperty();

    /**
     * The value to put into the custom column.
     */
    private final StringProperty myValue = new SimpleStringProperty();

    /**
     * The column name property.
     *
     * @return The column name property.
     */
    public StringProperty columnProperty()
    {
        return myColumn;
    }

    /**
     * Gets the name of the new custom column.
     *
     * @return the column name.
     */
    public String getColumn()
    {
        return myColumn.get();
    }

    /**
     * Gets the value for the custom column.
     *
     * @return the value.
     */
    public String getValue()
    {
        return myValue.get();
    }

    /**
     * Sets the name of the new custom column.
     *
     * @param column the column name to set
     */
    public void setColumn(String column)
    {
        myColumn.set(column);
    }

    /**
     * Sets the value for the column.
     *
     * @param value the value to set.
     */
    public void setValue(String value)
    {
        myValue.set(value);
    }

    /**
     * The value property.
     *
     * @return The value property.
     */
    public StringProperty valueProperty()
    {
        return myValue;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myColumn == null ? 0 : myColumn.hashCode());
        result = prime * result + (myValue == null ? 0 : myValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        CustomColumnAction other = (CustomColumnAction)obj;
        if (myColumn == null)
        {
            if (other.myColumn != null)
            {
                return false;
            }
        }
        else if (!myColumn.equals(other.myColumn))
        {
            return false;
        }
        if (myValue == null)
        {
            if (other.myValue != null)
            {
                return false;
            }
        }
        else if (!myValue.equals(other.myValue))
        {
            return false;
        }
        return true;
    }

    @Override
    protected String getName()
    {
        return "Set Custom Column";
    }
}
