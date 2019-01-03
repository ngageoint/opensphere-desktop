package io.opensphere.core.util.fx.tabpane.inputmap;

import java.util.function.Predicate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

/**
 * Abstract base class for all input mappings as used by the
 * {@link OSInputMap} class.
 *
 * @param <T> The type of {@link Event} the mapping represents.
 */
public abstract class Mapping<T extends Event>
{
    /** The {@link EventType} that is being listened for. */
    final EventType<T> myEventType;

    /**
     * The {@link EventHandler} to fire when the mapping is selected as the
     * most-specific mapping.
     */
    private final EventHandler<T> myEventHandler;

    /**
     * By default all mappings are enabled (so this disabled property is set
     * to false by default). In some cases it is useful to be able to
     * disable a mapping until it is applicable. In these cases, users may
     * simply toggle the disabled property until desired.
     *
     * <p>
     * When the disabled property is true, the mapping will not be
     * considered when input events are received, even if it is the most
     * specific mapping available.
     * </p>
     */
    private BooleanProperty myDisabledProperty = new SimpleBooleanProperty(this, "disabled", false);

    /**
     * By default mappings are set to 'auto consume' their specified event
     * handler. This means that the event handler will not propagate
     * further, but in some cases this is not desirable - sometimes it is
     * preferred that the event continue to 'bubble up' to parent nodes so
     * that they may also benefit from receiving this event. In these cases,
     * it is important that this myAutoConsumeProperty property be changed
     * from the default boolean true to instead be boolean false.
     */
    private BooleanProperty myAutoConsumeProperty = new SimpleBooleanProperty(this, "myAutoConsumeProperty", true);

    /**
     * The role of the interceptor is to block the mapping on which it is
     * set from executing, whenever the interceptor returns true. The
     * interceptor is called every time the mapping is the best match for a
     * given input event, and is allowed to reason on the given input event
     * before returning a boolean value, where boolean true means block
     * execution, and boolean false means to allow execution.
     */
    private ObjectProperty<Predicate<? extends Event>> myInterceptorProperty = new SimpleObjectProperty<>(this,
            "interceptor");

    /**
     * Creates a new Mapping instance.
     *
     * @param eventType The {@link EventType} that is being listened for.
     * @param eventHandler The {@link EventHandler} to fire when the mapping
     *            is selected as the most-specific mapping.
     */
    public Mapping(final EventType<T> eventType, final EventHandler<T> eventHandler)
    {
        this.myEventType = eventType;
        this.myEventHandler = eventHandler;
    }

    /**
     * Gets the property in which the auto-consume flag is stored.
     *
     * @return the property in which the auto-consume flag is stored.
     */
    public final BooleanProperty autoConsumeProperty()
    {
        return myAutoConsumeProperty;
    }

    /**
     * Gets the property in which the disabled flag is stored.
     *
     * @return the property in which the disabled flag is stored.
     */
    public final BooleanProperty disabledProperty()
    {
        return myDisabledProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Mapping))
        {
            return false;
        }

        Mapping<?> that = (Mapping<?>)o;

        if (myEventType != null ? !myEventType.equals(that.getEventType()) : that.getEventType() != null)
        {
            return false;
        }

        return true;
    }

    /**
     * The {@link EventHandler} that will be fired should this mapping be
     * the most-specific mapping for a given input, and should it not be
     * blocked by an interceptor (either at a
     * {@link OSInputMap#interceptorProperty() input map} level or a
     * {@link Mapping#interceptorProperty() mapping} level).
     *
     * @return The {@link EventHandler} to fire when the mapping is selected
     *         as the most-specific mapping.
     */
    public final EventHandler<T> getEventHandler()
    {
        return myEventHandler;
    }

    /**
     * Gets the {@link EventType} that is being listened for.
     *
     * @return the {@link EventType} that is being listened for.
     */
    public final EventType<T> getEventType()
    {
        return myEventType;
    }

    /**
     * Gets the interceptor from the interceptor property. The role of the
     * interceptor is to block the mapping on which it is set from
     * executing, whenever the interceptor returns true. The interceptor is
     * called every time the mapping is the best match for a given input
     * event, and is allowed to reason on the given input event before
     * returning a boolean value, where boolean true means block execution,
     * and boolean false means to allow execution.
     *
     * @return the interceptor from the interceptor property.
     */
    public final Predicate<? extends Event> getInterceptor()
    {
        return myInterceptorProperty.get();
    }

    /**
     * Gets the {@link EventType} that is being listened for.
     *
     * @return the {@link EventType} that is being listened for.
     */
    public Object getMappingKey()
    {
        return myEventType;
    }

    /**
     * This method must be implemented by all mapping implementations such
     * that it returns an integer value representing how closely the mapping
     * matches the given {@link Event}. The higher the number, the greater
     * the match. This allows the OSInputMap to determine which mapping is
     * most specific, and to therefore fire the appropriate mapping
     * {@link Mapping#getEventHandler() EventHandler}.
     *
     * @param event The {@link Event} that needs to be assessed for its
     *            specificity.
     * @return An integer indicating how close of a match the mapping is to
     *         the given Event. The higher the number, the greater the
     *         match.
     */
    public abstract int getSpecificity(Event event);

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return myEventType != null ? myEventType.hashCode() : 0;
    }

    /**
     * Gets the property in which the interceptor is stored. The role of the
     * interceptor is to block the mapping on which it is set from
     * executing, whenever the interceptor returns true. The interceptor is
     * called every time the mapping is the best match for a given input
     * event, and is allowed to reason on the given input event before
     * returning a boolean value, where boolean true means block execution,
     * and boolean false means to allow execution.
     *
     * @return the property in which the interceptor is stored.
     */
    public final ObjectProperty<Predicate<? extends Event>> interceptorProperty()
    {
        return myInterceptorProperty;
    }

    /**
     * Gets the state of the auto-consume property. By default mappings are
     * set to 'auto consume' their specified event handler. This means that
     * the event handler will not propagate further, but in some cases this
     * is not desirable - sometimes it is preferred that the event continue
     * to 'bubble up' to parent nodes so that they may also benefit from
     * receiving this event. In these cases, it is important that this
     * myAutoConsumeProperty property be changed from the default boolean
     * true to instead be boolean false.
     *
     * @return the state of the auto-consume property.
     */
    public final boolean isAutoConsume()
    {
        return myAutoConsumeProperty.get();
    }

    /**
     * Gets the disabled state from the disabled property. By default all
     * mappings are enabled (so this disabled property is set to false by
     * default). In some cases it is useful to be able to disable a mapping
     * until it is applicable. In these cases, users may simply toggle the
     * disabled property until desired.
     *
     * @return the value from the disabled property.
     */
    public final boolean isDisabled()
    {
        return myDisabledProperty.get();
    }

    /**
     * Sets the state of the auto-consume property. By default mappings are
     * set to 'auto consume' their specified event handler. This means that
     * the event handler will not propagate further, but in some cases this
     * is not desirable - sometimes it is preferred that the event continue
     * to 'bubble up' to parent nodes so that they may also benefit from
     * receiving this event. In these cases, it is important that this
     * myAutoConsumeProperty property be changed from the default boolean
     * true to instead be boolean false.
     *
     * @param value the state of the auto-consume property.
     */
    public final void setAutoConsume(boolean value)
    {
        myAutoConsumeProperty.set(value);
    }

    /**
     * Sets the disabled state from the disabled property. By default all
     * mappings are enabled (so this disabled property is set to false by
     * default). In some cases it is useful to be able to disable a mapping
     * until it is applicable. In these cases, users may simply toggle the
     * disabled property until desired.
     *
     * @param value the disabled state from the disabled property.
     */
    public final void setDisabled(boolean value)
    {
        myDisabledProperty.set(value);
    }

    /**
     * Sets the value of the interceptor property. The role of the
     * interceptor is to block the mapping on which it is set from
     * executing, whenever the interceptor returns true. The interceptor is
     * called every time the mapping is the best match for a given input
     * event, and is allowed to reason on the given input event before
     * returning a boolean value, where boolean true means block execution,
     * and boolean false means to allow execution.
     *
     * @param value the value to store in the interceptor property.
     */
    public final void setInterceptor(Predicate<? extends Event> value)
    {
        myInterceptorProperty.set(value);
    }
}
