package io.opensphere.analysis.table.functions;

import java.util.Objects;
import java.util.StringJoiner;

/** Joins all values in an array as a single String. */
public class StringJoin extends ColumnFunction
{
    /** Constructs a StringJoin function. */
    protected StringJoin()
    {
        super("String Join", 0, StringJoin::performJoin);
    }

    /**
     * Joins array of Objects to a single String, utilizing
     * {@link Objects#toString(Object)} as the String parser.
     * <p>
     * Resulting Strings will be in the format "0,1,...".
     *
     * @param objects the objects to operate on
     * @return the string
     */
    static String performJoin(Object... objects)
    {
        StringJoiner sj = new StringJoiner(",", "", "");
        for (Object o : objects)
        {
            sj.add(Objects.toString(o));
        }
        return sj.toString();
    }
}
