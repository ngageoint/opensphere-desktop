package io.opensphere.csvcommon.format.datetime;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.format.datetime.DateTimeCellSampler;

/**
 * Tests the DateTimeCellSampler class.
 *
 */
public class DateTimeCellSamplerTest
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

        DateTimeCellSampler sampler = new DateTimeCellSampler(testData);
        List<? extends List<? extends String>> samples = sampler.getBeginningSampleCells();

        assertEquals(testData.size(), samples.size());

        for (int i = 0; i < samples.size(); i++)
        {
            List<? extends String> row = samples.get(i);
            assertEquals(1, row.size());

            assertEquals(String.valueOf(i), row.get(0));
        }
    }

    /**
     * Tests getting the header cell.
     */
    @Test
    public void testHeaderCells()
    {
        DateTimeCellSampler sampler = new DateTimeCellSampler(New.<String>list());

        List<? extends String> headers = sampler.getHeaderCells();

        assertEquals(1, headers.size());
        assertEquals("Date", headers.get(0));
    }
}
