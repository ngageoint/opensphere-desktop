package io.opensphere.core.util.swing.pie;

import java.util.ArrayList;

/**
 * Simple 2-dimensional array list.
 *
 * @param <E> the element type
 */
public class TwoDimensionArrayList<E> extends ArrayList<E>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The x count. */
    private final int myXCount;

    /** The y count. */
    private final int myYCount;

    /**
     * Constructor.
     *
     * @param xCount the x count
     * @param yCount the y count
     */
    public TwoDimensionArrayList(int xCount, int yCount)
    {
        super(xCount * yCount);
        myXCount = xCount;
        myYCount = yCount;
    }

    /**
     * Gets the value at the given location.
     *
     * @param x the x index
     * @param y the y index
     * @return the value
     */
    public E get(int x, int y)
    {
        return get(getIndex(x, y));
    }

    /**
     * Gets the x count.
     *
     * @return the x count
     */
    public int getXCount()
    {
        return myXCount;
    }

    /**
     * Gets the y count.
     *
     * @return the y count
     */
    public int getYCount()
    {
        return myYCount;
    }

    /**
     * Gets the actual index for the given location.
     *
     * @param x the x index
     * @param y the y index
     * @return the index
     */
    private int getIndex(int x, int y)
    {
        return myXCount * y + x;
    }
}
