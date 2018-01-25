package io.opensphere.core.util.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link HeterogeneousSet}.
 */
public class HeterogeneousSetTest
{
    /**
     * Test for {@link HeterogeneousSet#addAll(java.util.Collection)}.
     */
    @Test
    public void testAddAllCollectionOfQextendsE()
    {
        TypeA a1 = new TypeA();
        TypeA a2 = new TypeA();
        TypeB b1 = new TypeB();
        TypeB b2 = new TypeB();
        HeterogeneousSet<Object> set = new HeterogeneousSet<>();

        Assert.assertEquals(0, set.size());

        set.addAll(Collections.singleton(a1));

        Assert.assertEquals(1, set.size());
        Assert.assertEquals(1, set.getObjectsOfClass(TypeA.class).size());
        Assert.assertEquals(0, set.getObjectsOfClass(TypeB.class).size());
        Assert.assertTrue(set.contains(a1));

        set.clear();
        Assert.assertEquals(0, set.size());
        Assert.assertEquals(0, set.getObjectsOfClass(TypeA.class).size());
        Assert.assertEquals(0, set.getObjectsOfClass(TypeB.class).size());

        set.addAll(Arrays.asList(a1, b1));

        Assert.assertEquals(2, set.size());
        Assert.assertEquals(1, set.getObjectsOfClass(TypeA.class).size());
        Assert.assertEquals(1, set.getObjectsOfClass(TypeB.class).size());
        Assert.assertTrue(set.contains(a1));
        Assert.assertTrue(set.contains(b1));

        set.clear();
        Assert.assertEquals(0, set.size());
        Assert.assertEquals(0, set.getObjectsOfClass(TypeA.class).size());
        Assert.assertEquals(0, set.getObjectsOfClass(TypeB.class).size());

        set.addAll(Arrays.asList(a1, a2, b1));

        Assert.assertEquals(3, set.size());
        Assert.assertEquals(2, set.getObjectsOfClass(TypeA.class).size());
        Assert.assertEquals(1, set.getObjectsOfClass(TypeB.class).size());
        Assert.assertTrue(set.contains(a1));
        Assert.assertTrue(set.contains(a2));
        Assert.assertTrue(set.contains(b1));

        set.clear();
        Assert.assertEquals(0, set.size());
        Assert.assertEquals(0, set.getObjectsOfClass(TypeA.class).size());
        Assert.assertEquals(0, set.getObjectsOfClass(TypeB.class).size());

        set.addAll(Arrays.asList(a1, a2, b1, b2));

        Assert.assertEquals(4, set.size());
        Assert.assertEquals(2, set.getObjectsOfClass(TypeA.class).size());
        Assert.assertEquals(2, set.getObjectsOfClass(TypeB.class).size());
        Assert.assertTrue(set.contains(a1));
        Assert.assertTrue(set.contains(a2));
        Assert.assertTrue(set.contains(b1));
        Assert.assertTrue(set.contains(b2));

        set.clear();
        Assert.assertEquals(0, set.size());
        Assert.assertEquals(0, set.getObjectsOfClass(TypeA.class).size());
        Assert.assertEquals(0, set.getObjectsOfClass(TypeB.class).size());
    }

    /**
     * Test for {@link HeterogeneousSet#getObjectsAsList()}.
     */
    @Test
    public void testGetObjectsAsList()
    {
        TypeA a1 = new TypeA();
        TypeA a2 = new TypeA();
        TypeB b1 = new TypeB();
        TypeB b2 = new TypeB();
        HeterogeneousSet<Object> set = new HeterogeneousSet<>();

        set.addAll(Arrays.asList(a1, a2, b1, b2, a1, a2));

        List<Object> list = set.getObjectsAsList();
        Assert.assertEquals(4, list.size());
        Assert.assertTrue(list.containsAll(set));
    }

    /** Test class. */
    private class TypeA
    {
    }

    /** Test class. */
    private class TypeB
    {
    }
}
