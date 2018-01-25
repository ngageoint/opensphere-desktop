package io.opensphere.csvcommon.detect.location.algorithm;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.detect.ListCellSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.algorithm.DMSMatchMaker;
import io.opensphere.csvcommon.detect.location.algorithm.DecimalLatLonMatchMaker;
import io.opensphere.csvcommon.detect.location.algorithm.MGRSMatchMaker;
import io.opensphere.csvcommon.detect.location.algorithm.PositionMatchMaker;
import io.opensphere.csvcommon.detect.location.algorithm.WktMatchMaker;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.csvcommon.util.CsvTestUtils;
import io.opensphere.importer.config.ColumnType;
import org.junit.Assert;

/**
 * The MatchMakerTests will test the ability of the match makers to recognize
 * data values and assign a corresponding type without a column header name.
 */
public class MatchMakerTest
{
    /**
     * Tests a set of data that has multiple lat/lon columns and decimal as well
     * as DMS lat/lon values.
     */
    @Test
    public void testDecimalLatLonMatchMaker()
    {
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf(' ');
        List<String> rows = CsvTestUtils.createMultipleDelimitedFormattedLatLonData(String.valueOf(delimiter));
        ListCellSampler sampler = new ListCellSampler(rows, delimiter, quote);
        DecimalLatLonMatchMaker dllMM = new DecimalLatLonMatchMaker();
        ValuesWithConfidence<LocationResults> results = dllMM.detect(sampler);
        List<LatLonColumnResults> colResults = results.getBestValue().getLatLonResults();
        Assert.assertEquals(5.0f, colResults.size(), 0f);
        Assert.assertEquals(.86f, colResults.get(0).getConfidence(), 6.7e-3);
        Assert.assertTrue(colResults.get(0).getLatColumn().getType().equals(ColumnType.LAT));
        Assert.assertTrue(colResults.get(0).getLonColumn().getType().equals(ColumnType.LON));
        Assert.assertEquals(.6f, colResults.get(1).getConfidence(), 0f);
        Assert.assertEquals(.53f, colResults.get(2).getConfidence(), 3.4e-3);
        Assert.assertEquals(.46f, colResults.get(3).getConfidence(), 6.7e-3);
        Assert.assertEquals(.19f, colResults.get(4).getConfidence(), 1e-2);
    }

    /**
     * Tests the position match maker. The position column data in this example
     * is surrounded by parenthesis, in lon/lat order and separated by a space.
     * ex: '(lonValue latValue)'
     */
    @Test
    public void testPositionMatchMaker()
    {
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');
        List<String> rows = CsvTestUtils.createMultipleDelimitedLocationData(String.valueOf(delimiter), quote);
        ListCellSampler sampler = new ListCellSampler(rows, delimiter, quote);

        PositionMatchMaker pmm = new PositionMatchMaker();
        ValuesWithConfidence<LocationResults> results = pmm.detect(sampler);
        Assert.assertEquals(1f, results.getBestConfidence(), 0f);
        List<LatLonColumnResults> locResults = results.getBestValue().getLatLonResults();

        Assert.assertEquals(1.0f, locResults.size(), 0f);
        LatLonColumnResults llcr = locResults.get(0);
        Assert.assertEquals(1.0f, llcr.getConfidence(), 0f);
        Assert.assertTrue(llcr.getLatColumn().getType().equals(ColumnType.LAT));
        Assert.assertTrue(llcr.getLonColumn().getType().equals(ColumnType.LON));
    }

    /**
     * This test decides if there is at least one MGRS column with a confidence
     * of 1.0.
     */
    @Test
    public void testMGRSMatchMaker()
    {
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');
        List<String> rows = CsvTestUtils.createMGRSDelimitedData(String.valueOf(delimiter));
        ListCellSampler sampler = new ListCellSampler(rows, delimiter, quote);

        MGRSMatchMaker mmm = new MGRSMatchMaker();
        ValuesWithConfidence<LocationResults> results = mmm.detect(sampler);
        Assert.assertEquals(.91f, results.getBestValue().getConfidence(), 0f);

        // Tests data with no MGRS column
        rows = CsvTestUtils.createNoLocationData(String.valueOf(delimiter));
        sampler = new ListCellSampler(rows, delimiter, quote);
        results = mmm.detect(sampler);
        Assert.assertEquals(0f, results.getBestValue().getConfidence(), 0f);
    }

    /**
     * This test decides if there is at least one column that contains valid DMS
     * values.
     */
    @Test
    public void testDMSMatchMaker()
    {
        DMSMatchMaker dmsMM = new DMSMatchMaker();

        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf(' ');

        List<String> rows = CsvTestUtils.createMultipleDelimitedFormattedLatLonData(String.valueOf(delimiter));
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);

        ValuesWithConfidence<LocationResults> result = dmsMM.detect(lcs);
        LocationResults lr = result.getBestValue();

        List<LatLonColumnResults> locResults = lr.getLatLonResults();
        Assert.assertEquals(1.0f, locResults.size(), 0f);

        PotentialLocationColumn latColumn = locResults.get(0).getLatColumn();
        Assert.assertTrue(latColumn.getColumnName().equals(ColumnType.LAT.toString()));
        Assert.assertTrue(latColumn.getLatFormat().equals(CoordFormat.DMS));

        PotentialLocationColumn lonColumn = locResults.get(0).getLonColumn();
        Assert.assertTrue(lonColumn.getColumnName().equals(ColumnType.LON.toString()));
        Assert.assertTrue(lonColumn.getLonFormat().equals(CoordFormat.DMS));
    }

    /**
     * This test will test DMS data that has different formats.
     */
    @Test
    public void testDMSLatLonStrings()
    {
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf(' ');

        DMSMatchMaker dmsMM = new DMSMatchMaker();

        List<String> latStrings = New.list("15°07'45.23\"S", "S15°07'45.23", "15°07'45.23\"N", "N15°07'45.23", "-15°07'45.23",
                "21°33'36.37\"N", "-21°33'36.37", "N063430'0.123", "634300S", "S412628", "412628N");
        List<String> lonStrings = New.list("98°50'19.42\"E", "E98°50'19.42", "159°00'11.91\"W", "W159°00'11.91", "-159°00'11.91",
                "41°44'51.97\"E", "-41°44'51.97", "E063430'0.123", "0634300E", "W0412628", "0412628W");

        List<String> dmsData = CsvTestUtils.createMultipleDelimitedFormatDMSData(String.valueOf(delimiter), quote, latStrings,
                lonStrings);

        ListCellSampler lcs = new ListCellSampler(dmsData, delimiter, quote);
        ValuesWithConfidence<LocationResults> lr = dmsMM.detect(lcs);

        List<LatLonColumnResults> locResults = lr.getBestValue().getLatLonResults();
        Assert.assertEquals(1.0f, locResults.size(), 0f);

        PotentialLocationColumn latColumn = locResults.get(0).getLatColumn();
        Assert.assertTrue(latColumn.getColumnName().equals(ColumnType.LAT.toString()));
        Assert.assertTrue(latColumn.getLatFormat().equals(CoordFormat.DMS));

        PotentialLocationColumn lonColumn = locResults.get(0).getLonColumn();
        Assert.assertTrue(lonColumn.getColumnName().equals(ColumnType.LON.toString()));
        Assert.assertTrue(lonColumn.getLonFormat().equals(CoordFormat.DMS));
    }

    /**
     * This test decides if there is at least one column that contains valid WKT
     * geometry values.
     */
    @Test
    public void testWktMatchMaker()
    {
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf('"');
        List<String> rows = CsvTestUtils.createWktDelimitedData(String.valueOf(delimiter));
        ListCellSampler sampler = new ListCellSampler(rows, delimiter, quote);
        WktMatchMaker wktMM = new WktMatchMaker();
        ValuesWithConfidence<LocationResults> lr = wktMM.detect(sampler);
        List<PotentialLocationColumn> locResults = lr.getBestValue().getLocationResults();
        Assert.assertEquals(1.0f, locResults.size(), 0f);
        Assert.assertTrue(locResults.get(0).getType().equals(ColumnType.WKT_GEOMETRY));
        Assert.assertEquals(1f, lr.getBestValue().getConfidence(), 0f);
    }
}
