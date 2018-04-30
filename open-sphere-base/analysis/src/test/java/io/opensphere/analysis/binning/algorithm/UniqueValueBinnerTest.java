package io.opensphere.analysis.binning.algorithm;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.bins.UniqueValueBin;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;

/**
 * Tests for {@link UniqueValueBinner}.
 */
public class UniqueValueBinnerTest
{
    /**
     * Tests automatic binning.
     * <p>
     * The idea is that, even though constructed Doubles are different objects,
     * their actual values will not be unique.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testAutoBin()
    {
        UniqueCriteria criteria = new UniqueCriteria();
        UniqueValueBinner<Double> binner = new UniqueValueBinner<>(criteria, d -> d.toString());
        binner.add(new Double(0));
        binner.add(new Double(10));
        binner.add(new Double(0.0));
        binner.add(new Double(10.00));
        binner.add(new Double(34));

        Assert.assertEquals(3, binner.getBinsMap().size());

        Assert.assertEquals(2, binner.getBinsMap().get("0.0").getSize());
        Assert.assertEquals(0., binner.getBinsMap().get("0.0").getData().get(0).doubleValue(), 0.001);
        Assert.assertEquals(0., binner.getBinsMap().get("0.0").getData().get(1).doubleValue(), 0.001);

        Assert.assertEquals(2, binner.getBinsMap().get("10.0").getSize());
        Assert.assertEquals(10., binner.getBinsMap().get("10.0").getData().get(0).doubleValue(), 0.001);
        Assert.assertEquals(10., binner.getBinsMap().get("10.0").getData().get(1).doubleValue(), 0.001);

        Assert.assertEquals(1, binner.getBinsMap().get("34.0").getSize());
        Assert.assertEquals(34., binner.getBinsMap().get("34.0").getData().get(0).doubleValue(), 0.001);

        binner.remove(new Double(34));

//        Assert.assertEquals(0, binner.getBins().get(2).getSize());
    }

    /** Tests custom binning. */
    @SuppressWarnings("deprecation")
    @Test
    public void testCustomBin()
    {
        Function<Double, String> valueFunction = d -> d.toString();
        List<Bin<Double>> bins = Arrays.asList(new UniqueValueBin<>("0.0", valueFunction),
                new UniqueValueBin<>("10.0", valueFunction));
        UniqueValueBinner<Double> binner = new UniqueValueBinner<>(bins);
        binner.add(new Double(0));
        binner.add(new Double(10));
        binner.add(new Double(20));

        Assert.assertEquals(2, binner.getBins().size());
        Assert.assertEquals(0., binner.getBins().get(0).getData().get(0).doubleValue(), 0.001);
        Assert.assertEquals(10., binner.getBins().get(1).getData().get(0).doubleValue(), 0.001);
    }
}
