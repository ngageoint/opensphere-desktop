package io.opensphere.core.geometry.renderproperties;

/** Event for when one or more render properties have changed. */
public class RenderPropertyChangedEvent
{
    /** The properties object which has changed. */
    private final RenderProperties myRenderProperties;

    /**
     * Constructor.
     *
     * @param props The properties object which has changed.
     */
    public RenderPropertyChangedEvent(RenderProperties props)
    {
        myRenderProperties = props;
    }

    /**
     * Get the renderProperties.
     *
     * @return the renderProperties
     */
    public RenderProperties getRenderProperties()
    {
        return myRenderProperties;
    }
}
