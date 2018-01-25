package io.opensphere.csvcommon.detect.lob;

import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.detect.ListCellSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.lob.LineOfBearingDetector;
import io.opensphere.csvcommon.detect.lob.model.LobColumnResults;
import io.opensphere.csvcommon.util.CsvTestUtils;
import io.opensphere.csvcommon.util.LocationTestUtils;

/**
 * The Class LineOfBearingDetectorTest.
 */
public class LineOfBearingDetectorTest
{
    /**
     * This test will not add a line of bearing column.
     */
    @Test
    public void testInValidLobColumnms()
    {
        LineOfBearingDetector lobDetector = new LineOfBearingDetector(LocationTestUtils.getPrefsRegistry());

        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf(' ');

        List<String> rows = CsvTestUtils.createBasicDelimitedData(String.valueOf(delimiter));
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);

        ValuesWithConfidence<LobColumnResults> result = lobDetector.detect(lcs);
        Assert.assertTrue(result.getBestConfidence() < 1f);

        Assert.assertEquals(null, result.getBestValue().getLineOfBearingColumn());
    }

    /**
     * This test will randomly pick a column name from the set of lob columns in
     * the LineOfBearingDetector, create sample data with those rows in the
     * header and then detect if the header has a valid(well known) column name
     * for line of bearing data.
     */
    @Test
    public void testValidLobColumns()
    {
        List<String> lobHeader = New.list(1);
        LineOfBearingDetector lobDetector = new LineOfBearingDetector(LocationTestUtils.getPrefsRegistry());
        Random randomIndex = new Random();
        Character delimiter = Character.valueOf(',');
        Character quote = Character.valueOf(' ');

        int index = randomIndex.nextInt(lobDetector.getLineOfBearingColumnNames().size());
        String lobColumn = lobDetector.getLineOfBearingColumnNames().get(index);
        lobHeader.add(lobColumn);

        List<String> rows = CsvTestUtils.createBasicDelimitedLobData(String.valueOf(delimiter), lobHeader);
        ListCellSampler lcs = new ListCellSampler(rows, delimiter, quote);

        ValuesWithConfidence<LobColumnResults> result = lobDetector.detect(lcs);
        Assert.assertEquals(1f, result.getBestConfidence(), 0f);

        Assert.assertEquals(lobColumn, result.getBestValue().getLineOfBearingColumn().getColumnName());
    }
}
