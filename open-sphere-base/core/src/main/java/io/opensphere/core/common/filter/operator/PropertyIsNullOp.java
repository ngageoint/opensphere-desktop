package io.opensphere.core.common.filter.operator;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class represents a comparison operator that determines if a specific
 * property is <code>null</code>.
 */
public class PropertyIsNullOp extends ComparisonOp
{
    /**
     * The property name.
     */
    private String name;

    /**
     * Constructor.
     *
     * @param name
     */
    public PropertyIsNullOp(String name)
    {
        setName(name);
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
     * @see io.opensphere.core.common.filter.operator.Operator#evaluate(FilterDTO)
     */
    @Override
    public boolean evaluate(FilterDTO dto)
    {
        return !dto.containsKey(getName()) || dto.get(getName()) == null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName() + " == null";
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#clone()
     */
    @Override
    public PropertyIsNullOp clone() throws CloneNotSupportedException
    {
        return new PropertyIsNullOp(name);
    }
}
