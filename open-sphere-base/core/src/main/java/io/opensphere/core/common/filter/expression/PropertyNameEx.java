package io.opensphere.core.common.filter.expression;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class represents a property name expression.
 */
public class PropertyNameEx implements Expression
{
    /**
     * The property name.
     */
    private String name;

    /**
     * Constructor.
     *
     * @param name the property name.
     */
    public PropertyNameEx(String name)
    {
        this.name = name;
    }

    /**
     * Sets the property name.
     *
     * @param name the property name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the property name.
     *
     * @return the property name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @see io.opensphere.core.common.filter.expression.Expression#getValueFrom(io.opensphere.core.common.filter.dto.FilterDTO)
     */
    @Override
    public Object getValueFrom(FilterDTO dto)
    {
        if (dto.containsKey(getName()))
        {
            return dto.get(getName());
        }
        else if (dto.containsKey(getName().toUpperCase()))
        {
            return dto.get(getName().toUpperCase());
        }
        else if (dto.containsKey(getName().toLowerCase()))
        {
            return dto.get(getName().toLowerCase());
        }
        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName();
    }

    /**
     * @see io.opensphere.core.common.filter.expression.Expression#clone()
     */
    @Override
    public PropertyNameEx clone() throws CloneNotSupportedException
    {
        return new PropertyNameEx(name);
    }
}
