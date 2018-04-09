package io.opensphere.core.datafilter.columns;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.lang.ToStringHelper;

/** ColumnMapping bean. */
@Immutable
public class ColumnMapping implements Serializable
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The defined column. */
    private final String myDefinedColumn;

    /** The layer key. */
    private final String myLayerKey;

    /** The layer column. */
    private final String myLayerColumn;

    /**
     * Constructor.
     *
     * @param definedColumn The defined column
     * @param layerKey The layer key
     * @param layerColumn The layer column
     */
    public ColumnMapping(String definedColumn, String layerKey, String layerColumn)
    {
        myDefinedColumn = definedColumn;
        myLayerKey = layerKey;
        myLayerColumn = layerColumn;
    }

    /**
     * Gets the defined column.
     *
     * @return the defined column
     */
    public String getDefinedColumn()
    {
        return myDefinedColumn;
    }

    /**
     * Gets the layer key.
     *
     * @return the layer key
     */
    public String getLayerKey()
    {
        return myLayerKey;
    }

    /**
     * Gets the layer column.
     *
     * @return the layer column
     */
    public String getLayerColumn()
    {
        return myLayerColumn;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myDefinedColumn);
        result = prime * result + HashCodeHelper.getHashCode(myLayerKey);
        result = prime * result + HashCodeHelper.getHashCode(myLayerColumn);
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
        ColumnMapping other = (ColumnMapping)obj;
        return Objects.equals(myDefinedColumn, other.myDefinedColumn) && Objects.equals(myLayerKey, other.myLayerKey)
                && Objects.equals(myLayerColumn, other.myLayerColumn);
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("definedColumn", myDefinedColumn);
        helper.add("layerKey", myLayerKey);
        helper.add("layerColumn", myLayerColumn);
        return helper.toString();
    }
}
