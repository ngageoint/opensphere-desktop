package io.opensphere.analysis.base.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.opensphere.analysis.base.model.UIBin;
import io.opensphere.analysis.binning.algorithm.TimeBinner;
import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.bins.TimeBin;
import io.opensphere.analysis.binning.criteria.TimeBinType;
import io.opensphere.analysis.binning.criteria.TimeCriteria;
import io.opensphere.core.model.IntegerRange;
import io.opensphere.mantle.data.element.DataElement;

/** Time binner for UIs. */
public class UITimeBinner extends TimeBinner<DataElement>
{
    /** The bin creator. */
    private final Function<Bin<DataElement>, UIBin> myBinCreator;

    /**
     * Constructor.
     *
     * @param criteria The bin criteria
     * @param dataToValue The function to convert data => value
     * @param binCreator The bin creator
     */
    public UITimeBinner(TimeCriteria criteria, Function<DataElement, Date> dataToValue,
            Function<Bin<DataElement>, UIBin> binCreator)
    {
        super(criteria, dataToValue);
        myBinCreator = binCreator;
    }

    @Override
    public Bin<DataElement> add(DataElement data)
    {
        if (getBinsMap().isEmpty())
        {
            createInitialBins();
        }
        return super.add(data);
    }

    @Override
    protected Bin<DataElement> createBin(DataElement data)
    {
        return myBinCreator.apply(super.createBin(data));
    }

    @Override
    protected List<Bin<DataElement>> createEmptyBins(int index, Bin<DataElement> bin)
    {
        return super.createEmptyBins(index, bin).stream().map(myBinCreator).collect(Collectors.toList());
    }

    /**
     * Creates any necessary initial bins.
     */
    private void createInitialBins()
    {
        if (isCreateEmptyBins())
        {
            TimeBinType timeBinType = getCriteria().getBinType();
            if (timeBinType.isPeriod())
            {
                IntegerRange range = timeBinType.getPeriodicRange();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(0);
                for (int val = range.getMinValue(); val <= range.getMaxValue(); val++)
                {
                    cal.set(timeBinType.getPeriodField(), val);
                    addBin(myBinCreator.apply(new TimeBin<>(timeBinType, cal.getTime(), getDataToValue())));
                }
            }
        }
    }
}
