package io.opensphere.core.util.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link StreamUtilities}.
 */
public class StreamUtilitiesTest
{
    /** Input array. */
    private static final String[] INPUT_ARRAY = new String[] { "Apple", "Banana", "Apricot" };

    /** Input collection. */
    private static final Collection<String> INPUT = Arrays.asList(INPUT_ARRAY);

    /**
     * Test {@link StreamUtilities#anyMatch(Iterable, Predicate)}.
     */
    @Test
    public void testAnyMatch()
    {
        boolean result;

        result = StreamUtilities.anyMatch(Collections.emptyList(), null);
        Assert.assertFalse(result);

        result = StreamUtilities.anyMatch(INPUT, new Predicate<String>()
        {
            @Override
            public boolean test(String s)
            {
                return s.charAt(0) == 'A';
            }
        });
        Assert.assertTrue(result);

        result = StreamUtilities.anyMatch(INPUT, new Predicate<String>()
        {
            @Override
            public boolean test(String s)
            {
                return s.charAt(0) == 'C';
            }
        });
        Assert.assertFalse(result);
    }

    /**
     * Test {@link StreamUtilities#filter(Collection, Predicate)}.
     */
    @Test
    public void testFilter()
    {
        Collection<String> actual;
        Collection<String> expected;

        expected = New.list(0);
        actual = StreamUtilities.filter(Collections.<String>emptyList(), null);
        Assert.assertEquals(expected, actual);

        expected = Arrays.asList(new String[] { "Apple", "Apricot" });

        actual = StreamUtilities.filter(INPUT, new Predicate<String>()
        {
            @Override
            public boolean test(String s)
            {
                return s.charAt(0) == 'A';
            }
        });
        Assert.assertEquals(expected, actual);

        actual = StreamUtilities.filter(INPUT, new Predicate<Object>()
        {
            @Override
            public boolean test(Object o)
            {
                return o.toString().charAt(0) == 'A';
            }
        });
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test {@link StreamUtilities#filter(Object[], Predicate)}.
     */
    @Test
    public void testFilterArray()
    {
        String[] expected = new String[] { "Apple", "Apricot" };
        String[] actual;

        actual = StreamUtilities.filter(INPUT_ARRAY, new Predicate<String>()
        {
            @Override
            public boolean test(String s)
            {
                return s.charAt(0) == 'A';
            }
        });
        Assert.assertTrue(Arrays.equals(expected, actual));
    }

    /**
     * Test {@link StreamUtilities#filterOne(Collection, Predicate)}.
     */
    @Test
    public void testFilterOne()
    {
        String expected;
        String actual;

        expected = null;
        actual = StreamUtilities.filterOne(Collections.<String>emptyList(), null);
        Assert.assertEquals(expected, actual);

        expected = "Apple";

        actual = StreamUtilities.filterOne(INPUT, new Predicate<String>()
        {
            @Override
            public boolean test(String s)
            {
                return s.charAt(0) == 'A';
            }
        });
        Assert.assertEquals(expected, actual);

        actual = StreamUtilities.filterOne(INPUT, new Predicate<Object>()
        {
            @Override
            public boolean test(Object o)
            {
                return o.toString().charAt(0) == 'A';
            }
        });
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test
     * {@link StreamUtilities#filterDowncast(java.util.stream.Stream, Class)}.
     */
    @Test
    public void testFilterDowcast()
    {
        List<Number> input = Arrays.asList(Integer.valueOf(1), Long.valueOf(2));
        Assert.assertEquals(Arrays.asList(Integer.valueOf(1)),
                StreamUtilities.filterDowncast(input.stream(), Integer.class).collect(Collectors.toList()));
    }
}
