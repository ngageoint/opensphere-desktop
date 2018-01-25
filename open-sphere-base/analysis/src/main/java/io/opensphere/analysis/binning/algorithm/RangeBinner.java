package io.opensphere.analysis.binning.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.bins.RangeBin;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreeTuple;

/**
 * Bins generic range data.
 *
 * @param <T> the type of data to bin
 */
public class RangeBinner<T> extends AbstractBinner<T>
{
    /** The bin criteria. */
    private final RangeCriteria myCriteria;

    /** The function to convert data => value. */
    private final Function<T, ?> myDataToValue;

    /** The function to convert value => double. */
    private final ToDoubleFunction<Object> myValueToDouble;

    /** The function to convert double => value. */
    private final BiFunction<Double, Class<?>, Object> myDoubleToValue;

    /**
     * Creates a binner that bins based on the given criteria.
     *
     * @param criteria The bin criteria
     * @param dataToValue The function to convert data => value
     * @param valueToDouble The function to convert value => double
     * @param doubleToValue The function to convert double => value
     */
    public RangeBinner(RangeCriteria criteria, Function<T, ?> dataToValue, ToDoubleFunction<Object> valueToDouble,
            BiFunction<Double, Class<?>, Object> doubleToValue)
    {
        super();
        myCriteria = criteria;
        myDataToValue = dataToValue;
        myValueToDouble = valueToDouble;
        myDoubleToValue = doubleToValue;
    }

    /**
     * Creates a binner with predefined bins. No new bins are created.
     *
     * @param bins the predefined bins to use
     */
    public RangeBinner(List<Bin<T>> bins)
    {
        super(bins);
        myCriteria = null;
        myDataToValue = null;
        myValueToDouble = null;
        myDoubleToValue = null;
    }

    @Override
    protected Bin<T> createBin(T data)
    {
        RangeBin<T> bin = null;
        if (!isCustomBins())
        {
            ThreeTuple<Double, Double, Object> minMaxBin = getMinMaxBinValueValue(data);
            if (minMaxBin != null)
            {
                bin = new RangeBin<>(minMaxBin.getFirstObject().doubleValue(), minMaxBin.getSecondObject().doubleValue(),
                        minMaxBin.getThirdObject(), myDataToValue, myValueToDouble);
            }
            else if (isCreateNABin())
            {
                bin = new RangeBin<>(0, 0, null, myDataToValue, null);
            }
        }
        return bin;
    }

    @Override
    protected List<Bin<T>> createEmptyBins(int index, Bin<T> bin)
    {
        final int maxBinCount = 1000;
        List<Bin<T>> emptyBins = Collections.emptyList();

        if (getBins().size() > maxBinCount)
        {
            return emptyBins;
        }

        boolean haveCatchAll = getBins().stream().anyMatch(b -> b.getValueObject() == null);
        int normalBinCount = haveCatchAll ? getBins().size() - 1 : getBins().size();
        if (normalBinCount > 1)
        {
            RangeBin<T> startBin = null;
            RangeBin<T> endBin = null;
            // This code assumes the catch all bin, if present, is at the end
            if (index == 0)
            {
                startBin = (RangeBin<T>)bin.getBin();
                endBin = (RangeBin<T>)getBins().get(index + 1).getBin();
            }
            else if (index == normalBinCount - 1)
            {
                startBin = (RangeBin<T>)getBins().get(index - 1).getBin();
                endBin = (RangeBin<T>)bin.getBin();
            }

            if (startBin != null)
            {
                emptyBins = New.list();
                double increment = round(startBin.getMax() - startBin.getMin());
                double max;
                for (double min = startBin.getMax(); min < endBin.getMin(); min = max)
                {
                    if (emptyBins.size() > maxBinCount)
                    {
                        break;
                    }

                    Object binValue = myDoubleToValue.apply(Double.valueOf(min), startBin.getValueObject().getClass());
                    max = round(min + increment);
                    emptyBins.add(new RangeBin<>(min, max, binValue, myDataToValue, myValueToDouble));
                }
            }
        }
        return emptyBins;
    }

    @Override
    protected boolean isCustomBins()
    {
        return myCriteria == null;
    }

    @Override
    protected Object getBinValue(T data)
    {
        ThreeTuple<Double, Double, Object> minMaxBin = getMinMaxBinValueValue(data);
        Object binValue = null;
        if (minMaxBin != null)
        {
            binValue = minMaxBin.getThirdObject();
        }
        return binValue;
    }

    /**
     * Gets the min max and bin value for the data.
     *
     * @param data The data to get the values. for.
     * @return The min max and bin value, or null.
     */
    ThreeTuple<Double, Double, Object> getMinMaxBinValueValue(T data)
    {
        ThreeTuple<Double, Double, Object> minMaxBin = null;
        Object sampleValue = myDataToValue.apply(data);
        if (sampleValue != null)
        {
            double sampleDoubleValue = myValueToDouble.applyAsDouble(sampleValue);
            double min = round(Math.floor(round(sampleDoubleValue / myCriteria.getBinWidth())) * myCriteria.getBinWidth());
            double max = round(min + myCriteria.getBinWidth());
            Object binValue = myDoubleToValue.apply(Double.valueOf(min), sampleValue.getClass());
            minMaxBin = new ThreeTuple<>(Double.valueOf(min), Double.valueOf(max), binValue);
        }

        return minMaxBin;
    }

    /**
     * Rounds (cleans up) a double value.
     *
     * @param val the value
     * @return the rounded value
     */
    private static double round(double val)
    {
        return new BigDecimal(val).setScale(10, RoundingMode.HALF_UP).doubleValue();
    }
}
