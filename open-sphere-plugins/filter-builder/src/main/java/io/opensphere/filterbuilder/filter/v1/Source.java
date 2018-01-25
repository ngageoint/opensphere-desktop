package io.opensphere.filterbuilder.filter.v1;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Source: the source that is to be filtered by the {@link Filter}.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "source", namespace = "4.0.0", propOrder = { "myTypeKey", "myServerName", "myTypeName", "myTypeDisplayName" })
public class Source implements Cloneable, Serializable
{
    /** The serial version ID. */
    private static final long serialVersionUID = 1L;

    /** The name of the data layer, or data type. */
    @XmlAttribute(name = "typeName", required = true)
    private String myTypeName;

    /** The display name of the data layer, or data type. */
    @XmlAttribute(name = "typeDisplayName", required = true)
    private String myTypeDisplayName;

    /** The display name of the server that the layer/data type came from. */
    @XmlAttribute(name = "serverName", required = true)
    private String myServerName;

    /** The data type key, the path to the data source. */
    @XmlAttribute(name = "typeKey", required = true)
    private String myTypeKey;

    /** Whether a virtual source is active. */
    @XmlAttribute(name = "active")
    private boolean myActive;

    /**
     * Creates a new Source from a DataTypeInfo.
     *
     * @param dataType the data type
     * @return the new Source
     */
    public static Source fromDataType(DataTypeInfo dataType)
    {
        return new Source(dataType.getDisplayName(), dataType.getTypeName(), dataType.getSourcePrefix(), dataType.getTypeKey());
    }

    /**
     * Creates a new source from the supplied type key.
     *
     * @param typeKey the name of the datatype from which the source will be created.
     * @return a new {@link Source} object generated and populated using the supplied type key.
     */
    public static Source fromTypeKey(String typeKey)
    {
        Source s = new Source();
        s.setTypeKey(typeKey);
        s.setTypeDisplayName(typeKey);
        return s;
    }

    /**
     * Instantiates a new source.
     */
    public Source()
    {
    }

    /**
     * Instantiates a new source.
     *
     * @param pTypeDisplayName the type display name
     * @param pTypeName the type name
     * @param pServerName the server name
     * @param pTypeKey the type key
     */
    public Source(String pTypeDisplayName, String pTypeName, String pServerName, String pTypeKey)
    {
        myTypeDisplayName = pTypeDisplayName;
        myTypeName = pTypeName;
        myServerName = pServerName;
        myTypeKey = pTypeKey;
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
        Source other = (Source)obj;
        //@formatter:off
        return Objects.equals(myServerName, other.myServerName)
                && Objects.equals(myTypeKey, other.myTypeKey)
                && Objects.equals(myTypeName, other.myTypeName);
        //@formatter:on
    }

    /**
     * Gets the server name.
     *
     * @return the server name
     */
    public String getServerName()
    {
        return myServerName;
    }

    /**
     * Gets the type display name.
     *
     * @return the type display name
     */
    public String getTypeDisplayName()
    {
        return myTypeDisplayName;
    }

    /**
     * Gets the type key.
     *
     * @return the type key
     */
    public String getTypeKey()
    {
        return myTypeKey;
    }

    /**
     * Gets the type name.
     *
     * @return the type name
     */
    public String getTypeName()
    {
        return myTypeName;
    }

    /**
     * Gets whether a virtual source is active.
     *
     * @return whether a virtual source is active
     */
    public boolean isActive()
    {
        return myActive;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myServerName);
        result = prime * result + HashCodeHelper.getHashCode(myTypeKey);
        result = prime * result + HashCodeHelper.getHashCode(myTypeName);
        return result;
    }

    /**
     * Sets the server name.
     *
     * @param pServerName the new server name
     */
    public void setServerName(String pServerName)
    {
        myServerName = pServerName;
    }

    /**
     * Sets the type display name.
     *
     * @param pTypeDisplayName the new type display name
     */
    public void setTypeDisplayName(String pTypeDisplayName)
    {
        myTypeDisplayName = pTypeDisplayName;
    }

    /**
     * Sets the type key.
     *
     * @param pTypeKey the new type key
     */
    public void setTypeKey(String pTypeKey)
    {
        myTypeKey = pTypeKey;
    }

    /**
     * Sets the type name.
     *
     * @param pTypeName the new type name
     */
    public void setTypeName(String pTypeName)
    {
        myTypeName = pTypeName;
    }

    /**
     * Sets whether a virtual source is active.
     *
     * @param active whether a virtual source is active
     */
    public void setActive(boolean active)
    {
        myActive = active;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64)

                .append("Source=" + "[" + "typeName=").append(myTypeName).append(", " + "serverName=").append(myServerName)
                .append(", " + "typeKey=").append(myTypeKey).append(']').toString();
    }

    @Override
    public Source clone() throws CloneNotSupportedException
    {
        return (Source)super.clone();
    }

    /**
     * Make a clone without worrying about CloneNotSupportedException.
     *
     * @return a shallow clone of this instance.
     */
    public Source copy()
    {
        try
        {
            return clone();
        }
        catch (CloneNotSupportedException eek)
        {
            // Never happen
        }
        return null;
    }
}
