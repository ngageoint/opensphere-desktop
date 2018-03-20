package io.opensphere.core.util;

import java.util.List;
import java.util.StringJoiner;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.collections.New;

/** Tests {@link Aggregator}. */
public class AggregatorTest
{
    /** Tests aggregation. */
    @Test
    public void testAggregation()
    {
        StringJoiner joiner = new StringJoiner("-");
        Aggregator<String> aggregator = new Aggregator<>(3, items -> items.forEach(joiner::add));
        aggregator.addItem("hey");
        Assert.assertEquals("", joiner.toString());
        aggregator.addItem("there");
        Assert.assertEquals("", joiner.toString());
        aggregator.addItem("dude");
        Assert.assertEquals("hey-there-dude", joiner.toString());
    }

    /** Tests no aggregation. */
    @Test
    public void testNoAggregation()
    {
        StringJoiner joiner = new StringJoiner("-");
        Aggregator<String> aggregator = new Aggregator<>(1, items -> items.forEach(joiner::add));
        aggregator.addItem("hey");
        Assert.assertEquals("hey", joiner.toString());
        aggregator.addItem("there");
        Assert.assertEquals("hey-there", joiner.toString());
    }

    /** Tests manual aggregation. */
    @Test
    public void testManualAggregation()
    {
        StringJoiner joiner = new StringJoiner("-");
        Aggregator<String> aggregator = new Aggregator<>(100, items -> items.forEach(joiner::add));
        aggregator.addItem("hey");
        Assert.assertEquals("", joiner.toString());
        aggregator.addItem("there");
        Assert.assertEquals("", joiner.toString());
        aggregator.addItem("dude");
        aggregator.processAll();
        Assert.assertEquals("hey-there-dude", joiner.toString());
    }

    /** Tests bulk aggregation. */
    @Test
    public void testBulkAggregation()
    {
        List<List<Number>> results = New.list();
        Aggregator<Number> aggregator = new Aggregator<>(10, results::add);

        aggregator.addItems(generateNumbers(0, 7));
        Assert.assertEquals(0, results.size());

        aggregator.addItems(generateNumbers(7, 15));
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(generateNumbers(0, 10), results.get(0));

        aggregator.addItems(generateNumbers(15, 20));
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(generateNumbers(10, 20), results.get(1));

        aggregator.addItems(generateNumbers(20, 23));
        Assert.assertEquals(2, results.size());

        aggregator.addItems(generateNumbers(23, 101));
        Assert.assertEquals(10, results.size());
        Assert.assertEquals(generateNumbers(90, 100), results.get(9));

        aggregator.processAll();
        Assert.assertEquals(11, results.size());
        Assert.assertEquals(generateNumbers(100, 101), results.get(10));
    }

    /**
     * Utility to generate a list of numbers.
     *
     * @param start the start
     * @param endExclusive the end exclusive
     * @return the list of numbers
     */
    private static List<Number> generateNumbers(int start, int endExclusive)
    {
        List<Number> list = New.list();
        for (int i = start; i < endExclusive; i++)
        {
            list.add(Integer.valueOf(i));
        }
        return list;
    }
}
