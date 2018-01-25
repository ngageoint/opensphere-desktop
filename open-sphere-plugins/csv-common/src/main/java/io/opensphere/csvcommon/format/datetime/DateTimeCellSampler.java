package io.opensphere.csvcommon.format.datetime;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.BaseSingleCellSampler;

/**
 * The cell sampler used by the date formatters to figure out a set of value's
 * formats.
 */
public class DateTimeCellSampler extends BaseSingleCellSampler
{
    /**
     * The header cell to return to possibly help suggest to the detector that
     * this is a date... really it is.
     */
    private static final List<? extends String> ourHeaderCells = New.list("Date");

    /**
     * Constructs a new sampler.
     *
     * @param data The data to be sampled.
     */
    public DateTimeCellSampler(List<String> data)
    {
        super(data, false);
    }

    @Override
    public List<? extends String> getHeaderCells()
    {
        return ourHeaderCells;
    }
}
