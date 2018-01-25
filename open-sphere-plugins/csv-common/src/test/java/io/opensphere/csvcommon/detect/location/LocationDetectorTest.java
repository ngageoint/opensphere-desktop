package io.opensphere.csvcommon.detect.location;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.csvcommon.detect.ListCellSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.LocationDetector;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.util.CsvTestUtils;
import io.opensphere.csvcommon.util.LocationTestUtils;

/**
 * Tests different location columns and formats.
 */
public class LocationDetectorTest
{
    /** Tests multiple individual latitude and longitude columns. */
    @Test
    public void testAdvancedLocationData()
    {
        LocationDetector latlonDetector = new LocationDetector(LocationTestUtils.getPrefsRegistry());

        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');

        List<String> rows = CsvTestUtils.createMultipleDelimitedLatLonData(String.valueOf(delimiter));
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);

        ValuesWithConfidence<LocationResults> result = latlonDetector.detect(lcs);
        Assert.assertEquals(1f, result.getBestValue().getConfidence(), 0f);
    }

    /**
     * Test multiple delimited location data.
     */
    @Test
    public void testMultipleDelimitedLocationData()
    {
        LocationDetector latlonDetector = new LocationDetector(LocationTestUtils.getPrefsRegistry());

        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');

        List<String> rows = CsvTestUtils.createMultipleDelimitedLocationData(String.valueOf(delimiter), quote);
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);

        ValuesWithConfidence<LocationResults> result = latlonDetector.detect(lcs);
        Assert.assertEquals(1f, result.getBestValue().getConfidence(), 0f);
    }

    /** Test data that contains no latitude or longitude columns. */
    @Test
    public void testNoLocationData()
    {
        LocationDetector latlonDetector = new LocationDetector(LocationTestUtils.getPrefsRegistry());

        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');

        List<String> rows = CsvTestUtils.createNoLocationData(String.valueOf(delimiter));
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);

        ValuesWithConfidence<LocationResults> result = latlonDetector.detect(lcs);
        Assert.assertEquals(0f, result.getBestValue().getConfidence(), 0f);
    }
}
