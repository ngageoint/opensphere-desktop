package io.opensphere.csvcommon.detect.location.format.decider;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.csvcommon.detect.ListCellSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.LocationDetector;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.csvcommon.util.ColumnHeaders;
import io.opensphere.csvcommon.util.CsvTestUtils;
import io.opensphere.csvcommon.util.LocationTestUtils;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class LocationFormatDeciderTests.
 */
public class LocationFormatDeciderTest
{
    /**
     * Tests a header with multiple latitude/longitude type columns.
     */
    @Test
    public void testAdvancedLatLonData()
    {
        LocationDetector latlonDetector = new LocationDetector(LocationTestUtils.getPrefsRegistry());

        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf(' ');

        List<String> rows = CsvTestUtils.createMultipleDelimitedFormattedLatLonData(String.valueOf(delimiter));
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);

        ValuesWithConfidence<LocationResults> result = latlonDetector.detect(lcs);

        List<LatLonColumnResults> latLonResults = result.getBestValue().getLatLonResults();
        Assert.assertEquals(7.0f, latLonResults.size(), 0f);

        LatLonColumnResults res1 = latLonResults.get(0);
        Assert.assertTrue(res1.getLatColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LATITUDE.toString()));
        Assert.assertEquals(1f, res1.getLatColumn().getConfidence(), 0f);
        Assert.assertTrue(res1.getLatColumn().getType().equals(ColumnType.LAT));
        Assert.assertTrue(res1.getLonColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LONGITUDE.toString()));
        Assert.assertEquals(1f, res1.getLonColumn().getConfidence(), 0f);
        Assert.assertTrue(res1.getLonColumn().getType().equals(ColumnType.LON));

        LatLonColumnResults res2 = latLonResults.get(1);
        Assert.assertTrue(res2.getLatColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LATITUDE.toString()));
        Assert.assertEquals(1f, res2.getLatColumn().getConfidence(), 0f);
        Assert.assertTrue(res2.getLatColumn().getType().equals(ColumnType.LAT));
        Assert.assertTrue(res2.getLonColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LON.toString()));
        Assert.assertEquals(1f, res2.getLonColumn().getConfidence(), 0f);
        Assert.assertTrue(res2.getLonColumn().getType().equals(ColumnType.LON));

        LatLonColumnResults res3 = latLonResults.get(2);
        Assert.assertTrue(res3.getLatColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LAT_1.toString()));
        Assert.assertTrue(res3.getLatColumn().getConfidence() < 1.0f);
        Assert.assertTrue(res3.getLatColumn().getType().equals(ColumnType.LAT));
        Assert.assertTrue(res3.getLonColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LON_1.toString()));
        Assert.assertTrue(res3.getLonColumn().getConfidence() < 1.0f);
        Assert.assertTrue(res3.getLonColumn().getType().equals(ColumnType.LON));

        LatLonColumnResults res4 = latLonResults.get(3);
        Assert.assertTrue(res4.getLatColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.STATIONLAT1.toString()));
        Assert.assertTrue(res4.getLatColumn().getConfidence() < 1.0f);
        Assert.assertTrue(res4.getLatColumn().getType().equals(ColumnType.LAT));
        Assert.assertTrue(res4.getLonColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.STATIONLON1.toString()));
        Assert.assertTrue(res4.getLonColumn().getConfidence() < 1.0f);
        Assert.assertTrue(res4.getLonColumn().getType().equals(ColumnType.LON));

        LatLonColumnResults res5 = latLonResults.get(4);
        Assert.assertTrue(res5.getLatColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.STATIONLAT.toString()));
        Assert.assertTrue(res5.getLatColumn().getConfidence() < 1.0f);
        Assert.assertTrue(res5.getLatColumn().getType().equals(ColumnType.LAT));
        Assert.assertTrue(res5.getLonColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.STATIONLON.toString()));
        Assert.assertTrue(res5.getLonColumn().getConfidence() < 1.0f);
        Assert.assertTrue(res5.getLonColumn().getType().equals(ColumnType.LON));

        LatLonColumnResults res6 = latLonResults.get(5);
        Assert.assertTrue(res6.getLatColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LAT.toString()));
        Assert.assertEquals(1f, res6.getLatColumn().getConfidence(), 0f);
        Assert.assertTrue(res6.getLatColumn().getType().equals(ColumnType.LAT));
        Assert.assertTrue(res6.getLonColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LONGITUDE.toString()));
        Assert.assertEquals(1f, res6.getLonColumn().getConfidence(), 0f);
        Assert.assertTrue(res6.getLonColumn().getType().equals(ColumnType.LON));

        LatLonColumnResults res7 = latLonResults.get(6);
        Assert.assertTrue(res7.getLatColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LAT.toString()));
        Assert.assertEquals(1f, res7.getLatColumn().getConfidence(), 0f);
        Assert.assertTrue(res7.getLatColumn().getType().equals(ColumnType.LAT));
        Assert.assertTrue(res7.getLonColumn().getColumnName().equalsIgnoreCase(ColumnHeaders.LON.toString()));
        Assert.assertEquals(1f, res7.getLonColumn().getConfidence(), 0f);
        Assert.assertTrue(res7.getLonColumn().getType().equals(ColumnType.LON));

        // Gets the highest confidence in the set
        Assert.assertEquals(1f, result.getBestValue().getConfidence(), 0f);
    }

    /**
     * Test a header that contains at least 1 location column with valid lat/lon
     * values in the sample data.
     */
    @Test
    public void testLocationData()
    {
        LocationDetector latlonDetector = new LocationDetector(LocationTestUtils.getPrefsRegistry());

        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');

        List<String> rows = CsvTestUtils.createMultipleDelimitedLocationData(String.valueOf(delimiter), quote);
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);
        ValuesWithConfidence<LocationResults> result = latlonDetector.detect(lcs);

        List<PotentialLocationColumn> locationResults = result.getBestValue().getLocationResults();
        Assert.assertEquals(1.0f, locationResults.size(), 0f);
        Assert.assertTrue(locationResults.get(0).getColumnName().equalsIgnoreCase(ColumnHeaders.POSITION.toString()));
        Assert.assertTrue(locationResults.get(0).getType().equals(ColumnType.POSITION));
        Assert.assertEquals(1f, locationResults.get(0).getConfidence(), 0f);
    }

    /**
     * Test a header that contains a MGRS column with less than 100% of valid
     * sample data. In this case, data that is outside the MGRS grid north/south
     * limits will not be valid.
     */
    @Test
    public void testMGRSData()
    {
        LocationDetector latlonDetector = new LocationDetector(LocationTestUtils.getPrefsRegistry());

        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf(' ');

        List<String> rows = CsvTestUtils.createMGRSDelimitedData(String.valueOf(delimiter));
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);
        ValuesWithConfidence<LocationResults> result = latlonDetector.detect(lcs);

        Assert.assertEquals(1.0f, result.getBestValue().getLocationResults().size(), 0f);
        PotentialLocationColumn plc = result.getBestValue().getLocationResults().get(0);

        Assert.assertTrue(plc.getType().equals(ColumnType.MGRS));
        Assert.assertTrue(plc.getLocationFormat().equals(CoordFormat.MGRS));
        Assert.assertEquals(1.0f, plc.getConfidence(), 0f);
    }

    /**
     * Test a header that contains a WKT geometry column with sample data that
     * contains 100% valid WKT geometries.
     */
    @Test
    public void testWKTGeometryData()
    {
        LocationDetector latlonDetector = new LocationDetector(LocationTestUtils.getPrefsRegistry());

        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');

        List<String> rows = CsvTestUtils.createWktDelimitedData(String.valueOf(delimiter));
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);
        ValuesWithConfidence<LocationResults> result = latlonDetector.detect(lcs);

        Assert.assertEquals(1.0f, result.getBestValue().getLocationResults().size(), 0f);
        PotentialLocationColumn plc = result.getBestValue().getLocationResults().get(0);

        Assert.assertTrue(plc.getType().equals(ColumnType.WKT_GEOMETRY));
        Assert.assertTrue(plc.getLocationFormat().equals(CoordFormat.WKT_GEOMETRY));
        Assert.assertEquals(1.0f, plc.getConfidence(), 0f);
    }
}
