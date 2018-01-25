package io.opensphere.core.util.predicate;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

/**
 * A predicate that accepts strings that can be parsed as numbers.
 */
public class NumberPredicate implements Predicate<String>
{
    @Override
    public boolean test(String token)
    {
        boolean isValid;
        if (StringUtils.isBlank(token))
        {
            isValid = false;
        }
        else
        {
            try
            {
                Double.valueOf(token);
                // Double.valueOf allows 'd' and 'f' but we don't
                isValid = token.indexOf('d') == -1 && token.indexOf('f') == -1;
            }
            catch (NumberFormatException e)
            {
                isValid = false;
            }
        }
        return isValid;
    }
}
