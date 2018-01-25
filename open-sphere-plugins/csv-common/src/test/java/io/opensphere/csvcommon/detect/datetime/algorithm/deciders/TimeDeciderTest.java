package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.TimeDecider;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.util.CsvTestUtils;

/**
 * Tests the DateDecider class.
 *
 */
public class TimeDeciderTest
{
    /**
     * Tests calculating the confidence.
     */
    @Test
    public void testCalculateConfidence()
    {
        List<List<String>> testData = CsvTestUtils.createMultipleTimesData();
        List<PotentialColumn> potential = createPotentialColumn();

        TimeDecider decider = new TimeDecider();

        List<Pair<PotentialColumn, Integer>> score = decider.calculateConfidence(potential, testData);

        assertEquals((Integer)(testData.size() - testData.size() / 5), score.get(0).getSecondObject());
    }

    /**
     * Tests compiling the results.
     */
    @Test
    public void testCompileResults()
    {
        List<PotentialColumn> potential = createPotentialColumn();

        TimeDecider decider = new TimeDecider();

        Pair<PotentialColumn, Integer> potentialAndScore = new Pair<>(potential.get(0), 90);

        List<Pair<DateColumn, Integer>> results = decider.compileResults(New.list(potentialAndScore));
        Pair<DateColumn, Integer> result = results.get(0);
        DateColumn column = result.getFirstObject();

        assertEquals((Integer)90, result.getSecondObject());
        assertEquals(3, column.getPrimaryColumnIndex());
        assertEquals(Type.TIME, column.getDateColumnType());
        assertEquals("HH:mm:ss", column.getPrimaryColumnFormat());
    }

    /**
     * Creates the test potential column.
     *
     * @return The test potential column.
     */
    private List<PotentialColumn> createPotentialColumn()
    {
        PotentialColumn potential = new PotentialColumn();
        potential.setColumnIndex(3);

        DateFormat format = new DateFormat();
        format.setType(Type.TIME);
        format.setSdf("HH:mm:ss");
        format.setRegex("\\d{2}\\:\\d{2}\\:\\d{2}");

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setFormat(format);
        successfulFormat.setNumberOfSuccesses(100);

        potential.getFormats().put(format.getSdf(), successfulFormat);

        SuccessfulFormat notSoSuccessful = new SuccessfulFormat();
        format = new DateFormat();
        format.setType(Type.TIME);
        notSoSuccessful.setFormat(format);
        notSoSuccessful.setNumberOfSuccesses(50);

        potential.getFormats().put("no", notSoSuccessful);

        potential.setMostSuccessfulFormat(successfulFormat);

        return New.list(potential);
    }
}
