package io.opensphere.csvcommon.format.datetime;

import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * The formatter for Date/Time values.
 *
 */
public class DateTimeFormatter extends BaseDateTimeFormatter
{
    /**
     * Constructs a date time formatter.
     *
     * @param preferencesRegistry The toolbox.
     */
    public DateTimeFormatter(PreferencesRegistry preferencesRegistry)
    {
        super(preferencesRegistry);
    }

    @Override
    public void saveNewFormat(String format)
    {
        boolean containsTime = format.contains("H") || format.contains("m") || format.contains("s") || format.contains("S")
                || format.contains("h");
        boolean containsDate = format.contains("y") || format.contains("M") || format.contains("d");
        if (containsTime && containsDate)
        {
            super.saveNewFormat(format);
        }
    }

    @Override
    protected Type getFormatType()
    {
        return Type.TIMESTAMP;
    }
}
