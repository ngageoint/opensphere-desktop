package io.opensphere.mantle.data.geom.style;

import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * The Interface VisualizationStyleParameter.
 */
public class VisualizationStyleParameter
{
    /** The Hint. */
    private final ParameterHint myHint;

    /** The Key. */
    private final String myKey;

    /** The Name. */
    private final String myName;

    /** The Parameter flags. */
    private final VisualizationStyleParameterFlags myParameterFlags;

    /** The Value. */
    private final Object myValue;

    /** The Value type. */
    private final Class<?> myValueType;

    /**
     * Instantiates a new default visualization style parameter.
     *
     * @param key the key
     * @param name the name
     * @param value the value
     * @param valueType the value type
     * @param paramFlags the parameter flags
     * @param hint the hint
     */
    public VisualizationStyleParameter(String key, String name, Object value, Class<?> valueType,
            VisualizationStyleParameterFlags paramFlags, ParameterHint hint)
    {
        Utilities.checkNull(key, "key");
        Utilities.checkNull(name, "name");
        Utilities.checkNull(valueType, "valueType");
        Utilities.checkNull(paramFlags, "paramFlags");
        myHint = hint;
        myValue = value;
        myName = name;
        myKey = key;
        myParameterFlags = paramFlags;
        myValueType = valueType;
    }

    /**
     * Copy constructor.
     *
     * @param other the other
     */
    public VisualizationStyleParameter(VisualizationStyleParameter other)
    {
        myHint = other.getHint() == null ? null : new ParameterHint(other.getHint());
        myName = other.myName;
        myValue = other.getValue();
        myValueType = other.getValueType();
        myParameterFlags = new VisualizationStyleParameterFlags(other.myParameterFlags);
        myKey = other.getKey();
    }

    /**
     * Derive with new value.
     *
     * @param value the value
     * @return the visualization style parameter
     */
    public VisualizationStyleParameter deriveWithNewValue(Object value)
    {
        if (value == null && !myParameterFlags.isNullable())
        {
            throw new IllegalArgumentException("Parameter " + myName + " cannot be null");
        }
        // For now, a List gets a free pass
        if (!(value instanceof List) && value != null && !myValueType.isAssignableFrom(value.getClass()))
        {
            throw new IllegalArgumentException(
                    myName + " expects " + myValueType.getSimpleName() + " but received " + value.getClass().getSimpleName());
        }

        return new VisualizationStyleParameter(myKey, myName, value, myValueType, myParameterFlags,
                myHint == null ? null : new ParameterHint(myHint));
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
        VisualizationStyleParameter other = (VisualizationStyleParameter)obj;
        return myParameterFlags.equals(other.myParameterFlags) && EqualsHelper.equals(myHint, other.myHint)
                && EqualsHelper.equals(myName, other.myName) && EqualsHelper.equals(myKey, other.myKey)
                && EqualsHelper.equals(myValue, other.myValue) && EqualsHelper.equals(myValueType, other.myValueType);
    }

    /**
     * Gets the parameter hint.
     *
     * @return the parameter hint
     */
    public ParameterHint getHint()
    {
        return myHint;
    }

    /**
     * Gets the parameter key.
     *
     * @return the key
     */
    public String getKey()
    {
        return myKey;
    }

    /**
     * Gets the parameter name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    public Object getValue()
    {
        return myValue;
    }

    /**
     * Gets the parameter value type.
     *
     * @return the parameter value type
     */
    public Class<?> getValueType()
    {
        return myValueType;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myHint);
        result = prime * result + HashCodeHelper.getHashCode(myKey);
        result = prime * result + HashCodeHelper.getHashCode(myName);
        result = prime * result + HashCodeHelper.getHashCode(myValue);
        result = prime * result + HashCodeHelper.getHashCode(myValueType);
        result = prime * result + HashCodeHelper.getHashCode(myParameterFlags);
        return result;
    }

    /**
     * Checks if is data type specific.
     *
     * @return true, if is data type specific
     */
    public boolean isDataTypeSpecific()
    {
        return myParameterFlags.isDataTypeSpecific();
    }

    /**
     * Checks if is nullable.
     *
     * @return true, if is nullable
     */
    public boolean isNullable()
    {
        return myParameterFlags.isNullable();
    }

    /**
     * Checks if is saved.
     *
     * @return true, if is saved
     */
    public boolean isSaved()
    {
        return myParameterFlags.isSaved();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(this.getClass().getSimpleName()).append(": Key[").append(myKey).append("] Name[").append(myName)
                .append("] Value[").append(myValue == null ? "NULL" : myValue.toString()).append("] ValType[")
                .append(myValueType.getName()).append("] DataTypeSpecific[").append(myParameterFlags.isDataTypeSpecific())
                .append("] Nullable[").append(myParameterFlags.isNullable()).append("] Saved[").append(myParameterFlags.isSaved())
                .append("] Hint[").append(myHint.toString()).append(']');
        return sb.toString();
    }
}
