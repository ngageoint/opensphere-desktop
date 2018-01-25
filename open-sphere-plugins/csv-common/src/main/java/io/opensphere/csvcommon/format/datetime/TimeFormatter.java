package io.opensphere.csvcommon.format.datetime;

import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * The formatter used to format time values.
 *
 */
public class TimeFormatter extends BaseDateTimeFormatter
{
    /**
     * Constructs a new time formatter.
     *
     * @param preferencesRegistry The preferences registry.
     */
    public TimeFormatter(PreferencesRegistry preferencesRegistry)
    {
        super(preferencesRegistry);
    }

    @Override
    public String getSystemFormat()
    {
        String systemFormat = super.getSystemFormat();

        int timeIndex = systemFormat.indexOf('H');
        systemFormat = systemFormat.substring(timeIndex, systemFormat.length());

        return systemFormat;
    }

    @Override
    public void saveNewFormat(String format)
    {
        if (!format.contains("y") && !format.contains("M") && !format.contains("d"))
        {
            super.saveNewFormat(format);
        }
    }

    @Override
    protected Type getFormatType()
    {
        return Type.TIME;
    }
}
