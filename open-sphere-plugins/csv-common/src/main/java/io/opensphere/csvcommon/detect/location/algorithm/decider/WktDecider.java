package io.opensphere.csvcommon.detect.location.algorithm.decider;

import java.util.List;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.importer.config.ColumnType;

/**
 * The WktDecider will determine if there is a column that qualifies as a WKT
 * column.
 */
public class WktDecider extends SingleLocationColumnDecider
{
    /** The Constant ourLongNames. */
    private final List<String> myLongNames;

    /** The Constant ourWktLongNames. */
    public static final String ourWktLongNames = "wktLongNames";

    /**
     * Instantiates a new wkt decider.
     *
     * @param prefsRegistry the preferences registry
     */
    public WktDecider(PreferencesRegistry prefsRegistry)
    {
        super(ColumnType.WKT_GEOMETRY, prefsRegistry);
        myLongNames = CSVColumnPrefsUtil.getCustomKeys(prefsRegistry, ourWktLongNames);
    }

    @Override
    public boolean isLongName(String name)
    {
        return myLongNames.contains(name);
    }
}
