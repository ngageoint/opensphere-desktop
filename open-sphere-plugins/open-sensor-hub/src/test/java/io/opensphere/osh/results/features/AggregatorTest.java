package io.opensphere.osh.results.features;

import java.util.StringJoiner;

import org.junit.Test;

import io.opensphere.core.util.Aggregator;
import org.junit.Assert;

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
}
