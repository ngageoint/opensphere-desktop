package io.opensphere.core.common.filter.expression;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class represents a literal (i.e. constant) expression.
 */
public class LiteralEx implements Expression
{
    /**
     * The literal value.
     */
    private Object value;

    /**
     * Constructor.
     *
     * @param value the literal value.
     */
    public LiteralEx(Object value)
    {
        this.value = value;
    }

    /**
     * Sets the literal value.
     *
     * @param value the literal value.
     */
    public void setValue(Object value)
    {
        this.value = value;
    }

    /**
     * Returns the literal value.
     *
     * @return the literal value.
     */
    public Object getValue()
    {
        return value;
    }

    @Override
    public Object getValueFrom(FilterDTO dto)
    {
        return value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (getValue() instanceof CharSequence)
        {
            return "\"" + getValue() + "\"";
        }
        return String.valueOf(getValue());
    }

    /**
     * Creates and returns a new {@link LiteralEx}.<br/>
     * <p>
     * <b>WARNING</b>: This is a shallow copy because the <i>value</i> may not
     * be cloneable.
     *
     * @see io.opensphere.core.common.filter.expression.Expression#clone()
     */
    @Override
    public LiteralEx clone() throws CloneNotSupportedException
    {
        return new LiteralEx(value);
    }
}
