package io.opensphere.analysis;

import java.util.List;

import io.opensphere.analysis.base.controller.SettingsModelController;
import io.opensphere.analysis.base.model.CommonSettingsModel;
import io.opensphere.analysis.baseball.BaseballController;
import io.opensphere.analysis.toolbox.AnalysisToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;

/**
 * Initializes the various analysis tools.
 *
 * NOTE: this is new and doesn't currently initialize every tool but eventually
 * they should all be initialized here.
 */
public class ToolInitializer
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public ToolInitializer(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Creates a list of services representing each analysis tool.
     *
     * @return the tool services
     */
    public List<Service> createToolServices()
    {
        List<Service> services = New.list();

        services.add(new BaseballController(myToolbox));

        CommonSettingsModel settingsModel = new CommonSettingsModel();
        AnalysisToolbox analysisToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(AnalysisToolbox.class);
        analysisToolbox.setSettingsModel(settingsModel);
        services.add(new SettingsModelController(myToolbox, settingsModel));

        return services;
    }
}
