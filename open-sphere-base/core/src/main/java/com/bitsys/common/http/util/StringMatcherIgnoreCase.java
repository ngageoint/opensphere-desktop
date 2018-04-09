package com.bitsys.common.http.util;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicate;

/**
 * This class is a {@link Predicate} for comparing strings without regard for
 * the case.
 */
public class StringMatcherIgnoreCase implements Predicate<String>
{
    /** The value for comparison. */
    private final String value;

    /**
     * Constructs a <code>StringMatcher</code>.
     *
     * @param value the value for comparison.
     */
    public StringMatcherIgnoreCase(final String value)
    {
        this.value = value;
    }

    /**
     * Returns the value for comparison.
     *
     * @return the value for comparison.
     */
    public String getValue()
    {
        return value;
    }

    @Override
    public boolean apply(final String value)
    {
        return StringUtils.equalsIgnoreCase(value, this.value);
    }
}
