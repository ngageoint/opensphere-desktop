package io.opensphere.core.util.net;

import java.util.function.Function;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * Function that replaces yankee characters with wildcard.
 */
public class YankeeReplacer implements Function<String, String>
{
    @Override
    public String apply(String value)
    {
        String modValue = value;
        if (StringUtilities.endsWith(value, 'Y'))
        {
            modValue = StringUtilities.replace(value, value.length() - 1, '*');
        }
        return modValue;
    }
}
