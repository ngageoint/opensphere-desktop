package io.opensphere.core.common.filter.operator;

import java.util.Date;

import io.opensphere.core.common.filter.dto.FilterDTO;
import io.opensphere.core.common.filter.expression.BinaryOperationEx;
import io.opensphere.core.common.filter.expression.Expression;

/**
 * This class represents a binary comparison operator that supports the basic
 * comparison functions:
 * <ul>
 * <li>less than (&lt;)
 * <li>less than or equal to (&lt;=)
 * <li>equal to (==)
 * <li>greater than or equal to (>=)
 * <li>greater than (>)
 * <li>not equal to (!=)
 * </ul>
 */
public class BinaryComparisonOp extends ComparisonOp
{
    public enum ComparisonType
    {
        LESS_THAN("<"), LESS_THAN_OR_EQUAL_TO("<="), EQUAL_TO("="), GREATER_THAN_OR_EQUAL_TO(">="), GREATER_THAN(
                ">"), NOT_EQUAL_TO("!=");

        /**
         * The Java symbol for this comparison.
         */
        private String symbol;

        /**
         * Constructor.
         *
         * @param symbol the Java symbol for this comparison.
         */
        ComparisonType(String symbol)
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
     * The comparison type.
     */
    private BinaryComparisonOp.ComparisonType type;

    /**
     * The left-hand side of the comparison.
     */
    private Expression leftExpression;

    /**
     * The right-hand side of the comparison.
     */
    private Expression rightExpression;

    /**
     * Indicates if case should be matched in the comparison.
     */
    private boolean matchCase;

    /**
     * Constructor.
     *
     * @param type the comparison type.
     * @param leftExpression the left-hand side of the comparison.
     * @param rightExpression the right-hand side of the comparison.
     * @param matchCase indicates if case should be matched in the comparison.
     */
    public BinaryComparisonOp(BinaryComparisonOp.ComparisonType type, Expression leftExpression, Expression rightExpression,
            boolean matchCase)
    {
        this.type = type;
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
        this.matchCase = matchCase;
    }

    /**
     * Returns the comparison type.
     *
     * @return the comparison type.
     */
    public BinaryComparisonOp.ComparisonType getType()
    {
        return type;
    }

    /**
     * Sets the comparison type.
     *
     * @param type the comparison type.
     */
    public void setType(BinaryComparisonOp.ComparisonType type)
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
     * Sets the match case flag.
     *
     * @param matchCase the match case flag.
     */
    public void setMatchCase(boolean matchCase)
    {
        this.matchCase = matchCase;
    }

    /**
     * Returns <code>true</code> if case matters in the comparison.
     *
     * @return <code>true</code> if case matters in the comparison.
     */
    public boolean isMatchCase()
    {
        return matchCase;
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#evaluate(FilterDTO)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(FilterDTO dto)
    {
        boolean result = false;
        Object obj1 = getLeftExpression().getValueFrom(dto);
        Object obj2 = getRightExpression().getValueFrom(dto);

        // If either of these are false then return false, usually
        // this means the filter contains a field that is not found
        // in the backing dto
        if (obj1 == null || obj2 == null)
        {
            return false;
        }

        // If the case does not matter and the objects are CharSequences, cast
        // and
        // do a different comparison.
        else if (!isMatchCase() && obj1 instanceof CharSequence && obj2 instanceof CharSequence)
        {
            String str1 = obj1.toString();
            String str2 = obj2.toString();
            switch (getType())
            {
                case LESS_THAN:
                    result = str1.compareToIgnoreCase(str2) < 0;
                    break;
                case LESS_THAN_OR_EQUAL_TO:
                    result = str1.compareToIgnoreCase(str2) <= 0;
                    break;
                case EQUAL_TO:
                    result = str1.compareToIgnoreCase(str2) == 0;
                    break;
                case GREATER_THAN_OR_EQUAL_TO:
                    result = str1.compareToIgnoreCase(str2) >= 0;
                    break;
                case GREATER_THAN:
                    result = str1.compareToIgnoreCase(str2) > 0;
                    break;
                case NOT_EQUAL_TO:
                    result = str1.compareToIgnoreCase(str2) != 0;
                    break;
            }
        }

        else
        {
            // if the class types do not match, see if we can force a match.
            if (!obj1.getClass().equals(obj2.getClass()))
            {
                // If both are Numbers, convert to BigDecimal.
                if (obj1 instanceof Number && obj2 instanceof Number)
                {
                    obj1 = BinaryOperationEx.toBigDecimal(obj1);
                    obj2 = BinaryOperationEx.toBigDecimal(obj2);
                }

                // If both are CharSequences, convert to String.
                else if (obj1 instanceof CharSequence && obj2 instanceof CharSequence)
                {
                    obj1 = obj1.toString();
                    obj2 = obj2.toString();
                }

                // If both are Dates, convert to Date.
                else if (obj1 instanceof Date && obj2 instanceof Date)
                {
                    if (Date.class != obj1.getClass())
                    {
                        obj1 = new Date(((Date)obj1).getTime());
                    }
                    if (Date.class != obj2.getClass())
                    {
                        obj2 = new Date(((Date)obj2).getTime());
                    }
                }
            }

            // Ensure that the Class types match before comparing.
            if (obj1.getClass().equals(obj2.getClass()))
            {
                switch (getType())
                {
                    case LESS_THAN:
                        result = ((Comparable<Object>)obj1).compareTo(obj2) < 0;
                        break;
                    case LESS_THAN_OR_EQUAL_TO:
                        result = ((Comparable<Object>)obj1).compareTo(obj2) <= 0;
                        break;
                    case EQUAL_TO:
                        result = ((Comparable<Object>)obj1).compareTo(obj2) == 0;
                        break;
                    case GREATER_THAN_OR_EQUAL_TO:
                        result = ((Comparable<Object>)obj1).compareTo(obj2) >= 0;
                        break;
                    case GREATER_THAN:
                        result = ((Comparable<Object>)obj1).compareTo(obj2) > 0;
                        break;
                    case NOT_EQUAL_TO:
                        result = ((Comparable<Object>)obj1).compareTo(obj2) != 0;
                        break;
                }
            }
        }

        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "(" + getLeftExpression() + " " + getType().getSymbol() + (isMatchCase() ? " " : " (ignore case) ")
                + getRightExpression() + ")";
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#clone()
     */
    @Override
    public BinaryComparisonOp clone() throws CloneNotSupportedException
    {
        return new BinaryComparisonOp(type, leftExpression.clone(), rightExpression.clone(), matchCase);
    }
}
