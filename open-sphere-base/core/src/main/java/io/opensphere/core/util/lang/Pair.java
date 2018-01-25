package io.opensphere.core.util.lang;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

/**
 * Immutable pair of objects.
 *
 * @param <S> The type for the first object.
 * @param <T> The type for the second object.
 */
@Immutable
public class Pair<S, T>
{
    /** The first object. */
    private final S myFirstObject;

    /** The second object. */
    private final T mySecondObject;

    /**
     * Convenience factory method for creating pairs.
     *
     * @param <S> The type of the first object.
     * @param <T> The type of the second object.
     * @param firstObject The first object.
     * @param secondObject The second object.
     * @return The pair.
     */
    public static <S, T> Pair<S, T> create(S firstObject, T secondObject)
    {
        return new Pair<>(firstObject, secondObject);
    }

    /**
     * Construct a pair.
     *
     * @param firstObject The first object.
     * @param secondObject The second object.
     */
    public Pair(S firstObject, T secondObject)
    {
        myFirstObject = firstObject;
        mySecondObject = secondObject;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>)obj;
        return Objects.equals(myFirstObject, other.myFirstObject) && Objects.equals(mySecondObject, other.mySecondObject);
    }

    /**
     * Accessor for the firstObject.
     *
     * @return The firstObject.
     */
    public S getFirstObject()
    {
        return myFirstObject;
    }

    /**
     * Accessor for the secondObject.
     *
     * @return The secondObject.
     */
    public T getSecondObject()
    {
        return mySecondObject;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myFirstObject == null ? 0 : myFirstObject.hashCode());
        result = prime * result + (mySecondObject == null ? 0 : mySecondObject.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64).append("Pair[").append(getFirstObject()).append(',').append(getSecondObject()).append(']')
                .toString();
    }
}
