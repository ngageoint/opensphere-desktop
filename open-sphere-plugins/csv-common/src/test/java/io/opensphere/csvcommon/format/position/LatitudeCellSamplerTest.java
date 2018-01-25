package io.opensphere.csvcommon.format.position;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.position.LatitudeCellSampler;

/**
 * Tests the LatitudeCellSampler class.
 *
 */
public class LatitudeCellSamplerTest
{
    /**
     * Tests getting the sample data.
     */
    @Test
    public void testGetBeginningSampleCells()
    {
        List<String> testData = New.list();
        for (int i = 0; i < 100; i++)
        {
            testData.add(String.valueOf(i));
        }

        LatitudeCellSampler sampler = new LatitudeCellSampler(testData);
        List<? extends List<? extends String>> samples = sampler.getBeginningSampleCells();

        assertEquals(testData.size(), samples.size());

        for (int i = 0; i < samples.size(); i++)
        {
            List<? extends String> row = samples.get(i);
            assertEquals(2, row.size());

            assertEquals(String.valueOf(i), row.get(0));
            assertEquals("0", row.get(1));
        }
    }

    /**
     * Tests the getHeaderCells.
     */
    @Test
    public void testGetHeaderCells()
    {
        LatitudeCellSampler sampler = new LatitudeCellSampler(New.<String>list());
        List<? extends String> headers = sampler.getHeaderCells();

        assertEquals(2, headers.size());
        assertEquals("LAT", headers.get(0));
        assertEquals("LON", headers.get(1));
    }
}
