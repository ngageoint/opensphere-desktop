package io.opensphere.analysis.base.controller;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.opensphere.analysis.base.model.UIBin;
import io.opensphere.analysis.binning.algorithm.RangeBinner;
import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.util.DataTypeUtilities;
import io.opensphere.mantle.data.element.DataElement;

/** Range binner for UIs. */
public class UIRangeBinner extends RangeBinner<DataElement>
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
    public UIRangeBinner(RangeCriteria criteria, Function<DataElement, ?> dataToValue,
            Function<Bin<DataElement>, UIBin> binCreator)
    {
        super(criteria, dataToValue, DataTypeUtilities::toDouble, DataTypeUtilities::fromDouble);
        myBinCreator = binCreator;
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
}
