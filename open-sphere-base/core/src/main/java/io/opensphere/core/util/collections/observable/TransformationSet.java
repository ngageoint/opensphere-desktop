package io.opensphere.core.util.collections.observable;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;

/**
 * A base class for all sets that wrap other sets in a way that changes the
 * set's elements, size or generally it's structure. If the source set is
 * observable, a listener is automatically added to it and the events are
 * delegated to #onSourceChanged(javafx.collections.SetChangeListener.Change)
 *
 * @param <E> the type parameter of this list.
 * @param <F> the upper bound of the type of the source list
 */
public abstract class TransformationSet<E, F> extends ObservableSetBase<E>
{
    /**
     * Contains the source set of this transformation set. This is never null
     * and should be used to directly access source set content.
     */
    private final ObservableSet<? extends F> mySource;

    /**
     * This field contains the result of expression "<code>source instanceof
     * {@link javafx.collections.ObservableSet}</code>". If this is true, it is
     * possible to do transforms online.
     */
    private SetChangeListener<F> mySourceListener;

    /**
     * Creates a new Transformation set wrapped around the source set.
     *
     * @param source the wrapped set (must be <code>non-null</code>).
     */
    protected TransformationSet(@Nonnull ObservableSet<? extends F> source)
    {
        this.mySource = requireNonNull(source, "Argument 'mySource' must not be null");
        source.addListener(new WeakSetChangeListener<>(getListener()));
    }

    /**
     * The source set specified in the constructor of this transformation set.
     *
     * @return The {@link Set} that is directly wrapped by this
     *         {@link TransformationSet}.
     */
    @Nonnull
    public final ObservableSet<? extends F> getSource()
    {
        return mySource;
    }

    /**
     * Gets the change listener registered for source changes.
     *
     * @return the change listener registered for source changes.
     */
    public SetChangeListener<F> getListener()
    {
        if (mySourceListener == null)
        {
            mySourceListener = TransformationSet.this::sourceChanged;
        }
        return mySourceListener;
    }

    /**
     * Checks whether the provided set is in the chain under this
     * {@link TransformationSet}.
     *
     * This means the set is either the direct source as returned by
     * {@link #getSource()} or the direct source is a {@link TransformationSet},
     * and the set is in its transformation chain.
     *
     * @param set the list to check
     * @return <code>true</code> if the set is in the transformation chain as
     *         specified above.
     */
    public final boolean isInTransformationChain(@Nullable ObservableSet<?> set)
    {
        if (mySource == set)
        {
            return true;
        }
        Set<?> currentSource = mySource;
        while (currentSource instanceof TransformationSet)
        {
            currentSource = ((TransformationSet<?, ?>)currentSource).mySource;
            if (currentSource == set)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a change from the source is triggered.
     *
     * @param c the object describing the changes made to the list.
     */
    protected abstract void sourceChanged(SetChangeListener.Change<? extends F> c);
}
