package io.opensphere.csvcommon.format.position;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.BaseSingleCellSampler;

/**
 * The sampler to use to help detect the format for latitude/longitude data.
 *
 */
public class PositionCellSampler extends BaseSingleCellSampler
{
    /**
     * The header cell to suggest to the detector that the data is latitude.
     */
    private static final List<String> ourHeaderCells = New.list("Location");

    /**
     * Constructs a position cell sampler.
     *
     * @param data The position data to sample.
     */
    public PositionCellSampler(List<String> data)
    {
        super(data, false);
    }

    @Override
    public List<? extends String> getHeaderCells()
    {
        return ourHeaderCells;
    }
}
