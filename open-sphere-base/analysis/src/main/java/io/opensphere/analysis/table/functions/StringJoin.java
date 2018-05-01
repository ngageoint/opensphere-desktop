package io.opensphere.analysis.table.functions;

import org.apache.commons.lang.StringUtils;

/** Joins all values in an array as a single String. */
public class StringJoin extends ColumnFunction
{
    /** Constructs a StringJoin function. */
    protected StringJoin()
    {
        super("String Join", 0, StringUtils::join);
    }

}
