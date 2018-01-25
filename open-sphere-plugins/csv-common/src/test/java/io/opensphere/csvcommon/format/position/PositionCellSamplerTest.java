package io.opensphere.csvcommon.format.position;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.position.PositionCellSampler;

/**
 * Tests the PositionCellSampler class.
 *
 */
public class PositionCellSamplerTest
{
    /**
     * Tests the getHeaderCells.
     */
    @Test
    public void testGetHeaderCells()
    {
        PositionCellSampler sampler = new PositionCellSampler(New.<String>list());
        List<? extends String> headers = sampler.getHeaderCells();

        assertEquals(1, headers.size());
        assertEquals("Location", headers.get(0));
    }
}
