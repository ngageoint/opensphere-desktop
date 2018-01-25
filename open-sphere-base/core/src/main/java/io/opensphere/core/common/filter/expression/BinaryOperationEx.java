package io.opensphere.core.common.filter.expression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableFloat;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This expression represents a binary operation expression (e.g. add, subtract,
 * multiply and divide).
 */
public class BinaryOperationEx implements Expression
{
    public enum OperationType
    {
        ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/");

        /**
         * The Java symbol for this operation.
         */
        private String symbol;

        /**
         * Constructor.
         *
         * @param symbol the Java symbol for this operation.
         */
        OperationType(String symbol)
        {
            this.symbol = symbol;
        }

        /**
         * Returns the symbol.
         *
         * @return the symbol.
         */
        public String getSymbol()
        {
            return symbol;
        }
    }

    /**
     * The <code>MathContext</code> for arithmetic in
     * {@link #getValueFrom(FilterDTO)}.
     */
    protected static final MathContext DEFAULT_MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);

    /**
     * The operation type.
     */
    private BinaryOperationEx.OperationType type;

    /**
     * The left-hand side of the comparison.
     */
    private Expression leftExpression;

    /**
     * The right-hand side of the comparison.
     */
    private Expression rightExpression;

    /**
     * Constructor.
     *
     * @param type the operation type.
     * @param leftExpression the left-hand side of the comparison.
     * @param rightExpression the right-hand side of the comparison.
     */
    public BinaryOperationEx(BinaryOperationEx.OperationType type, Expression leftExpression, Expression rightExpression)
    {
        this.type = type;
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    /**
     * Returns the operation type.
     *
     * @return the operation type.
     */
    public BinaryOperationEx.OperationType getType()
    {
        return type;
    }

    /**
     * Sets the operation type.
     *
     * @param type the operation type.
     */
    public void setType(BinaryOperationEx.OperationType type)
    {
        this.type = type;
    }

    /**
     * Returns the left-hand side of the comparison.
     *
     * @return the left-hand side of the comparison.
     */
    public Expression getLeftExpression()
    {
        return leftExpression;
    }

    /**
     * Sets the left-hand side of the comparison.
     *
     * @param leftExpression the left-hand side of the comparison.
     */
    public void setLeftExpression(Expression leftExpression)
    {
        this.leftExpression = leftExpression;
    }

    /**
     * Returns the right-hand side of the comparison.
     *
     * @return the right-hand side of the comparison.
     */
    public Expression getRightExpression()
    {
        return rightExpression;
    }

    /**
     * Sets the right-hand side of the comparison.
     *
     * @param rightExpression the right-hand side of the comparison.
     */
    public void setRightExpression(Expression rightExpression)
    {
        this.rightExpression = rightExpression;
    }

    /**
     * @see io.opensphere.core.common.filter.expression.Expression#getValueFrom(FilterDTO)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getValueFrom(FilterDTO dto)
    {
        Object result;

        Object obj1 = getLeftExpression().getValueFrom(dto);
        Object obj2 = getRightExpression().getValueFrom(dto);

        // Handle date arithmetic (date +/- duration)
        if (obj1 instanceof Date)
        {
            Date date = (Date)obj1;
            Long duration = toLong(obj2);

            Date arithResult = null;
            if (duration != null)
            {
                switch (getType())
                {
                    case ADD:
                        arithResult = new Date(date.getTime() + duration);
                        break;
                    case SUBTRACT:
                        arithResult = new Date(date.getTime() - duration);
                        break;
                    default:
                        throw new ExpressionException(
                                "Cannot perform a '" + getType() + "' operation on a date and duration: " + this);
                }
            }

            result = arithResult;
        }

        // Handle date arithmetic (duration + date)
        else if (obj2 instanceof Date)
        {
            Long duration = toLong(obj1);
            Date date = (Date)obj2;

            Date arithResult = null;
            if (duration != null)
            {
                switch (getType())
                {
                    case ADD:
                        arithResult = new Date(duration + date.getTime());
                        break;
                    default:
                        throw new ExpressionException(
                                "Cannot perform a '" + getType() + "' operation on a duration and date: " + this);
                }
            }

            result = arithResult;
        }

        // Handle numeric arithmetic
        else
        {
            Comparable<? extends Number> num1 = (Comparable<? extends Number>)obj1;
            Comparable<? extends Number> num2 = (Comparable<? extends Number>)obj2;

            // Convert the values to BigDecimals.
            BigDecimal bd1 = toBigDecimal(num1);
            BigDecimal bd2 = toBigDecimal(num2);

            // Perform the arithmetic.
            BigDecimal arithResult = null;
            switch (getType())
            {
                case ADD:
                    arithResult = bd1.add(bd2, DEFAULT_MATH_CONTEXT);
                    break;
                case SUBTRACT:
                    arithResult = bd1.subtract(bd2, DEFAULT_MATH_CONTEXT);
                    break;
                case MULTIPLY:
                    arithResult = bd1.multiply(bd2, DEFAULT_MATH_CONTEXT);
                    break;
                case DIVIDE:
                    arithResult = bd1.divide(bd2, DEFAULT_MATH_CONTEXT);
                    break;
            }

            result = convert(arithResult, num1, num2);
        }
        return result;
    }

    /**
     * Converts the given number to a <code>BigDecimal</code>.
     *
     * @param number the number to convert.
     * @return the <code>BigDecimal</code> version of the given number.
     */
    public static BigDecimal toBigDecimal(Object number)
    {
        BigDecimal result = null;
        if (number instanceof BigDecimal)
        {
            result = (BigDecimal)number;
        }
        else if (number instanceof BigInteger)
        {
            result = new BigDecimal((BigInteger)number);
        }
        else if (number instanceof Double || number instanceof Float || number instanceof MutableDouble
                || number instanceof MutableFloat)
        {
            result = BigDecimal.valueOf(((Number)number).doubleValue());
        }
        else if (number != null)
        {
            result = BigDecimal.valueOf(((Number)number).longValue());
        }

        return result;
    }

    /**
     * Utility method to convert an unknown Object to a Long.
     *
     * @param obj The object to convert
     * @return The Long value from the object or null if not possible
     */
    private static Long toLong(Object obj)
    {
        Long result = null;
        if (obj instanceof Number)
        {
            result = ((Number)obj).longValue();
        }
        else if (obj instanceof String)
        {
            try
            {
                result = Long.parseLong((String)obj);
            }
            catch (NumberFormatException e)
            {
                result = null;
            }
        }
        return result;
    }

    /**
     * Converts the given <code>BigDecimal</code> to the type most appropriate
     * for the two given numbers.
     *
     * @param value the <code>BigDecimal</code> to convert.
     * @param num1 the first number to check.
     * @param num2 the second number to check.
     * @return the narrowing converted representation of <code>value</code>.
     */
    public static Comparable<? extends Number> convert(BigDecimal value, Comparable<? extends Number> num1,
            Comparable<? extends Number> num2)
    {
        Comparable<? extends Number> result = null;
        if (num1 instanceof BigDecimal || num2 instanceof BigDecimal)
        {
            result = value;
        }
        else if (num1 instanceof Double || num2 instanceof Double)
        {
            result = value.doubleValue();
        }
        else if (num1 instanceof Float || num2 instanceof Float)
        {
            result = value.floatValue();
        }
        else if (num1 instanceof BigInteger || num2 instanceof BigInteger)
        {
            result = value.toBigInteger();
        }
        else if (num1 instanceof Long || num2 instanceof Long)
        {
            result = value.longValue();
        }
        else if (num1 instanceof Integer || num2 instanceof Integer)
        {
            result = value.intValue();
        }
        else if (num1 instanceof Short || num2 instanceof Short)
        {
            result = value.shortValue();
        }
        else if (num1 instanceof Byte || num2 instanceof Byte)
        {
            result = value.byteValue();
        }
        else
        {
            throw new IllegalArgumentException(
                    "Unexpected Number types: " + num1.getClass().getName() + " and " + num2.getClass().getName());
        }
        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "(" + getLeftExpression() + " " + getType() + " " + getRightExpression() + ")";
    }

    /**
     * @see io.opensphere.core.common.filter.expression.Expression#clone()
     */
    @Override
    public BinaryOperationEx clone() throws CloneNotSupportedException
    {
        return new BinaryOperationEx(type, leftExpression.clone(), rightExpression.clone());
    }
}
