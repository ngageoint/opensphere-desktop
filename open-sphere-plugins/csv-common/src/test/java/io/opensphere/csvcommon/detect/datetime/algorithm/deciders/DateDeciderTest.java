package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.DateDecider;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.util.CsvTestUtils;

/**
 * Tests the DateDecider class.
 *
 */
public class DateDeciderTest
{
    /**
     * Tests calculating the confidence.
     */
    @Test
    public void testCalculateConfidence()
    {
        List<List<String>> testData = CsvTestUtils.createMultipleTimesData();
        List<PotentialColumn> potential = createPotentialColumn();

        DateDecider decider = new DateDecider();

        List<Pair<PotentialColumn, Integer>> score = decider.calculateConfidence(potential, testData);

        assertEquals((Integer)(testData.size() - testData.size() / 10), score.get(0).getSecondObject());
    }

    /**
     * Tests compiling the results.
     */
    @Test
    public void testCompileResults()
    {
        List<PotentialColumn> potential = createPotentialColumn();

        DateDecider decider = new DateDecider();

        Pair<PotentialColumn, Integer> potentialAndScore = new Pair<>(potential.get(0), 90);

        List<Pair<DateColumn, Integer>> results = decider.compileResults(New.list(potentialAndScore));
        Pair<DateColumn, Integer> result = results.get(0);
        DateColumn column = result.getFirstObject();

        assertEquals((Integer)90, result.getSecondObject());
        assertEquals(6, column.getPrimaryColumnIndex());
        assertEquals(Type.DATE, column.getDateColumnType());
        assertEquals("MM-dd-yyyy", column.getPrimaryColumnFormat());
    }

    /**
     * Creates the test potential column.
     *
     * @return The test potential column.
     */
    private List<PotentialColumn> createPotentialColumn()
    {
        PotentialColumn potential = new PotentialColumn();
        potential.setColumnIndex(6);

        DateFormat format = new DateFormat();
        format.setType(Type.DATE);
        format.setSdf("MM-dd-yyyy");
        format.setRegex("\\d{2}\\-\\d{2}\\-\\d{4}");

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setFormat(format);
        successfulFormat.setNumberOfSuccesses(100);

        potential.getFormats().put(format.getSdf(), successfulFormat);

        SuccessfulFormat notSoSuccessful = new SuccessfulFormat();
        format = new DateFormat();
        format.setType(Type.DATE);
        notSoSuccessful.setFormat(format);
        notSoSuccessful.setNumberOfSuccesses(50);

        potential.getFormats().put("no", notSoSuccessful);

        potential.setMostSuccessfulFormat(successfulFormat);

        return New.list(potential);
    }
}
