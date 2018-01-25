package io.opensphere.wfs.layer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * Class to represent a layer for a specific server and all the columns
 * associated with that layer.
 */
@XmlRootElement(name = "Layer")
@XmlAccessorType(XmlAccessType.FIELD)
public class WFSLayerConfig implements Serializable
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Layer key. */
    @XmlAttribute(name = "key")
    private String myLayerKey;

    /** The Is automatically disable empty columns. */
    @XmlAttribute(name = "automaticallyDisableEmptyColumns", required = false)
    private boolean myIsAutomaticallyDisableEmptyColumns = true;

    /** The set of column names associated with layer. */
    @XmlElement(name = "SelectedColumns", required = false)
    private Set<String> myDeprecatedSelectedColumns;

    /** The set of column names associated with layer. */
    @XmlElement(name = "DeselectedColumns")
    private Set<String> myDeselectedColumns;

    /**
     * Default constructor.
     */
    public WFSLayerConfig()
    {
        myDeselectedColumns = new HashSet<>();
    }

    /**
     * Automatically disable empty columns.
     *
     * @return true, if successful
     */
    public boolean automaticallyDisableEmptyColumns()
    {
        return myIsAutomaticallyDisableEmptyColumns;
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
        WFSLayerConfig other = (WFSLayerConfig)obj;
        //@formatter:off
        return Objects.equals(myLayerKey, other.myLayerKey)
                && myIsAutomaticallyDisableEmptyColumns == other.myIsAutomaticallyDisableEmptyColumns
                && Objects.equals(myDeselectedColumns, other.myDeselectedColumns);
        //@formatter:on
    }

    /**
     * Accessor for the deselected columns.
     *
     * @return The selected column names.
     */
    public Set<String> getDeselectedColumns()
    {
        return myDeselectedColumns;
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myLayerKey);
        result = prime * result + HashCodeHelper.getHashCode(myIsAutomaticallyDisableEmptyColumns);
        result = prime * result + HashCodeHelper.getHashCode(myDeselectedColumns);
        return result;
    }

    /**
     * Sets the automatically disable empty columns.
     *
     * @param automaticallyDisable the new automatically disable empty columns
     */
    public void setAutomaticallyDisableEmptyColumns(boolean automaticallyDisable)
    {
        myIsAutomaticallyDisableEmptyColumns = automaticallyDisable;
    }

    /**
     * Mutator for the deselected columns.
     *
     * @param selectedColumns The new selected columns.
     */
    public void setDeselectedColumns(Set<String> selectedColumns)
    {
        if (myDeprecatedSelectedColumns != null)
        {
            myDeprecatedSelectedColumns.clear();
        }
        myDeselectedColumns = selectedColumns;
    }

    /**
     * Sets the layer key.
     *
     * @param layerKey the new layer key
     */
    public void setLayerKey(String layerKey)
    {
        myLayerKey = layerKey;
    }
}
