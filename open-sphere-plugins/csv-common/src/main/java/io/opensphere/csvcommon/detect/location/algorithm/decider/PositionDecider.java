package io.opensphere.csvcommon.detect.location.algorithm.decider;

import java.util.List;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.importer.config.ColumnType;

/**
 * The PositionDecider will determine if there is a column that qualifies as a
 * position column.
 */
public class PositionDecider extends SingleLocationColumnDecider
{
    /** The Constant ourLongNames. */
    private final List<String> myPositionLongNames;

    /** The Constant ourLongNameKey. */
    public static final String ourLongNameKey = "positionLongNames";

    /**
     * Instantiates a new position decider.
     *
     * @param prefsRegistry the preferences registry
     */
    public PositionDecider(PreferencesRegistry prefsRegistry)
    {
        super(ColumnType.POSITION, prefsRegistry);
        myPositionLongNames = CSVColumnPrefsUtil.getCustomKeys(prefsRegistry, ourLongNameKey);
    }

    @Override
    public boolean isLongName(String name)
    {
        return myPositionLongNames.contains(name);
    }
}
