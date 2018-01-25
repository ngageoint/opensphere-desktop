package io.opensphere.core.util.ref;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link TransparentEqualsWeakReference}.
 */
public class TransparentEqualsWeakReferenceTest
{
    /**
     * Test for {@link TransparentEqualsWeakReference#equals(Object)}.
     */
    @Test
    public void testEqualsObject()
    {
        TestClass testObj1 = new TestClass();
        TestClass testObj2 = new TestClass();
        TransparentEqualsWeakReference<TestClass> ref1a = new TransparentEqualsWeakReference<TransparentEqualsWeakReferenceTest.TestClass>(
                testObj1);
        TransparentEqualsWeakReference<TestClass> ref1b = new TransparentEqualsWeakReference<TransparentEqualsWeakReferenceTest.TestClass>(
                testObj1);
        TransparentEqualsWeakReference<TestClass> ref2 = new TransparentEqualsWeakReference<TransparentEqualsWeakReferenceTest.TestClass>(
                testObj2);

        Assert.assertTrue(ref1a.equals(ref1b));
        Assert.assertTrue(ref1b.equals(ref1a));
        Assert.assertFalse(ref2.equals(ref1a));
        Assert.assertFalse(ref1a.equals(ref2));
    }

    /**
     * Test for {@link TransparentEqualsWeakReference#hashCode()}.
     */
    @Test
    public void testHashCode()
    {
        TestClass testObj1 = new TestClass();
        TestClass testObj2 = new TestClass();
        TransparentEqualsWeakReference<TestClass> ref1a = new TransparentEqualsWeakReference<TransparentEqualsWeakReferenceTest.TestClass>(
                testObj1);
        TransparentEqualsWeakReference<TestClass> ref1b = new TransparentEqualsWeakReference<TransparentEqualsWeakReferenceTest.TestClass>(
                testObj1);
        TransparentEqualsWeakReference<TestClass> ref2 = new TransparentEqualsWeakReference<TransparentEqualsWeakReferenceTest.TestClass>(
                testObj2);

        Assert.assertTrue(ref1a.hashCode() == ref1b.hashCode());
        Assert.assertTrue(ref1a.hashCode() != ref2.hashCode());
    }

    /**
     * Test the use case of using a {@link TransparentEqualsWeakReference} in a
     * {@link HashMap}.
     */
    @Test
    public void testHashMap()
    {
        TestClass testObj1 = new TestClass();
        TestClass testObj2 = new TestClass();
        TransparentEqualsWeakReference<TestClass> ref1a = new TransparentEqualsWeakReference<TransparentEqualsWeakReferenceTest.TestClass>(
                testObj1);
        TransparentEqualsWeakReference<TestClass> ref1b = new TransparentEqualsWeakReference<TransparentEqualsWeakReferenceTest.TestClass>(
                testObj1);

        Map<Reference<TestClass>, TestClass> map = new HashMap<>();
        map.put(ref1a, testObj1);
        map.put(new TransparentEqualsWeakReference<TransparentEqualsWeakReferenceTest.TestClass>(testObj2), testObj2);
        Assert.assertTrue(map.containsKey(ref1b));
        Assert.assertEquals(testObj1, map.get(ref1b));
        Assert.assertEquals(testObj1, map.remove(ref1b));
    }

    /**
     * Test class.
     */
    private static class TestClass
    {
    }
}
