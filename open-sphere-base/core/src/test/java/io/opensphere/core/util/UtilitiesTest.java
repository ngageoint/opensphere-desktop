package io.opensphere.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.lang.ImpossibleException;

/**
 * Test for {@link Utilities}.
 */
public class UtilitiesTest
{
    /**
     * Test for {@link Utilities#checkNull(Object, String)}.
     */
    @Test
    public void testCheckNull()
    {
        try
        {
            Utilities.checkNull(new Object(), "param");
        }
        catch (IllegalArgumentException e)
        {
            Assert.fail("Should not have thrown illegal argument exception for non-null argument.");
        }

        try
        {
            Utilities.checkNull(null, null);
            Assert.fail("Should have thrown illegal argument exception for null argument.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected.
            Assert.assertTrue(true);
        }
    }

    /** Test for {@link Utilities#clone(java.util.Date)}. */
    @Test
    public void testCloneDate()
    {
        Assert.assertNull(Utilities.clone((Date)null));

        Date date = new Date();
        Date clone = Utilities.clone(date);
        Assert.assertNotSame(clone, date);
        Assert.assertEquals(clone, date);
    }

    /**
     * Test for {@link Utilities#close(AutoCloseable...)}.
     */
    @Test(expected = RuntimeException.class)
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void testCloseCloseableRuntimeException()
    {
        testCloseWith(createCloseable(new RuntimeException()));
    }

    /** Test for {@link Utilities#close(AutoCloseable...)}. */
    @Test(expected = Error.class)
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void testCloseError()
    {
        testCloseWith(createAutoCloseable(new Error()));
    }

    /** Test for {@link Utilities#close(AutoCloseable...)}. */
    @Test
    public void testCloseIOException()
    {
        testCloseWith(createAutoCloseable(new IOException()));
    }

    /** Test for {@link Utilities#close(AutoCloseable...)}. */
    @Test
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void testCloseRuntimeException()
    {
        testCloseWith(createAutoCloseable(new RuntimeException()));
    }

    /**
     * Test for
     * {@link Utilities#getNonNull(Object, Object, java.util.function.BinaryOperator)}
     * .
     */
    @Test
    public void testGetNonNull()
    {
        Assert.assertEquals("a", Utilities.getNonNull("a", null, null));
        Assert.assertEquals("b", Utilities.getNonNull(null, "b", null));
        Assert.assertEquals("ab", Utilities.getNonNull("a", "b", (o1, o2) -> o1 + o2));
        Assert.assertArrayEquals((Object[])null, Utilities.getNonNull(null, null, null));
    }

    /** Test for {@link Utilities#close(AutoCloseable...)}. */
    @Test(expected = Error.class)
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void testCloseMultipleExceptionsError1()
    {
        AutoCloseable errorCloseable = createCloseable(new Error());
        AutoCloseable runtimeExceptionCloseable = createCloseable(new RuntimeException());
        AutoCloseable closeable3 = createCloseable(null);

        try
        {
            Utilities.close(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
        catch (Error e)
        {
            Assert.assertTrue(e.getSuppressed().length == 1 && e.getSuppressed()[0].getClass() == RuntimeException.class);
            throw e;
        }
        finally
        {
            EasyMock.verify(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
    }

    /** Test for {@link Utilities#close(AutoCloseable...)}. */
    @Test(expected = Error.class)
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void testCloseMultipleExceptionsError2()
    {
        AutoCloseable errorCloseable = createCloseable(new Error());
        AutoCloseable runtimeExceptionCloseable = createCloseable(new RuntimeException());
        AutoCloseable closeable3 = createCloseable(null);

        try
        {
            Utilities.close(runtimeExceptionCloseable, errorCloseable, closeable3);
        }
        catch (Error e)
        {
            Assert.assertTrue(e.getSuppressed().length == 1 && e.getSuppressed()[0].getClass() == RuntimeException.class);
            throw e;
        }
        finally
        {
            EasyMock.verify(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
    }

    /**
     * Test for {@link Utilities#close(AutoCloseable...)} with two
     * {@link Closeable}s that throw {@link RuntimeException}.
     */
    @Test(expected = RuntimeException.class)
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void testCloseMultipleExceptionsRuntimeException1()
    {
        AutoCloseable errorCloseable = createCloseable(new RuntimeException());
        AutoCloseable runtimeExceptionCloseable = createCloseable(new RuntimeException());
        AutoCloseable closeable3 = createCloseable(null);

        try
        {
            Utilities.close(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
        catch (RuntimeException e)
        {
            Assert.assertTrue(e.getSuppressed().length == 1 && e.getSuppressed()[0].getClass() == RuntimeException.class);
            throw e;
        }
        finally
        {
            EasyMock.verify(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
    }

    /**
     * Test for {@link Utilities#close(AutoCloseable...)} with two
     * {@link AutoCloseable}s that throw {@link RuntimeException}.
     */
    @Test
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void testCloseMultipleExceptionsRuntimeException2()
    {
        AutoCloseable errorCloseable = createAutoCloseable(new RuntimeException());
        AutoCloseable runtimeExceptionCloseable = createAutoCloseable(new RuntimeException());
        AutoCloseable closeable3 = createAutoCloseable(null);

        try
        {
            Utilities.close(runtimeExceptionCloseable, errorCloseable, closeable3);
        }
        finally
        {
            EasyMock.verify(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
    }

    /**
     * Test for {@link Utilities#close(AutoCloseable...)} with one
     * {@link Closeable} that throws nothing and one {@link AutoCloseable} that
     * throws {@link RuntimeException}.
     */
    @Test
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void testCloseMultipleExceptionsRuntimeException3()
    {
        AutoCloseable errorCloseable = createCloseable(null);
        AutoCloseable runtimeExceptionCloseable = createAutoCloseable(new RuntimeException());
        AutoCloseable closeable3 = createCloseable(null);

        try
        {
            Utilities.close(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
        finally
        {
            EasyMock.verify(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
    }

    /**
     * Test for {@link Utilities#close(AutoCloseable...)} with one
     * {@link Closeable} that throws {@link RuntimeException} and one
     * {@link AutoCloseable} that throws nothing.
     */
    @Test(expected = RuntimeException.class)
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void testCloseMultipleExceptionsRuntimeException4()
    {
        AutoCloseable errorCloseable = createCloseable(new RuntimeException());
        AutoCloseable runtimeExceptionCloseable = createAutoCloseable(null);
        AutoCloseable closeable3 = createCloseable(null);

        try
        {
            Utilities.close(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
        finally
        {
            EasyMock.verify(errorCloseable, runtimeExceptionCloseable, closeable3);
        }
    }

    /** Test for {@link Utilities#equalsOrBothNaN(double, double)}. */
    @Test
    public void testEqualsOrBothNaN()
    {
        Assert.assertTrue(Utilities.equalsOrBothNaN(Double.NaN, Double.NaN));
        Assert.assertFalse(Utilities.equalsOrBothNaN(Double.NaN, 0.));
        Assert.assertFalse(Utilities.equalsOrBothNaN(0., Double.NaN));
        Assert.assertTrue(Utilities.equalsOrBothNaN(0., 0.));

        Assert.assertTrue(Utilities.equalsOrBothNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        Assert.assertFalse(Utilities.equalsOrBothNaN(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
        Assert.assertFalse(Utilities.equalsOrBothNaN(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        Assert.assertTrue(Utilities.equalsOrBothNaN(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));

        Assert.assertTrue(Utilities.equalsOrBothNaN(Float.NaN, Float.NaN));
        Assert.assertFalse(Utilities.equalsOrBothNaN(Float.NaN, 0.));
        Assert.assertFalse(Utilities.equalsOrBothNaN(0f, Float.NaN));
        Assert.assertTrue(Utilities.equalsOrBothNaN(0f, 0f));

        Assert.assertTrue(Utilities.equalsOrBothNaN(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        Assert.assertFalse(Utilities.equalsOrBothNaN(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY));
        Assert.assertFalse(Utilities.equalsOrBothNaN(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
        Assert.assertTrue(Utilities.equalsOrBothNaN(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
    }

    /**
     * Test for {@link Utilities#indexOf(char[], char)}.
     */
    @Test
    public void testIndexOf()
    {
        Assert.assertEquals(-1, Utilities.indexOf(new char[] {}, 'a'));
        Assert.assertEquals(0, Utilities.indexOf(new char[] { 'a', 'a' }, 'a'));
        Assert.assertEquals(0, Utilities.indexOf(new char[] { 'a', 'b' }, 'a'));
        Assert.assertEquals(1, Utilities.indexOf(new char[] { 'a', 'b' }, 'b'));
        Assert.assertEquals(-1, Utilities.indexOf(new char[] { 'a', 'b' }, 'c'));
    }

    /**
     * Test for {@link Utilities#isSequential(long[])}.
     */
    @Test
    public void testIsSequential()
    {
        Assert.assertTrue(Utilities.isSequential(new int[] {}));
        Assert.assertTrue(Utilities.isSequential(new int[] { 0 }));
        Assert.assertTrue(Utilities.isSequential(new int[] { 2, 3, 4, 5 }));
        Assert.assertTrue(Utilities.isSequential(new int[] { 5, 4, 3, 2 }));
        Assert.assertTrue(Utilities.isSequential(new int[] { -2, -1, 0, 1, 2 }));
        Assert.assertTrue(Utilities.isSequential(new int[] { 2147483645, 2147483646, 2147483647 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { 1, 1 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { 5, 4, 3, 1 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { 5, 4, 2, 1 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { 5, 4, 3, 4 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { 1, 3, 4, 5 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { 1, 2, 4, 5 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { 3, 2, 4, 5 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { -2, 0, 1, 2 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { -2, -1, 0, -1 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { 2147483647, -2147483648 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { -2147483648, 2147483647 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { 2147483646, 2147483647, -2147483648 }));
        Assert.assertFalse(Utilities.isSequential(new int[] { -2147483647, -2147483648, 2147483647 }));

        Assert.assertTrue(Utilities.isSequential(new long[] {}));
        Assert.assertTrue(Utilities.isSequential(new long[] { 0 }));
        Assert.assertTrue(Utilities.isSequential(new long[] { 2, 3, 4, 5 }));
        Assert.assertTrue(Utilities.isSequential(new long[] { 5, 4, 3, 2 }));
        Assert.assertTrue(Utilities.isSequential(new long[] { -2, -1, 0, 1, 2 }));
        Assert.assertTrue(
                Utilities.isSequential(new long[] { 9223372036854775805L, 9223372036854775806L, 9223372036854775807L }));
        Assert.assertFalse(Utilities.isSequential(new long[] { 1, 1 }));
        Assert.assertFalse(Utilities.isSequential(new long[] { 5, 4, 3, 1 }));
        Assert.assertFalse(Utilities.isSequential(new long[] { 5, 4, 2, 1 }));
        Assert.assertFalse(Utilities.isSequential(new long[] { 5, 4, 3, 4 }));
        Assert.assertFalse(Utilities.isSequential(new long[] { 1, 3, 4, 5 }));
        Assert.assertFalse(Utilities.isSequential(new long[] { 1, 2, 4, 5 }));
        Assert.assertFalse(Utilities.isSequential(new long[] { 3, 2, 4, 5 }));
        Assert.assertFalse(Utilities.isSequential(new long[] { -2, 0, 1, 2 }));
        Assert.assertFalse(Utilities.isSequential(new long[] { -2, -1, 0, -1 }));
        Assert.assertFalse(Utilities.isSequential(new long[] { 9223372036854775807L, -9223372036854775808L }));
        Assert.assertFalse(Utilities.isSequential(new long[] { -9223372036854775808L, 9223372036854775807L }));
        Assert.assertFalse(
                Utilities.isSequential(new long[] { 9223372036854775806L, 9223372036854775807L, -9223372036854775808L }));
        Assert.assertFalse(
                Utilities.isSequential(new long[] { -9223372036854775807L, -9223372036854775808L, 9223372036854775807L }));
    }

    /**
     * Test for {@link Utilities#removeFromArray(Object[], Object)}.
     */
    @Test
    public void testRemoveFromArray()
    {
        Integer[] arr;
        Integer[] result;

        Integer v1 = Integer.valueOf(1);
        Integer v2 = Integer.valueOf(2);
        Integer v3 = Integer.valueOf(3);

        // Remove object not contained.
        arr = new Integer[] { v1 };
        result = Utilities.removeFromArray(arr, v2);
        Assert.assertSame(arr, result);

        // Remove only object.
        result = Utilities.removeFromArray(arr, v1);
        Assert.assertTrue(result.length == 0);

        // Remove first object.
        arr = new Integer[] { v1, v2, v3 };
        result = Utilities.removeFromArray(arr, v1);
        Assert.assertTrue(result.length == 2);
        Assert.assertTrue(Arrays.equals(result, new Integer[] { v2, v3 }));

        // Remove middle object.
        result = Utilities.removeFromArray(arr, v2);
        Assert.assertTrue(result.length == 2);
        Assert.assertTrue(Arrays.equals(result, new Integer[] { v1, v3 }));

        // Remove last object.
        result = Utilities.removeFromArray(arr, v3);
        Assert.assertTrue(result.length == 2);
        Assert.assertTrue(Arrays.equals(result, new Integer[] { v1, v2 }));

        // Test null.
        arr = new Integer[] { v1, null, v2 };
        result = Utilities.removeFromArray(arr, v1);
        Assert.assertTrue(result.length == 2);
        Assert.assertTrue(Arrays.equals(result, new Integer[] { null, v2 }));
        result = Utilities.removeFromArray(arr, null);
        Assert.assertTrue(result.length == 2);
        Assert.assertTrue(Arrays.equals(result, new Integer[] { v1, v2 }));
        result = Utilities.removeFromArray(arr, v2);
        Assert.assertTrue(result.length == 2);
        Assert.assertTrue(Arrays.equals(result, new Integer[] { v1, null }));
    }

    /**
     * Test for {@link Utilities#sumDouble(java.util.Collection)}.
     */
    @Test
    public void testSumDouble()
    {
        Assert.assertEquals(1138. + Integer.MAX_VALUE, Utilities.sumDouble(Arrays.asList(Integer.valueOf(7), Integer.valueOf(-1),
                Integer.valueOf(1142), Integer.valueOf(-10), Integer.valueOf(Integer.MAX_VALUE))), 0.);
        Assert.assertEquals(1138. + Float.MAX_VALUE, Utilities.sumDouble(Arrays.asList(Float.valueOf(7.1f), Float.valueOf(-1.4f),
                Float.valueOf(1142.9f), Float.valueOf(-10.1f), Float.valueOf(Float.MAX_VALUE))), 0.);
        Assert.assertEquals(1138. + Float.MAX_VALUE, Utilities.sumDouble(Arrays.asList(Double.valueOf(7.1), Double.valueOf(-1.4),
                Double.valueOf(1142.9), Double.valueOf(-10.1), Double.valueOf(Float.MAX_VALUE))), 0.);
        Assert.assertEquals(1138. + Integer.MAX_VALUE, Utilities.sumDouble(Arrays.asList(Long.valueOf(7), Long.valueOf(-1),
                Long.valueOf(1142), Long.valueOf(-10), Long.valueOf(Integer.MAX_VALUE))), 0.);
    }

    /**
     * Test for {@link Utilities#sumFloat(java.util.Collection)}.
     */
    @Test
    public void testSumFloat()
    {
        Assert.assertEquals(1138f, Utilities.sumFloat(
                Arrays.asList(Integer.valueOf(7), Integer.valueOf(-1), Integer.valueOf(1142), Integer.valueOf(-10))), 0.);
        Assert.assertEquals(1138.5f,
                Utilities.sumFloat(
                        Arrays.asList(Float.valueOf(7.1f), Float.valueOf(-1.4f), Float.valueOf(1142.9f), Float.valueOf(-10.1f))),
                0.);
        Assert.assertEquals(1138.5f,
                Utilities.sumFloat(
                        Arrays.asList(Double.valueOf(7.1), Double.valueOf(-1.4), Double.valueOf(1142.9), Double.valueOf(-10.1))),
                0.);
        Assert.assertEquals(1138f,
                Utilities.sumFloat(Arrays.asList(Long.valueOf(7), Long.valueOf(-1), Long.valueOf(1142), Long.valueOf(-10))), 0.);
    }

    /**
     * Test for {@link Utilities#sumInt(java.util.Collection)}.
     */
    @Test
    public void testSumInt()
    {
        Assert.assertEquals(1138, Utilities
                .sumInt(Arrays.asList(Integer.valueOf(7), Integer.valueOf(-1), Integer.valueOf(1142), Integer.valueOf(-10))));
        Assert.assertEquals(1138, Utilities
                .sumInt(Arrays.asList(Float.valueOf(7.1f), Float.valueOf(-1.4f), Float.valueOf(1142.9f), Float.valueOf(-10.1f))));
        Assert.assertEquals(1138, Utilities
                .sumInt(Arrays.asList(Double.valueOf(7.1), Double.valueOf(-1.4), Double.valueOf(1142.9), Double.valueOf(-10.1))));
        Assert.assertEquals(1138,
                Utilities.sumInt(Arrays.asList(Long.valueOf(7), Long.valueOf(-1), Long.valueOf(1142), Long.valueOf(-10))));
    }

    /**
     * Test for {@link Utilities#sumLong(java.util.Collection)}.
     */
    @Test
    public void testSumLong()
    {
        Assert.assertEquals(1138L + Integer.MAX_VALUE, Utilities.sumLong(Arrays.asList(Integer.valueOf(7), Integer.valueOf(-1),
                Integer.valueOf(1142), Integer.valueOf(-10), Integer.valueOf(Integer.MAX_VALUE))));
        Assert.assertEquals(1138L + (long)Float.MAX_VALUE, Utilities.sumLong(Arrays.asList(Float.valueOf(7.1f),
                Float.valueOf(-1.4f), Float.valueOf(1142.9f), Float.valueOf(-10.1f), Float.valueOf(Float.MAX_VALUE))));
        Assert.assertEquals(1138L + (long)Float.MAX_VALUE, Utilities.sumLong(Arrays.asList(Double.valueOf(7.1),
                Double.valueOf(-1.4), Double.valueOf(1142.9), Double.valueOf(-10.1), Double.valueOf(Float.MAX_VALUE))));
        Assert.assertEquals(1138L + Integer.MAX_VALUE, Utilities.sumLong(Arrays.asList(Long.valueOf(7), Long.valueOf(-1),
                Long.valueOf(1142), Long.valueOf(-10), Long.valueOf(Integer.MAX_VALUE))));
    }

    /** Test for {@link Utilities#sumOverflow(long, long)}. */
    @Test
    public void testSumOverflow()
    {
        Assert.assertFalse(Utilities.sumOverflow(0L, 0L));
        Assert.assertFalse(Utilities.sumOverflow(Long.MAX_VALUE, 0L));
        Assert.assertTrue(Utilities.sumOverflow(Long.MAX_VALUE, 1L));
        Assert.assertFalse(Utilities.sumOverflow(Long.MAX_VALUE, -1L));
    }

    /**
     * Test for {@link Utilities#sumRoundInt(java.util.Collection)}.
     */
    @Test
    public void testSumRoundInt()
    {
        Assert.assertEquals(1138, Utilities.sumRoundInt(
                Arrays.asList(Integer.valueOf(7), Integer.valueOf(-1), Integer.valueOf(1142), Integer.valueOf(-10))));
        Assert.assertEquals(1139, Utilities.sumRoundInt(
                Arrays.asList(Float.valueOf(7.1f), Float.valueOf(-1.4f), Float.valueOf(1142.9f), Float.valueOf(-10.1f))));
        Assert.assertEquals(1139, Utilities.sumRoundInt(
                Arrays.asList(Double.valueOf(7.1), Double.valueOf(-1.4), Double.valueOf(1142.9), Double.valueOf(-10.1))));
        Assert.assertEquals(1138,
                Utilities.sumRoundInt(Arrays.asList(Long.valueOf(7), Long.valueOf(-1), Long.valueOf(1142), Long.valueOf(-10))));
    }

    /**
     * Test for {@link Utilities#sumRoundLong(java.util.Collection)}.
     */
    @Test
    public void testSumRoundLong()
    {
        Assert.assertEquals(1138L + Integer.MAX_VALUE, Utilities.sumRoundLong(Arrays.asList(Integer.valueOf(7),
                Integer.valueOf(-1), Integer.valueOf(1142), Integer.valueOf(-10), Integer.valueOf(Integer.MAX_VALUE))));
        Assert.assertEquals(1139L + (long)Float.MAX_VALUE, Utilities.sumRoundLong(Arrays.asList(Float.valueOf(7.1f),
                Float.valueOf(-1.4f), Float.valueOf(1142.9f), Float.valueOf(-10.1f), Float.valueOf(Float.MAX_VALUE))));
        Assert.assertEquals(1139L + (long)Float.MAX_VALUE, Utilities.sumRoundLong(Arrays.asList(Double.valueOf(7.1),
                Double.valueOf(-1.4), Double.valueOf(1142.9), Double.valueOf(-10.1), Double.valueOf(Float.MAX_VALUE))));
        Assert.assertEquals(1138L + Integer.MAX_VALUE, Utilities.sumRoundLong(Arrays.asList(Long.valueOf(7), Long.valueOf(-1),
                Long.valueOf(1142), Long.valueOf(-10), Long.valueOf(Integer.MAX_VALUE))));
    }

    /**
     * Test for {@link Utilities#unique(int[])}.
     */
    @Test
    public void testUnique()
    {
        int[] arr;
        int[] expected;
        int[] result;
        String msg = "Arrays are not equal.";

        arr = new int[0];
        expected = new int[0];

        result = Utilities.unique(arr);
        Assert.assertTrue(msg, Arrays.equals(result, expected));

        arr = new int[] { 0 };
        expected = new int[] { 0 };

        result = Utilities.unique(arr);
        Assert.assertTrue(msg, Arrays.equals(result, expected));

        arr = new int[] { 1, 1, 1 };
        expected = new int[] { 1 };

        result = Utilities.unique(arr);
        Assert.assertTrue(msg, Arrays.equals(result, expected));

        arr = new int[] { -5, -5, -3, -2, 0, 1, 1, 2, 2 };
        expected = new int[] { -5, -3, -2, 0, 1, 2 };

        result = Utilities.unique(arr);
        Assert.assertTrue(msg, Arrays.equals(result, expected));
    }

    /**
     * Test for {@link Utilities#uniqueUnsorted(int[])}.
     */
    @Test
    public void testUniqueUnsorted()
    {
        int[] arr;
        int[] expected;
        int[] result;
        String msg = "Arrays are not equal.";

        arr = new int[0];
        expected = new int[0];

        result = Utilities.uniqueUnsorted(arr);
        Assert.assertTrue(msg, Arrays.equals(result, expected));

        arr = new int[] { 0 };
        expected = new int[] { 0 };

        result = Utilities.uniqueUnsorted(arr);
        Assert.assertTrue(msg, Arrays.equals(result, expected));

        arr = new int[] { 1, 1, 1 };
        expected = new int[] { 1 };

        result = Utilities.uniqueUnsorted(arr);
        Assert.assertTrue(msg, Arrays.equals(result, expected));

        arr = new int[] { -5, -3, -5, 2, 2, 1, 1, 0, -2 };
        expected = new int[] { -5, -3, -2, 0, 1, 2 };

        result = Utilities.uniqueUnsorted(arr);
        Assert.assertTrue(msg, Arrays.equals(result, expected));
    }

    /**
     * Create a mock closable for testing.
     *
     * @param throwable Optional throwable to throw from the close method.
     * @return The mock object.
     */
    private AutoCloseable createAutoCloseable(Throwable throwable)
    {
        AutoCloseable closeable = EasyMock.createMock(AutoCloseable.class);
        try
        {
            closeable.close();
        }
        catch (Exception e)
        {
            throw new ImpossibleException(e);
        }
        if (throwable != null)
        {
            EasyMock.expectLastCall().andThrow(throwable);
        }
        EasyMock.replay(closeable);
        return closeable;
    }

    /**
     * Create a mock closable for testing.
     *
     * @param throwable Optional throwable to throw from the close method.
     * @return The mock object.
     */
    private Closeable createCloseable(Throwable throwable)
    {
        Closeable closeable = EasyMock.createMock(Closeable.class);
        try
        {
            closeable.close();
        }
        catch (IOException e)
        {
            throw new ImpossibleException(e);
        }
        if (throwable != null)
        {
            EasyMock.expectLastCall().andThrow(throwable);
        }
        EasyMock.replay(closeable);
        return closeable;
    }

    /**
     * Test closing with the given object.
     *
     * @param closeable2 The test object.
     */
    private void testCloseWith(AutoCloseable closeable2)
    {
        AutoCloseable closeable1 = createAutoCloseable(null);
        AutoCloseable closeable3 = createAutoCloseable(null);

        try
        {
            Utilities.close(closeable1, closeable2, closeable3);
        }
        finally
        {
            EasyMock.verify(closeable1, closeable2, closeable3);
        }
    }
}
