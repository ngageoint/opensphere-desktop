package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.util.CsvTestUtils;

/**
 * Tests the DateDecider class.
 *
 */
@SuppressWarnings("boxing")
public class DateTimeDeciderTest
{
    /**
     * Tests calculating the confidence.
     */
    @Test
    public void testCalculateConfidence()
    {
        List<List<String>> testData = CsvTestUtils.createMultipleTimesData();
        List<PotentialColumn> potential = createPotentialColumn();

        DateTimeDecider decider = new DateTimeDecider();

        List<Pair<PotentialColumn, Integer>> score = decider.calculateConfidence(potential, testData);

        assertEquals((Integer)testData.size(), score.get(0).getSecondObject());
    }

    /**
     * Tests compiling the results.
     */
    @Test
    public void testCompileResults()
    {
        List<PotentialColumn> potential = createPotentialColumn();

        DateTimeDecider decider = new DateTimeDecider();

        Pair<PotentialColumn, Integer> potentialAndScore = new Pair<>(potential.get(0), 90);

        List<Pair<DateColumn, Integer>> results = decider.compileResults(New.list(potentialAndScore));
        Pair<DateColumn, Integer> result = results.get(0);
        DateColumn column = result.getFirstObject();

        assertEquals((Integer)90, result.getSecondObject());
        assertEquals(0, column.getPrimaryColumnIndex());
        assertEquals(Type.TIMESTAMP, column.getDateColumnType());
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", column.getPrimaryColumnFormat());
    }

    /**
     * Creates the test potential column.
     *
     * @return The test potential column.
     */
    private List<PotentialColumn> createPotentialColumn()
    {
        PotentialColumn potential = new PotentialColumn();
        potential.setColumnIndex(0);

        DateFormat format = new DateFormat();
        format.setType(Type.TIMESTAMP);
        format.setSdf("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setRegex("\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2}Z");

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setFormat(format);
        successfulFormat.setNumberOfSuccesses(100);

        potential.getFormats().put(format.getSdf(), successfulFormat);

        SuccessfulFormat notSoSuccessful = new SuccessfulFormat();
        format = new DateFormat();
        format.setType(Type.TIMESTAMP);
        notSoSuccessful.setFormat(format);
        notSoSuccessful.setNumberOfSuccesses(50);

        potential.getFormats().put("no", notSoSuccessful);

        potential.setMostSuccessfulFormat(successfulFormat);

        return New.list(potential);
    }
}
