package io.opensphere.analysis.binning.algorithm;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.bins.UniqueValueBin;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;

/**
 * Bins generic unique value data.
 *
 * @param <T> the type of data to bin
 */
public class UniqueValueBinner<T> extends AbstractBinner<T>
{
    /** The bin criteria. */
    private final UniqueCriteria myCriteria;

    /** The function to get the value for comparison. */
    private final Function<T, ?> myToValueFunction;

    /**
     * Creates a binner that bins based on the given criteria.
     *
     * @param criteria The bin criteria
     * @param toValueFunction The function to get the value for comparison
     */
    public UniqueValueBinner(UniqueCriteria criteria, Function<T, ?> toValueFunction)
    {
        super();
        myCriteria = criteria;
        myToValueFunction = toValueFunction;
    }

    /**
     * Creates a binner with predefined bins. No new bins are created.
     *
     * @param bins the predefined bins to use
     */
    public UniqueValueBinner(List<Bin<T>> bins)
    {
        super(bins);
        myCriteria = null;
        myToValueFunction = null;
    }

    @Override
    protected Bin<T> createBin(T data)
    {
        UniqueValueBin<T> bin = null;
        if (!isCustomBins())
        {
            Object value = getBinValue(data);
            if (value != null || isCreateNABin())
            {
                bin = new UniqueValueBin<>(value, myToValueFunction);
            }
        }
        return bin;
    }

    @Override
    protected List<Bin<T>> createEmptyBins(int index, Bin<T> bin)
    {
        return Collections.emptyList();
    }

    @Override
    protected boolean isCustomBins()
    {
        return myCriteria == null;
    }

    @Override
    protected Object getBinValue(T data)
    {
        return myToValueFunction.apply(data);
    }
}
