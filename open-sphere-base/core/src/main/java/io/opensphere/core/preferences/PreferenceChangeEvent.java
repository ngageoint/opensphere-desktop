package io.opensphere.core.preferences;

import javax.xml.bind.JAXBException;

import io.opensphere.core.util.ref.WeakReference;

/**
 * An event for a preference change.
 */
public class PreferenceChangeEvent
{
    /**
     * Weak reference to the originator of the message.
     */
    private final WeakReference<Object> mySourceRef;

    /** The preference topic. */
    private final String myTopic;

    /** The preference change event value. */
    private final Preference<?> myValue;

    /**
     * Constructor with key and value.
     *
     * @param topic The preference topic.
     * @param newValue The new preference.
     * @param source The originator of the change that produced the event.
     */
    public PreferenceChangeEvent(String topic, Preference<?> newValue, Object source)
    {
        myTopic = topic;
        myValue = newValue;
        mySourceRef = new WeakReference<>(source);
    }

    /**
     * Gets the key for the change event.
     *
     * @return the key
     */
    public String getKey()
    {
        return myValue.getKey();
    }

    /**
     * Gets the source of the preference change.
     *
     * @return the source object
     */
    public Object getSource()
    {
        return mySourceRef.get();
    }

    /**
     * Returns the topic for the preferences change event.
     *
     * @return the topic
     */
    public String getTopic()
    {
        return myTopic;
    }

    /**
     * Gets the value as a boolean, or default if could not be converted to a
     * boolean.
     *
     * @param def the default value to return if the value could not be
     *            converted
     * @return the value as a boolean
     */
    public boolean getValueAsBoolean(boolean def)
    {
        return myValue.isNull() ? def : myValue.getBooleanValue(def);
    }

    /**
     * Gets the value as a boolean, or default if could not be converted to a
     * boolean.
     *
     * @param def the default value to return if the value could not be
     *            converted
     * @return the value as a boolean
     */
    public Boolean getValueAsBoolean(Boolean def)
    {
        return myValue.isNull() ? def : myValue.getBooleanValue(def);
    }

    /**
     * Gets the value as a double or default if it could not be converted.
     *
     * @param def the default value to return if the value could not be
     *            converted
     * @return the value as a double or default if it could not be converted
     */
    public double getValueAsDouble(double def)
    {
        return myValue.isNull() ? def : myValue.getDoubleValue(def);
    }

    /**
     * Gets the value as a double or default if it could not be converted.
     *
     * @param def the default value to return if the value could not be
     *            converted
     * @return the value as a double or default if it could not be converted
     */
    public Double getValueAsDouble(Double def)
    {
        return myValue.isNull() ? def : myValue.getDoubleValue(def);
    }

    /**
     * Gets the value as a float or default if it could not be converted.
     *
     * @param def the default value to return if the value could not be
     *            converted
     * @return the value as a float or default if it could not be converted
     */
    public float getValueAsFloat(float def)
    {
        return myValue.isNull() ? def : myValue.getFloatValue(def);
    }

    /**
     * Gets the value as a float or default if it could not be converted.
     *
     * @param def the default value to return if the value could not be
     *            converted
     * @return the value as a float or default if it could not be converted
     */
    public Float getValueAsFloat(Float def)
    {
        return myValue.isNull() ? def : myValue.getFloatValue(def);
    }

    /**
     * Gets the value as a int or default if it could not be converted.
     *
     * @param def the default value to return if the value could not be
     *            converted
     * @return the value or default if it could not be converted
     */
    public int getValueAsInt(int def)
    {
        return myValue.isNull() ? def : myValue.getIntValue(def);
    }

    /**
     * Gets the value as a int or default if it could not be converted.
     *
     * @param def the default value to return if the value could not be
     *            converted
     * @return the value or default if it could not be converted
     */
    public Integer getValueAsInteger(Integer def)
    {
        return myValue.isNull() ? def : myValue.getIntegerValue(def);
    }

    /**
     * Gets the value as a long or default if it could not be converted.
     *
     * @param def the default value to return if the value could not be
     *            converted
     *
     * @return value or default if it could not be converted
     */
    public long getValueAsLong(long def)
    {
        return myValue.isNull() ? def : myValue.getLongValue(def);
    }

    /**
     * Gets the value as a long or default if it could not be converted.
     *
     * @param def the default value to return if the value could not be
     *            converted
     *
     * @return value or default if it could not be converted
     */
    public Long getValueAsLong(Long def)
    {
        return myValue.isNull() ? def : myValue.getLongValue(def);
    }

    /**
     * Gets the value as an {@link Object} or default if it is null.
     *
     * @param <T> The type of the object.
     * @param def The default object to return.
     * @return The object value of the preference or def if null.
     * @throws JAXBException Thrown if the object could not be unmarshalled from
     *             its JAXB form.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValueAsObject(T def) throws JAXBException
    {
        return myValue.isNull() ? def : (T)myValue.getValue();
    }

    /**
     * Gets the value as a {@link String}, or default if could not be converted
     * to a {@link String}.
     *
     * @param def the default value to return if the value could not be
     *            converted
     * @return The value as a {@link String}.
     */
    public String getValueAsString(String def)
    {
        return myValue.isNull() ? def : myValue.getStringValue(def);
    }

    @Override
    public String toString()
    {
        return "PreferenceChangeEvent: Topic[" + myTopic + "] Key[" + getKey() + "] Value[" + myValue + "]";
    }
}
