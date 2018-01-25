package io.opensphere.core.util.lang;

import java.util.Objects;

/**
 * Immutable quadruplet of objects.
 *
 * @param <S> The type for the first object.
 * @param <T> The type for the second object.
 * @param <U> The type for the third object.
 * @param <V> The type for the fourth object.
 */
public class FourTuple<S, T, U, V>
{
    /** The first object. */
    private final S myFirstObject;

    /** The third object. */
    private final V myFourthObject;

    /** The second object. */
    private final T mySecondObject;

    /** The third object. */
    private final U myThirdObject;

    /**
     * Construct a quadruplet.
     *
     * @param firstObject The first object.
     * @param secondObject The second object.
     * @param thirdObject The third object.
     * @param fourthObject The fourth object.
     */
    public FourTuple(S firstObject, T secondObject, U thirdObject, V fourthObject)
    {
        myFirstObject = firstObject;
        mySecondObject = secondObject;
        myThirdObject = thirdObject;
        myFourthObject = fourthObject;
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
        FourTuple<?, ?, ?, ?> other = (FourTuple<?, ?, ?, ?>)obj;
        return Objects.equals(myFirstObject, other.myFirstObject) && Objects.equals(mySecondObject, other.mySecondObject)
                && Objects.equals(myThirdObject, other.myThirdObject) && Objects.equals(myFourthObject, other.myFourthObject);
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
     * Get the fourthObject.
     *
     * @return the fourthObject
     */
    public V getFourthObject()
    {
        return myFourthObject;
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
        result = prime * result + (myFourthObject == null ? 0 : myFourthObject.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64).append("FourTuple[").append(getFirstObject()).append(',').append(getSecondObject())
                .append(',').append(getThirdObject()).append(']').append(getFourthObject()).append(']').toString();
    }
}
