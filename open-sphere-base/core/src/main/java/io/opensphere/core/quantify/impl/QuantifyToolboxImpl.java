package io.opensphere.core.quantify.impl;

import io.opensphere.core.quantify.QuantifyService;
import io.opensphere.core.quantify.QuantifyToolbox;

/** A toolbox extension for the Quantify plugin. */
public class QuantifyToolboxImpl implements QuantifyToolbox
{
    /** The service in which metrics are managed. */
    private final QuantifyService myQuantifyService;

    /**
     * Creates a new toolbox with the supplied service.
     *
     * @param quantifyService the service in which metrics are managed.
     */
    public QuantifyToolboxImpl(QuantifyService quantifyService)
    {
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
     * @see io.opensphere.core.PluginToolbox#getDescription()
     */
    @Override
    public String getDescription()
    {
        return "A toolbox extension for the Quantify plugin";
    }
}
