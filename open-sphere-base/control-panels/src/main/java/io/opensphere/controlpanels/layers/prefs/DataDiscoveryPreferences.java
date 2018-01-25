package io.opensphere.controlpanels.layers.prefs;

import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * The Class ExternalToolsPreferences.
 */
public final class DataDiscoveryPreferences
{
    /** The DEFAULT_SHOW_ACTIVE_LAYER_FEATURE_COUNTS. */
    public static final boolean DEFAULT_SHOW_ACTIVE_LAYER_FEATURE_COUNTS = true;

    /** The Constant DEFAULT_SHOW_ACTIVE_LAYER_TYPE_ICONS. */
    public static final boolean DEFAULT_SHOW_ACTIVE_LAYER_TYPE_ICONS = true;

    /** The Constant DEFAULT_SHOW_ACTIVE_LAYER_TYPE_LABELS. */
    public static final boolean DEFAULT_SHOW_ACTIVE_LAYER_TYPE_LABELS = true;

    /** The Constant DEFAULT_SHOW_ACTIVE_SOURCE_LABELS. */
    public static final boolean DEFAULT_SHOW_ACTIVE_SOURCE_LABELS = false;

    /** The Constant DEFAULT_SHOW_AVAILABLE_SOURCE_TYPE_LABELS. */
    public static final boolean DEFAULT_SHOW_AVAILABLE_SOURCE_TYPE_LABELS = false;

    /** The Constant SHOW_ACTIVE_LAYER_FEATURE_COUNTS. */
    public static final String SHOW_ACTIVE_LAYER_FEATURE_COUNTS = "ShowActiveLayerFeatureCounts";

    /** The Constant SHOW_ACTIVE_LAYER_TYPE_ICONS. */
    public static final String SHOW_ACTIVE_LAYER_TYPE_ICONS = "ShowActiveLayerTypeIcons";

    /** The Constant SHOW_ACTIVE_LAYER_TYPE_LABELS. */
    public static final String SHOW_ACTIVE_LAYER_TYPE_LABELS = "ShowActiveLayerTypeLabels";

    /** The Constant SHOW_ACTIVE_SOURCE_TYPE_LABELS. */
    public static final String SHOW_ACTIVE_SOURCE_LABELS = "ShowActiveLayerSourceLabels";

    /** The Constant SHOW_AVAILABLE_LAYER_TYPE_LABELS. */
    public static final String SHOW_AVAILABLE_SOURCE_TYPE_LABELS = "ShowAvailableLayerTypeLabels";

    /**
     * Checks if is show active layer feature counts.
     *
     * @param prefsRegistry The system preferences registry.
     * @return true, if is show active layer feature counts
     */
    public static boolean isShowActiveLayerFeatureCounts(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(DataDiscoveryPreferences.class)
                .getBoolean(DataDiscoveryPreferences.SHOW_ACTIVE_LAYER_FEATURE_COUNTS, DEFAULT_SHOW_ACTIVE_LAYER_FEATURE_COUNTS);
    }

    /**
     * Checks if is show active layer type icons.
     *
     * @param prefsRegistry The system preferences registry.
     * @return true, if is show active layer type icons
     */
    public static boolean isShowActiveLayerTypeIcons(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(DataDiscoveryPreferences.class)
                .getBoolean(DataDiscoveryPreferences.SHOW_ACTIVE_LAYER_TYPE_ICONS, DEFAULT_SHOW_ACTIVE_LAYER_TYPE_ICONS);
    }

    /**
     * Checks if is show active layer type labels.
     *
     * @param prefsRegistry The system preferences registry.
     * @return true, if is show active layer type labels
     */
    public static boolean isShowActiveLayerTypeLabels(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(DataDiscoveryPreferences.class)
                .getBoolean(DataDiscoveryPreferences.SHOW_ACTIVE_LAYER_TYPE_LABELS, DEFAULT_SHOW_ACTIVE_LAYER_TYPE_LABELS);
    }

    /**
     * Checks if is show active source type labels.
     *
     * @param prefsRegistry The system preferences registry.
     * @return true, if is show active source type labels
     */
    public static boolean isShowActiveSourceTypeLabels(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(DataDiscoveryPreferences.class)
                .getBoolean(DataDiscoveryPreferences.SHOW_ACTIVE_SOURCE_LABELS, DEFAULT_SHOW_ACTIVE_SOURCE_LABELS);
    }

    /**
     * Checks if is show available source type labels.
     *
     * @param prefsRegistry The system preferences registry.
     * @return true, if is show available source type labels
     */
    public static boolean isShowAvailableSourceTypeLabels(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(DataDiscoveryPreferences.class).getBoolean(
                DataDiscoveryPreferences.SHOW_AVAILABLE_SOURCE_TYPE_LABELS, DEFAULT_SHOW_AVAILABLE_SOURCE_TYPE_LABELS);
    }

    /**
     * Sets the show active layer feature counts.
     *
     * @param prefsRegistry The system preferences registry.
     * @param showCounts the show counts
     * @param source the source
     */
    public static void setShowActiveLayerFeatureCounts(PreferencesRegistry prefsRegistry, boolean showCounts, Object source)
    {
        if (showCounts == DEFAULT_SHOW_ACTIVE_LAYER_FEATURE_COUNTS)
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).remove(SHOW_ACTIVE_LAYER_FEATURE_COUNTS, source);
        }
        else
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).putBoolean(SHOW_ACTIVE_LAYER_FEATURE_COUNTS, showCounts,
                    source);
        }
    }

    /**
     * Sets the show active layer type icons.
     *
     * @param prefsRegistry The system preferences registry.
     * @param showActiveLayerTypeIcons the show active layer type icons
     * @param source the source
     */
    public static void setShowActiveLayerTypeIcons(PreferencesRegistry prefsRegistry, boolean showActiveLayerTypeIcons,
            Object source)
    {
        if (showActiveLayerTypeIcons == DEFAULT_SHOW_ACTIVE_LAYER_TYPE_ICONS)
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).remove(SHOW_ACTIVE_LAYER_TYPE_ICONS, source);
        }
        else
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).putBoolean(SHOW_ACTIVE_LAYER_TYPE_ICONS,
                    showActiveLayerTypeIcons, source);
        }
    }

    /**
     * Sets the show active layer type labels.
     *
     * @param prefsRegistry The system preferences registry.
     * @param showActiveLayerTypeLabels the show active layer type labels
     * @param source the source
     */
    public static void setShowActiveLayerTypeLabels(PreferencesRegistry prefsRegistry, boolean showActiveLayerTypeLabels,
            Object source)
    {
        if (showActiveLayerTypeLabels == DEFAULT_SHOW_ACTIVE_LAYER_TYPE_LABELS)
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).remove(SHOW_ACTIVE_LAYER_TYPE_LABELS, source);
        }
        else
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).putBoolean(SHOW_ACTIVE_LAYER_TYPE_LABELS,
                    showActiveLayerTypeLabels, source);
        }
    }

    /**
     * Sets the show active source type labels.
     *
     * @param prefsRegistry The system preferences registry.
     * @param showActiveSourceTypeLabels the show active source type labels
     * @param source the source
     */
    public static void setShowActiveSourceTypeLabels(PreferencesRegistry prefsRegistry, boolean showActiveSourceTypeLabels,
            Object source)
    {
        if (showActiveSourceTypeLabels == DEFAULT_SHOW_ACTIVE_SOURCE_LABELS)
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).remove(SHOW_ACTIVE_SOURCE_LABELS, source);
        }
        else
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).putBoolean(SHOW_ACTIVE_SOURCE_LABELS,
                    showActiveSourceTypeLabels, source);
        }
    }

    /**
     * Sets the show available source type labels.
     *
     * @param prefsRegistry The system preferences registry.
     * @param showAvailableSourceTypeLabels the show available source type
     *            labels
     * @param source the source
     */
    public static void setShowAvailableSourceTypeLabels(PreferencesRegistry prefsRegistry, boolean showAvailableSourceTypeLabels,
            Object source)
    {
        if (showAvailableSourceTypeLabels == DEFAULT_SHOW_AVAILABLE_SOURCE_TYPE_LABELS)
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).remove(SHOW_AVAILABLE_SOURCE_TYPE_LABELS, source);
        }
        else
        {
            prefsRegistry.getPreferences(DataDiscoveryPreferences.class).putBoolean(SHOW_AVAILABLE_SOURCE_TYPE_LABELS,
                    showAvailableSourceTypeLabels, source);
        }
    }

    /**
     * Don't allow instantiation.
     */
    private DataDiscoveryPreferences()
    {
    }
}
