package io.opensphere.analysis.base.controller;

import java.util.function.Function;

import io.opensphere.analysis.base.model.UIBin;
import io.opensphere.analysis.binning.algorithm.UniqueValueBinner;
import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.mantle.data.element.DataElement;

/** Unique value binner for UIs. */
public class UIUniqueValueBinner extends UniqueValueBinner<DataElement>
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
    public UIUniqueValueBinner(UniqueCriteria criteria, Function<DataElement, ?> dataToValue,
            Function<Bin<DataElement>, UIBin> binCreator)
    {
        super(criteria, dataToValue);
        myBinCreator = binCreator;
    }

    @Override
    protected Bin<DataElement> createBin(DataElement data)
    {
        return myBinCreator.apply(super.createBin(data));
    }
}
