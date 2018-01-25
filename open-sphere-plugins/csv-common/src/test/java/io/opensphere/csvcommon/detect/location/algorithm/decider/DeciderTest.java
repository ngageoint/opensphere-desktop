package io.opensphere.csvcommon.detect.location.algorithm.decider;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.csvcommon.detect.ListCellSampler;
import io.opensphere.csvcommon.detect.location.algorithm.decider.LatLonDecider;
import io.opensphere.csvcommon.detect.location.algorithm.decider.LocationDecider;
import io.opensphere.csvcommon.detect.location.algorithm.decider.MGRSDecider;
import io.opensphere.csvcommon.detect.location.algorithm.decider.PositionDecider;
import io.opensphere.csvcommon.detect.location.algorithm.decider.WktDecider;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.util.CsvTestUtils;
import io.opensphere.csvcommon.util.LocationTestUtils;

/**
 * The DeciderTests class tests each of the location column deciders with valid
 * header data and headers without location data.
 */
public class DeciderTest
{
    /**
     * Test test decides if there is at least one lat/lon pair of columns with a
     * confidence of 1.0.
     */
    @Test
    public void testLatLonDecider()
    {
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf(' ');
        List<String> rows = CsvTestUtils.createMultipleDelimitedLatLonData(String.valueOf(delimiter));
        ListCellSampler sampler = new ListCellSampler(rows, delimiter, quote);
        LatLonDecider decider = new LatLonDecider(LocationTestUtils.getPrefsRegistry());
        LocationResults result = decider.determineLocationColumns(sampler);
        Assert.assertEquals(1f, result.getConfidence(), 0f);

        testNoLocationData(delimiter, quote, decider);
    }

    /**
     * This test decides if there is at least one position column with a
     * confidence of 1.0.
     */
    @Test
    public void testPositionDecider()
    {
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');
        List<String> rows = CsvTestUtils.createMultipleDelimitedLocationData(String.valueOf(delimiter), quote);
        ListCellSampler sampler = new ListCellSampler(rows, delimiter, quote);
        PositionDecider decider = new PositionDecider(LocationTestUtils.getPrefsRegistry());
        LocationResults result = decider.determineLocationColumn(sampler);
        Assert.assertEquals(1f, result.getConfidence(), 0f);

        testNoLocationData(delimiter, quote, decider);
    }

    /**
     * This test decides if there is at least one MGRS column with a confidence
     * of 1.0.
     */
    @Test
    public void testMGRSDecider()
    {
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf(' ');
        List<String> rows = CsvTestUtils.createMGRSDelimitedData(String.valueOf(delimiter));
        ListCellSampler sampler = new ListCellSampler(rows, delimiter, quote);

        MGRSDecider decider = new MGRSDecider(LocationTestUtils.getPrefsRegistry());
        LocationResults result = decider.determineLocationColumns(sampler);
        Assert.assertEquals(1f, result.getConfidence(), 0f);

        testNoLocationData(delimiter, quote, decider);
    }

    /**
     * This test decides if there is at least one WKT column with a confidence
     * of 1.0.
     */
    @Test
    public void testWktDecider()
    {
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');
        List<String> rows = CsvTestUtils.createWktDelimitedData(String.valueOf(delimiter));
        ListCellSampler sampler = new ListCellSampler(rows, delimiter, quote);
        WktDecider decider = new WktDecider(LocationTestUtils.getPrefsRegistry());
        LocationResults result = decider.determineLocationColumns(sampler);
        Assert.assertEquals(1f, result.getConfidence(), 0f);

        testNoLocationData(delimiter, quote, decider);
    }

    /**
     * Test header without location data.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @param decider the decider
     */
    private void testNoLocationData(Character delimiter, Character quote, LocationDecider decider)
    {
        List<String> rows = CsvTestUtils.createNoLocationData(String.valueOf(delimiter));
        ListCellSampler sampler = new ListCellSampler(rows, delimiter, quote);
        LocationResults result = decider.determineLocationColumns(sampler);
        Assert.assertEquals(0f, result.getConfidence(), 0f);
    }
}
