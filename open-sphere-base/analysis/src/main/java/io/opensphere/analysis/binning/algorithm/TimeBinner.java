package io.opensphere.analysis.binning.algorithm;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.bins.TimeBin;
import io.opensphere.analysis.binning.criteria.TimeCriteria;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.collections.New;

/**
 * Bins generic time value data.
 *
 * @param <T> the type of data to bin
 */
public class TimeBinner<T> extends AbstractBinner<T>
{
    /** The bin criteria. */
    private final TimeCriteria myCriteria;

    /** The function to convert data => value. */
    private final Function<T, Date> myDataToValue;

    /**
     * Creates a binner that bins based on the given criteria.
     *
     * @param criteria The bin criteria
     * @param dataToValue The function to convert data => value
     */
    public TimeBinner(TimeCriteria criteria, Function<T, Date> dataToValue)
    {
        super();
        myCriteria = criteria;
        myDataToValue = dataToValue;
    }

    /**
     * Creates a binner with predefined bins. No new bins are created.
     *
     * @param bins the predefined bins to use
     */
    public TimeBinner(List<Bin<T>> bins)
    {
        super(bins);
        myCriteria = null;
        myDataToValue = null;
    }

    @Override
    protected Bin<T> createBin(T data)
    {
        TimeBin<T> bin = null;
        if (!isCustomBins())
        {
            Date sampleValue = myDataToValue.apply(data);
            if (sampleValue != null || isCreateNABin())
            {
                bin = new TimeBin<>(myCriteria.getBinType(), sampleValue, myDataToValue);
            }
        }
        return bin;
    }

    @Override
    protected List<Bin<T>> createEmptyBins(int index, Bin<T> bin)
    {
        // TODO don't create empty bin that already exists

        List<Bin<T>> emptyBins = Collections.emptyList();
        final int maxBinCount = 1000;

        if (!myCriteria.getBinType().isRange() || getBins().size() > maxBinCount)
        {
            return emptyBins;
        }

        boolean haveCatchAll = getBins().stream().anyMatch(b -> b.getValueObject() == null);
        int normalBinCount = haveCatchAll ? getBins().size() - 1 : getBins().size();
        if (normalBinCount > 1)
        {
            TimeBin<T> startBin = null;
            TimeBin<T> endBin = null;
            // This code assumes the catch all bin, if present, is at the end
            if (index == 0)
            {
                startBin = (TimeBin<T>)bin.getBin();
                endBin = (TimeBin<T>)getBins().get(index + 1).getBin();
            }
            else if (index == normalBinCount - 1)
            {
                startBin = (TimeBin<T>)getBins().get(index - 1).getBin();
                endBin = (TimeBin<T>)bin.getBin();
            }

            // startBin and endBin are null or non-null both but Eclipse is
            // complaining
            if (startBin != null && endBin != null)
            {
                emptyBins = New.list();
                TimeInstant start = TimeInstant.get(startBin.getBinType().getMax(startBin.getDate()));
                TimeInstant end = TimeInstant.get(endBin.getBinType().getMin(endBin.getDate()));
                Duration increment = myCriteria.getBinType().getDuration();
                TimeInstant max;
                for (TimeInstant min = start; min.isBefore(end); min = max)
                {
                    if (emptyBins.size() > maxBinCount)
                    {
                        break;
                    }

                    max = min.plus(increment);
                    emptyBins.add(new TimeBin<>(myCriteria.getBinType(), min.toDate(), myDataToValue));
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
        return TimeBin.getValue(myDataToValue.apply(data), myCriteria.getBinType());
    }

    /**
     * Gets the criteria.
     *
     * @return the criteria
     */
    protected TimeCriteria getCriteria()
    {
        return myCriteria;
    }

    /**
     * Gets the dataToValue.
     *
     * @return the dataToValue
     */
    protected Function<T, Date> getDataToValue()
    {
        return myDataToValue;
    }
}
