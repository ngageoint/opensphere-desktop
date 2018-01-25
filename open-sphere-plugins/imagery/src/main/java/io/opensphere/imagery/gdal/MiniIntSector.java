package io.opensphere.imagery.gdal;

/**
 * The Class MiniIntSector.
 */
public class MiniIntSector
{
    /** The x1. */
    private int myX1 = Integer.MAX_VALUE;

    /** The x2. */
    private int myX2 = Integer.MIN_VALUE;

    /** The y1. */
    private int myY1 = Integer.MAX_VALUE;

    /** The y2. */
    private int myY2 = Integer.MIN_VALUE;

    /**
     * Gets the x1.
     *
     * @return the x1
     */
    public int getX1()
    {
        return myX1;
    }

    /**
     * Gets the x2.
     *
     * @return the x2
     */
    public int getX2()
    {
        return myX2;
    }

    /**
     * Gets the y1.
     *
     * @return the y1
     */
    public int getY1()
    {
        return myY1;
    }

    /**
     * Gets the y2.
     *
     * @return the y2
     */
    public int getY2()
    {
        return myY2;
    }

    /**
     * Sets the x1.
     *
     * @param x1 the new x1
     */
    public void setX1(int x1)
    {
        myX1 = x1;
    }

    /**
     * Sets the x2.
     *
     * @param x2 the new x2
     */
    public void setX2(int x2)
    {
        myX2 = x2;
    }

    /**
     * Sets the y1.
     *
     * @param y1 the new y1
     */
    public void setY1(int y1)
    {
        myY1 = y1;
    }

    /**
     * Sets the y2.
     *
     * @param y2 the new y2
     */
    public void setY2(int y2)
    {
        myY2 = y2;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("x1 ").append(myX1);
        sb.append(" x2 ").append(myX2);
        sb.append(" y1 ").append(myY1);
        sb.append(" y2 ").append(myY2);
        return sb.toString();
    }
}
