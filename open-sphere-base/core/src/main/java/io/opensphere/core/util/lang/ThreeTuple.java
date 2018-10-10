package io.opensphere.core.util.lang;

import java.util.Objects;

/**
 * Immutable triplet of objects.
 *
 * @param <S> The type for the first object.
 * @param <T> The type for the second object.
 * @param <U> The type for the third object.
 */
public class ThreeTuple<S, T, U>
{
    /** The first object. */
    private final S myFirstObject;

    /** The second object. */
    private final T mySecondObject;

    /** The third object. */
    private final U myThirdObject;

    /**
     * Convenience factory method for creating triplets.
     *
     * @param <S> The type of the first object.
     * @param <T> The type of the second object.
     * @param <U> The type for the third object.
     * @param firstObject The first object.
     * @param secondObject The second object.
     * @param thirdObject The third object.
     * @return The triplet.
     */
    public static <S, T, U> ThreeTuple<S, T, U> create(S firstObject, T secondObject, U thirdObject)
    {
        return new ThreeTuple<>(firstObject, secondObject, thirdObject);
    }

    /**
     * Construct a triplet.
     *
     * @param firstObject The first object.
     * @param secondObject The second object.
     * @param thirdObject The third object.
     */
    public ThreeTuple(S firstObject, T secondObject, U thirdObject)
    {
        myFirstObject = firstObject;
        mySecondObject = secondObject;
        myThirdObject = thirdObject;
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
        ThreeTuple<?, ?, ?> other = (ThreeTuple<?, ?, ?>)obj;
        return Objects.equals(myFirstObject, other.myFirstObject) && Objects.equals(mySecondObject, other.mySecondObject)
                && Objects.equals(myThirdObject, other.myThirdObject);
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

    /**
     * Accessor for the thirdObject.
     *
     * @return The thirdObject.
     */
    public U getThirdObject()
    {
        return myThirdObject;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myFirstObject == null ? 0 : myFirstObject.hashCode());
        result = prime * result + (mySecondObject == null ? 0 : mySecondObject.hashCode());
        result = prime * result + (myThirdObject == null ? 0 : myThirdObject.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64).append("ThreeTuple[").append(getFirstObject()).append(',').append(getSecondObject())
                .append(',').append(getThirdObject()).append(']').toString();
    }
}
