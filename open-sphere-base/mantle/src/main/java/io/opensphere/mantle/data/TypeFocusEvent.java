package io.opensphere.mantle.data;

import java.util.Collection;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.core.util.collections.New;

/**
 * The Class TypeFocusEvent.
 *
 * @param <T> the generic type
 */
public class TypeFocusEvent<T> extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The source. */
    private final Object mySource;

    /** The types associated with the event. */
    private final Collection<? extends T> myTypes;

    /** The focus type. */
    private final FocusType myFocusType;

    /**
     * Instantiates a new focus event.
     *
     * @param types the types
     * @param source - the source of the event
     */
    public TypeFocusEvent(Collection<? extends T> types, Object source)
    {
        this(types, source, FocusType.CLICK);
    }

    /**
     * Instantiates a new focus event.
     *
     * @param types the types
     * @param source - the source of the event
     * @param focusType the focus type
     */
    public TypeFocusEvent(Collection<? extends T> types, Object source, FocusType focusType)
    {
        myTypes = New.unmodifiableCollection(types);
        mySource = source;
        myFocusType = focusType;
    }

    @Override
    public String getDescription()
    {
        return "Type Focus Event";
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Gets the type associated with the event.
     *
     * @return the group.
     */
    public Collection<? extends T> getTypes()
    {
        return myTypes;
    }

    /**
     * Gets the focus type.
     *
     * @return the focus type
     */
    public FocusType getFocusType()
    {
        return myFocusType;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append("TypeFocusEvent : ").append(myTypes.getClass().getSimpleName());
        return sb.toString();
    }

    /** Focus type. */
    public enum FocusType
    {
        /** Click. */
        CLICK,

        /** Hover gained. */
        HOVER_GAINED,

        /** Hover lost. */
        HOVER_LOST;
    }
}
