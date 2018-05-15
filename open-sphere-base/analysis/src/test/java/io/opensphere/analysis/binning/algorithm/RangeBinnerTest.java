package io.opensphere.analysis.binning.algorithm;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.bins.RangeBin;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.core.util.lang.ThreeTuple;

/**
 * Tests for {@link RangeBinner}.
 */
@SuppressWarnings("boxing")
public class RangeBinnerTest
{
    /** Tests automatic binning. */
    @Test
    public void testAutoBin()
    {
        RangeCriteria criteria = new RangeCriteria();
        criteria.setBinWidth(10);
        RangeBinner<Point2D.Double> binner = new RangeBinner<>(criteria, p -> Double.valueOf(p.x), o -> ((Double)o).doubleValue(),
                (v, c) -> Double.valueOf(v));
        binner.add(new Point2D.Double(0, 0));
        binner.add(new Point2D.Double(3.5, 0));
        binner.add(new Point2D.Double(10, 0));
        binner.add(new Point2D.Double(33, 0));
        binner.add(new Point2D.Double(34, 0));

        Assert.assertEquals(3, binner.getBins().size());

        Assert.assertEquals(2, binner.getBins().get(0).getSize());
        Assert.assertEquals(0., binner.getBins().get(0).getData().get(0).x, 0.);
        Assert.assertEquals(3.5, binner.getBins().get(0).getData().get(1).x, 0.);

        Assert.assertEquals(1, binner.getBins().get(1).getSize());
        Assert.assertEquals(10., binner.getBins().get(1).getData().get(0).x, 0.);

        Assert.assertEquals(2, binner.getBins().get(2).getSize());
        Assert.assertEquals(33., binner.getBins().get(2).getData().get(0).x, 0.);
        Assert.assertEquals(34., binner.getBins().get(2).getData().get(1).x, 0.);

        binner.remove(new Point2D.Double(34, 0));

        Assert.assertEquals(1, binner.getBins().get(2).getSize());
        Assert.assertEquals(33., binner.getBins().get(2).getData().get(0).x, 0.);
    }

    /** Tests custom binning. */
    @Test
    public void testCustomBin()
    {
        List<Bin<Point2D.Double>> bins = Arrays.asList(
                new RangeBin<>(0, 20, 0, p -> Double.valueOf(p.x), o -> ((Double)o).doubleValue()),
                new RangeBin<>(30, 40, 30, p -> Double.valueOf(p.x), o -> ((Double)o).doubleValue()));
        RangeBinner<Point2D.Double> binner = new RangeBinner<>(bins);
        binner.add(new Point2D.Double(5, 0));
        binner.add(new Point2D.Double(10, 0));
        binner.add(new Point2D.Double(25, 0));
        binner.add(new Point2D.Double(35, 0));

        Assert.assertEquals(2, binner.getBins().size());

        Assert.assertEquals(2, binner.getBins().get(0).getSize());
        Assert.assertEquals(5., binner.getBins().get(0).getData().get(0).x, 0.);
        Assert.assertEquals(10., binner.getBins().get(0).getData().get(1).x, 0.);

        Assert.assertEquals(1, binner.getBins().get(1).getSize());
        Assert.assertEquals(35., binner.getBins().get(1).getData().get(0).x, 0.);
    }

    /** Tests empty bin creation. */
    @Test
    public void testEmptyBins()
    {
        RangeCriteria criteria = new RangeCriteria();
        criteria.setBinWidth(10);
        RangeBinner<Point2D.Double> binner = new RangeBinner<>(criteria, p -> Double.valueOf(p.x), o -> ((Double)o).doubleValue(),
                (v, c) -> Double.valueOf(v));
        binner.setCreateEmptyBins(true);

        binner.add(new Point2D.Double(35, 0));
        binner.add(new Point2D.Double(65, 0));

        Assert.assertEquals(4, binner.getBins().size());
        Assert.assertEquals(30., ((RangeBin<Point2D.Double>)binner.getBins().get(0)).getMin(), 0.);
        Assert.assertEquals(40., ((RangeBin<Point2D.Double>)binner.getBins().get(1)).getMin(), 0.);
        Assert.assertEquals(50., ((RangeBin<Point2D.Double>)binner.getBins().get(2)).getMin(), 0.);
        Assert.assertEquals(60., ((RangeBin<Point2D.Double>)binner.getBins().get(3)).getMin(), 0.);

        binner.add(new Point2D.Double(15, 0));

        Assert.assertEquals(6, binner.getBins().size());
        Assert.assertEquals(10., ((RangeBin<Point2D.Double>)binner.getBins().get(0)).getMin(), 0.);
        Assert.assertEquals(20., ((RangeBin<Point2D.Double>)binner.getBins().get(1)).getMin(), 0.);
        Assert.assertEquals(30., ((RangeBin<Point2D.Double>)binner.getBins().get(2)).getMin(), 0.);
        Assert.assertEquals(40., ((RangeBin<Point2D.Double>)binner.getBins().get(3)).getMin(), 0.);
        Assert.assertEquals(50., ((RangeBin<Point2D.Double>)binner.getBins().get(4)).getMin(), 0.);
        Assert.assertEquals(60., ((RangeBin<Point2D.Double>)binner.getBins().get(5)).getMin(), 0.);
    }

    /** Tests empty bin creation with N/A bin. */
    @Test
    public void testEmptyBinsAndNA()
    {
        RangeCriteria criteria = new RangeCriteria();
        criteria.setBinWidth(10);
        RangeBinner<Point2D.Double> binner = new RangeBinner<>(criteria, p -> p == null ? null : Double.valueOf(p.x),
                o -> ((Double)o).doubleValue(), (v, c) -> Double.valueOf(v));
        binner.setCreateEmptyBins(true);

        binner.add(null);
        binner.add(new Point2D.Double(35, 0));
        binner.add(new Point2D.Double(65, 0));

        Assert.assertEquals(5, binner.getBins().size());
        Assert.assertEquals(30., ((RangeBin<Point2D.Double>)binner.getBins().get(0)).getMin(), 0.);
        Assert.assertEquals(40., ((RangeBin<Point2D.Double>)binner.getBins().get(1)).getMin(), 0.);
        Assert.assertEquals(50., ((RangeBin<Point2D.Double>)binner.getBins().get(2)).getMin(), 0.);
        Assert.assertEquals(60., ((RangeBin<Point2D.Double>)binner.getBins().get(3)).getMin(), 0.);
        Assert.assertNull(((RangeBin<Point2D.Double>)binner.getBins().get(4)).getValueObject());

        binner.add(new Point2D.Double(15, 0));

        Assert.assertEquals(7, binner.getBins().size());
        Assert.assertEquals(10., ((RangeBin<Point2D.Double>)binner.getBins().get(0)).getMin(), 0.);
        Assert.assertEquals(20., ((RangeBin<Point2D.Double>)binner.getBins().get(1)).getMin(), 0.);
        Assert.assertEquals(30., ((RangeBin<Point2D.Double>)binner.getBins().get(2)).getMin(), 0.);
        Assert.assertEquals(40., ((RangeBin<Point2D.Double>)binner.getBins().get(3)).getMin(), 0.);
        Assert.assertEquals(50., ((RangeBin<Point2D.Double>)binner.getBins().get(4)).getMin(), 0.);
        Assert.assertEquals(60., ((RangeBin<Point2D.Double>)binner.getBins().get(5)).getMin(), 0.);
        Assert.assertNull(((RangeBin<Point2D.Double>)binner.getBins().get(6)).getValueObject());
    }

    /** Tests {@link RangeBinner#getMinMaxBinValueValue(Object)}. */
    @Test
    public void testGetMinMaxBinValueValue()
    {
        RangeCriteria criteria = new RangeCriteria();
        criteria.setBinWidth(0.1);
        RangeBinner<Point2D.Double> binner = new RangeBinner<>(criteria, p -> Double.valueOf(p.x), o -> ((Double)o).doubleValue(),
                (v, c) -> Double.valueOf(v));
        ThreeTuple<Double, Double, Object> t;

        t = binner.getMinMaxBinValueValue(new Point2D.Double(0, 0));
        Assert.assertEquals(Double.valueOf(0), t.getFirstObject());
        Assert.assertEquals(Double.valueOf(0.1), t.getSecondObject());
        Assert.assertEquals(Double.valueOf(0), t.getThirdObject());

        t = binner.getMinMaxBinValueValue(new Point2D.Double(0.01, 0));
        Assert.assertEquals(Double.valueOf(0), t.getFirstObject());
        Assert.assertEquals(Double.valueOf(0.1), t.getSecondObject());
        Assert.assertEquals(Double.valueOf(0), t.getThirdObject());

        t = binner.getMinMaxBinValueValue(new Point2D.Double(0.3, 0));
        Assert.assertEquals(Double.valueOf(0.3), t.getFirstObject());
        Assert.assertEquals(Double.valueOf(0.4), t.getSecondObject());
        Assert.assertEquals(Double.valueOf(0.3), t.getThirdObject());
    }
}
