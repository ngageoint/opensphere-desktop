package io.opensphere.core.cache.matcher;

import java.util.regex.Pattern;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * A string property matcher.
 */
public final class StringPropertyMatcher extends AbstractPropertyMatcher<String>
{
    /** The operator that indicates how the string should be matched. */
    private final StringPropertyMatcher.OperatorType myOperator;

    /** The compiled pattern. */
    private transient Pattern myPattern;

    /**
     * Generate a Java regex pattern from an SQL <i>like</i> expression.
     *
     * @param like The like expression.
     * @return The pattern.
     */
    private static Pattern generatePatternFromLikeExpression(String like)
    {
        StringBuilder sb = new StringBuilder();
        boolean quote = false;
        boolean escape = false;
        char[] charArray = like.toCharArray();
        for (int ix = 0; ix < charArray.length; ++ix)
        {
            if (charArray[ix] == '\\')
            {
                if (escape)
                {
                    escape = false;
                    if (!quote)
                    {
                        quote = true;
                        sb.append("\\Q");
                    }
                    sb.append('\\');
                }
                else
                {
                    escape = true;
                }
            }
            else if (escape)
            {
                escape = false;
                if (!quote)
                {
                    quote = true;
                    sb.append("\\Q");
                }
                sb.append(charArray[ix]);
            }
            else if (charArray[ix] == '%')
            {
                if (quote)
                {
                    quote = false;
                    sb.append("\\E");
                }
                sb.append(".*");
            }
            else if (charArray[ix] == '_')
            {
                if (quote)
                {
                    quote = false;
                    sb.append("\\E");
                }
                sb.append('.');
            }
            else
            {
                if (!quote)
                {
                    quote = true;
                    sb.append("\\Q");
                }
                sb.append(charArray[ix]);
            }
        }
        if (quote)
        {
            sb.append("\\E");
        }
        return Pattern.compile(sb.toString(), Pattern.DOTALL);
    }

    /**
     * Construct the property matcher.
     *
     * @param propertyDescriptor The property descriptor.
     * @param operator The operator.
     * @param operand The string to be matched.
     */
    public StringPropertyMatcher(PropertyDescriptor<String> propertyDescriptor, OperatorType operator, String operand)
    {
        super(propertyDescriptor, operand);
        myOperator = operator;
    }

    /**
     * Construct the property matcher with an {@link OperatorType#EQ} operator.
     *
     * @param propertyDescriptor The property descriptor.
     * @param operand The string to be matched.
     */
    public StringPropertyMatcher(PropertyDescriptor<String> propertyDescriptor, String operand)
    {
        this(propertyDescriptor, OperatorType.EQ, operand);
    }

    /**
     * Construct the property matcher.
     *
     * @param propertyName The property name.
     * @param operator The operator.
     * @param operand The string to be matched.
     */
    public StringPropertyMatcher(String propertyName, OperatorType operator, String operand)
    {
        super(new PropertyDescriptor<String>(propertyName, String.class), operand);
        myOperator = operator;
    }

    /**
     * Construct the property matcher with an {@link OperatorType#EQ} operator.
     *
     * @param propertyName The property name.
     * @param operand The string to be matched.
     */
    public StringPropertyMatcher(String propertyName, String operand)
    {
        this(propertyName, OperatorType.EQ, operand);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        StringPropertyMatcher other = (StringPropertyMatcher)obj;
        return myOperator == other.myOperator;
    }

    /**
     * The operator that indicates how the string should be matched.
     *
     * @return The operator.
     */
    public StringPropertyMatcher.OperatorType getOperator()
    {
        return myOperator;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myOperator == null ? 0 : myOperator.hashCode());
        return result;
    }

    @Override
    public boolean matches(Object operand)
    {
        if (myOperator == OperatorType.LIKE)
        {
            if (operand instanceof CharSequence)
            {
                return getPattern().matcher((String)operand).matches();
            }
            else
            {
                return false;
            }
        }
        else
        {
            return getOperator() == OperatorType.NE ^ EqualsHelper.equals(getOperand(), operand);
        }
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(getClass().getSimpleName()).append('[')
                .append(getPropertyDescriptor().getPropertyName()).append(' ').append(getOperator()).append(' ')
                .append(getOperand()).append(']').toString();
    }

    /**
     * Generate a regex pattern from my operand.
     *
     * @return The pattern.
     */
    private synchronized Pattern getPattern()
    {
        if (myPattern == null)
        {
            myPattern = generatePatternFromLikeExpression(getOperand());
        }
        return myPattern;
    }

    /** Supported operator types. */
    public enum OperatorType
    {
        /** Operator that indicates the strings must match exactly. */
        EQ,

        /**
         * Operator that indicates the strings must match except where the
         * operand contains a %.
         */
        LIKE,

        /** Operator that indicates the strings must not match. */
        NE,
    }
}
