package io.opensphere.core.quantify.impl;

import io.opensphere.core.quantify.QuantifyService;
import io.opensphere.core.quantify.QuantifyToolbox;
import io.opensphere.core.quantify.settings.QuantifySettingsModel;

/** A toolbox extension for the Quantify plugin. */
public class QuantifyToolboxImpl implements QuantifyToolbox
{
    /** The service in which metrics are managed. */
    private final QuantifyService myQuantifyService;

    /** The model in which settings are persisted. */
    private final QuantifySettingsModel mySettingsModel;

    /**
     * Creates a new toolbox with the supplied service.
     *
     * @param settingsModel The model in which settings are persisted.
     * @param quantifyService the service in which metrics are managed.
     */
    public QuantifyToolboxImpl(QuantifySettingsModel settingsModel, QuantifyService quantifyService)
    {
        mySettingsModel = settingsModel;
        myQuantifyService = quantifyService;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.quantify.QuantifyToolbox#getQuantifyService()
     */
    @Override
    public QuantifyService getQuantifyService()
    {
        return myQuantifyService;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.quantify.QuantifyToolbox#getSettingsModel()
     */
    @Override
    public QuantifySettingsModel getSettingsModel()
    {
        return mySettingsModel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.PluginToolbox#getDescription()
     */
    @Override
    public String getDescription()
    {
        return "A toolbox extension for the Quantify plugin";
    }
}
