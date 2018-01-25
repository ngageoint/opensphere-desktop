package io.opensphere.core.map;

import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.PreferencesRegistry;

/** Help class to manage preferences for the map manager. */
public abstract class MapManagerPreferenceHelper
{
    /** The options provider for the "Advanced" sub-topic of map options. */
    private final AdvancedMapOptionsProvider myAdvancedOptionsProvider;

    /** The main map options provider. */
    private final MapOptionsProvider myMapOptionsProvider;

    /** Listener for changes to the model density preference. */
    private final PreferenceChangeListener myModelDensityListener = new PreferenceChangeListener()
    {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt)
        {
            int density = evt.getValueAsInt(80);
            handleModelDensityChanged(density);
        }
    };

    /** Listener for changes to the zoom rate preference. */
    private final PreferenceChangeListener myZoomListener = new PreferenceChangeListener()
    {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt)
        {
            handleZoomRateChanged(evt.getValueAsInt(20));
        }
    };

    /** The system preferences registry. */
    private final PreferencesRegistry myPrefsRegistry;

    /**
     * Constructor.
     *
     * @param prefsRegistry The system preferences registry.
     * @param optionsRegistry The options registry.
     */
    public MapManagerPreferenceHelper(PreferencesRegistry prefsRegistry, OptionsRegistry optionsRegistry)
    {
        myPrefsRegistry = prefsRegistry;
        int initialDensity = prefsRegistry.getPreferences(AdvancedMapOptionsProvider.class)
                .getInt(AdvancedMapOptionsProvider.MODEL_DENSITY_KEY, 80);
        handleModelDensityChanged(initialDensity);

        // Add the mouse options provider, currently (5/6/13) this only controls
        // mouse
        // wheel view zoom rate.
        myMapOptionsProvider = new MapOptionsProvider(prefsRegistry);
        optionsRegistry.addOptionsProvider(myMapOptionsProvider);
        prefsRegistry.getPreferences(MapOptionsProvider.class).addPreferenceChangeListener(MapOptionsProvider.VIEW_ZOOM_RATE_KEY,
                myZoomListener);

        prefsRegistry.getPreferences(AdvancedMapOptionsProvider.class)
                .addPreferenceChangeListener(AdvancedMapOptionsProvider.MODEL_DENSITY_KEY, myModelDensityListener);

        myAdvancedOptionsProvider = new AdvancedMapOptionsProvider(prefsRegistry);
        myMapOptionsProvider.addSubTopic(myAdvancedOptionsProvider);
    }

    /** Perform any required cleanup. */
    public void close()
    {
        myMapOptionsProvider.removeSubTopic(myAdvancedOptionsProvider);
        myPrefsRegistry.getPreferences(MapOptionsProvider.class)
                .removePreferenceChangeListener(MapOptionsProvider.VIEW_ZOOM_RATE_KEY, myZoomListener);
    }

    /**
     * Read the preferences and set the zoom rate to the set value if available.
     */
    public void setDefaultZoomRate()
    {
        int initialZoom = myPrefsRegistry.getPreferences(MapOptionsProvider.class).getInt(MapOptionsProvider.VIEW_ZOOM_RATE_KEY,
                20);
        handleZoomRateChanged(initialZoom);
    }

    /**
     * Handle a change to the model density.
     *
     * @param density The suggested new terrain pixel density.
     */
    abstract void handleModelDensityChanged(int density);

    /**
     * Handle a change to the zoom rate.
     *
     * @param rate The new zoom rate.
     */
    abstract void handleZoomRateChanged(int rate);
}
