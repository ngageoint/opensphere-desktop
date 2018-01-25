package io.opensphere.core.common.filter.operator;

import org.apache.commons.lang3.StringUtils;

/**
 * This abstract class represents binary logical operators (e.g. && and ||).
 */
public abstract class BinaryLogicalOp extends LogicalOp
{
    public enum BinaryLogicType
    {
        AND("&&"), OR("||");

        /**
         * The Java symbol for this logical operator.
         */
        private String symbol;

        /**
         * Constructor.
         *
         * @param symbol the Java symbol for this logical operator.
         */
        BinaryLogicType(String symbol)
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
    private BinaryLogicType type;

    /**
     * Constructor.
     *
     * @param type the binary logic type.
     */
    public BinaryLogicalOp(BinaryLogicType type)
    {
        this.type = type;
    }

    /**
     * Returns the binary logic type.
     *
     * @return the binary logic type.
     */
    public BinaryLogicType getType()
    {
        return type;
    }

    /**
     * Sets the binary logic type.
     *
     * @param type the binary logic type.
     */
    public void setType(BinaryLogicType type)
    {
        this.type = type;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "(" + StringUtils.join(getOperators(), " " + getType() + " ") + ")";
    }
}
