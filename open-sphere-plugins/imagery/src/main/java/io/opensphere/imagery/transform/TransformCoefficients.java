package io.opensphere.imagery.transform;

import java.util.Arrays;

import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * Coefficients to be used by the ImageryTransformAssistants.
 */
public class TransformCoefficients implements Cloneable
{
    /** The Constant COEFFICENT_ARRAY_LENGTH. */
    private static final int COEFFICENT_ARRAY_LENGTH = 3;

    /** The order. */
    private int myOrder = 1;

    /** The ready. */
    private boolean myReady;

    /** The X coefficients. */
    private double[] myXCoefficients = new double[6];

    /** The Y coefficients. */
    private double[] myYCoefficients = new double[6];

    /** The Z coefficients. */
    private double[] myZCoefficients = new double[6];

    /** The Z enabled. */
    private boolean myZEnabled;

    /**
     * Instantiates a new transform coefficients.
     *
     * @param order the order
     */
    public TransformCoefficients(int order)
    {
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
        TransformCoefficients other = (TransformCoefficients)obj;

        return myZEnabled == other.myZEnabled && myOrder == other.myOrder && myReady == other.myReady
                && Arrays.equals(myXCoefficients, other.myXCoefficients) && Arrays.equals(myYCoefficients, other.myYCoefficients)
                && Arrays.equals(myZCoefficients, other.myZCoefficients);
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public int getOrder()
    {
        return myOrder;
    }

    /**
     * Gets the x coefficients.
     *
     * @return the x coefficients
     */
    public double[] getXCoefficients()
    {
        return Arrays.copyOf(myXCoefficients, myXCoefficients.length);
    }

    /**
     * Gets the y coefficients.
     *
     * @return the y coefficients
     */
    public double[] getYCoefficients()
    {
        return Arrays.copyOf(myYCoefficients, myYCoefficients.length);
    }

    /**
     * Gets the z coefficients.
     *
     * @return the z coefficients
     */
    public double[] getZCoefficients()
    {
        return Arrays.copyOf(myZCoefficients, myZCoefficients.length);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        result = prime * result + Arrays.hashCode(myXCoefficients);
        result = prime * result + Arrays.hashCode(myYCoefficients);
        result = prime * result + Arrays.hashCode(myZCoefficients);
        result = prime * result + (myZEnabled ? 1231 : 1237);
        result = prime * result + myOrder;
        result = prime * result + (myReady ? 1231 : 1237);
        return result;
    }

    /**
     * Checks if is ready.
     *
     * @return true, if is ready
     */
    public boolean isReady()
    {
        return myReady;
    }

    /**
     * Checks if is z enabled.
     *
     * @return true, if is z enabled
     */
    public boolean isZEnabled()
    {
        return myZEnabled;
    }

    /**
     * Sets the order.
     *
     * @param order the new order
     */
    public void setOrder(int order)
    {
        myOrder = order;
    }

    /**
     * Sets the ready.
     *
     * @param ready the new ready
     */
    public void setReady(boolean ready)
    {
        myReady = ready;
    }

    /**
     * Sets the x coefficients.
     *
     * @param coefficients the new x coefficients
     */
    public void setXCoefficients(double[] coefficients)
    {
        myXCoefficients = coefficients == null ? null : Arrays.copyOf(coefficients, coefficients.length);
    }

    /**
     * Sets the y coefficients.
     *
     * @param coefficients the new y coefficients
     */
    public void setYCoefficients(double[] coefficients)
    {
        myYCoefficients = coefficients == null ? null : Arrays.copyOf(coefficients, coefficients.length);
    }

    /**
     * Sets the z coefficients.
     *
     * @param coefficients the new z coefficients
     */
    public void setZCoefficients(double[] coefficients)
    {
        myZCoefficients = coefficients == null ? null : Arrays.copyOf(coefficients, coefficients.length);
    }

    /**
     * Sets the z enabled.
     *
     * @param enabled the new z enabled
     */
    public void setZEnabled(boolean enabled)
    {
        myZEnabled = enabled;
    }

    @Override
    public String toString()
    {
        return "TransformCoefficients [XCoefficients=" + Arrays.toString(myXCoefficients) + ", YCoefficients="
                + Arrays.toString(myYCoefficients) + ", ZCoefficients=" + Arrays.toString(myZCoefficients) + ", order=" + myOrder
                + ", length=" + COEFFICENT_ARRAY_LENGTH + ", ready=" + myReady + ", ZEnabled=" + myZEnabled + "]";
    }

    @Override
    public TransformCoefficients clone()
    {
        TransformCoefficients clone;
        try
        {
            clone = (TransformCoefficients)super.clone();
            clone.myXCoefficients = myXCoefficients.clone();
            clone.myYCoefficients = myYCoefficients.clone();
            clone.myZCoefficients = myZCoefficients.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
        return clone;
    }
}
