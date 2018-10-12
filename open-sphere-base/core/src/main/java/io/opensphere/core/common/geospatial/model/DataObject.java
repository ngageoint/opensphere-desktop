package io.opensphere.core.common.geospatial.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Most base transfer object. Used as a transfer object between the ogc server
 * and any clients that want to acquire feature results in a more optimzed way.
 * This class is used on both client and server side.
 *
 */
public class DataObject implements Serializable, io.opensphere.core.common.geospatial.model.interfaces.IDataObject, Cloneable
{

    private static final long serialVersionUID = 1L;

    protected long id = 0L;

    protected String featureId;

    // Use Lists rather than HashMap to save memory

    /**
     * Values hold the actual data for the object
     */
    protected List<Object> values;

    /**
     * Keys are the keys to the values in the values list
     */
    protected List<String> keys;

    /**
     * Whether object is selected
     */
    protected boolean selected;

    /**
     * Color of the object
     */
    protected Color color = java.awt.Color.BLACK;

    /**
     * Start Date/time of point
     */
    protected Date startDate;

    /**
     * End date of point
     */
    protected Date endDate;

    /**
     * Default constructor needed to be serialization friendly.
     */
    public DataObject()
    {
        selected = false;
        values = new ArrayList<>();
        keys = new ArrayList<>();
    }

    // Getters and setters
    @Override
    public List<?> getProperties()
    {
        return values;
    }

    @Override
    public List<String> getPropertyKeys()
    {
        return keys;
    }

    @Override
    public boolean hasProperty(String key)
    {
        if (keys.contains(key))
        {
            return true;
        }
        return false;
    }

    @Override
    public Object getProperty(String key)
    {
        Object ret = null;
        if (keys.contains(key))
        {
            ret = values.get(keys.indexOf(key));
        }
        return ret;
    }

    @Override
    public void setProperty(String property, Object value)
    {
        int index = keys.indexOf(property);
        if (index >= 0)
        {
            values.set(index, value);
        }
        else
        {
            keys.add(property);
            values.add(value);
        }
    }

    @Override
    public void removeProperty(String property)
    {
        if (property != null && keys.contains(property))
        {
            values.remove(keys.indexOf(property));
            keys.remove(keys.indexOf(property));
        }
    }

    @Override
    public long getId()
    {
        return id;
    }

    @Override
    public void setId(long id)
    {
        this.id = id;
    }

    @Override
    public Color getColor()
    {
        return color;
    }

    @Override
    public void setColor(Color color)
    {
        this.color = color;
    }

    @Override
    public Date getStartDate()
    {
        return startDate;
    }

    @Override
    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    /**
     * Convience methods if point just has one date to it Same as getStartDate
     *
     * @return startDate
     */
    @Override
    public Date getDate()
    {
        return startDate;
    }

    /**
     * Set the start date, same as setStartDate
     *
     * @param startDate
     */
    @Override
    public void setDate(Date startDate)
    {
        this.startDate = startDate;
    }

    @Override
    public Date getEndDate()
    {
        return endDate;
    }

    @Override
    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("DataObject:\n");
        buf.append("--Id: " + id);
        buf.append("\n--Start: " + startDate);
        buf.append("\n--End: " + endDate);
        buf.append("\n--Properties:");
        for (int i = 0; i < keys.size(); i++)
        {
            buf.append("\n----" + keys.get(i) + ": " + getProperty(keys.get(i)));
        }
        return buf.toString();
    }

    @Override
    public String getFeatureId()
    {
        return featureId;
    }

    @Override
    public void setFeatureId(String featureId)
    {
        this.featureId = featureId;
    }

    @Override
    public String getDataType()
    {
        String dataType = null;
        if (featureId != null)
        {
            String[] parts = featureId.split("\\.");
            dataType = parts[0];
        }
        return dataType;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    @Override
    public DataObject clone()
    {
        DataObject clone = new DataObject();
        clone.setColor(color);
        clone.setDate(startDate);
        clone.setEndDate(endDate);
        clone.setFeatureId(featureId);
        clone.setId(id);
        int ndx = 0;
        for (String property : this.keys)
        {
            clone.setProperty(property, this.values.get(ndx++));
        }
        clone.setSelected(selected);
        clone.setStartDate(startDate);
        return clone;
    }
}
