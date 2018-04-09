package io.opensphere.core.datafilter;

/**
 * The Interface Operators.
 */
public interface DataFilterOperators
{
    /**
     * The Enum Conditional.
     */
    enum Conditional
    {
    /** The EQ. */
    EQ("=", "is equal to"),

    /** The NEQ. */
    NEQ("!=", "is not equal to"),

    /** The LT. */
    LT("<", "is less than"),

    /** The LTE. */
    LTE("<=", "is less than or equal to"),

    /** The GT. */
    GT(">", "is greater than"),

    /** The GTE. */
    GTE(">=", "is greater than or equal to"),

    /** The LIKE. */
    LIKE("like", "is like"),

    /** The NOT_LIKE. */
    NOT_LIKE("not like", "is not like"),

    /** Is empty. */
    EMPTY("IS NULL", "is empty"),

    /** Is not empty. */
    NOT_EMPTY("IS NOT NULL", "is not empty"),

    /** The LIST. */
    IN_LIST("IN", "is in list"),

    /** The NOT_IN_LIST. Applause, please, for auto-generated docs. */
    NOT_IN_LIST("NOT_IN", "is not in list"),

    /** The LIKE_LIST. */
    LIKE_LIST("like list", "is like list"),

    /** The NOT_LIKE_LIST. */
    NOT_LIKE_LIST("not like list", "is not like list"),

    /** The BETWEEN. */
    BETWEEN("BETWEEN", "is between"),

    /** The contains operator. */
    CONTAINS("contains", "contains"),

    /** Matches a regex. */
    MATCHES("matches", "matches regex");

        /** The values that are supported for numerics. */
        public static final Conditional[] NUMBER_VALUES = new Conditional[] { EQ, NEQ, LT, LTE, GT, GTE, EMPTY, NOT_EMPTY,
            IN_LIST, NOT_IN_LIST, BETWEEN };

        /** The values that are supported for strings. */
        public static final Conditional[] STRING_VALUES = new Conditional[] { EQ, NEQ, LT, LTE, GT, GTE, LIKE, NOT_LIKE, EMPTY,
            NOT_EMPTY, IN_LIST, NOT_IN_LIST, LIKE_LIST, NOT_LIKE_LIST, BETWEEN, CONTAINS };

        /** The symbol. */
        private final String mySymbol;

        /** The display text. */
        private final String myDisplayText;

        /**
         * Instantiates a new conditional.
         *
         * @param sym the sym
         * @param displayText the display text
         */
        Conditional(String sym, String displayText)
        {
            mySymbol = sym;
            myDisplayText = displayText;
        }

        /**
         * Symbol.
         *
         * @return the string
         */
        public String symbol()
        {
            return mySymbol;
        }

        @Override
        public String toString()
        {
            return myDisplayText;
        }
    }

    /**
     * The Enum Logical.
     */
    enum Logical
    {
        /** The AND. */
        AND("&&", "All", "And"),

        /** The OR. */
        OR("||", "Any", "Or"),

        /** The NOT. */
        NOT("!", "Not", "Not");

        /** The symbol. */
        private final String mySymbol;

        /** The display text. */
        private final String myDisplayText;

        /** The logic text. */
        private final String myLogicText;

        /**
         * Instantiates a new logical.
         *
         * @param sym the sym
         * @param displayText the display text
         * @param logicText the logic display text
         */
        Logical(String sym, String displayText, String logicText)
        {
            mySymbol = sym;
            myDisplayText = displayText;
            myLogicText = logicText;
        }

        /**
         * Gets the logic text.
         *
         * @return the logic text
         */
        public String getLogicText()
        {
            return myLogicText;
        }

        /**
         * Symbol.
         *
         * @return the string
         */
        public String symbol()
        {
            return mySymbol;
        }

        @Override
        public String toString()
        {
            return myDisplayText;
        }
    }
}
