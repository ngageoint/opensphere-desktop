package io.opensphere.core.util.collections.observable;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;

public abstract class TransformationSet<E, F> extends ObservableSetBase<E>
{
    private final ObservableSet<? extends F> source;

    private SetChangeListener<F> sourceListener;

    protected TransformationSet(@Nonnull ObservableSet<? extends F> source)
    {
        this.source = requireNonNull(source, "Argument 'source' must not be null");
        source.addListener(new WeakSetChangeListener<>(getListener()));
    }

    @Nonnull
    public final ObservableSet<? extends F> getSource()
    {
        return source;
    }

    public SetChangeListener<F> getListener()
    {
        if (sourceListener == null)
        {
            sourceListener = TransformationSet.this::sourceChanged;
        }
        return sourceListener;
    }

    public final boolean isInTransformationChain(@Nullable ObservableSet<?> set)
    {
        if (source == set)
        {
            return true;
        }
        Set<?> currentSource = source;
        while (currentSource instanceof TransformationSet)
        {
            currentSource = ((TransformationSet)currentSource).source;
            if (currentSource == set)
            {
                return true;
            }
        }
        return false;
    }

    protected abstract void sourceChanged(SetChangeListener.Change<? extends F> c);
}