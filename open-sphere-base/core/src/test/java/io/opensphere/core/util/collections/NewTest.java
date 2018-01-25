package io.opensphere.core.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link New}. */
public class NewTest
{
    /** Test message. */
    private static final String ARRAYS_SHOULD_HAVE_BEEN_EQUAL = "Arrays should have been equal.";

    /** Test message. */
    private static final String ELEMENT_AT_INDEX = "Element at index ";

    /** Test message. */
    private static final String NOT_EQUAL = " not equal.";

    /** Test for {@link New#array(java.util.Collection, Class)}. */
    @Test
    public void testArrayCollectionClass()
    {
        Integer[] arr = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        List<Integer> list = Arrays.asList(arr);
        Integer[] arr1 = New.array(list, Integer.class);
        Assert.assertTrue(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, Arrays.equals(arr, arr1));
    }

    /** Test for {@link New#array(java.util.Collection, Class, int, int)}. */
    @Test
    public void testArrayCollectionClassIntInt()
    {
        Integer[] arr = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        List<Integer> list = Arrays.asList(arr);

        for (int prefix = 0; prefix < 10; ++prefix)
        {
            for (int postfix = 0; postfix < 10; ++postfix)
            {
                Integer[] result = New.array(list, Integer.class, prefix, postfix);
                for (int i = 0; i < prefix; ++i)
                {
                    Assert.assertNull("Element should have been null.", result[i]);
                }

                for (int i = 0; i < arr.length; ++i)
                {
                    Assert.assertSame("Elements do not match.", arr[i], result[prefix + i]);
                }
                for (int i = prefix + arr.length; i < prefix + arr.length + postfix; ++i)
                {
                    Assert.assertNull("Element should have been null.", result[i]);
                }
            }
        }
    }

    /** Test for {@link New#array(int[])}. */
    @Test
    public void testArrayIntArray()
    {
        int[] arr = new int[] { 1, 2, 3 };
        Integer[] result = New.array(arr);
        Integer[] expected = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), };
        Assert.assertTrue(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, Arrays.equals(expected, result));
    }

    /** Test for {@link New#array(Object...)}. */
    @Test
    public void testArrayObject()
    {
        Integer one = Integer.valueOf(1);
        Integer two = Integer.valueOf(2);
        Integer three = Integer.valueOf(3);
        Integer[] expected = new Integer[] { one, two, three };
        Integer[] actual = New.array(one, two, three);
        Assert.assertTrue(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, Arrays.equals(expected, actual));
    }

    /** Test for {@link New#array(Object, Class)}. */
    @Test
    public void testArrayObjectClass()
    {
        long[] arr = new long[] { 1L, 2L, 3L };
        Long[] result = New.array(arr, Long.class);
        Long[] expected = new Long[] { Long.valueOf(1L), Long.valueOf(2L), Long.valueOf(3L), };
        Assert.assertTrue(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, Arrays.equals(expected, result));
    }

    /** Test for {@link New#booleanArray(java.util.Collection)}. */
    @Test
    public void testBooleanArray()
    {
        Boolean[] arr = new Boolean[] { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE };
        List<Boolean> list = Arrays.asList(arr);
        boolean[] result = New.booleanArray(list);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, list.size(), result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].booleanValue(), result[index]);
        }
    }

    /** Test for {@link New#booleanArray(java.util.Collection)}. */
    @Test
    public void testBooleanArrayArray()
    {
        Boolean[] arr = new Boolean[] { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE };
        boolean[] result = New.booleanArray(arr);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr.length, result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].booleanValue(), result[index]);
        }
    }

    /** Test for {@link New#byteArray(java.util.Collection)}. */
    @Test
    public void testByteArray()
    {
        Byte[] arr = new Byte[] { Byte.valueOf((byte)1), Byte.valueOf((byte)2), Byte.valueOf((byte)3) };
        List<Byte> list = Arrays.asList(arr);
        byte[] result = New.byteArray(list);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, list.size(), result.length);
        for (byte index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].byteValue(), result[index]);
        }
    }

    /** Test for {@link New#byteArray(java.util.Collection)}. */
    @Test
    public void testByteArrayArray()
    {
        Byte[] arr = new Byte[] { Byte.valueOf((byte)1), Byte.valueOf((byte)2), Byte.valueOf((byte)3) };
        byte[] result = New.byteArray(arr);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr.length, result.length);
        for (byte index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].byteValue(), result[index]);
        }
    }

    /** Test for {@link New#charArray(java.util.Collection)}. */
    @Test
    public void testCharArray()
    {
        Character[] arr = new Character[] { Character.valueOf((char)1), Character.valueOf((char)2), Character.valueOf((char)3) };
        List<Character> list = Arrays.asList(arr);
        char[] result = New.charArray(list);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, list.size(), result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].charValue(), result[index]);
        }
    }

    /** Test for {@link New#charArray(java.util.Collection)}. */
    @Test
    public void testCharArrayArray()
    {
        Character[] arr = new Character[] { Character.valueOf((char)1), Character.valueOf((char)2), Character.valueOf((char)3) };
        char[] result = New.charArray(arr);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr.length, result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].charValue(), result[index]);
        }
    }

    /** Test for {@link New#doubleArray(java.util.Collection)}. */
    @Test
    public void testDoubleArray()
    {
        Double[] arr = new Double[] { Double.valueOf(1), Double.valueOf(2), Double.valueOf(3) };
        List<Double> list = Arrays.asList(arr);
        double[] result = New.doubleArray(list);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, list.size(), result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].doubleValue(), result[index], 0.);
        }
    }

    /** Test for {@link New#doubleArray(java.util.Collection)}. */
    @Test
    public void testDoubleArrayArray()
    {
        Double[] arr = new Double[] { Double.valueOf(1), Double.valueOf(2), Double.valueOf(3) };
        double[] result = New.doubleArray(arr);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr.length, result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].doubleValue(), result[index], 0.);
        }
    }

    /** Test for {@link New#floatArray(java.util.Collection)}. */
    @Test
    public void testFloatArray()
    {
        Float[] arr = new Float[] { Float.valueOf(1), Float.valueOf(2), Float.valueOf(3) };
        List<Float> list = Arrays.asList(arr);
        float[] result = New.floatArray(list);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, list.size(), result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].floatValue(), result[index], 0f);
        }
    }

    /** Test for {@link New#floatArray(java.util.Collection)}. */
    @Test
    public void testFloatArrayArray()
    {
        Float[] arr = new Float[] { Float.valueOf(1), Float.valueOf(2), Float.valueOf(3) };
        float[] result = New.floatArray(arr);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr.length, result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].floatValue(), result[index], 0f);
        }
    }

    /** Test for {@link New#intArray(java.util.Collection)}. */
    @Test
    public void testIntArray()
    {
        Integer[] arr = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        List<Integer> list = Arrays.asList(arr);
        int[] result = New.intArray(list);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, list.size(), result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].intValue(), result[index]);
        }
    }

    /** Test for {@link New#intArray(Integer[])}. */
    @Test
    public void testIntArrayArray()
    {
        Integer[] arr = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        int[] result = New.intArray(arr);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr.length, result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].intValue(), result[index]);
        }
    }

    /** Test for {@link New#longArray(java.util.Collection)}. */
    @Test
    public void testLongArray()
    {
        Long[] arr = new Long[] { Long.valueOf(1), Long.valueOf(2), Long.valueOf(3) };
        List<Long> list = Arrays.asList(arr);
        long[] result = New.longArray(list);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, list.size(), result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].longValue(), result[index]);
        }
    }

    /** Test for {@link New#longArray(Collection)}. */
    @Test
    public void testLongArrayArray()
    {
        Long[] arr = new Long[] { Long.valueOf(1), Long.valueOf(2), Long.valueOf(3) };
        long[] result = New.longArray(arr);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr.length, result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].longValue(), result[index]);
        }
    }

    /** Test for {@link New#shortArray(java.util.Collection)}. */
    @Test
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public void testShortArray()
    {
        Short[] arr = new Short[] { Short.valueOf((short)1), Short.valueOf((short)2), Short.valueOf((short)3) };
        List<Short> list = Arrays.asList(arr);
        short[] result = New.shortArray(list);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, list.size(), result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].shortValue(), result[index]);
        }
    }

    /** Test for {@link New#shortArray(java.util.Collection)}. */
    @Test
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public void testShortArrayArray()
    {
        Short[] arr = new Short[] { Short.valueOf((short)1), Short.valueOf((short)2), Short.valueOf((short)3) };
        short[] result = New.shortArray(arr);
        Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr.length, result.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ELEMENT_AT_INDEX + index + NOT_EQUAL, arr[index].shortValue(), result[index]);
        }
    }

    /** Test for {@link New#uncheckedArray(java.util.Collection, Class)}. */
    @Test
    public void testUncheckedArrayCollection()
    {
        Integer[] arr = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        List<Integer> list = Arrays.asList(arr);
        Object arr1 = New.uncheckedArray(list, Integer.class);
        Assert.assertTrue(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, Arrays.equals(arr, (Object[])arr1));
    }

    /** Test for {@link New#uncheckedArray(Object, Class)}. */
    @Test
    public void testUncheckedArrayObject()
    {
        Integer[] arr = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        int[] arr1 = (int[])New.uncheckedArray(arr, int.class);
        Assert.assertTrue(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr1.length == arr.length);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, arr[index].intValue(), arr1[index]);
        }
    }

    /** Test for {@link New#unmodifiableCollection(java.util.Collection)}. */
    @SuppressWarnings("unchecked")
    @Test
    public void testUnmodifiableCollection()
    {
        Collection<Object> empty = new ArrayList<>(0);
        Collection<Object> emptyUnmod = (Collection<Object>)New.unmodifiableCollection(empty);
        empty.add(new Object());

        Assert.assertNotSame(empty, emptyUnmod);
        Assert.assertFalse(empty.isEmpty());
        Assert.assertTrue(emptyUnmod.isEmpty());

        try
        {
            emptyUnmod.add(new Object());
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }

        Collection<Object> singleton = new ArrayList<>(1);
        singleton.add(new Object());
        Collection<Object> singletonUnmod = (Collection<Object>)New.unmodifiableCollection(singleton);
        singleton.add(new Object());

        Assert.assertNotSame(singleton, singletonUnmod);
        Assert.assertTrue(singleton.size() == 2);
        Assert.assertTrue(singletonUnmod.size() == 1);

        try
        {
            singletonUnmod.add(new Object());
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }

        Collection<Object> col = new ArrayList<>(4);
        col.add(new Object());
        col.add(new Object());
        col.add(new Object());
        Collection<Object> colUnmod = (Collection<Object>)New.unmodifiableCollection(col);
        Assert.assertTrue(colUnmod.containsAll(col));
        col.add(new Object());

        Assert.assertNotSame(col, colUnmod);
        Assert.assertTrue(col.size() == 4);
        Assert.assertTrue(colUnmod.size() == 3);

        try
        {
            colUnmod.add(new Object());
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }
    }

    /** Test for {@link New#unmodifiableList(java.util.Collection)}. */
    @Test
    public void testUnmodifiableList()
    {
        Collection<Object> empty = new ArrayList<>(0);
        List<Object> emptyUnmod = New.unmodifiableList(empty);
        empty.add(new Object());

        Assert.assertNotSame(empty, emptyUnmod);
        Assert.assertFalse(empty.isEmpty());
        Assert.assertTrue(emptyUnmod.isEmpty());

        try
        {
            emptyUnmod.add(new Object());
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }

        Collection<Object> singleton = new ArrayList<>(1);
        singleton.add(new Object());
        List<Object> singletonUnmodList = New.unmodifiableList(singleton);
        singleton.add(new Object());

        Assert.assertNotSame(singleton, singletonUnmodList);
        Assert.assertTrue(singleton.size() == 2);
        Assert.assertTrue(singletonUnmodList.size() == 1);

        try
        {
            singletonUnmodList.add(new Object());
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }

        Collection<Object> col = new ArrayList<>(4);
        col.add(new Object());
        col.add(new Object());
        col.add(new Object());
        List<Object> colUnmod = New.unmodifiableList(col);
        Assert.assertEquals(col, colUnmod);
        col.add(new Object());

        Assert.assertNotSame(col, colUnmod);
        Assert.assertTrue(col.size() == 4);
        Assert.assertTrue(colUnmod.size() == 3);

        try
        {
            colUnmod.add(new Object());
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }
    }

    /** Test for {@link New#unmodifiableMap(java.util.Map)}. */
    @Test
    public void testUnmodifiableMap()
    {
        Map<Integer, Float> empty = new HashMap<>(0);
        Map<Integer, Float> emptyUnmod = New.unmodifiableMap(empty);
        empty.put(Integer.valueOf(1), Float.valueOf(1f));

        Assert.assertNotSame(empty, emptyUnmod);
        Assert.assertFalse(empty.isEmpty());
        Assert.assertTrue(emptyUnmod.isEmpty());

        try
        {
            emptyUnmod.put(Integer.valueOf(1), Float.valueOf(1f));
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }

        Map<Integer, Float> singleton = new HashMap<>(1);
        singleton.put(Integer.valueOf(1), Float.valueOf(1f));
        Map<Integer, Float> singletonUnmod = New.unmodifiableMap(singleton);
        singleton.put(Integer.valueOf(2), Float.valueOf(2f));

        Assert.assertNotSame(singleton, singletonUnmod);
        Assert.assertTrue(singleton.size() == 2);
        Assert.assertTrue(singletonUnmod.size() == 1);

        try
        {
            singletonUnmod.put(Integer.valueOf(2), Float.valueOf(2f));
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }

        Map<Integer, Float> map = new HashMap<>(4);
        map.put(Integer.valueOf(1), Float.valueOf(1f));
        map.put(Integer.valueOf(2), Float.valueOf(2f));
        map.put(Integer.valueOf(3), Float.valueOf(3f));
        Map<Integer, Float> mapUnmod = New.unmodifiableMap(map);
        Assert.assertEquals(map, mapUnmod);
        map.put(Integer.valueOf(4), Float.valueOf(4f));

        Assert.assertNotSame(map, mapUnmod);
        Assert.assertTrue(map.size() == 4);
        Assert.assertTrue(mapUnmod.size() == 3);

        try
        {
            mapUnmod.put(Integer.valueOf(4), Float.valueOf(4f));
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }
    }

    /** Test for {@link New#unmodifiableSet(java.util.Collection)}. */
    @SuppressWarnings("unchecked")
    @Test
    public void testUnmodifiableSet()
    {
        Collection<Object> empty = new ArrayList<>(0);
        Set<Object> emptyUnmod = (Set<Object>)New.unmodifiableSet(empty);
        empty.add(new Object());

        Assert.assertNotSame(empty, emptyUnmod);
        Assert.assertFalse(empty.isEmpty());
        Assert.assertTrue(emptyUnmod.isEmpty());

        try
        {
            emptyUnmod.add(new Object());
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }

        Collection<Object> singleton = new ArrayList<>(1);
        singleton.add(new Object());
        Set<Object> singletonUnmodSet = (Set<Object>)New.unmodifiableSet(singleton);
        singleton.add(new Object());

        Assert.assertNotSame(singleton, singletonUnmodSet);
        Assert.assertTrue(singleton.size() == 2);
        Assert.assertTrue(singletonUnmodSet.size() == 1);

        try
        {
            singletonUnmodSet.add(new Object());
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }

        Collection<Object> col = new ArrayList<>(4);
        col.add(new Object());
        col.add(new Object());
        col.add(new Object());
        Set<Object> colUnmod = (Set<Object>)New.unmodifiableSet(col);
        Assert.assertTrue(colUnmod.containsAll(col));
        col.add(new Object());

        Assert.assertNotSame(col, colUnmod);
        Assert.assertTrue(col.size() == 4);
        Assert.assertTrue(colUnmod.size() == 3);

        try
        {
            colUnmod.add(new Object());
            Assert.fail();
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(true);
        }
    }
}
