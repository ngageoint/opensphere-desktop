package io.opensphere.imagery.transform;

import java.util.List;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.imagery.algorithm.genetic.Candidate;
import io.opensphere.imagery.algorithm.genetic.FitnessFunction;

/**
 * The Class FitnessFunctionBaseWarp.
 */
public abstract class FitnessFunctionBaseWarp implements FitnessFunction
{
    /** The gp. */
    private TransformCoefficients myGp;

    /** The length of a gene. */
    private int myLength = SeqGenSimple.SIZE;

    /** The original. */
    private final ImageryTransformNthOrder myOriginal;

    /** The original error. */
    private final double myOriginalError;

    /** The original list. */
    private final List<GroundControlPoint> myOriginalList;

    /** The pg. */
    private TransformCoefficients myPg;

    /** The size. */
    private int mySize = ImageryTransformNthOrder.getNumCoefficientsForOrder(1);

    /**
     * Instantiates a new fitness function base warp.
     *
     * @param list the list
     */
    public FitnessFunctionBaseWarp(List<GroundControlPoint> list)
    {
        myOriginalList = list;
        myOriginal = (ImageryTransformNthOrder)ImageryTransformFactory.getImageryTransformBestFit(list);
        myOriginalError = myOriginal.getRMSEMeters();
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    public int getLength()
    {
        return myLength;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize()
    {
        return mySize;
    }

    @Override
    public Object measureFitness(Candidate cand)
    {
        return null;
    }

    @Override
    public double measureFitnessSpecific(Candidate cand)
    {
        ImageryTransformNthOrder itno = new ImageryTransformNthOrder();

        myGp = itno.getGeoToPixelTransformCoeff();
        myPg = itno.getPixelToGeoTransformCoeff();

        double[] coef = new double[mySize];

        // 012345678901234567
        // -12345.1234567E-10

        sequenceDecode(cand, coef);

        myGp.setXCoefficients(null);
        myGp.setYCoefficients(null);
        myPg.setXCoefficients(null);
        myPg.setYCoefficients(null);

        findError(cand, itno, coef);

        return cand.getFitness();
    }

    /**
     * Sequence decode.
     *
     * @param cand the cand
     * @param coef the coef
     */
    public void sequenceDecode(Candidate cand, double[] coef)
    {
        for (int i = 0; i < mySize; i++)
        {
            int begin = i * myLength;
            int end = begin + myLength;

            coef[i] = Double.parseDouble(cand.getSequence().getSequence().substring(begin, end).replace("+", ""));
        }
    }

    /**
     * Sets the length.
     *
     * @param length the new length
     */
    public void setLength(int length)
    {
        myLength = length;
    }

    /**
     * Sets the size.
     *
     * @param size the new size
     */
    public void setSize(int size)
    {
        mySize = size;
    }

    /**
     * Find error.
     *
     * @param cand the cand
     * @param itno the itno
     * @param coef the coef
     */
    protected abstract void findError(Candidate cand, ImageryTransformNthOrder itno, double[] coef);

    /**
     * Gets the gp.
     *
     * @return the gp
     */
    protected final TransformCoefficients getGp()
    {
        return myGp;
    }

    /**
     * Gets the original.
     *
     * @return the original
     */
    protected final ImageryTransformNthOrder getOriginal()
    {
        return myOriginal;
    }

    /**
     * Gets the original error.
     *
     * @return the original error
     */
    protected double getOriginalError()
    {
        return myOriginalError;
    }

    /**
     * Gets the original list.
     *
     * @return the original list
     */
    protected List<GroundControlPoint> getOriginalList()
    {
        return myOriginalList;
    }

    /**
     * Gets the pg.
     *
     * @return the pg
     */
    protected final TransformCoefficients getPg()
    {
        return myPg;
    }
}
