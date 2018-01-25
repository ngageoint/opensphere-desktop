package io.opensphere.csvcommon.util;

import org.easymock.EasyMock;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.ClasspathPreferencesPersistenceManager;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.detect.location.algorithm.decider.LatLonDecider;
import io.opensphere.csvcommon.detect.location.algorithm.decider.PositionDecider;
import io.opensphere.csvcommon.detect.location.algorithm.decider.WktDecider;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.importer.config.ColumnType;

/**
 * A utility class for testing location detection in CSV files.
 */
public final class LocationTestUtils
{
    /** The Preferences manager. */
    private static final ClasspathPreferencesPersistenceManager ourPrefsManager = new ClasspathPreferencesPersistenceManager();

    /** The Topic. */
    private static final String ourTopic = "io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil";

    /**
     * Not constructible.
     */
    private LocationTestUtils()
    {
    }

    /**
     * Gets the preferences registry. from the preferences file.
     *
     * @return the prefs registry
     */
    public static PreferencesRegistry getPrefsRegistry()
    {
        Toolbox toolbox = EasyMock.createNiceMock(Toolbox.class);

        PreferencesRegistry pr = EasyMock.createNiceMock(PreferencesRegistry.class);
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(pr).anyTimes();

        Preferences prefs = EasyMock.createNiceMock(Preferences.class);
        EasyMock.expect(pr.getPreferences(CSVColumnPrefsUtil.class)).andReturn(prefs).anyTimes();

        loadLocationColumnLists(prefs);

        EasyMock.replay(toolbox, pr, prefs);

        return pr;
    }

    /**
     * Loads the location column name lists from a config file.
     *
     * @param prefs the preferences
     */
    public static void loadLocationColumnLists(Preferences prefs)
    {
        Preferences loadedPrefs = ourPrefsManager.load(ourTopic, null, false);
        // Mock lat/lon columns
        EasyMock.expect(prefs.getStringList(ColumnType.LAT.name(), null))
                .andReturn(loadedPrefs.getStringList(ColumnType.LAT.name(), null)).anyTimes();
        EasyMock.expect(prefs.getStringList(ColumnType.LON.name(), null))
                .andReturn(loadedPrefs.getStringList(ColumnType.LON.name(), null)).anyTimes();
        EasyMock.expect(prefs.getStringList(LatLonDecider.ourLongLatLonNamesKey, null))
                .andReturn(loadedPrefs.getStringList(LatLonDecider.ourLongLatLonNamesKey, null)).anyTimes();
        EasyMock.expect(prefs.getStringList(LatLonDecider.ourShortLatLonNamesKey, null))
                .andReturn(loadedPrefs.getStringList(LatLonDecider.ourShortLatLonNamesKey, null)).anyTimes();

        // Mock position columns
        EasyMock.expect(prefs.getStringList(ColumnType.POSITION.name(), null))
                .andReturn(loadedPrefs.getStringList(ColumnType.POSITION.name(), null)).anyTimes();
        EasyMock.expect(prefs.getStringList(PositionDecider.ourLongNameKey, null))
                .andReturn(loadedPrefs.getStringList(PositionDecider.ourLongNameKey.toString(), null)).anyTimes();

        // Mock MGRS columns
        EasyMock.expect(prefs.getStringList(ColumnType.MGRS.name(), null))
                .andReturn(loadedPrefs.getStringList(ColumnType.MGRS.toString(), null)).anyTimes();

        // Mock WKT columns
        EasyMock.expect(prefs.getStringList(ColumnType.WKT_GEOMETRY.name(), null))
                .andReturn(loadedPrefs.getStringList(ColumnType.WKT_GEOMETRY.name(), null)).anyTimes();
        EasyMock.expect(prefs.getStringList(WktDecider.ourWktLongNames, null))
                .andReturn(loadedPrefs.getStringList(WktDecider.ourWktLongNames, null)).anyTimes();

        // Mock CEP column
        EasyMock.expect(prefs.getStringList(ColumnType.RADIUS.name(), null))
                .andReturn(loadedPrefs.getStringList(ColumnType.RADIUS.name(), null)).anyTimes();

        // Mock LOB column
        EasyMock.expect(prefs.getStringList(ColumnType.LOB.name(), null))
                .andReturn(loadedPrefs.getStringList(ColumnType.LOB.name(), null)).anyTimes();

        EasyMock.expect(prefs.getStringList(ColumnType.TIMESTAMP.name() + "_exclude", null))
                .andReturn(loadedPrefs.getStringList(ColumnType.TIMESTAMP.name() + "_exclude", null)).anyTimes();
    }
}
