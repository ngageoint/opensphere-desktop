package io.opensphere.csvcommon.common;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Constants. */
public final class Constants
{
    /** Error label. */
    public static final String ERROR_LABEL = "ERROR";

    /** The text displayed in the Formatted column if the data was unchanged. */
    public static final String NON_FORMATTED_TEXT = "N/A";

    /** The column name for time. */
    public static final String TIME = "TIME";

    /** The column name for latitude. */
    public static final String LAT = "LAT";

    /** The column name for longitude. */
    public static final String LON = "LON";

    /** The column name for MGRS. */
    public static final String MGRS = "MGRS";

    /** These values do not force a save. */
    public static final Set<String> NO_SAVE_SET = getNoSave();

    /**
     * Construct the NO_SAVE_SET.
     * @return value of NO_SAVE_SET
     */
    private static Set<String> getNoSave()
    {
        TreeSet<String> ret = new TreeSet<>();
        ret.add(ERROR_LABEL);
        ret.add(NON_FORMATTED_TEXT);
        return Collections.unmodifiableSet(ret);
    }

    /** Private constructor. */
    private Constants()
    {
    }
}
