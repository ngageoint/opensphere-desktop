package io.opensphere.core.common.filter.operator;

import java.util.Arrays;
import java.util.regex.Pattern;

import io.opensphere.core.common.filter.dto.FilterDTO;

/**
 * This class is a comparison operator specializing in <code>LIKE</code>
 * operations.
 */
public class PropertyIsLikeOp extends ComparisonOp
{
    /**
     * The class encapsulates a SQL <code>LIKE</code> condition.
     */
    public static class LikeCondition
    {
        /**
         * The parameterized SQL string.
         */
        private String sql;

        /**
         * The parameter values for the SQL string.
         */
        private Object[] values;

        /**
         * Constructor.
         *
         * @param sql the parameterized SQL string.
         * @param values the parameter values for the SQL string.
         */
        public LikeCondition(String sql, Object[] values)
        {
            this.sql = sql;
            this.values = values;
        }

        /**
         * Returns the parameterized SQL string. This string will contain
         * question marks. The values for these parameters are returned in
         * {@link #getValues()}.
         *
         * @return the parameterized SQL string.
         */
        public String getSql()
        {
            return sql;
        }

        /**
         * The parameter values for the SQL string. These values correspond to
         * the question marks listed in the SQL string. The values are returned
         * here so that they can be properly added to a JDBC
         * <code>Statement</code> thus eliminating concerns with SQL injection.
         *
         * @return the parameter values for the SQL string.
         */
        public Object[] getValues()
        {
            return values;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("LikeCondition [sql=");
            builder.append(sql);
            builder.append(", values=");
            builder.append(Arrays.toString(values));
            builder.append("]");
            return builder.toString();
        }
    }

    /**
     * The default SQL escape character. This value should be used rather than
     * relying on
     */
    public static final char SQL_ESCAPE_CHAR = '\\';

    /**
     * The property name.
     */
    private String name;

    /**
     * The pattern against which the value corresponding to name is compared.
     */
    private String pattern;

    /**
     * The pattern of characters that matches zero or more characters. In
     * regular expressions, this is ".*".
     */
    private String wildCard;

    /**
     * The character that matches exactly one character. In regular expressions,
     * this is ".".
     */
    private String singleChar;

    /**
     * The character used to escape the meaning of the {@link #wildCard},
     * {@link #singleChar} and {@link #escapeChar} itself.
     */
    private String escapeChar;

    /**
     * The regular expression pattern.
     */
    private Pattern regexPattern = null;

    /**
     * Constructor.
     *
     * @param name the property name.
     * @param pattern the full pattern to use in comparison.
     * @param wildCard the wild card pattern.
     * @param singleChar the single character pattern.
     * @param escapeChar the escape character.
     */
    public PropertyIsLikeOp(String name, String pattern, String wildCard, String singleChar, String escapeChar)
    {
        this.name = name;
        this.pattern = pattern;
        this.wildCard = wildCard;
        this.singleChar = singleChar;
        this.escapeChar = escapeChar;
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
     * Sets the property name.
     *
     * @param name the property name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the pattern against which the value corresponding to name is
     * compared.
     *
     * @return the pattern.
     */
    public String getPattern()
    {
        return pattern;
    }

    /**
     * Sets the pattern against which the value corresponding to name is
     * compared.
     *
     * @param pattern the pattern.
     */
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
        regexPattern = null;
    }

    /**
     * Returns the pattern of characters that matches zero or more characters.
     * In regular expressions, this is ".*".
     *
     * @return the wild card pattern.
     */
    public String getWildCard()
    {
        return wildCard;
    }

    /**
     * Sets the pattern of characters that matches zero or more characters. In
     * regular expressions, this is ".*".
     *
     * @param wildCard the wild card pattern.
     */
    public void setWildCard(String wildCard)
    {
        this.wildCard = wildCard;
        regexPattern = null;
    }

    /**
     * Returns the character that matches exactly one character. In regular
     * expressions, this is ".".
     *
     * @return the single character pattern.
     */
    public String getSingleChar()
    {
        return singleChar;
    }

    /**
     * Sets the character that matches exactly one character. In regular
     * expressions, this is ".".
     *
     * @param singleChar the single character pattern.
     */
    public void setSingleChar(String singleChar)
    {
        this.singleChar = singleChar;
        regexPattern = null;
    }

    /**
     * Returns the character used to escape the meaning of the
     * {@link #getWildCard()}, {@link #getSingleChar()} and
     * {@link #getEscapeChar()} itself.
     *
     * @return the escape character.
     */
    public String getEscapeChar()
    {
        return escapeChar;
    }

    /**
     * Sets the character used to escape the meaning of the
     * {@link #getWildCard()}, {@link #getSingleChar()} and
     * {@link #getEscapeChar()} itself.
     *
     * @param escapeChar the escape character.
     */
    public void setEscapeChar(String escapeChar)
    {
        this.escapeChar = escapeChar;
        regexPattern = null;
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#evaluate(FilterDTO)
     */
    @Override
    public boolean evaluate(FilterDTO dto)
    {
        boolean result = false;
        Object value = dto.get(getName());
        if (value != null)
        {
            if (value instanceof CharSequence)
            {
                // If the regex pattern has not been compiled, do so now.
                if (regexPattern == null)
                {
                    regexPattern = Pattern.compile(convertToRegex());
                }
                result = regexPattern.matcher((CharSequence)value).matches();
            }
            else
            {
                throw new IllegalArgumentException("The value associated with " + getName()
                        + " must be a character sequence and not a " + value.getClass().getName());
            }
        }
        return result;
    }

    /**
     * Converts the pattern, wild card, single and escape characters to the
     * regular expression equivalents. Everything else in the pattern is treated
     * as a regex literal.
     *
     * @return the equivalent regular expression string.
     */
    public String convertToRegex()
    {
        // Convert the pattern to a regular expression.
        StringBuilder regex = new StringBuilder();
        String likePattern = getPattern();

        // These are required in WFS but we'll handle null values here.
        int escapeCharIndex = getEscapeChar() == null ? -1 : likePattern.indexOf(getEscapeChar());
        int wildCardIndex = getWildCard() == null ? -1 : likePattern.indexOf(getWildCard());
        int singleCharIndex = getSingleChar() == null ? -1 : likePattern.indexOf(getSingleChar());
        for (int index = 0; index < likePattern.length(); index++)
        {
            char ch = likePattern.charAt(index);
            if (index == escapeCharIndex)
            {
                escapeCharIndex = likePattern.indexOf(getEscapeChar(), escapeCharIndex + getEscapeChar().length());

                // If there are consecutive escape characters, skip to the
                // next one to save it in the regex.
                if (index + 1 == escapeCharIndex)
                {
                    escapeCharIndex = likePattern.indexOf(getEscapeChar(), escapeCharIndex + getEscapeChar().length());
                }
                else if (index + 1 == wildCardIndex)
                {
                    wildCardIndex = likePattern.indexOf(getWildCard(), wildCardIndex + getWildCard().length());
                }
                else if (index + 1 == singleCharIndex)
                {
                    singleCharIndex = likePattern.indexOf(getSingleChar(), singleCharIndex + getSingleChar().length());
                }
                else
                {
                    // This is an undefined condition, just skip the escape
                    // character.
                }
            }

            // Insert the regular expression equivalent of a wild card.
            else if (index == wildCardIndex)
            {
                regex.append(".*");
                index += getWildCard().length() - 1;
                wildCardIndex = likePattern.indexOf(getWildCard(), wildCardIndex + getWildCard().length());
            }

            // Insert the regular expression equivalent of the single char.
            else if (index == singleCharIndex)
            {
                regex.append(".");
                index += getSingleChar().length() - 1;
                singleCharIndex = likePattern.indexOf(getSingleChar(), singleCharIndex + getSingleChar().length());
            }

            // Handle certain characters in a special manner.
            else if ('[' == ch || ']' == ch || '\\' == ch || '^' == ch)
            {
                regex.append('\\').append(ch);
            }

            // Force everything else to be literals.
            else
            {
                regex.append('[').append(ch).append(']');
            }
        }
        return regex.toString();
    }

    /**
     * Converts the pattern, wild card, single and escape characters to the SQL
     * <code>LIKE</code> predicate syntax. Everything else in the pattern
     * remains unchanged.
     * <p>
     * The resulting SQL will be <code><i>searchValue</i> LIKE ? ESCAPE ?</code>
     * which should work in MySQL, Oracle and PostgreSQL. The
     * <code>Object</code> array will contain the values for the two parameters
     * in the SQL string.
     *
     * @param searchValue the value to be checked against the pattern.
     * @return the equivalent <code>LIKE</code> predicate SQL.
     */
    public LikeCondition convertToLikeCondition(String searchValue)
    {
        Object[] values = new Object[] { generateSqlPattern(), String.valueOf(SQL_ESCAPE_CHAR) };
        return new LikeCondition(searchValue + " LIKE ? ESCAPE ?", values);
    }

    /**
     * Converts the pattern, wild card, single and escape characters to the SQL
     * <code>LIKE</code>-equivalent syntax. Everything else in the pattern
     * remains unchanged. <br/>
     * <br/>
     * NOTE: Callers of this method also need to specify <code>LIKE</code>'s
     * <code>ESCAPE</code> parameter as the backslash (\).
     *
     * @return the equivalent SQL <code>LIKE</code> clause string.
     * @deprecated Use {@link #convertToLikeCondition(String)} instead.
     */
    @Deprecated
    public String convertToSql()
    {
        return generateSqlPattern();
    }

    /**
     * Converts the pattern, wild card, single and escape characters to the SQL
     * <code>LIKE</code>-equivalent syntax. Everything else in the pattern
     * remains unchanged. <br/>
     * <br/>
     * NOTE: Callers of this method also need to specify <code>LIKE</code>'s
     * <code>ESCAPE</code> parameter as the backslash (\).
     *
     * @return the equivalent SQL <code>LIKE</code> clause string.
     */
    String generateSqlPattern()
    {
        // Convert the pattern to SQL's LIKE syntax.
        StringBuilder sql = new StringBuilder();
        String likePattern = getPattern();

        // These are required in WFS but we'll handle null values here.
        int escapeCharIndex = getEscapeChar() == null ? -1 : likePattern.indexOf(getEscapeChar());
        int wildCardIndex = getWildCard() == null ? -1 : likePattern.indexOf(getWildCard());
        int singleCharIndex = getSingleChar() == null ? -1 : likePattern.indexOf(getSingleChar());

        // Process each of the characters in the pattern.
        for (int index = 0; index < likePattern.length(); index++)
        {
            char ch = likePattern.charAt(index);
            if (index == escapeCharIndex)
            {
                escapeCharIndex = likePattern.indexOf(getEscapeChar(), escapeCharIndex + getEscapeChar().length());

                // If there are consecutive escape characters, skip to the
                // next one to save it in the regex.
                if (index + 1 == escapeCharIndex)
                {
                    escapeCharIndex = likePattern.indexOf(getEscapeChar(), escapeCharIndex + getEscapeChar().length());
                }
                else if (index + 1 == wildCardIndex)
                {
                    wildCardIndex = likePattern.indexOf(getWildCard(), wildCardIndex + getWildCard().length());
                }
                else if (index + 1 == singleCharIndex)
                {
                    singleCharIndex = likePattern.indexOf(getSingleChar(), singleCharIndex + getSingleChar().length());
                }
                else
                {
                    // This is an undefined condition, just skip the escape
                    // character.
                }
            }

            // Insert the SQL equivalent of a wild card.
            else if (index == wildCardIndex)
            {
                sql.append("%");
                index += getWildCard().length() - 1;
                wildCardIndex = likePattern.indexOf(getWildCard(), wildCardIndex + getWildCard().length());
            }

            // Insert the SQL equivalent of the single char.
            else if (index == singleCharIndex)
            {
                sql.append("_");
                index += getSingleChar().length() - 1;
                singleCharIndex = likePattern.indexOf(getSingleChar(), singleCharIndex + getSingleChar().length());
            }

            // Ensure that the literal escape character is properly escaped.
            else if (getEscapeChar() != null && getEscapeChar().charAt(0) == ch)
            {
                sql.append(SQL_ESCAPE_CHAR).append(SQL_ESCAPE_CHAR);
            }

            // Handle certain characters in a special manner.
            else if ('%' == ch || '_' == ch || SQL_ESCAPE_CHAR == ch)
            {
                sql.append(SQL_ESCAPE_CHAR).append(ch);
            }

            // Treat everything else as literals.
            else
            {
                sql.append(ch);
            }
        }

        return sql.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName() + " matches " + convertToRegex();
    }

    /**
     * @see io.opensphere.core.common.filter.operator.Operator#clone()
     */
    @Override
    public PropertyIsLikeOp clone() throws CloneNotSupportedException
    {
        return new PropertyIsLikeOp(name, pattern, wildCard, singleChar, escapeChar);
    }
}
