package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.CompositeDateTimeDecider;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.util.CsvTestUtils;

/**
 * Tests the DateDecider class.
 *
 */
public class CompositeDateTimeDeciderTest
{
    /**
     * Tests calculating the confidence.
     */
    @Test
    public void testCalculateConfidence()
    {
        List<List<String>> testData = CsvTestUtils.createMultipleTimesData();
        List<PotentialColumn> potential = createPotentialColumn();

        CompositeDateTimeDecider decider = new CompositeDateTimeDecider();

        List<Pair<PotentialColumn, Integer>> score = decider.calculateConfidence(potential, testData);

        assertEquals((Integer)(testData.size() - testData.size() / 10), score.get(0).getSecondObject());
        assertEquals(8, score.get(0).getFirstObject().getColumnIndex());
        assertEquals((Integer)(testData.size() - testData.size() / 5), score.get(1).getSecondObject());
        assertEquals(9, score.get(1).getFirstObject().getColumnIndex());
    }

    /**
     * Tests compiling the results.
     */
    @Test
    public void testCompileResults()
    {
        List<PotentialColumn> potential = createPotentialColumn();

        CompositeDateTimeDecider decider = new CompositeDateTimeDecider();

        Pair<PotentialColumn, Integer> potentialAndScoreDate = new Pair<>(potential.get(0), 90);
        Pair<PotentialColumn, Integer> potentialAndScoreTime = new Pair<>(potential.get(1), 90);

        List<Pair<DateColumn, Integer>> results = decider.compileResults(New.list(potentialAndScoreDate, potentialAndScoreTime));
        Pair<DateColumn, Integer> result = results.get(0);

        DateColumn column = result.getFirstObject();

        assertEquals((Integer)90, result.getSecondObject());
        assertEquals(8, column.getPrimaryColumnIndex());
        assertEquals(9, column.getSecondaryColumnIndex());
        assertEquals(Type.TIMESTAMP, column.getDateColumnType());
        assertEquals("MM-dd-yyyy", column.getPrimaryColumnFormat());
        assertEquals("HH:mm:ss", column.getSecondaryColumnFormat());
    }

    /**
     * Creates the test potential column.
     *
     * @return The test potential column.
     */
    private List<PotentialColumn> createPotentialColumn()
    {
        PotentialColumn potential = new PotentialColumn();
        potential.setColumnIndex(8);

        DateFormat format = new DateFormat();
        format.setType(Type.DATE);
        format.setSdf("MM-dd-yyyy");
        format.setRegex("\\d{2}\\-\\d{2}\\-\\d{4}");

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setFormat(format);
        successfulFormat.setNumberOfSuccesses(90);

        potential.getFormats().put(format.getSdf(), successfulFormat);
        potential.setMostSuccessfulFormat(successfulFormat);

        PotentialColumn timePotential = new PotentialColumn();
        timePotential.setColumnIndex(9);
        SuccessfulFormat timeFormat = new SuccessfulFormat();
        format = new DateFormat();
        format.setType(Type.TIME);
        format.setSdf("HH:mm:ss");
        format.setRegex("\\d{2}\\:\\d{2}\\:\\d{2}");
        timeFormat.setFormat(format);
        timeFormat.setNumberOfSuccesses(80);

        timePotential.getFormats().put(format.getSdf(), timeFormat);
        timePotential.setMostSuccessfulFormat(timeFormat);

        return New.list(potential, timePotential);
    }
}
