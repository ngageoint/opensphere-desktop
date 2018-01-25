package io.opensphere.core.pipeline;

import java.util.Set;

import io.opensphere.core.geometry.RenderingCapabilities;
import io.opensphere.core.util.collections.New;

/**
 * Implementation of {@link RenderingCapabilities}.
 */
abstract class RenderingCapabilitiesImpl implements RenderingCapabilities
{
    /** The set of supported capabilities. */
    private volatile Set<? extends String> myCapabilities;

    /** The renderer identifier. */
    private String myRendererIdentifier;

    @Override
    public String getRendererIdentifier()
    {
        waitForInitComplete();
        return myRendererIdentifier;
    }

    @Override
    public Set<? extends String> getSupportedCapabilities()
    {
        return myCapabilities;
    }

    @Override
    public boolean isCapabilitySupported(String capability)
    {
        waitForInitComplete();
        return myCapabilities.contains(capability);
    }

    /**
     * Set the capabilities.
     *
     * @param caps The capabilities.
     */
    public void setCapabilities(Set<? extends String> caps)
    {
        myCapabilities = New.unmodifiableSet(caps);
    }

    /**
     * Set the renderer identifier.
     *
     * @param rendererIdentifier The identifier.
     */
    public void setRendererIdentifier(String rendererIdentifier)
    {
        myRendererIdentifier = rendererIdentifier;
    }

    /**
     * Hook method to wait for the pipeline to be initialized.
     */
    public abstract void waitForInitComplete();
}
