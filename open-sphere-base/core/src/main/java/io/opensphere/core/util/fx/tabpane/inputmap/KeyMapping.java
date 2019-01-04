package io.opensphere.core.util.fx.tabpane.inputmap;

import java.util.Objects;
import java.util.function.Predicate;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * The KeyMapping class provides API to specify {@link Mapping mappings} related
 * to key input.
 */
public class KeyMapping extends Mapping<KeyEvent>
{
    /** The key binding for which the instance is configured. */
    private final OSKeyBinding myKeyBinding;

    /**
     * Creates a new KeyMapping instance that will fire when the given
     * {@link KeyCode} is entered into the application by the user, and this
     * will result in the given {@link EventHandler} being fired.
     *
     * @param keyCode The {@link KeyCode} to listen for.
     * @param eventHandler The {@link EventHandler} to fire when the
     *            {@link KeyCode} is observed.
     */
    public KeyMapping(final KeyCode keyCode, final EventHandler<KeyEvent> eventHandler)
    {
        this(new OSKeyBinding(keyCode), eventHandler);
    }

    /**
     * Creates a new KeyMapping instance that will fire when the given
     * {@link KeyCode} is entered into the application by the user, and this
     * will result in the given {@link EventHandler} being fired. The eventType
     * argument can be one of the following:
     *
     * <ul>
     * <li>{@link KeyEvent#ANY}</li>
     * <li>{@link KeyEvent#KEY_PRESSED}</li>
     * <li>{@link KeyEvent#KEY_TYPED}</li>
     * <li>{@link KeyEvent#KEY_RELEASED}</li>
     * </ul>
     *
     * @param keyCode The {@link KeyCode} to listen for.
     * @param eventType The type of {@link KeyEvent} to listen for.
     * @param eventHandler The {@link EventHandler} to fire when the
     *            {@link KeyCode} is observed.
     */
    public KeyMapping(final KeyCode keyCode, final EventType<KeyEvent> eventType, final EventHandler<KeyEvent> eventHandler)
    {
        this(new OSKeyBinding(keyCode, eventType), eventHandler);
    }

    /**
     * Creates a new KeyMapping instance that will fire when the given
     * {@link OSKeyBinding} is entered into the application by the user, and
     * this will result in the given {@link EventHandler} being fired.
     *
     * @param keyBinding The {@link OSKeyBinding} to listen for.
     * @param eventHandler The {@link EventHandler} to fire when the
     *            {@link OSKeyBinding} is observed.
     */
    public KeyMapping(OSKeyBinding keyBinding, final EventHandler<KeyEvent> eventHandler)
    {
        this(keyBinding, eventHandler, null);
    }

    /**
     * Creates a new KeyMapping instance that will fire when the given
     * {@link OSKeyBinding} is entered into the application by the user, and
     * this will result in the given {@link EventHandler} being fired, as long
     * as the given interceptor is not true.
     *
     * @param keyBinding The {@link OSKeyBinding} to listen for.
     * @param eventHandler The {@link EventHandler} to fire when the
     *            {@link OSKeyBinding} is observed.
     * @param interceptor A {@link Predicate} that, if true, will prevent the
     *            {@link EventHandler} from being fired.
     */
    public KeyMapping(OSKeyBinding keyBinding, final EventHandler<KeyEvent> eventHandler, Predicate<KeyEvent> interceptor)
    {
        super(keyBinding == null ? null : keyBinding.getType(), eventHandler);
        if (keyBinding == null)
        {
            throw new IllegalArgumentException("KeyMapping keyBinding constructor argument can not be null");
        }
        this.myKeyBinding = keyBinding;
        setInterceptor(interceptor);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.fx.tabpane.inputmap.Mapping#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof KeyMapping))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        KeyMapping that = (KeyMapping)o;

        // we know keyBinding is non-null here
        return myKeyBinding.equals(that.myKeyBinding);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.fx.tabpane.inputmap.Mapping#getMappingKey()
     */
    @Override
    public Object getMappingKey()
    {
        return myKeyBinding;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.fx.tabpane.inputmap.Mapping#getSpecificity(javafx.event.Event)
     */
    @Override
    public int getSpecificity(Event e)
    {
        if (isDisabled())
        {
            return 0;
        }
        if (!(e instanceof KeyEvent))
        {
            return 0;
        }
        return myKeyBinding.getSpecificity((KeyEvent)e);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.fx.tabpane.inputmap.Mapping#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(myKeyBinding);
    }
}
