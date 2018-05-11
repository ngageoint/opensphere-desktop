package io.opensphere.csvcommon.common.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Tests the Migrator class.
 *
 */
@SuppressWarnings("boxing")
public class MigratorTest
{
    /**
     * The new format.
     */
    private static final DateFormat ourNewFormat = new DateFormat(Type.DATE, "yyyyMMdd", "pattern");

    /**
     * Tests the migrator on a new install.
     */
    @Test
    public void testMigrateNewInstall()
    {
        EasyMockSupport support = new EasyMockSupport();

        ConfigurationProvider provider = createProvider(support, createNewFormats());
        PreferencesRegistry registry = createRegistry(support, null, false);

        support.replayAll();

        Migrator migrator = new Migrator();
        migrator.migrate(provider, registry);

        support.verifyAll();
    }

    /**
     * Tests the migrator migrating the old file.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMigrate()
    {
        EasyMockSupport support = new EasyMockSupport();

        ConfigurationProvider provider = createProvider(support, createNewFormats());
        provider.saveFormats(EasyMock.isA(Collection.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                Collection<DateFormat> savedFormats = (Collection<DateFormat>)EasyMock.getCurrentArguments()[0];
                assertSavedFormats(savedFormats);

                return null;
            }
        });

        PreferencesRegistry registry = createRegistry(support, createOldFormats(), false);

        support.replayAll();

        Migrator migrator = new Migrator();
        migrator.migrate(provider, registry);

        support.verifyAll();
    }

    /**
     * Tests the migrator when file is already migrated.
     */
    @Test
    public void testAlreadyMigrated()
    {
        EasyMockSupport support = new EasyMockSupport();

        ConfigurationProvider provider = support.createMock(ConfigurationProvider.class);
        PreferencesRegistry registry = createRegistry(support, null, true);

        support.replayAll();

        Migrator migrator = new Migrator();
        migrator.migrate(provider, registry);

        support.verifyAll();
    }

    /**
     * Creates the test formats config representing the new formats.
     *
     * @return The test formats.
     */
    private synchronized DateFormatsConfig createNewFormats()
    {
        DateFormatsConfig config = new DateFormatsConfig();

        config.addFormat(ourNewFormat);

        return config;
    }

    /**
     * Creates the test formats config representing the old formats.
     *
     * @return The old test formats.
     */
    private DateFormatsConfig createOldFormats()
    {
        DateFormatsConfig config = new DateFormatsConfig();

        config.addFormat(new DateFormat(Type.DATE, "yyyyMMdd"));

        config.addFormat(new DateFormat(Type.TIMESTAMP, "yyyyMMdd HHmmss"));
        config.addFormat(new DateFormat(Type.DATE, "yyyyMMdd HHmmss.SSS"));

        config.addFormat(new DateFormat(Type.DATE, DateTimeFormats.DATE_FORMAT));
        config.addFormat(new DateFormat(Type.TIME, "yyyy/MM/dd"));

        config.addFormat(new DateFormat(Type.TIME, "HHmmss"));
        config.addFormat(new DateFormat(Type.TIMESTAMP, "HHmmss.SSS"));

        config.addFormat(new DateFormat(Type.DATE, "yyyyMMdf"));
        config.addFormat(new DateFormat(Type.DATE, ""));

        return config;
    }

    /**
     * Asserts the formats being saved.
     *
     * @param savedFormats The formats being saved.
     */
    private void assertSavedFormats(Collection<DateFormat> savedFormats)
    {
        assertEquals(6, savedFormats.size());

        Map<String, DateFormat> mappedFormats = New.map();

        for (DateFormat format : savedFormats)
        {
            mappedFormats.put(format.getSdf(), format);
        }

        DateFormat dateFormat = mappedFormats.get("yyyyMMdd HHmmss");
        assertNotNull(dateFormat);
        assertEquals(Type.TIMESTAMP, dateFormat.getType());

        dateFormat = mappedFormats.get("yyyyMMdd HHmmss.SSS");
        assertNotNull(dateFormat);
        assertEquals(Type.TIMESTAMP, dateFormat.getType());

        dateFormat = mappedFormats.get(DateTimeFormats.DATE_FORMAT);
        assertNotNull(dateFormat);
        assertEquals(Type.DATE, dateFormat.getType());

        dateFormat = mappedFormats.get("yyyy/MM/dd");
        assertNotNull(dateFormat);
        assertEquals(Type.DATE, dateFormat.getType());

        dateFormat = mappedFormats.get("HHmmss");
        assertNotNull(dateFormat);
        assertEquals(Type.TIME, dateFormat.getType());

        dateFormat = mappedFormats.get("HHmmss.SSS");
        assertNotNull(dateFormat);
        assertEquals(Type.TIME, dateFormat.getType());
    }

    /**
     * Creates the provider.
     *
     * @param support The easy mock support.
     * @param newFormats The new formats.
     * @return The provider.
     */
    private ConfigurationProvider createProvider(EasyMockSupport support, DateFormatsConfig newFormats)
    {
        ConfigurationProvider provider = support.createMock(ConfigurationProvider.class);
        provider.getDateFormats();
        EasyMock.expectLastCall().andReturn(newFormats);

        return provider;
    }

    /**
     * Creates the registry.
     *
     * @param support The easy mock support.
     * @param oldFormats The old formats to return.
     * @param alreadyMigrated The already migrated preference value to return.
     * @return The preferences registry.
     */
    private PreferencesRegistry createRegistry(EasyMockSupport support, DateFormatsConfig oldFormats, boolean alreadyMigrated)
    {
        Preferences newPreferences = support.createMock(Preferences.class);
        newPreferences.getBoolean(EasyMock.cmpEq("v1Migrated"), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(alreadyMigrated);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);

        if (!alreadyMigrated)
        {
            newPreferences.putBoolean(EasyMock.cmpEq("v1Migrated"), EasyMock.eq(true), EasyMock.isA(Migrator.class));
            EasyMock.expectLastCall().andReturn(true);

            Preferences oldPreferences = support.createMock(Preferences.class);
            oldPreferences.getJAXBObject(EasyMock.eq(DateFormatsConfig.class),
                    EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY), (DateFormatsConfig)EasyMock.isNull());
            EasyMock.expectLastCall().andReturn(oldFormats);

            registry.getPreferences("DateFormatConfiguration");
            EasyMock.expectLastCall().andReturn(oldPreferences);
        }

        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(newPreferences);

        return registry;
    }
}
