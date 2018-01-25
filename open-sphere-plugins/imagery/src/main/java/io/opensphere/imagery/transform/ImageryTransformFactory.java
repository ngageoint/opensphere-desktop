package io.opensphere.imagery.transform;

import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.imagery.algorithm.genetic.Candidate;

/**
 * Factory to facilitate creation of ImageryTransforms.
 */
public final class ImageryTransformFactory
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ImageryTransformFactory.class);

    /** The our max order. */
    private static int ourMaxOrder = 8;

    /**
     * Generate ga solution.
     *
     * @param listOfGCPs the list of gc ps
     * @param order the order
     * @return the imagery transform
     */
    public static ImageryTransform generateGASolution(List<GroundControlPoint> listOfGCPs, int order)
    {
        int gens = 20;

        FitnessFunctionBaseWarp ff = new FitnessFunctionPixToGeoX(listOfGCPs);

        LifeCycleWarp lcw = new LifeCycleWarp(gens, 10000, 10000, ff);
        lcw.setGCPs(listOfGCPs);
        lcw.run();

        List<Candidate> lpgx = lcw.getBestCandidates();

        double[] pgx = new double[ff.getSize()];
        ff.sequenceDecode(lpgx.get(0), pgx);
        ff = new FitnessFunctionPixToGeoY(listOfGCPs);
        lcw = new LifeCycleWarp(gens, 10000, 10000, ff);
        lcw.setGCPs(listOfGCPs);
        lcw.run();

        List<Candidate> lpgy = lcw.getBestCandidates();
        double[] pgy = new double[ff.getSize()];
        ff.sequenceDecode(lpgy.get(0), pgy);
        ff = new FitnessFunctionGeoToPixX(listOfGCPs);
        lcw = new LifeCycleWarp(gens, 10000, 10000, ff);
        lcw.setGCPs(listOfGCPs);
        lcw.run();

        List<Candidate> lgpx = lcw.getBestCandidates();

        double[] gpx = new double[ff.getSize()];
        ff.sequenceDecode(lgpx.get(0), gpx);
        ff = new FitnessFunctionGeoToPixY(listOfGCPs);
        lcw = new LifeCycleWarp(gens, 10000, 10000, ff);
        lcw.setGCPs(listOfGCPs);
        lcw.run();

        List<Candidate> lgpy = lcw.getBestCandidates();

        double[] gpy = new double[ff.getSize()];
        ff.sequenceDecode(lgpy.get(0), gpy);

        ImageryTransformNthOrder itno = new ImageryTransformNthOrder();

        itno.findTransform(listOfGCPs);

//        TransformCoefficients tcgp = itno.getGeoToPixelTransformCoeff();
        TransformCoefficients tcpg = itno.getPixelToGeoTransformCoeff();
        // tcgp.setXCoefficients(gpx);
        // tcgp.setYCoefficients(gpy);
        tcpg.setXCoefficients(pgx);
        tcpg.setYCoefficients(pgy);

        return itno;
    }

    /**
     * Counts GCPs to determine the best imagery transform, will drop down to
     * lower level transforms if higher ones throw exceptions. 3 to 5 GCPs is
     * first order, 6 to 9 is second order and 10 to MAXInt are third order.
     *
     * @param listOfGCPs the list of gc ps
     * @return the imagery transform best fit
     */
    public static ImageryTransform getImageryTransformBestFit(List<GroundControlPoint> listOfGCPs)
    {
        int size = listOfGCPs.size();

        if (size < 3)
        {
            String msg = "Not enough GCPs for getImageryTransformUserPicksOrder, GCP count: " + size;
            throw new IllegalArgumentException(msg);
        }

        if (size >= 3 && size < 6)
        {
            return createHighestTransformPossible(listOfGCPs, 1);
        }
        else if (size >= 6 && size < 10)
        {
            return createHighestTransformPossible(listOfGCPs, 2);
        }
        else if (size >= 10 && size < 15)
        {
            return createHighestTransformPossible(listOfGCPs, 3);
        }
        else if (size >= 15 && size < 21)
        {
            return createHighestTransformPossible(listOfGCPs, 4);
        }
        else if (size >= 21 && size < 28)
        {
            return createHighestTransformPossible(listOfGCPs, 5);
        }
        else if (size >= 28 && size < 36)
        {
            return createHighestTransformPossible(listOfGCPs, 6);
        }
        else if (size >= 36 && size < 45)
        {
            return createHighestTransformPossible(listOfGCPs, 7);
        }
        else // if (size >= 45)
        {
            return createHighestTransformPossible(listOfGCPs, 8);
        }
    }

    /**
     * Returns an imagery transform based on the order that the user picks. If a
     * high level transform cannot be built(solved) a lower one _is_ returned
     * instead.
     *
     * @param listOfGCPs - you must have at least
     * @param order - 1,2 and 3 are valid values, 4 - MAXInt gets mapped to 3,
     *            negatives will throw an IllegalArgumentException
     * @return the {@link ImageryTransform}
     */
    public static ImageryTransform getImageryTransformUserPicksOrder(List<GroundControlPoint> listOfGCPs, int order)
    {
        int size = listOfGCPs.size();
        if (size < 3)
        {
            String msg = "Not enough GCPs for getImageryTransformUserPicksOrder, GCP count: " + size;
            throw new IllegalArgumentException(msg);
        }

        if (size >= 3 && order == 1)
        {
            return createHighestTransformPossible(listOfGCPs, 1);
        }
        else if (size >= 6 && order == 2)
        {
            return createHighestTransformPossible(listOfGCPs, 2);
        }
        else if (size >= 10 && order == 3)
        {
            return createHighestTransformPossible(listOfGCPs, 3);
        }
        else if (size >= 15 && order == 4)
        {
            // return generateGASolution(listOfGCPs, order);//XXX
            return createHighestTransformPossible(listOfGCPs, 4);
        }
        else if (size >= 21 && order == 5)
        {
            return createHighestTransformPossible(listOfGCPs, 5);
        }
        else if (size >= 28 && order == 6)
        {
            return createHighestTransformPossible(listOfGCPs, 6);
        }
        else if (size >= 36 && order == 7)
        {
            return createHighestTransformPossible(listOfGCPs, 7);
        }
        else if (size >= 45 && order == 8)
        {
            return createHighestTransformPossible(listOfGCPs, 8);
        }

        return createHighestTransformPossible(listOfGCPs, 1);
    }

    /**
     * Gets the max method.
     *
     * @return the max method
     */
    public static int getMaxMethod()
    {
        return 8;
    }

    /**
     * Return the max order the factory can generate, allows a GUI to show the
     * correct range of options.
     *
     * @return the max order.
     */
    public static int getMaxOrder()
    {
        return ourMaxOrder;
    }

    /**
     * While wrapping the findTransform call with try/catch, find the highest
     * possible transform that is successful up to highestOrder.
     *
     * @param listOfGCPs - GCPs to find transform with
     * @param pHighestOrder - highest transform to be attempted
     * @return the imagery transform
     */
    private static ImageryTransform createHighestTransformPossible(List<GroundControlPoint> listOfGCPs, int pHighestOrder)
    {
        int highestOrder = pHighestOrder;
        if (highestOrder > ourMaxOrder)
        {
            highestOrder = ourMaxOrder;
        }

        for (int i = highestOrder; i > 0; i--)
        {
            try
            {
                ImageryTransform it = new ImageryTransformNthOrder(i);
                it.findTransform(listOfGCPs);
                return it;
            }
            catch (RuntimeException e)
            {
                LOGGER.error("Backing down to " + i + " order transform.", e);
            }
        }
        return null;
    }

    /**
     * Do not allow instantiation.
     */
    private ImageryTransformFactory()
    {
    }
}
