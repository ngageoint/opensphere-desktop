package io.opensphere.analysis.toolbox;

import io.opensphere.analysis.base.model.CommonSettingsModel;
import io.opensphere.core.PluginToolbox;

/** The Analysis toolbox. */
public final class AnalysisToolbox implements PluginToolbox
{
    /** The settings model. */
    private volatile CommonSettingsModel mySettingsModel;

    @Override
    public String getDescription()
    {
        return "The Analysis toolbox";
    }

    /**
     * Gets the settingsModel.
     *
     * @return the settingsModel
     */
    public CommonSettingsModel getSettingsModel()
    {
        return mySettingsModel;
    }

    /**
     * Sets the settingsModel.
     *
     * @param settingsModel the settingsModel
     */
    public void setSettingsModel(CommonSettingsModel settingsModel)
    {
        mySettingsModel = settingsModel;
    }
}
