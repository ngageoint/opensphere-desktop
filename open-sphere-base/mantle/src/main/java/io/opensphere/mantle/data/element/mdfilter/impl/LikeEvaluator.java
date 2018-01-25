package io.opensphere.mantle.data.element.mdfilter.impl;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;

/**
 * The Class LikeEvaluator.
 */
public class LikeEvaluator extends AbstractEvaluator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LikeEvaluator.class);

    /** The Constant ESCAPED_ESCAPE. */
    private static final String ESCAPED_ESCAPE = "\\\\";

    /** The Pattern. */
    private Pattern myPattern;

    /**
     * Instantiates a new like evaluator.
     *
     * @param value the value
     */
    public LikeEvaluator(Object value)
    {
        this(value, false);
    }

    /**
     * Instantiates a new like evaluator.
     *
     * @param value the value
     * @param isRegex true if the value is a regex, false if a simple user-friendly search string
     */
    public LikeEvaluator(Object value, boolean isRegex)
    {
        super(value);
        compileLikePattern(getValueAsString(), isRegex);
    }

    @Override
    public boolean evaluate(Object valueToEvaluate)
    {
        if (valueToEvaluate != null)
        {
            return myPattern != null ? myPattern.matcher(valueToEvaluate.toString()).matches()
                    : valueToEvaluate.toString().toLowerCase().indexOf(getValueAsString().toLowerCase()) != -1;
        }
        return false;
    }

    /**
     * Compile like pattern.
     *
     * @param valueAsString the value as string
     * @param isRegex true if the value is a regex, false if a simple user-friendly search string
     */
    private void compileLikePattern(String valueAsString, boolean isRegex)
    {
        try
        {
            String value = !isRegex ? quoteMeta(valueAsString) : valueAsString;
            myPattern = Pattern.compile(value, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        }
        catch (PatternSyntaxException e)
        {
            myPattern = null;
            LOGGER.error(e);
        }
    }

    /**
     * Take an input string and quote out any special regex characters, but
     * replace any non-escaped (\) * or ? with the regex match all or match one.
     *
     * @param input the input
     * @return the string
     */
    private static String quoteMeta(String input)
    {
        Utilities.checkNull(input, "input");
        int length = input.length();
        if (length == 0)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int escapeCount = 0;
        for (int i = 0; i < length; i++)
        {
            char c = input.charAt(i);
            if ("\\".indexOf(c) != -1)
            {
                escapeCount++;
                if (escapeCount == 2)
                {
                    sb.append(ESCAPED_ESCAPE);
                    escapeCount = 0;
                }
            }
            else if ("*?".indexOf(c) != -1)
            {
                if (escapeCount == 0)
                {
                    sb.append(c == '*' ? ".*?" : ".");
                }
                else
                {
                    sb.append(ESCAPED_ESCAPE).append(c);
                    escapeCount = 0;
                }
            }
            else if ("[](){}.+$^|#".indexOf(c) != -1)
            {
                if (escapeCount == 1)
                {
                    sb.append(ESCAPED_ESCAPE);
                    escapeCount = 0;
                }
                sb.append('\\');
                sb.append(c);
            }
            else
            {
                if (escapeCount == 1)
                {
                    sb.append(ESCAPED_ESCAPE);
                    escapeCount = 0;
                }
                sb.append(c);
            }
        }
        if (escapeCount == 1)
        {
            sb.append('\\');
        }
        return sb.toString();
    }
}
