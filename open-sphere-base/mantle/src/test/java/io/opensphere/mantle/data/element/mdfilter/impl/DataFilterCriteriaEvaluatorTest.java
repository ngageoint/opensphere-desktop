package io.opensphere.mantle.data.element.mdfilter.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.datafilter.DataFilterCriteria;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.impl.ImmutableDataFilterCriteria;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;

/** Tests for {@link DataFilterCriteriaEvaluator}. */
public class DataFilterCriteriaEvaluatorTest
{
    /** Field constant. */
    private static final String FIELD = "FIELD";

    /** Test for {@link Conditional#EQ}. */
    @Test
    public void testEQ()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.EQ, "40");
        Assert.assertTrue(evaluator.accepts(element(FIELD, Double.valueOf(40))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, Double.valueOf(39))));

        evaluator = evaluator(FIELD, Conditional.EQ, "tom");
        Assert.assertTrue(evaluator.accepts(element(FIELD, "tom")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "jerry")));

        evaluator = evaluator(FIELD, Conditional.EQ, "2017-08-09T00:00:00Z");
        Assert.assertTrue(evaluator.accepts(element(FIELD, date(2017, 8, 9))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, date(2017, 8, 9, 1, 0, 0))));

        Assert.assertTrue(evaluator.accepts(element(FIELD, TimeSpan.get(date(2017, 8, 9)))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, TimeSpan.get(date(2017, 8, 9, 1, 0, 0)))));
    }

    /** Test for {@link Conditional#NEQ}. */
    @Test
    public void testNEQ()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.NEQ, "40");
        Assert.assertFalse(evaluator.accepts(element(FIELD, Double.valueOf(40))));
        Assert.assertTrue(evaluator.accepts(element(FIELD, Double.valueOf(39))));

        evaluator = evaluator(FIELD, Conditional.NEQ, "tom");
        Assert.assertFalse(evaluator.accepts(element(FIELD, "tom")));
        Assert.assertTrue(evaluator.accepts(element(FIELD, "jerry")));

        evaluator = evaluator(FIELD, Conditional.NEQ, "2017-08-09T00:00:00Z");
        Assert.assertFalse(evaluator.accepts(element(FIELD, date(2017, 8, 9))));
        Assert.assertTrue(evaluator.accepts(element(FIELD, date(2017, 8, 9, 1, 0, 0))));

        Assert.assertFalse(evaluator.accepts(element(FIELD, TimeSpan.get(date(2017, 8, 9)))));
        Assert.assertTrue(evaluator.accepts(element(FIELD, TimeSpan.get(date(2017, 8, 9, 1, 0, 0)))));
    }

    /** Test for {@link Conditional#LT}. */
    @Test
    public void testLT()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.LT, "40");
        Assert.assertTrue(evaluator.accepts(element(FIELD, Double.valueOf(35))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, Double.valueOf(40))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, Double.valueOf(45))));

        evaluator = evaluator(FIELD, Conditional.LT, "2017-08-09T00:00:00Z");
        Assert.assertTrue(evaluator.accepts(element(FIELD, date(2017, 8, 8))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, date(2017, 8, 10))));

        Assert.assertTrue(evaluator.accepts(element(FIELD, TimeSpan.get(date(2017, 8, 8)))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, TimeSpan.get(date(2017, 8, 10)))));
    }

    /** Test for {@link Conditional#LTE}. */
    @Test
    public void testLTE()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.LTE, "40");
        Assert.assertTrue(evaluator.accepts(element(FIELD, Double.valueOf(35))));
        Assert.assertTrue(evaluator.accepts(element(FIELD, Double.valueOf(40))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, Double.valueOf(45))));
    }

    /** Test for {@link Conditional#GT}. */
    @Test
    public void testGT()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.GT, "40");
        Assert.assertTrue(evaluator.accepts(element(FIELD, Double.valueOf(45))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, Double.valueOf(40))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, Double.valueOf(35))));
    }

    /** Test for {@link Conditional#GTE}. */
    @Test
    public void testGTE()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.GTE, "40");
        Assert.assertTrue(evaluator.accepts(element(FIELD, Double.valueOf(45))));
        Assert.assertTrue(evaluator.accepts(element(FIELD, Double.valueOf(40))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, Double.valueOf(35))));
    }

    /** Test for {@link Conditional#LIKE}. */
    @Test
    public void testLIKE()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.LIKE, "*b*");
        Assert.assertTrue(evaluator.accepts(element(FIELD, "abc")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "aec")));
    }

    /** Test for {@link Conditional#NOT_LIKE}. */
    @Test
    public void testNOTLIKE()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.NOT_LIKE, "*b*");
        Assert.assertTrue(evaluator.accepts(element(FIELD, "aec")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "aabcc")));
    }

    /** Test for {@link Conditional#EMPTY}. */
    @Test
    public void testEMPTY()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.EMPTY, "");
        Assert.assertTrue(evaluator.accepts(element(FIELD, "")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "a")));
    }

    /** Test for {@link Conditional#NOT_EMPTY}. */
    @Test
    public void testNOTEMPTY()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.NOT_EMPTY, "");
        Assert.assertTrue(evaluator.accepts(element(FIELD, "a")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "")));
    }

    /** Test for {@link Conditional#CONTAINS}. */
    @Test
    public void testCONTAINS()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.CONTAINS, "b");
        Assert.assertTrue(evaluator.accepts(element(FIELD, "abc")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "aec")));
    }

    // TODO LIST
    // TODO NOT_IN_LIST
    // TODO LIKE_LIST
    // TODO NOT_LIKE_LIST

    /** Test for {@link Conditional#BETWEEN}. */
    @Test
    public void testBETWEEN()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.BETWEEN, "100 - 200");
        Assert.assertFalse(evaluator.accepts(element(FIELD, "50")));
        Assert.assertTrue(evaluator.accepts(element(FIELD, "100")));
        Assert.assertTrue(evaluator.accepts(element(FIELD, "150")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "200")));

        evaluator = evaluator(FIELD, Conditional.BETWEEN, "-200 - -100");
        Assert.assertFalse(evaluator.accepts(element(FIELD, "-250")));
        Assert.assertTrue(evaluator.accepts(element(FIELD, "-200")));
        Assert.assertTrue(evaluator.accepts(element(FIELD, "-150")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "-100")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "-50")));

        evaluator = evaluator(FIELD, Conditional.BETWEEN, "2017-08-09T:00:00:00Z - 2017-08-10T:00:00:00Z");
        Assert.assertTrue(evaluator.accepts(element(FIELD, date(2017, 8, 9))));
        Assert.assertTrue(evaluator.accepts(element(FIELD, date(2017, 8, 9, 1, 0, 0))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, date(2017, 8, 10))));

        Assert.assertTrue(evaluator.accepts(element(FIELD, TimeSpan.get(date(2017, 8, 9)))));
        Assert.assertTrue(evaluator.accepts(element(FIELD, TimeSpan.get(date(2017, 8, 9, 1, 0, 0)))));
        Assert.assertFalse(evaluator.accepts(element(FIELD, TimeSpan.get(date(2017, 8, 10)))));
    }

    /** Test for {@link Conditional#MATCHES}. */
    @Test
    public void testMATCHES()
    {
        DataFilterCriteriaEvaluator evaluator = evaluator(FIELD, Conditional.MATCHES, ".*b.*");
        Assert.assertTrue(evaluator.accepts(element(FIELD, "abc")));
        Assert.assertFalse(evaluator.accepts(element(FIELD, "aec")));
    }

    /**
     * Creates an evaluator.
     *
     * @param field the field
     * @param conditional the conditional
     * @param value the value
     * @return the evaluator
     */
    private static DataFilterCriteriaEvaluator evaluator(String field, Conditional conditional, String value)
    {
        DataFilterCriteria criteria = new ImmutableDataFilterCriteria(field, value, conditional, null);
        return new DataFilterCriteriaEvaluator(criteria, null);
    }

    /**
     * Creates a meta data provider.
     *
     * @param field the field
     * @param value the value
     * @return the meta data provider
     */
    private static DataElement element(String field, Serializable value)
    {
        Map<String, Serializable> dataMap = New.map();
        dataMap.put(field, value);
        return new DefaultDataElement(0, null, null, new SimpleMetaDataProvider(dataMap));
    }

    /**
     * Creates a date.
     *
     * @param year the year
     * @param month the month
     * @param day the day
     * @return the date
     */
    private static Date date(int year, int month, int day)
    {
        return date(year, month, day, 0, 0, 0);
    }

    /**
     * Creates a date.
     *
     * @param year the year
     * @param month the month
     * @param day the day
     * @param hourOfDay the hour of day
     * @param minute the minute
     * @param second the second
     * @return the date
     */
    private static Date date(int year, int month, int day, int hourOfDay, int minute, int second)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hourOfDay, minute, second);
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cal.getTime();
    }
}
