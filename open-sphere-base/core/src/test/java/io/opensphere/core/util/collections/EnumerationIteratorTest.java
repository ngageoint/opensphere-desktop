package io.opensphere.core.util.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/** Test {@link EnumerationIterator}. */
public class EnumerationIteratorTest
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(EnumerationIteratorTest.class);

    /** Test {@link EnumerationIterator}. */
    @Test
    public void test()
    {
        List<String> list = Arrays.asList("one", "two", "three");
        Enumeration<String> en = Collections.enumeration(list);
        EnumerationIterator<String> iter = new EnumerationIterator<>(en);

        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("one", iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("two", iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("three", iter.next());
        Assert.assertFalse(iter.hasNext());

        try
        {
            iter.next();
            Assert.fail();
        }
        catch (NoSuchElementException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("success", e);
            }
        }
    }
}
