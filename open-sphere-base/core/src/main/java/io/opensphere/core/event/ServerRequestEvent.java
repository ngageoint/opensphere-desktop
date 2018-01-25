package io.opensphere.core.event;

/**
 * An event indicating the state of a server request.
 */
public class ServerRequestEvent extends AbstractMultiStateEvent
{
    @Override
    public String getDescription()
    {
        return "Event indicating the state of a server request.";
    }
}
