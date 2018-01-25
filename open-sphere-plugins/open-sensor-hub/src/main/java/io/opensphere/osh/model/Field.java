package io.opensphere.osh.model;

import java.io.Serializable;

import io.opensphere.core.util.lang.ToStringHelper;

/** A data record field. */
public class Field implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The name. */
    private final String myName;

    /** The full path name. */
    private String myFullPath;

    /** The property. */
    private String myProperty;

    /** The label. */
    private String myLabel;

    /** The data type. */
    private String myDataType;

    /** The ID, if available. */
    private String myId;

    /**
     * Constructor.
     *
     * @param name The name
     */
    public Field(String name)
    {
        myName = name;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Sets the fullPath.
     *
     * @param fullPath the fullPath
     */
    public void setFullPath(String fullPath)
    {
        myFullPath = fullPath;
    }

    /**
     * Gets the fullPath.
     *
     * @return the fullPath
     */
    public String getFullPath()
    {
        return myFullPath;
    }

    /**
     * Sets the property.
     *
     * @param property the property
     */
    public void setProperty(String property)
    {
        myProperty = property;
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public String getProperty()
    {
        return myProperty;
    }

    /**
     * Sets the label.
     *
     * @param label the label
     */
    public void setLabel(String label)
    {
        myLabel = label;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Gets the dataType.
     *
     * @return the dataType
     */
    public String getDataType()
    {
        return myDataType;
    }

    /**
     * Sets the dataType.
     *
     * @param dataType the dataType
     */
    public void setDataType(String dataType)
    {
        myDataType = dataType;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Sets the id.
     *
     * @param id the id
     */
    public void setId(String id)
    {
        myId = id;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("name", myName);
        helper.add("fullPath", myFullPath);
        helper.add("property", myProperty);
        helper.add("label", myLabel);
        helper.add("dataType", myDataType);
        helper.add("id", myId);
        return helper.toString();
    }
}
