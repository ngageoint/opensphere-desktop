package io.opensphere.analysis.base.controller;

import java.util.concurrent.Executor;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

import io.opensphere.analysis.base.model.Orientation;
import io.opensphere.analysis.base.model.SettingsModel;
import io.opensphere.analysis.base.model.SortMethod;
import io.opensphere.core.preferences.BooleanPreferenceBinding;
import io.opensphere.core.preferences.EnumPreferenceBinding;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.CompositeService;

/** Persists the settings model. */
public class SettingsModelPersister extends CompositeService
{
    /** The preferences. */
    private final Preferences myPreferences;

    /** The executor. */
    private final Executor myExecutor;

    /** The histogram tool preference prefix. */
    private static final String BETA_HISTOGRAM_PREFIX = "BetaHistogram:";

    /**
     * Constructor.
     *
     * @param model The settings model
     * @param preferencesRegistry The preferences registry
     */
    public SettingsModelPersister(SettingsModel model, PreferencesRegistry preferencesRegistry)
    {
        super();
        myPreferences = preferencesRegistry.getPreferences(model.getClass());
        myExecutor = Platform::runLater;
        addProperty(model.lockedProperty());
        addProperty(model.allTimeProperty());
        addProperty(SortMethod.class, model.sortMethodProperty());
        addProperty(Orientation.class, model.orientationProperty());
    }

    /**
     * Adds the property to the persister.
     *
     * @param property the property
     */
    private void addProperty(BooleanProperty property)
    {
        addService(new BooleanPreferenceBinding(property, BETA_HISTOGRAM_PREFIX + property.getName(), property.get(),
                myPreferences, myExecutor, true));
    }

    /**
     * Adds the property to the persister.
     *
     * @param <T> the type of the enum
     * @param type the class type of the enum
     * @param property the property
     */
    private <T extends Enum<T>> void addProperty(Class<T> type, ObjectProperty<T> property)
    {
        addService(new EnumPreferenceBinding<>(type, property, myPreferences, BETA_HISTOGRAM_PREFIX + property.getName(),
                property.get(), myExecutor, true));
    }
}
