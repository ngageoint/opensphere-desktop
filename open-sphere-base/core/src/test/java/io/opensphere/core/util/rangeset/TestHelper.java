package io.opensphere.core.util.rangeset;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;

/**
 * Test helper class.
 */
public final class TestHelper
{
    /**
     * Test that calling {@link Iterator#next()} throws a
     * {@link NoSuchElementException}.
     *
     * @param itr The iterator.
     */
    public static void testNoSuchElement(Iterator<?> itr)
    {
        boolean gotNoSuchElementException = false;
        try
        {
            itr.next();
        }
        catch (NoSuchElementException e)
        {
            gotNoSuchElementException = true;
        }
        Assert.assertTrue(gotNoSuchElementException);
    }

    /**
     * Test that calling {@link Iterator#remove()} throws an
     * {@link UnsupportedOperationException}.
     *
     * @param itr The iterator.
     */
    public static void testUnsupportedRemove(Iterator<?> itr)
    {
        boolean gotUnsupportedOperationExceptionn = false;
        try
        {
            itr.remove();
        }
        catch (UnsupportedOperationException e)
        {
            gotUnsupportedOperationExceptionn = true;
        }
        Assert.assertTrue(gotUnsupportedOperationExceptionn);
    }

    /** Disallow instantiation. */
    private TestHelper()
    {
    }
}
