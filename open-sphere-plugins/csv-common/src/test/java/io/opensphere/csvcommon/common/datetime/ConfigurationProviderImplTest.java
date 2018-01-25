package io.opensphere.csvcommon.common.datetime;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.common.datetime.ConfigurationProviderImpl;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Tests the ConfigurationProviderImpl class.
 */
public class ConfigurationProviderImplTest
{
    /**
     * The test config.
     */
    private static final DateFormatsConfig ourConfig = new DateFormatsConfig();

    /**
     * Tests getting the date formats.
     */
    @Test
    public void testGetDateFormats()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry prefsRegistry = createPreferencesRegistry(support);

        support.replayAll();

        ConfigurationProviderImpl provider = new ConfigurationProviderImpl(prefsRegistry);
        DateFormatsConfig config = provider.getDateFormats();

        assertEquals(ourConfig, config);

        support.verifyAll();
    }

    /**
     * Tests saving the date formats.
     */
    @Test
    public void testSaveDateFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormat existingFormat = new DateFormat();
        existingFormat.setSdf("existing");

        ourConfig.addFormat(existingFormat);

        DateFormat format = new DateFormat();

        PreferencesRegistry prefsRegistry = createSaveRegistry(support, format);

        support.replayAll();

        ConfigurationProviderImpl provider = new ConfigurationProviderImpl(prefsRegistry);
        provider.saveFormat(format);

        format.setSdf("existing");

        provider.saveFormat(format);

        format.setSdf("save");

        provider.saveFormat(format);

        support.verifyAll();
    }

    /**
     * Create an easy mocked preferences registry.
     *
     * @param support The easy mock support object.
     * @return The preferences registry.
     */
    private PreferencesRegistry createPreferencesRegistry(EasyMockSupport support)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.getJAXBObject(EasyMock.eq(DateFormatsConfig.class), EasyMock.eq("DateFormatConfig"),
                EasyMock.isA(DateFormatsConfig.class));
        EasyMock.expectLastCall().andReturn(ourConfig);
        preferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(true);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().atLeastOnce();

        return registry;
    }

    /**
     * Creates a mocked preferences registry expecting save to be called.
     *
     * @param support The easy mock support object.
     * @param format The expected format to be saved.
     * @return The easy mocked preferences registry.
     */
    private PreferencesRegistry createSaveRegistry(EasyMockSupport support, final DateFormat format)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.getJAXBObject(EasyMock.eq(DateFormatsConfig.class), EasyMock.eq("DateFormatConfig"),
                EasyMock.isA(DateFormatsConfig.class));
        EasyMock.expectLastCall().andReturn(ourConfig);
        EasyMock.expectLastCall().atLeastOnce();
        preferences.putJAXBObject(EasyMock.cmpEq("DateFormatConfig"), EasyMock.isA(DateFormatsConfig.class), EasyMock.eq(true),
                EasyMock.isA(ConfigurationProviderImpl.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                DateFormatsConfig config = (DateFormatsConfig)EasyMock.getCurrentArguments()[1];
                assertEquals(ourConfig, config);
                assertEquals(2, config.getFormats().size());
                assertEquals(format, config.getFormats().get(1));
                return null;
            }
        });
        preferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(true);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().atLeastOnce();

        return registry;
    }
}
