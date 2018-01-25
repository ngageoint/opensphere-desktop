package io.opensphere.csvcommon.common.datetime;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Provides the configured date formats and their regular expression
 * representing them.
 *
 */
public class ConfigurationProviderImpl implements ConfigurationProvider
{
    /**
     * The preferences registry.
     */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Constructs a new configuration provider.
     *
     * @param prefsRegistry The system preferences registry.
     */
    public ConfigurationProviderImpl(PreferencesRegistry prefsRegistry)
    {
        myPreferencesRegistry = prefsRegistry;
        Migrator migrator = new Migrator();
        migrator.migrate(this, prefsRegistry);
    }

    @Override
    public synchronized DateFormatsConfig getDateFormats()
    {
        Preferences preferences = myPreferencesRegistry.getPreferences(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC);
        return preferences.getJAXBObject(DateFormatsConfig.class, MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY,
                new DateFormatsConfig());
    }

    @Override
    public void saveFormat(DateFormat format)
    {
        saveFormats(New.list(format));
    }

    @Override
    public List<String> getExcludeColumns()
    {
        return CSVColumnPrefsUtil.getCustomKeys(myPreferencesRegistry, ColumnType.TIMESTAMP.name() + "_exclude");
    }

    @Override
    public synchronized void saveFormats(Collection<DateFormat> formats)
    {
        Set<String> existingFormats = New.set();

        DateFormatsConfig config = getDateFormats();

        for (DateFormat existing : config.getFormats())
        {
            existingFormats.add(existing.getSdf());
        }

        List<DateFormat> formatsToSave = New.list();

        for (DateFormat newFormat : formats)
        {
            if (StringUtils.isNotEmpty(newFormat.getSdf()) && !existingFormats.contains(newFormat.getSdf()))
            {
                formatsToSave.add(newFormat);
            }
        }

        if (!formatsToSave.isEmpty())
        {
            config.getFormats().addAll(formatsToSave);

            Preferences preferences = myPreferencesRegistry.getPreferences(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC);
            preferences.putJAXBObject(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY, config, true, this);
        }
    }
}
