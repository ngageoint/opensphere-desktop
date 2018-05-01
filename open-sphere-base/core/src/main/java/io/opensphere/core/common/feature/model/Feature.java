package io.opensphere.core.common.feature.model;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Represents as Feature. This should be analogous to a WFS feature, but this
 * class will be serialized to an internal format using Java serialization
 * format, not OpenGIS WFS format.
 */
public class Feature implements Serializable
{
    /**
     * Constants for certain important keys
     */
    public static final String DOWN_DATE_TIME = "DOWN_DATE_TIME";

    public static final String UP_DATE_TIME = "UP_DATE_TIME";

    public static final String DATE_TIME = "DATE_TIME";

    public static final String VALID_TIME = "validTime";

    /**
     * Enumeration indicating whether this event is timeless (i.e. no time
     * associated), instant (i.e. has only a single Date), or takes place over a
     * time interval (i.e. has a start Date and a end Date)
     */
    public enum TimeType
    {
        TIMELESS, INSTANT, INTERVAL;
    }

    /**
     * For Serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique ID for this feature
     */
    protected String featureID;

    /**
     * List of keys
     */
    protected List<String> keys;

    /**
     * Values hold the actual data for the object
     */
    protected List<Object> values;

    /**
     * Color of the object
     *
     */
    protected Color color;

    /**
     * No-arg Feature constructor for serialization
     */
    public Feature()
    {
        featureID = new String();
        values = new ArrayList<>(0);
        color = java.awt.Color.BLACK;
    }

    public String getFeatureID()
    {
        return featureID;
    }

    public void setFeatureID(String featureID)
    {
        this.featureID = featureID;
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public Object getValue(String key)
    {
        int index = keyToIndex(key);
        return values.get(index);
    }

    public void setValue(String key, Object newValue)
    {
        int index = keyToIndex(key);
        values.set(index, newValue);
    }

    public void addValue(Object newValue)
    {
        values.add(newValue);
    }

    private int keyToIndex(String key)
    {
        return keys.indexOf(key);
    }

    public List<String> getKeys()
    {
        return keys;
    }

    public void setKeys(List<String> keys)
    {
        this.keys = keys;

        // Initialize the values list to be the same length as the keys list.
        if (values == null || values.size() == 0)
        {
            values = new ArrayList<>(this.keys.size());
            for (int i = 0; i < this.keys.size(); i++)
            {
                values.add(null);
            }
        }
    }

    /**
     * Determines the time type of this event.
     *
     * @return the time type
     */
    public TimeType getTimeType()
    {
        if (isSet(UP_DATE_TIME) && isSet(DOWN_DATE_TIME))
        {
            return TimeType.INTERVAL;
        }
        else if (isSet(DATE_TIME) || isSet(VALID_TIME))
        {
            return TimeType.INSTANT;
        }
        else
        {
            return TimeType.TIMELESS;
        }
    }

    /**
     * Returns the time instant key.
     *
     * @return The time instant key
     */
    public String getTimeInstantKey()
    {
        return isSet(DATE_TIME) ? DATE_TIME : VALID_TIME;
    }

    /**
     * Determines if the given value is set
     *
     * @param key The key for the value in question
     * @return True is the key exists and the corresponding value is not null,
     *         false otherwise.
     */
    private boolean isSet(String key)
    {
        return keys.contains(key) && getValue(key) != null;
    }

    /**
     * Overrides the default serialization of this object.
     *
     * @param out
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.writeObject(featureID);
        out.writeObject(keys);

        for (String key : keys)
        {
            Object value = getValue(key);
            Object newValue = packValue(value);
            out.writeObject(newValue);
        }

        out.writeObject(color);
    }

    /**
     * Prepares the given value for serialization. This method handles special
     * cases the the serialized value is different than the stored value for
     * efficiency.
     *
     * @param unpackedValue The Object to be "packed" for serialization.
     * @return The packed form of the given Object.
     */
    private Object packValue(Object unpackedValue)
    {
        Object packedValue = null;

        if (unpackedValue != null)
        {
            // If the current value is a Geometry we need to handle it
            // differently.
            if (unpackedValue instanceof Geometry)
            {
                Geometry geometry = (Geometry)unpackedValue;
                packedValue = new GeometryWrapper(geometry);
            }
            else
            {
                packedValue = unpackedValue;
            }
        }

        return packedValue;
    }

    /**
     * Overrides the deserialization of this object.
     *
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        featureID = (String)in.readObject();
        keys = (List<String>)in.readObject();

        values = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++)
        {
            Object value = in.readObject();
            Object newValue = unpackValue(value);
            values.add(newValue);
        }

        color = (Color)in.readObject();
    }

    /**
     * Unpacks a value from deserialization. This method handles cases where the
     * serialized form of the data is different than the stored form.
     *
     * @param packedValue The Object to be "unpacked" from deserialization.
     * @return The "Unpacked" version of the given value.
     */
    private Object unpackValue(Object packedValue)
    {
        Object unpackedValue = null;

        if (packedValue != null)
        {
            if (packedValue instanceof GeometryWrapper)
            {
                GeometryWrapper wrapper = (GeometryWrapper)packedValue;
                unpackedValue = wrapper.getGeometry();
            }
            else
            {
                unpackedValue = packedValue;
            }
        }

        return unpackedValue;
    }

    @SuppressWarnings("unused")
    private void readObjectNoData() throws ObjectStreamException
    {
        new Feature();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (color == null ? 0 : color.hashCode());
        result = prime * result + (featureID == null ? 0 : featureID.hashCode());
        result = prime * result + (keys == null ? 0 : keys.hashCode());
        result = prime * result + (values == null ? 0 : values.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        Feature other = (Feature)obj;
        if (color == null)
        {
            if (other.color != null)
            {
                return false;
            }
        }
        else if (!color.equals(other.color))
        {
            return false;
        }
        if (featureID == null)
        {
            if (other.featureID != null)
            {
                return false;
            }
        }
        else if (!featureID.equals(other.featureID))
        {
            return false;
        }
        if (keys == null)
        {
            if (other.keys != null)
            {
                return false;
            }
        }
        else if (!keys.equals(other.keys))
        {
            return false;
        }
        if (values == null)
        {
            if (other.values != null)
            {
                return false;
            }
        }
        else if (!values.equals(other.values))
        {
            return false;
        }
        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        final int maxLen = 10;
        StringBuilder builder = new StringBuilder();
        builder.append("Feature [");
        if (featureID != null)
        {
            builder.append("featureID=");
            builder.append(featureID);
            builder.append(", ");
        }
        if (color != null)
        {
            builder.append("color=");
            builder.append(color);
            builder.append(", ");
        }
        if (keys != null)
        {
            builder.append("keys=");
            builder.append(keys.subList(0, Math.min(keys.size(), maxLen)));
            builder.append(", ");
        }
        if (values != null)
        {
            builder.append("values=");
            builder.append(values.subList(0, Math.min(values.size(), maxLen)));
        }
        builder.append("]");
        return builder.toString();
    }
}
