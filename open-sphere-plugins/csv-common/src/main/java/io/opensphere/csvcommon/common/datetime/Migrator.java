package io.opensphere.csvcommon.common.datetime;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Migrates the old DateFormatsConfiguration.xml to the new
 * com.bitsys.common.configuration.date.v1.DateFormatsConfiguration.xml.
 *
 */
public class Migrator
{
    /**
     * The key to the migration property.
     */
    private static final String ourMigratedKey = "v1Migrated";

    /**
     * Migrates the old DateFormatsConfiguration.xml to the new
     * com.bitsys.common.configuration.date.v1.DateFormatsConfiguration.xml.
     *
     * @param provider Used get and save the new formats file.
     * @param registry Used to get the old formats file.
     */
    public void migrate(ConfigurationProvider provider, PreferencesRegistry registry)
    {
        Preferences newPreferences = registry.getPreferences(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC);

        boolean alreadyMigrated = newPreferences.getBoolean(ourMigratedKey, false);

        if (!alreadyMigrated)
        {
            Preferences preferences = registry.getPreferences("DateFormatConfiguration");
            DateFormatsConfig oldConfig = preferences.getJAXBObject(DateFormatsConfig.class,
                    MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY, null);

            DateFormatsConfig newConfig = provider.getDateFormats();

            List<DateFormat> formatsToAdd = New.list();

            if (oldConfig != null)
            {
                Map<String, DateFormat> formats = mapFormats(newConfig);

                for (DateFormat format : oldConfig.getFormats())
                {
                    if (!formats.containsKey(format.getSdf()))
                    {
                        boolean isValid = isFormatValid(format);
                        if (isValid)
                        {
                            formatsToAdd.add(format);
                        }
                    }
                }

                if (!formatsToAdd.isEmpty())
                {
                    provider.saveFormats(formatsToAdd);
                }
            }

            newPreferences.putBoolean(ourMigratedKey, true, this);
        }
    }

    /**
     * Checks to see if this format is worthy enough for the new formats
     * configuration file.
     *
     * @param format The format to check.
     * @return True if the format is valid, false otherwise.
     */
    private boolean isFormatValid(DateFormat format)
    {
        boolean isValid = false;

        if (StringUtils.isNotEmpty(format.getSdf()) && !format.getSdf().contains("f"))
        {
            String formatString = format.getSdf();

            boolean containsTime = formatString.contains("H") || formatString.contains("m") || formatString.contains("s")
                    || formatString.contains("S") || formatString.contains("h");
            boolean containsDate = formatString.contains("y") || formatString.contains("M") || formatString.contains("d");

            if (containsTime && containsDate && format.getType() != Type.TIMESTAMP)
            {
                format.setType(Type.TIMESTAMP);
            }
            else if (containsTime && !containsDate && format.getType() != Type.TIME)
            {
                format.setType(Type.TIME);
            }
            else if (!containsTime && containsDate && format.getType() != Type.DATE)
            {
                format.setType(Type.DATE);
            }

            isValid = true;
        }

        return isValid;
    }

    /**
     * Maps the formats and uses their simple date format string as a unique
     * key.
     *
     * @param config The configuration containing the formats to use.
     * @return The mapped formats.
     */
    private Map<String, DateFormat> mapFormats(DateFormatsConfig config)
    {
        Map<String, DateFormat> mappedFormats = New.map();

        for (DateFormat format : config.getFormats())
        {
            mappedFormats.put(format.getSdf(), format);
        }

        return mappedFormats;
    }
}
