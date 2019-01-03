package io.opensphere.core.util.fx.tabpane.inputmap;

import java.util.function.Predicate;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.MouseEvent;

/**
 * Convenience class that can act as a mouse input interceptor, either at a
 * {@link OSInputMap#interceptorProperty() input map} level or a
 * {@link Mapping#interceptorProperty() mapping} level.
 *
 * @see OSInputMap#interceptorProperty()
 * @see Mapping#interceptorProperty()
 */
public class MouseMappingInterceptor implements Predicate<Event>
{
    /** The event type for which the interceptor is configured. */
    private final EventType<MouseEvent> myEventType;

    /**
     * Creates a new MouseMappingInterceptor, which will block execution of
     * event handlers (either at a {@link OSInputMap#interceptorProperty() input
     * map} level or a {@link Mapping#interceptorProperty() mapping} level),
     * where the input received is equal to the given {@link EventType}.
     *
     * @param eventType The {@link EventType} for which mapping execution should
     *            be blocked (typically one of {@link MouseEvent#MOUSE_PRESSED},
     *            {@link MouseEvent#MOUSE_CLICKED}, or
     *            {@link MouseEvent#MOUSE_RELEASED}).
     */
    public MouseMappingInterceptor(EventType<MouseEvent> eventType)
    {
        this.myEventType = eventType;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.function.Predicate#test(java.lang.Object)
     */
    @Override
    public boolean test(Event event)
    {
        if (!(event instanceof MouseEvent))
        {
            return false;
        }
        return event.getEventType() == this.myEventType;
    }
}
