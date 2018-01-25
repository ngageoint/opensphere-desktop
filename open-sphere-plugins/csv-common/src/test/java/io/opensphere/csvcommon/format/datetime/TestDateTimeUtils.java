package io.opensphere.csvcommon.format.datetime;

import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.ClasspathPreferencesPersistenceManager;
import io.opensphere.core.preferences.InternalPreferencesIF;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.common.datetime.ConfigurationProviderImpl;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Contains utility methods for setting up tests.
 *
 */
public final class TestDateTimeUtils
{
    /**
     * Creates the preferences registry.
     *
     * @param support The easy mock support.
     * @param config The config to return.
     * @return The preferences registry.
     */
    public static PreferencesRegistry createPreferencesRegistry(EasyMockSupport support, DateFormatsConfig config)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.getJAXBObject(EasyMock.eq(DateFormatsConfig.class), EasyMock.eq("DateFormatConfig"),
                EasyMock.isA(DateFormatsConfig.class));
        EasyMock.expectLastCall().andReturn(config);
        preferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(true);
        EasyMock.expectLastCall().atLeastOnce();

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().atLeastOnce();

        return registry;
    }

    /**
     * Create an easy mocked preferences registry.
     *
     * @param support The easy mock support object.
     * @param formats The formats to return.
     * @return The system preferences registry.
     */
    public static PreferencesRegistry createPreferencesRegistryForFormat(EasyMockSupport support, DateFormatsConfig formats)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.getJAXBObject(EasyMock.eq(DateFormatsConfig.class), EasyMock.eq("DateFormatConfig"),
                EasyMock.isA(DateFormatsConfig.class));
        EasyMock.expectLastCall().andReturn(formats);
        preferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(true);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(preferences);

        return registry;
    }

    /**
     * Creates a preferences registry expecting the basic calls.
     *
     * @param support The easy mock support.
     * @return The registry.
     */
    public static PreferencesRegistry createPreferencesRegistry(EasyMockSupport support)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(true);
        EasyMock.expectLastCall().atLeastOnce();

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().atLeastOnce();

        return registry;
    }

    /**
     * Creates an easy mock preferences registry expected the list tool
     * preferences to be called.
     *
     * @param support The easy mock support object.
     * @param precision The number of decimal places for the format.
     * @return The preferences registry that expects list tool format
     *         preferences to be called.
     */
    public static PreferencesRegistry createPreferencesRegistry(EasyMockSupport support, int precision)
    {
        Preferences listPreferences = support.createMock(Preferences.class);
        listPreferences.getInt(EasyMock.cmpEq(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS), EasyMock.eq(0));
        EasyMock.expectLastCall().andReturn(precision);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.eq(ListToolPreferences.class));
        EasyMock.expectLastCall().andReturn(listPreferences);

        Preferences preferences = support.createMock(Preferences.class);
        preferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(true);

        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(preferences);

        return registry;
    }

    /**
     * Creates a mocked preferences registry expecting save to be called.
     *
     * @param support The easy mock support object.
     * @param format The expected format to be saved.
     * @param expectedType The expected date time format type.
     * @return The easy mocked preferences registry.
     */
    public static PreferencesRegistry createSaveRegistry(EasyMockSupport support, final String format, final Type expectedType)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.putJAXBObject(EasyMock.cmpEq("DateFormatConfig"), EasyMock.isA(DateFormatsConfig.class), EasyMock.eq(true),
                EasyMock.isA(ConfigurationProviderImpl.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                DateFormatsConfig config = (DateFormatsConfig)EasyMock.getCurrentArguments()[1];

                boolean hasFormat = false;

                for (DateFormat dataFormat : config.getFormats())
                {
                    if (dataFormat.getSdf().equals(format) && dataFormat.getType() == expectedType)
                    {
                        hasFormat = true;
                        break;
                    }
                }

                assertTrue(hasFormat);

                return null;
            }
        });

        preferences.getJAXBObject(EasyMock.eq(DateFormatsConfig.class), EasyMock.cmpEq("DateFormatConfig"),
                EasyMock.isA(DateFormatsConfig.class));
        EasyMock.expectLastCall().andReturn(new DateFormatsConfig());
        EasyMock.expectLastCall().atLeastOnce();

        preferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(true);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().atLeastOnce();

        return registry;
    }

    /**
     * Gets the configuration.
     *
     * @return The DateFormatsConfig.
     */
    public static DateFormatsConfig getConfiguration()
    {
        ClasspathPreferencesPersistenceManager manager = new ClasspathPreferencesPersistenceManager();
        InternalPreferencesIF preferences = manager.load(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC, null, false);

        return preferences.getJAXBObject(DateFormatsConfig.class, "DateFormatConfig", null);
    }

    /**
     * Not constructible.
     */
    private TestDateTimeUtils()
    {
    }
}
