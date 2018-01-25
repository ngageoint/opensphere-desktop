package io.opensphere.csvcommon.format.position;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.BaseSingleCellSampler;

/**
 * The sampler to use to help detect the format for longitude data.
 *
 */
public class LongitudeCellSampler extends BaseSingleCellSampler
{
    /**
     * The header cell to suggest to the detector that the data is longitude.
     */
    private static final List<String> ourHeaders = New.list("LON", "LAT");

    /**
     * Constructs a longitude cell sampler.
     *
     * @param data The longitude data to sample.
     */
    public LongitudeCellSampler(List<String> data)
    {
        super(data, true);
    }

    @Override
    public List<? extends String> getHeaderCells()
    {
        return ourHeaders;
    }
}
