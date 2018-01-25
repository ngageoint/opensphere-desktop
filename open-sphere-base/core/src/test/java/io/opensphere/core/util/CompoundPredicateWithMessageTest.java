package io.opensphere.core.util;

import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import org.junit.Assert;

/** Test for {@link CompoundPredicateWithMessage}. */
public class CompoundPredicateWithMessageTest
{
    /** Test message 1. */
    private static final String TEST1 = "Test1";

    /** Test message 2. */
    private static final String TEST2 = "Test2";

    /** Test message 3. */
    private static final String TEST3 = "Test3";

    /** Test all predicates {@code false}. */
    @Test
    public void testAllFalse()
    {
        Object input = new Object();
        Collection<PredicateWithMessage<Object>> predicates = New.collection();
        predicates.add(createPredicate(input, Boolean.FALSE, TEST1));
        predicates.add(createPredicate(input, Boolean.FALSE, TEST2));
        predicates.add(createPredicate(input, Boolean.FALSE, TEST3));

        CompoundPredicateWithMessage<Object> predicate;
        predicate = new CompoundPredicateWithMessage<>(predicates);
        Assert.assertFalse(predicate.test(input));
        Assert.assertEquals(TEST1, predicate.getMessage());
    }

    /** Test all predicates {@code true}. */
    @Test
    public void testAllTrue()
    {
        Object input = new Object();
        Collection<PredicateWithMessage<Object>> predicates = New.collection();
        predicates.add(createPredicate(input, Boolean.TRUE, TEST1));
        predicates.add(createPredicate(input, Boolean.TRUE, TEST2));
        predicates.add(createPredicate(input, Boolean.TRUE, TEST3));

        CompoundPredicateWithMessage<Object> predicate;
        predicate = new CompoundPredicateWithMessage<>(predicates);
        Assert.assertTrue(predicate.test(input));
        Assert.assertEquals(TEST3, predicate.getMessage());
    }

    /** Test last predicate {@code false}. */
    @Test
    public void testLastFalse()
    {
        Object input = new Object();
        Collection<PredicateWithMessage<Object>> predicates = New.collection();
        predicates.add(createPredicate(input, Boolean.TRUE, TEST1));
        predicates.add(createPredicate(input, Boolean.TRUE, TEST2));
        predicates.add(createPredicate(input, Boolean.FALSE, TEST3));

        CompoundPredicateWithMessage<Object> predicate;

        predicate = new CompoundPredicateWithMessage<>(predicates);
        Assert.assertFalse(predicate.test(input));
        Assert.assertEquals(TEST3, predicate.getMessage());
    }

    /** Test middle predicate {@code false}. */
    @Test
    public void testMiddleFalse()
    {
        Object input = new Object();
        Collection<PredicateWithMessage<Object>> predicates = New.collection();
        predicates.add(createPredicate(input, Boolean.TRUE, TEST1));
        predicates.add(createPredicate(input, Boolean.FALSE, TEST2));
        predicates.add(createPredicate(input, Boolean.TRUE, TEST3));

        CompoundPredicateWithMessage<Object> predicate;

        predicate = new CompoundPredicateWithMessage<>(predicates);
        Assert.assertFalse(predicate.test(input));
        Assert.assertEquals(TEST2, predicate.getMessage());
    }

    /**
     * Create a predicate mock object.
     *
     * @param input The expected predicate input.
     * @param testResult The result of the test.
     * @param message The message.
     * @return The mock object.
     */
    private PredicateWithMessage<Object> createPredicate(Object input, Boolean testResult, String message)
    {
        @SuppressWarnings("unchecked")
        PredicateWithMessage<Object> predicate = EasyMock.createMock(PredicateWithMessage.class);
        EasyMock.expect(Boolean.valueOf(predicate.test(input))).andReturn(testResult);
        EasyMock.expect(predicate.getMessage()).andReturn(message);
        EasyMock.replay(predicate);
        return predicate;
    }
}
