package io.opensphere.core.appl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.export.ExportController;
import io.opensphere.core.util.CompositeService;

/** Initializes Core components after plugin initialization. */
public class PostPluginInit extends CompositeService
{
    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public PostPluginInit(Toolbox toolbox)
    {
        super(1);
        addService(new ExportController(toolbox));
    }
}
