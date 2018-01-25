package io.opensphere.csvcommon.format.datetime;

import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * The formatter for dates.
 *
 */
public class DateFormatter extends BaseDateTimeFormatter
{
    /**
     * Constructs a new date formatter.
     *
     * @param preferencesRegistry The preferences registry.
     */
    public DateFormatter(PreferencesRegistry preferencesRegistry)
    {
        super(preferencesRegistry);
    }

    @Override
    public String getSystemFormat()
    {
        String systemFormat = super.getSystemFormat();

        int timeIndex = systemFormat.indexOf('H');
        systemFormat = systemFormat.substring(0, timeIndex - 1);

        systemFormat = systemFormat.trim();

        return systemFormat;
    }

    @Override
    public void saveNewFormat(String format)
    {
        if (!format.contains("H") && !format.contains("m") && !format.contains("s") && !format.contains("S")
                && !format.contains("h"))
        {
            super.saveNewFormat(format);
        }
    }

    @Override
    protected Type getFormatType()
    {
        return Type.DATE;
    }
}
