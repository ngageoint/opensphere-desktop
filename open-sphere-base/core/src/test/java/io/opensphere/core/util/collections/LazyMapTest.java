package io.opensphere.core.util.collections;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.util.collections.LazyMap.Factory;

/**
 * Test for {@link LazyMap}.
 */
public class LazyMapTest
{
    /** A value for the null key. */
    private static final String NULL_VALUE1 = "null";

    /** Another value for the null key. */
    private static final String NULL_VALUE2 = "new";

    /** One. */
    private static final String ONE = "one";

    /** Two. */
    private static final String TWO = "two";

    /** Three. */
    private static final String THREE = "three";

    /** Four. */
    private static final String FOUR = "four";

    /** A factory for the map. */
    private final Factory<? super Integer, ? extends String> myFactory = new Factory<Integer, String>()
    {
        @Override
        public String create(Integer key)
        {
            return key == null ? NULL_VALUE1 : key.toString();
        }
    };

    /** The map being tested. */
    private Map<Integer, String> myLazyMap;

    /** The wrapped map. */
    private Map<Integer, String> myWrappedMap;

    /**
     * Setup for the tests.
     */
    @Before
    public void beforeTest()
    {
        myWrappedMap = new HashMap<>();
        myLazyMap = new LazyMap<>(myWrappedMap, Integer.class, myFactory);
        myLazyMap.get(null);
        myLazyMap.get(Integer.valueOf(1));
        myLazyMap.get(Integer.valueOf(2));
        myLazyMap.get(Integer.valueOf(3));
        myLazyMap.put(Integer.valueOf(4), FOUR);
    }

    /**
     * Test for {@link LazyMap#clear()}.
     */
    @Test
    public void testClear()
    {
        Assert.assertFalse(myWrappedMap.isEmpty());
        myLazyMap.clear();
        Assert.assertTrue(myWrappedMap.isEmpty());
    }

    /**
     * Test for {@link LazyMap#containsKey(Object)}.
     */
    @Test
    public void testContainsKey()
    {
        Assert.assertTrue(myLazyMap.containsKey(null));
        Assert.assertTrue(myLazyMap.containsKey(Integer.valueOf(1)));
        Assert.assertFalse(myLazyMap.containsKey(Integer.valueOf(0)));
        myLazyMap.get(Integer.valueOf(0));
        Assert.assertTrue(myLazyMap.containsKey(Integer.valueOf(0)));
    }

    /**
     * Test for {@link LazyMap#containsValue(Object)}.
     */
    @Test
    public void testContainsValue()
    {
        Assert.assertTrue(myLazyMap.containsValue(NULL_VALUE1));
        Assert.assertTrue(myLazyMap.containsValue("1"));
        Assert.assertFalse(myLazyMap.containsValue("0"));
        myLazyMap.get(Integer.valueOf(0));
        Assert.assertTrue(myLazyMap.containsValue("0"));
    }

    /**
     * Test for {@link LazyMap#entrySet()}.
     */
    @Test
    public void testEntrySet()
    {
        Assert.assertEquals(myWrappedMap.entrySet(), myLazyMap.entrySet());
    }

    /**
     * Test for {@link LazyMap#get(Object)}.
     */
    @Test
    public void testGet()
    {
        Assert.assertEquals(NULL_VALUE1, myLazyMap.get(null));
        Assert.assertEquals("1", myLazyMap.get(Integer.valueOf(1)));
        Assert.assertEquals("2", myLazyMap.get(Integer.valueOf(2)));
        Assert.assertEquals("3", myLazyMap.get(Integer.valueOf(3)));
        Assert.assertEquals(FOUR, myLazyMap.get(Integer.valueOf(4)));
        Assert.assertEquals("5", myLazyMap.get(Integer.valueOf(5)));

        Assert.assertNull(myLazyMap.get(new Object()));
        Assert.assertNull(myLazyMap.get(Float.valueOf(1f)));
    }

    /**
     * Test for {@link LazyMap#isEmpty()}.
     */
    @Test
    public void testIsEmpty()
    {
        Assert.assertFalse(myLazyMap.isEmpty());
        myWrappedMap.clear();
        Assert.assertTrue(myLazyMap.isEmpty());
    }

    /**
     * Test for {@link LazyMap#keySet()}.
     */
    @Test
    public void testKeySet()
    {
        Assert.assertEquals(myWrappedMap.keySet(), myLazyMap.keySet());
    }

    /**
     * Test for {@link LazyMap#put(Object, Object)}.
     */
    @Test
    public void testPut()
    {
        Assert.assertEquals("1", myLazyMap.put(Integer.valueOf(1), ONE));
        Assert.assertEquals(ONE, myWrappedMap.get(Integer.valueOf(1)));
        Assert.assertEquals(NULL_VALUE1, myLazyMap.put(null, NULL_VALUE2));
        Assert.assertEquals(NULL_VALUE2, myWrappedMap.get(null));
    }

    /**
     * Test for {@link LazyMap#putAll(java.util.Map)}.
     */
    @Test
    public void testPutAll()
    {
        Map<Integer, String> map = new HashMap<>();
        map.put(null, NULL_VALUE2);
        map.put(Integer.valueOf(1), ONE);
        map.put(Integer.valueOf(2), TWO);
        map.put(Integer.valueOf(3), THREE);

        myLazyMap.putAll(map);

        Assert.assertEquals(NULL_VALUE2, myLazyMap.get(null));
        Assert.assertEquals(ONE, myLazyMap.get(Integer.valueOf(1)));
        Assert.assertEquals(TWO, myLazyMap.get(Integer.valueOf(2)));
        Assert.assertEquals(THREE, myLazyMap.get(Integer.valueOf(3)));
    }

    /**
     * Test for {@link LazyMap#remove(Object)}.
     */
    @Test
    public void testRemove()
    {
        Assert.assertTrue(myLazyMap.containsKey(null));
        Assert.assertTrue(myLazyMap.containsKey(Integer.valueOf(1)));
        int sizeBefore = myWrappedMap.size();
        Assert.assertNull(myLazyMap.remove(Integer.valueOf(0)));
        Assert.assertEquals(NULL_VALUE1, myLazyMap.remove(null));
        Assert.assertEquals("1", myLazyMap.remove(Integer.valueOf(1)));
        Assert.assertEquals(sizeBefore - 2, myWrappedMap.size());
    }

    /**
     * Test for {@link LazyMap#size()}.
     */
    @Test
    public void testSize()
    {
        Assert.assertEquals(myWrappedMap.size(), myLazyMap.size());

        int count = myLazyMap.size();
        myLazyMap.get(Integer.valueOf(4));
        Assert.assertEquals(count, myLazyMap.size());
        myLazyMap.get(Integer.valueOf(5));
        Assert.assertEquals(count + 1, myLazyMap.size());
        myLazyMap.put(Integer.valueOf(5), "five");
        Assert.assertEquals(count + 1, myLazyMap.size());
        myLazyMap.put(Integer.valueOf(6), "six");
        Assert.assertEquals(count + 2, myLazyMap.size());

        myWrappedMap.clear();
        Assert.assertEquals(0, myLazyMap.size());
    }

    /**
     * Test for {@link LazyMap#values()}.
     */
    @Test
    public void testValues()
    {
        Assert.assertEquals(myWrappedMap.values(), myLazyMap.values());
    }
}
