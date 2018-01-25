package io.opensphere.csvcommon.detect.location.algorithm.decider;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.importer.config.ColumnType;

/**
 * The MGRSDecider will determine if there is a column that qualifies as a MGRS
 * column.
 */
public class MGRSDecider extends SingleLocationColumnDecider
{
    /**
     * Instantiates a new mGRS decider.
     *
     * @param prefsRegistry the preferences registry
     */
    public MGRSDecider(PreferencesRegistry prefsRegistry)
    {
        super(ColumnType.MGRS, prefsRegistry);
    }

    @Override
    public boolean isLongName(String name)
    {
        return true;
    }
}
