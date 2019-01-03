package io.opensphere.core.util.fx.tabpane.inputmap;

import java.util.function.Predicate;

import javafx.event.Event;
import javafx.scene.input.KeyEvent;

/**
 * Convenience class that can act as an keyboard input interceptor, either at a
 * {@link OSInputMap#interceptorProperty() input map} level or a
 * {@link Mapping#interceptorProperty() mapping} level.
 *
 * @see OSInputMap#interceptorProperty()
 * @see Mapping#interceptorProperty()
 */
public class KeyMappingInterceptor implements Predicate<Event>
{
    /** The key binding for which the interceptor is configured. */
    private final OSKeyBinding myKeyBinding;

    /**
     * Creates a new KeyMappingInterceptor, which will block execution of event
     * handlers (either at a {@link OSInputMap#interceptorProperty() input map}
     * level or a {@link Mapping#interceptorProperty() mapping} level), where
     * the input received is equal to the given {@link OSKeyBinding}.
     *
     * @param keyBinding The {@link OSKeyBinding} for which mapping execution
     *            should be blocked.
     */
    public KeyMappingInterceptor(OSKeyBinding keyBinding)
    {
        this.myKeyBinding = keyBinding;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.function.Predicate#test(java.lang.Object)
     */
    @Override
    public boolean test(Event event)
    {
        if (!(event instanceof KeyEvent))
        {
            return false;
        }
        return OSKeyBinding.toKeyBinding((KeyEvent)event).equals(myKeyBinding);
    }
}
