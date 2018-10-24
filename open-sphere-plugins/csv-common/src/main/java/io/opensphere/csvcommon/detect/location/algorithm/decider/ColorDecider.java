package io.opensphere.csvcommon.detect.location.algorithm.decider;

import java.util.List;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.importer.config.ColumnType;

/**
 * The ColorDecider will determine if there exists 1 or more columns that
 * qualify as a 'COLOR' column.
 */
public class ColorDecider extends SingleLocationColumnDecider
{
    /** The list of long names used for color columns. */
    private final List<String> myLongNames;

    /**
     * The constant containing the name of the key used to store the names of
     * the color columns.
     */
    public static final String COLOR_LONG_NAMES_KEY = "COLOR";

    /**
     * Instantiates a new color decider.
     *
     * @param prefsRegistry the preferences registry
     */
    public ColorDecider(PreferencesRegistry prefsRegistry)
    {
        super(ColumnType.COLOR, prefsRegistry);
        myLongNames = CSVColumnPrefsUtil.getCustomKeys(prefsRegistry, COLOR_LONG_NAMES_KEY);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.csvcommon.detect.location.algorithm.decider.SingleLocationColumnDecider#isLongName(java.lang.String)
     */
    @Override
    public boolean isLongName(String name)
    {
        return myLongNames.contains(name);
    }
}
