package io.opensphere.core.common.convolve;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import io.opensphere.core.common.geospatial.model.DataEllipse;

public class Convolve
{

    /** 95% error ellipse assumption. */
    private final double k = 2.47746;

    private final double kk = k * k;

    private Algebra mat = new Algebra();

    private Log LOGGER = LogFactory.getLog(Convolve.class);

    private DoubleMatrix2D buildRu(double a, double b)
    {
        DoubleMatrix2D r = new DenseDoubleMatrix2D(2, 2);
        r.setQuick(0, 0, b * b / kk);
        r.setQuick(0, 1, 0);
        r.setQuick(1, 0, 0);
        r.setQuick(1, 1, a * a / kk);
        return r;
    }

    private DoubleMatrix2D buildA(double o)
    {
        DoubleMatrix2D r = new DenseDoubleMatrix2D(2, 2);
        r.setQuick(0, 0, Math.cos(o));
        r.setQuick(0, 1, Math.sin(o));
        r.setQuick(1, 0, -Math.sin(o));
        r.setQuick(1, 1, Math.cos(o));
        return r;
    }

    private DoubleMatrix1D buildXY(double x, double y)
    {
        DoubleMatrix1D r = new DenseDoubleMatrix1D(2);
        r.setQuick(0, x);
        r.setQuick(1, y);
        return r;
    }

    private DoubleMatrix1D add(DoubleMatrix1D a, DoubleMatrix1D b)
    {

        try
        {
            a.checkSize(b);
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.error("Matrices are incompatible for adding");
            return a;
        }
        DoubleMatrix1D sum = new DenseDoubleMatrix1D(a.size());
        for (int i = 0; i < a.size(); i++)
        {
            sum.setQuick(i, a.getQuick(i) + b.getQuick(i));
        }

        return sum;
    }

    private DoubleMatrix2D add(DoubleMatrix2D a, DoubleMatrix2D b)
    {

        try
        {
            a.checkShape(b);
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.error("Matrices are incompatible for adding");
            return a;
        }
        DoubleMatrix2D sum = new DenseDoubleMatrix2D(a.rows(), a.columns());
        for (int i = 0; i < a.rows(); i++)
        {
            for (int j = 0; j < a.columns(); j++)
            {
                sum.setQuick(i, j, a.getQuick(i, j) + b.getQuick(i, j));
            }
        }

        return sum;
    }

    private DoubleMatrix1D diag(DoubleMatrix2D d)
    {

        int size = d.rows();
        DoubleMatrix1D result = new DenseDoubleMatrix1D(size);
        for (int i = 0; i < size; i++)
        {
            result.setQuick(i, d.getQuick(i, i));
        }

        return result;
    }

    private List<Number> findMin(DoubleMatrix1D d)
    {
        double min = d.getQuick(0);
        int ndx = 0;

        for (int i = 1; i < d.size(); i++)
        {
            double v = d.getQuick(i);
            if (v < min)
            {
                min = v;
                ndx = i;
            }
        }
        Vector<Number> ans = new Vector<>(2);
        ans.add(Double.valueOf(min));
        ans.add(Integer.valueOf(ndx));
        return ans;
    }

    private List<Number> findMax(DoubleMatrix1D d)
    {
        double max = d.getQuick(0);
        int ndx = 0;

        for (int i = 1; i < d.size(); i++)
        {
            double v = d.getQuick(i);
            if (v > max)
            {
                max = v;
                ndx = i;
            }
        }
        Vector<Number> ans = new Vector<>(2);
        ans.add(Double.valueOf(max));
        ans.add(Integer.valueOf(ndx));
        return ans;
    }

    /**
     * Takes a list of Ellipse objects and convolves them. Avg 80000 Nanos using
     * Colt classes.
     *
     * @param ellipses
     * @return the convolved ellipse.
     */
    public DataEllipse calculate(java.util.List<DataEllipse> ellipses)
    {

        if (ellipses == null || ellipses.size() == 0)
        {
            LOGGER.info("No ellipses specified, returning null.");
            return null;
        }
        else if (ellipses.size() == 1)
        {
            return ellipses.get(0);
        }

        DataEllipse conv = new DataEllipse();
        DoubleMatrix1D sum1 = new DenseDoubleMatrix1D(2);
        DoubleMatrix2D sum2 = new DenseDoubleMatrix2D(2, 2);

        Iterator<DataEllipse> iter = ellipses.iterator();

        while (iter.hasNext())
        {
            DataEllipse e = iter.next();
            e.setOrientation(e.getOrientation() % 360); // Handle out of range
                                                        // degrees
            DoubleMatrix2D ru = buildRu(e.getSemiMajorAxis(), e.getSemiMinorAxis());
            DoubleMatrix2D a = buildA(e.getOrientation() * Math.PI / 180.0);
            DoubleMatrix2D ar = mat.mult(a, ru);
            DoubleMatrix2D trans = mat.transpose(a);
            DoubleMatrix2D r = mat.mult(ar, trans);
            DoubleMatrix2D invR;

            try
            {
                invR = mat.inverse(r);
            }
            catch (IllegalArgumentException ex)
            {
                LOGGER.info("Omitting singular matrix. ");
                continue;
            }

            DoubleMatrix1D xy = buildXY(e.getLon(), e.getLat());
            DoubleMatrix1D tmp = mat.mult(invR, xy);

            sum1 = add(sum1, tmp);
            sum2 = add(sum2, invR);
        }

        DoubleMatrix2D r_rml = null;
        try
        {
            r_rml = mat.inverse(sum2);
        }
        catch (IllegalArgumentException ex)
        {
            LOGGER.info("Ellipses could not be convolved, returning null.");
            return null;
        }

        /* Compute the center point. */
        DoubleMatrix1D r_ml = mat.mult(r_rml, sum1);
        conv.setLon(r_ml.getQuick(0));
        conv.setLat(r_ml.getQuick(1));

        EigenvalueDecomposition eig = new EigenvalueDecomposition(r_rml);
        DoubleMatrix2D v = eig.getV();
        DoubleMatrix2D d = eig.getD();
        DoubleMatrix1D diagD = diag(d);

        /* Compute Semi-Major axis. */
        List<Number> max = findMax(diagD);
        double maxVal = max.get(0).doubleValue();
        double val = k * Math.sqrt(maxVal);
        conv.setSemiMajorAxis(val);

        /* Compute Semi-Minor axis. */
        List<Number> min = findMin(diagD);
        double minVal = min.get(0).doubleValue();
        val = k * Math.sqrt(minVal);
        conv.setSemiMinorAxis(val);

        /* Compute Orientation */
        int max_ndx = max.get(1).intValue();
        double p1 = v.getQuick(0, max_ndx);
        double p2 = v.getQuick(1, max_ndx);
        double rads = Math.atan2(p1, p2);
        // convert to degrees
        conv.setOrientation(rads * 180.0 / Math.PI);

        return conv;

    }

}
