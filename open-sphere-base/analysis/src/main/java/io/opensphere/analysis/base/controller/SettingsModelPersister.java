package io.opensphere.analysis.base.controller;

import java.util.concurrent.Executor;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.analysis.base.model.BinType;
import io.opensphere.analysis.base.model.Orientation;
import io.opensphere.analysis.base.model.SettingsModel;
import io.opensphere.analysis.base.model.SortMethod;
import io.opensphere.analysis.binning.criteria.TimeBinType;
import io.opensphere.core.preferences.BooleanPreferenceBinding;
import io.opensphere.core.preferences.DoublePreferenceBinding;
import io.opensphere.core.preferences.EnumPreferenceBinding;
import io.opensphere.core.preferences.IntegerPreferenceBinding;
import io.opensphere.core.preferences.ObjectPreferenceBinding;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.preferences.StringPreferenceBinding;
import io.opensphere.core.util.CompositeService;
import io.opensphere.core.util.lang.StringUtilities;

/** Persists the settings model. */
public class SettingsModelPersister extends CompositeService
{
    /** The preferences. */
    private final Preferences myPreferences;

    /** The executor. */
    private final Executor myExecutor;

    /** The preference prefix. */
    private final String myPrefix;

    /**
     * Constructor.
     *
     * @param model The settings model
     * @param preferencesRegistry The preferences registry
     * @param toolName The name of the tool
     */
    public SettingsModelPersister(SettingsModel model, PreferencesRegistry preferencesRegistry, String toolName)
    {
        super();
        myPreferences = preferencesRegistry.getPreferences(model.getClass());
        myExecutor = Platform::runLater;
        myPrefix = StringUtilities.replaceSpecialCharacters(toolName) + ":";
        addProperty(model.lockedProperty());
        addProperty(model.allTimeProperty());
        addProperty(model.binWidthProperty());
        addProperty(BinType.class, model.numericBinTypeProperty());
        addProperty(TimeBinType.class, model.timeBinTypeProperty());
        addProperty(model.showEmptyBinsProperty());
        addProperty(model.showNABinProperty());
        addProperty(SortMethod.class, model.sortMethodProperty());
        addProperty(Orientation.class, model.orientationProperty());
        StringConverter<Color> colorConverter = new StringConverter<Color>()
        {
            @Override
            public String toString(Color color)
            {
                return color == null ? null : color.toString();
            }

            @Override
            public Color fromString(String string)
            {
                return StringUtils.isEmpty(string) || "null".equalsIgnoreCase(string) ? null : Color.web(string);
            }
        };
        addProperty(model.backgroundColorProperty(), colorConverter);
        addProperty(model.foregroundColorProperty(), colorConverter);

        addProperty(model.showTitleProperty());
        addProperty(model.titleTextProperty());
        addProperty(model.categoryAxisTextProperty());
        addProperty(model.countAxisTextProperty());
        model.getLabelModels().stream().forEach(labelModel ->
        {
            addProperty(labelModel.colorProperty(), colorConverter);
            addProperty(labelModel.fontProperty());
            addProperty(labelModel.sizeProperty());
        });
    }

    /**
     * Adds the property to the persister.
     *
     * @param property the property
     */
    private void addProperty(IntegerProperty property)
    {
        addService(new IntegerPreferenceBinding(property, myPrefix + property.getName(), property.get(), myPreferences, myExecutor));
    }

    /**
     * Adds the property to the persister.
     *
     * @param property the property
     */
    private void addProperty(StringProperty property)
    {
        addService(new StringPreferenceBinding(property, myPrefix + property.getName(), property.get(), myPreferences, myExecutor));
    }

    /**
     * Adds the property to the persister.
     *
     * @param property the property
     */
    private void addProperty(BooleanProperty property)
    {
        addService(new BooleanPreferenceBinding(property, myPrefix + property.getName(), property.get(), myPreferences, myExecutor));
    }

    /**
     * Adds the property to the persister.
     *
     * @param property the property
     */
    private void addProperty(DoubleProperty property)
    {
        addService(new DoublePreferenceBinding(property, myPrefix + property.getName(), property.get(), myPreferences, myExecutor));
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
        addService(new EnumPreferenceBinding<>(type, property, myPreferences, myPrefix + property.getName(), property.get(), myExecutor));
    }

    /**
     * Adds the property to the persister.
     *
     * @param <T> the type of the property
     * @param property the property
     * @param converter The string converter
     */
    private <T> void addProperty(ObjectProperty<T> property, StringConverter<T> converter)
    {
        addService(
                new ObjectPreferenceBinding<>(property, myPreferences, myPrefix + property.getName(), property.get(), myExecutor, converter));
    }
}
