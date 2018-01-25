package io.opensphere.csvcommon.format.position;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.BaseSingleCellSampler;

/**
 * The sampler to use to help detect the format for latitude data.
 *
 */
public class LatitudeCellSampler extends BaseSingleCellSampler
{
    /**
     * The header cell to suggest to the detector that the data is latitude.
     */
    private static final List<String> ourHeaderCells = New.list("LAT", "LON");

    /**
     * Constructs a latitude cell sampler.
     *
     * @param data The latitude data to sample.
     */
    public LatitudeCellSampler(List<String> data)
    {
        super(data, true);
    }

    @Override
    public List<? extends String> getHeaderCells()
    {
        return ourHeaderCells;
    }
}
