package io.opensphere.controlpanels.animation.controller;

import io.opensphere.controlpanels.animation.config.v1.AnimationConfig;
import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.ObservableValueService;

/**
 * The animation configuration manager.
 */
class AnimationConfigManager extends ObservableValueService
{
    /** Config preferences key. */
    private static final String PREFERENCES_KEY = "config";

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The preferences. */
    private final Preferences myPreferences;

    /** The toolbox through which application state is accessed. */
    private Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param preferencesRegistry the preferences registry
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public AnimationConfigManager(Toolbox toolbox, PreferencesRegistry preferencesRegistry)
    {
        myToolbox = toolbox;
        myPreferences = preferencesRegistry.getPreferences("io.opensphere.controlpanels.animation.AnimationConfigManager");
        AnimationConfig config = myPreferences.getJAXBObject(AnimationConfig.class, PREFERENCES_KEY, null);
        myAnimationModel = config == null ? new AnimationConfig().getAnimationModel() : config.getAnimationModel();

        ChangeListener<Object> listener = (observable, oldValue, newValue) -> saveConfig();
        ListDataListener<TimeSpan> timeSpanListener = new ListDataListener<TimeSpan>()
        {
            @Override
            public void elementsAdded(ListDataEvent<TimeSpan> e)
            {
                saveConfig();
            }

            @Override
            public void elementsRemoved(ListDataEvent<TimeSpan> e)
            {
                saveConfig();
            }

            @Override
            public void elementsChanged(ListDataEvent<TimeSpan> e)
            {
                saveConfig();
            }
        };

        bindModel(myAnimationModel.getActiveSpanDuration(), listener);
        bindModel(myAnimationModel.getLoopSpan(), listener);
        bindModel(myAnimationModel.advanceDurationProperty(), listener);
        bindModel(myAnimationModel.getFPS(), listener);
        bindModel(myAnimationModel.getRememberTimes(), listener);
        bindModel(myAnimationModel.getFade(), listener);
        bindModel(myAnimationModel.getChartType(), listener);
        bindModel(myAnimationModel.getViewPreference(), listener);
        bindModel(myAnimationModel.getLastShownView(), listener);
        bindModel(myAnimationModel.getSnapToDataBoundaries(), listener);
        bindModel(myAnimationModel.getHeldIntervals(), timeSpanListener);
        bindModel(myAnimationModel.getSkippedIntervals(), timeSpanListener);
        bindModel(myAnimationModel.loadIntervalsProperty(), timeSpanListener);
    }

    /**
     * Gets the animation model.
     *
     * @return the animation model
     */
    public AnimationModel getAnimationModel()
    {
        return myAnimationModel;
    }

    /**
     * Saves the animation configuration.
     */
    private void saveConfig()
    {
        if (myAnimationModel.isValid())
        {
            myPreferences.putJAXBObject(PREFERENCES_KEY, new AnimationConfig(myAnimationModel), false, this);
        }
    }
}
