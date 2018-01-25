package io.opensphere.csvcommon.format.position;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.position.LongitudeCellSampler;

/**
 * Tests the LongitudeCellSampler class.
 *
 */
public class LongitudeCellSamplerTest
{
    /**
     * Tests the getHeaderCells.
     */
    @Test
    public void testGetHeaderCells()
    {
        LongitudeCellSampler sampler = new LongitudeCellSampler(New.<String>list());
        List<? extends String> headers = sampler.getHeaderCells();

        assertEquals(2, headers.size());
        assertEquals("LON", headers.get(0));
        assertEquals("LAT", headers.get(1));
    }
}
