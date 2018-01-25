package io.opensphere.mantle.data.impl.specialkey;

import java.util.Objects;

import io.opensphere.mantle.data.SpecialKey;

/**
 * The Class DefaultSpecialKey.
 *
 * Note that hashcode and equals only compare the key name, the unit is ignored.
 */
public abstract class AbstractSpecialKey implements SpecialKey
{
    /**
     * Serial Version Id.
     */
    private static final long serialVersionUID = 1L;

    /** The key name. */
    private final String myKeyName;

    /** The key unit. */
    private final Object myKeyUnit;

    /**
     * Instantiates a new special key with an undefined unit.
     *
     * @param keyName the key name
     */
    public AbstractSpecialKey(String keyName)
    {
        this(keyName, SpecialKey.UNDEFINED_UNIT);
    }

    /**
     * Instantiates a new special key.
     *
     * @param keyName the key name
     * @param keyUnit the key unit
     */
    public AbstractSpecialKey(String keyName, Object keyUnit)
    {
        myKeyName = keyName;
        myKeyUnit = keyUnit == null ? UNDEFINED_UNIT : keyUnit;
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
        AbstractSpecialKey other = (AbstractSpecialKey)obj;
        return Objects.equals(myKeyName, other.myKeyName);
    }

    @Override
    public String getKeyName()
    {
        return myKeyName;
    }

    @Override
    public Object getKeyUnit()
    {
        return myKeyUnit;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myKeyName == null ? 0 : myKeyName.hashCode());
        return result;
    }

    @Override
    public boolean isUnitDefined()
    {
        return UNDEFINED_UNIT.equals(myKeyUnit);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(myKeyName).append(" : ").append(myKeyUnit == null ? "?" : myKeyUnit.toString());
        return sb.toString();
    }
}
