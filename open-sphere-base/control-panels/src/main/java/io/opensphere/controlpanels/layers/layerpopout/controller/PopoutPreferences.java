package io.opensphere.controlpanels.layers.layerpopout.controller;

import java.util.Collection;

import io.opensphere.controlpanels.layers.layerpopout.model.v1.PopoutModel;
import io.opensphere.controlpanels.layers.layerpopout.model.v1.PopoutModels;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;

/**
 * Responsible for loading and saving the layer pop out windows.
 */
public final class PopoutPreferences
{
    /**
     * The instance of this class.
     */
    private static final PopoutPreferences ourPreferences = new PopoutPreferences();

    /**
     * The property name for the popout models.
     */
    private static final String POPOUT_MODEL_PROP = "popoutModels";

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static PopoutPreferences getInstance()
    {
        return ourPreferences;
    }

    /**
     * Helps make this class a singleton.
     */
    private PopoutPreferences()
    {
    }

    /**
     * Gets all saved models.
     *
     * @param toolbox The toolbox.
     * @return A collection of all the saved models.
     */
    public synchronized Collection<PopoutModel> getModels(Toolbox toolbox)
    {
        Preferences preferences = toolbox.getPreferencesRegistry().getPreferences(PopoutPreferences.class);
        PopoutModels models = preferences.getJAXBObject(PopoutModels.class, POPOUT_MODEL_PROP, new PopoutModels());

        return models.getModels().values();
    }

    /**
     * Removes the model from the saved models collection.
     *
     * @param toolbox The toolbox.
     * @param model The model to remove.
     */
    public synchronized void removeModel(Toolbox toolbox, PopoutModel model)
    {
        Preferences preferences = toolbox.getPreferencesRegistry().getPreferences(PopoutPreferences.class);
        PopoutModels models = preferences.getJAXBObject(PopoutModels.class, POPOUT_MODEL_PROP, new PopoutModels());
        models.getModels().remove(model.getId());

        preferences.putJAXBObject(POPOUT_MODEL_PROP, models, false, this);
    }

    /**
     * Saves the model.
     *
     * @param toolbox The toolbox.
     * @param model The model to save.
     */
    public synchronized void saveModel(Toolbox toolbox, PopoutModel model)
    {
        Preferences preferences = toolbox.getPreferencesRegistry().getPreferences(PopoutPreferences.class);
        PopoutModels models = preferences.getJAXBObject(PopoutModels.class, POPOUT_MODEL_PROP, new PopoutModels());
        models.getModels().put(model.getId(), model);

        preferences.putJAXBObject(POPOUT_MODEL_PROP, models, false, this);
    }
}
