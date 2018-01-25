package io.opensphere.core.common.lobintersect;

import java.util.ArrayList;
import java.util.List;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * Computes a covariance matrix for a calculated Lob intersection.
 */
public class UnscentedTransform
{

    private final static int wt_size = 5;

    private final static double GN = 2;

    private final static double kappa = 3 - GN;

    private final static double npk = GN + kappa;

    private final static double npk2 = npk * 2;

    private final static double k_n = kappa / npk;

    private final static double o_n = 1 / npk2;

    private final static double deg2Rad = Math.PI / 180;

    private final static double sigma2 = deg2Rad * deg2Rad;

    private final static double ns2 = Math.sqrt(npk * sigma2);

    private DoubleMatrix1D weight = new DenseDoubleMatrix1D(5);

    private DoubleMatrix2D pXX = new DenseDoubleMatrix2D(2, 2);

    private DoubleMatrix2D xBar = new DenseDoubleMatrix2D(2, 5);

    private CourseIntersect courseIntersect = new CourseIntersect();

    /**
     * Constructor initializes constant value matrices
     */
    public UnscentedTransform()
    {
        for (int i = 0; i < wt_size; i++)
        {
            weight.setQuick(i, o_n);
        }
        weight.setQuick(0, k_n);

        pXX.setQuick(0, 0, ns2);
        pXX.setQuick(1, 1, ns2);
        pXX.setQuick(0, 1, 0);
        pXX.setQuick(1, 0, 0);
    }

    /**
     * Performs unscented transformation.
     *
     * @param pt1
     * @param pt2
     * @return covariance matrix
     */
    public DoubleMatrix2D calculate(LobPoint point1, LobPoint point2)
    {

        /* Create clones so original is not modified. */
        LobPoint pt1 = new LobPoint(point1);
        LobPoint pt2 = new LobPoint(point2);

        boolean useCrsIntersect = true;

        double bearing0 = pt1.getBearing();
        double bearing1 = pt2.getBearing();

        xBar.setQuick(0, 0, bearing0);
        xBar.setQuick(1, 0, bearing1);

        double val = bearing0 + pXX.get(0, 0);
        xBar.setQuick(0, 1, val);

        val = bearing1 + pXX.get(1, 0);
        xBar.setQuick(1, 1, val);

        val = bearing0 - pXX.get(0, 0);
        xBar.setQuick(0, 3, val);

        val = bearing1 - pXX.get(1, 0);
        xBar.setQuick(1, 3, val);

        val = bearing0 + pXX.get(0, 1);
        xBar.setQuick(0, 2, val);

        val = bearing1 + pXX.get(1, 1);
        xBar.setQuick(1, 2, val);

        val = bearing0 - pXX.get(0, 1);
        xBar.setQuick(0, 4, val);

        val = bearing1 - pXX.get(1, 1);
        xBar.setQuick(1, 4, val);

        List<LatLon> yPt = new ArrayList<>(5);
        double yBarLat = 0;
        double yBarLon = 0;

        for (int ix = 0; ix < 5; ++ix)
        {

            double b0 = xBar.get(0, ix);
            double b1 = xBar.get(1, ix);
            pt1.setBearing(b0);
            pt2.setBearing(b1);

            /* compute intersection here and store into pt.at(2) Compute
             * intersection resulting from two collectors */
            LatLon ll = null;

            if (useCrsIntersect)
            {
                List<Double> retVals = new ArrayList<>(6);
                if (!courseIntersect.calculate(pt1, pt2, 1e-6, retVals))
                {
                    return null;
                }
                else
                {
                    ll = new LatLon(retVals.get(0), retVals.get(1));
                }
            }
            else
            {
                ll = LobIntersection.getLobIntersection(pt1, pt2);
            }

            if (ll != null)
            {
                /* Sum resulting intersection pts with weight vector (computed
                 * mean); */
                yPt.add(ll);
                yBarLat += ll.getLat() * weight.get(ix);
                yBarLon += ll.getLon() * weight.get(ix);
            }
            else
            {
                return null;
            }
        }

        DoubleMatrix2D Y = new DenseDoubleMatrix2D(2, 5);

        for (int u = 0; u < 5; u++)
        {

            LatLon x = yPt.get(u);
            double yPtLat = x.getLat();
            double yPtLon = x.getLon();
            double dLat = yPtLat - yBarLat;
            double dLon = yPtLon - yBarLon;

            Y.setQuick(0, u, dLat);
            Y.setQuick(1, u, dLon);
        }

        DoubleMatrix2D pYY = new DenseDoubleMatrix2D(2, 2);
        pYY.setQuick(1, 1,
                weight.getQuick(0) * Y.getQuick(0, 0) * Y.getQuick(0, 0)
                        + weight.getQuick(1) * Y.getQuick(0, 1) * Y.getQuick(0, 1)
                        + weight.getQuick(2) * Y.getQuick(0, 2) * Y.getQuick(0, 2)
                        + weight.getQuick(3) * Y.getQuick(0, 3) * Y.getQuick(0, 3)
                        + weight.getQuick(4) * Y.getQuick(0, 4) * Y.getQuick(0, 4));
        pYY.setQuick(0, 0,
                weight.getQuick(0) * Y.getQuick(1, 0) * Y.getQuick(1, 0)
                        + weight.getQuick(1) * Y.getQuick(1, 1) * Y.getQuick(1, 1)
                        + weight.getQuick(2) * Y.getQuick(1, 2) * Y.getQuick(1, 2)
                        + weight.getQuick(3) * Y.getQuick(1, 3) * Y.getQuick(1, 3)
                        + weight.getQuick(4) * Y.getQuick(1, 4) * Y.getQuick(1, 4));
        pYY.setQuick(0, 1,
                weight.getQuick(0) * Y.getQuick(0, 0) * Y.getQuick(1, 0)
                        + weight.getQuick(1) * Y.getQuick(0, 1) * Y.getQuick(1, 1)
                        + weight.getQuick(2) * Y.getQuick(0, 2) * Y.getQuick(1, 2)
                        + weight.getQuick(3) * Y.getQuick(0, 3) * Y.getQuick(1, 3)
                        + weight.getQuick(4) * Y.getQuick(0, 4) * Y.getQuick(1, 4));
        pYY.setQuick(1, 0, pYY.getQuick(0, 1));

        return pYY;
    }
}
