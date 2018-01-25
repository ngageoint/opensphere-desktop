package io.opensphere.wps;

import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.event.EventListener;
import io.opensphere.server.services.ServerConfigEvent;

/**
 * An event listener container, used to isolate event processing and delegation
 * to a single class. Makes use of remote methods by way of {@link Consumer}
 * objects to perform event processing. Instances of this class can be
 * subscribed to events directly, without having to subscribe the parent object.
 */
public class WpsServerConfigurationEventListener implements EventListener<ServerConfigEvent>
{
    /**
     * The consumer to be called when a server activation event is received.
     */
    private final Consumer<ServerConfigEvent> myActivationListener;

    /**
     * The consumer to be called when a server deactivation event is received.
     */
    private final Consumer<ServerConfigEvent> myDeactivationListener;

    /**
     * The consumer to be called when a load operation has been completed for a
     * given server.
     */
    private final Consumer<ServerConfigEvent> myLoadCompleteListener;

    /**
     * Creates a new event listener, using the supplied consumers to process
     * received events.
     *
     * @param pActivationListener The consumer to be called when a server
     *            activation event is received.
     * @param pDeactivationListener The consumer to be called when a server
     *            deactivation event is received.
     * @param pLoadCompleteListener The consumer to be called when a load
     *            operation has been completed for a given server.
     *
     */
    public WpsServerConfigurationEventListener(Consumer<ServerConfigEvent> pActivationListener,
            Consumer<ServerConfigEvent> pDeactivationListener, Consumer<ServerConfigEvent> pLoadCompleteListener)
    {
        myActivationListener = pActivationListener;
        myDeactivationListener = pDeactivationListener;
        myLoadCompleteListener = pLoadCompleteListener;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.event.EventListener#notify(java.lang.Object)
     */
    @Override
    public void notify(ServerConfigEvent event)
    {
        if (event.getServer() != null && StringUtils.isNotEmpty(event.getServer().getWpsUrl()))
        {
            switch (event.getEventAction())
            {
                case ACTIVATE:
                    myActivationListener.accept(event);
                    break;
                case DEACTIVATE:
                    myDeactivationListener.accept(event);
                    break;
                case LOADCOMPLETE:
                    myLoadCompleteListener.accept(event);
                    break;
                default:
                    break;
            }
        }
    }
}
