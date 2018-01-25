package io.opensphere.core.util.collections;

import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link CompactMap}.
 */
public class CompactMapTest
{
    /** Run through the common operations. */
    @Test
    public void testAll()
    {
        CompactMap<Object, Object> map1 = new CompactMap<>();
        Assert.assertTrue(map1.isEmpty());

        CompactMap<Object, Object> map2 = new CompactMap<>();
        Assert.assertTrue(map1.isEmpty());

        // Add an object to map1.
        Object key1 = new Object();
        Object value1 = new Object();
        Assert.assertNull(map1.put(key1, value1));
        Assert.assertEquals(1, map1.size());
        Assert.assertEquals(value1, map1.get(key1));

        // Add the objects from map1 to map2.
        map2.putAll(map1);
        Assert.assertEquals(1, map1.size());
        Assert.assertEquals(1, map2.size());
        compareMaps(map1, map2);

        // Add another object to map1, using the same key.
        Object value2 = new Object();
        Assert.assertEquals(value1, map1.put(key1, value2));
        Assert.assertEquals(1, map1.size());
        Assert.assertEquals(1, map2.size());

        // Add the objects from map1 to map2.
        map2.putAll(map1);
        Assert.assertEquals(1, map1.size());
        Assert.assertEquals(1, map2.size());
        compareMaps(map1, map2);

        // Add the last object to map1, using a new key.
        Object key2 = new Object();
        Assert.assertNull(map1.put(key2, value2));
        Assert.assertEquals(2, map1.size());
        Assert.assertEquals(value2, map1.get(key1));
        Assert.assertEquals(value2, map1.get(key2));

        // Add the objects from map1 to map2.
        map2.putAll(map1);
        Assert.assertEquals(2, map1.size());
        Assert.assertEquals(2, map2.size());
        compareMaps(map1, map2);

        // Add another object to map1.
        Object key3 = new Object();
        Object value3 = new Object();
        Assert.assertNull(map1.put(key3, value3));
        Assert.assertEquals(3, map1.size());
        Assert.assertEquals(value2, map1.get(key1));
        Assert.assertEquals(value2, map1.get(key2));
        Assert.assertEquals(value3, map1.get(key3));

        // Add the objects from map1 to map2.
        map2.putAll(map1);
        Assert.assertEquals(3, map1.size());
        Assert.assertEquals(3, map2.size());
        compareMaps(map1, map2);

        // Remove an object from map1.
        map1.remove(key1);
        Assert.assertEquals(2, map1.size());
        Assert.assertFalse(map1.containsKey(key1));

        map1.remove(key2);
        Assert.assertEquals(1, map1.size());
        Assert.assertFalse(map1.containsKey(key2));

        map1.remove(key3);
        Assert.assertTrue(map1.isEmpty());

        map2.clear();
        Assert.assertTrue(map2.isEmpty());
    }

    /**
     * Compare two maps and see if they have the same contents.
     *
     * @param map1 One map.
     * @param map2 Another map.
     */
    private void compareMaps(CompactMap<Object, Object> map1, CompactMap<Object, Object> map2)
    {
        Assert.assertEquals(map1.size(), map2.size());
        for (Entry<Object, Object> entry : map1.entrySet())
        {
            Assert.assertEquals(entry.getValue(), map2.get(entry.getKey()));
        }
    }
}
