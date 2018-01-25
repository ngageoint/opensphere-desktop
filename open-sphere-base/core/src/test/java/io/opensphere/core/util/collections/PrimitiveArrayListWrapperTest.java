package io.opensphere.core.util.collections;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link PrimitiveArrayListWrapper}. */
public class PrimitiveArrayListWrapperTest
{
    /** Test for {@link PrimitiveArrayListWrapper}. */
    @Test
    public void test()
    {
        int[] arr = new int[] { 5, 3, 1 };
        List<Integer> list = new PrimitiveArrayListWrapper<>(arr, Integer.class);

        Assert.assertEquals(arr.length, list.size());
        for (int index = 0; index < list.size(); ++index)
        {
            Assert.assertEquals(Integer.valueOf(arr[index]), list.get(index));
        }
    }

    /** Test for {@link PrimitiveArrayListWrapper#add(Object)}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd()
    {
        new PrimitiveArrayListWrapper<Object>(new Integer[0], Object.class).add(new Object());
    }

    /** Test for {@link PrimitiveArrayListWrapper#clear()}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testClear()
    {
        new PrimitiveArrayListWrapper<Object>(new Integer[1], Object.class).clear();
    }

    /** Test for {@link PrimitiveArrayListWrapper} with compatible arguments. */
    @SuppressWarnings("unused")
    @Test
    public void testCompatible()
    {
        new PrimitiveArrayListWrapper<Object>(new Integer[0], Object.class);
    }

    /**
     * Test for {@link PrimitiveArrayListWrapper} with incompatible arguments.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testIncompatible()
    {
        new PrimitiveArrayListWrapper<Integer>(new Object[0], Integer.class);
    }

    /** Test for {@link PrimitiveArrayListWrapper} with non-array. */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testNonArray()
    {
        new PrimitiveArrayListWrapper<Integer>(new Object(), Integer.class);
    }

    /** Test for {@link PrimitiveArrayListWrapper} with null array. */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testNullArray()
    {
        new PrimitiveArrayListWrapper<Integer>(null, Integer.class);
    }

    /** Test for {@link PrimitiveArrayListWrapper#set(int, Object)}. */
    @Test(expected = UnsupportedOperationException.class)
    public void testSet()
    {
        new PrimitiveArrayListWrapper<Object>(new Integer[0], Object.class).set(0, new Object());
    }
}
