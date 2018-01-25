package io.opensphere.core.util.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link TinySet}. */
public class TinySetTest
{
    /** Test object. */
    private static final Object TEST_OBJ1 = new Object();

    /** Test object. */
    private static final Object TEST_OBJ2 = new Object();

    /** Test object. */
    private static final Object TEST_OBJ3 = new Object();

    /** Test for {@link TinySet#add(Object)}. */
    @Test
    public void testAdd()
    {
        TinySet<Object> set = new TinySet<>();
        checkSize0(set);
        Assert.assertTrue(set.add(TEST_OBJ1));
        checkSize1(set);
        Assert.assertFalse(set.add(TEST_OBJ1));
        checkSize1(set);
        Assert.assertTrue(set.add(TEST_OBJ2));
        checkSizeN(set, 2);
        Assert.assertTrue(set.add(TEST_OBJ3));
        checkSizeN(set, 3);
    }

    /** Test for {@link TinySet#addAll(java.util.Collection)}. */
    @Test
    public void testAddAll()
    {
        TinySet<Object> set = new TinySet<>();
        checkSize0(set);
        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1)));
        checkSize1(set);
        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2)));
        checkSizeN(set, 2);
        Assert.assertFalse(set.addAll(Arrays.asList(TEST_OBJ1)));
        checkSizeN(set, 2);
        Assert.assertFalse(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2)));
        checkSizeN(set, 2);
        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        checkSizeN(set, 3);
        Assert.assertFalse(set.addAll(Collections.emptySet()));
        checkSizeN(set, 3);
    }

    /** Test for {@link TinySet#clear()}. */
    @Test
    public void testClear()
    {
        TinySet<Object> set = new TinySet<>();
        set.clear();
        checkSize0(set);
        Assert.assertTrue(set.add(TEST_OBJ1));
        checkSize1(set);
        set.clear();
        checkSize0(set);
        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        checkSizeN(set, 3);
        set.clear();
        checkSize0(set);
    }

    /** Test for {@link TinySet#remove(Object)}. */
    @Test
    public void testRemove()
    {
        TinySet<Object> set = new TinySet<>();
        Assert.assertFalse(set.remove(TEST_OBJ2));
        checkSize0(set);
        Assert.assertTrue(set.add(TEST_OBJ1));
        checkSize1(set);
        Assert.assertFalse(set.remove(TEST_OBJ2));
        checkSize1(set);
        Assert.assertTrue(set.remove(TEST_OBJ1));
        checkSize0(set);
        Assert.assertFalse(set.remove(TEST_OBJ1));
        checkSize0(set);

        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        checkSizeN(set, 3);
        Assert.assertTrue(set.remove(TEST_OBJ1));
        checkSizeN(set, 2);
        Assert.assertFalse(set.remove(TEST_OBJ1));
        checkSizeN(set, 2);
        Assert.assertTrue(set.remove(TEST_OBJ3));
        checkSize1(set);
        Assert.assertTrue(set.remove(TEST_OBJ2));
        checkSize0(set);
    }

    /** Test for {@link TinySet#removeAll(java.util.Collection)}. */
    @Test
    public void testRemoveAll()
    {
        TinySet<Object> set = new TinySet<>();
        Assert.assertFalse(set.removeAll(Collections.emptySet()));
        checkSize0(set);

        Assert.assertTrue(set.add(TEST_OBJ1));
        checkSize1(set);
        Assert.assertFalse(set.removeAll(Collections.emptySet()));
        checkSize1(set);
        Assert.assertTrue(set.removeAll(Arrays.asList(TEST_OBJ1)));
        checkSize0(set);
        Assert.assertFalse(set.removeAll(Arrays.asList(TEST_OBJ1)));
        checkSize0(set);

        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        checkSizeN(set, 3);
        Assert.assertFalse(set.removeAll(Collections.emptySet()));
        checkSizeN(set, 3);
        Assert.assertTrue(set.removeAll(Arrays.asList(TEST_OBJ1)));
        checkSizeN(set, 2);
        Assert.assertFalse(set.removeAll(Arrays.asList(TEST_OBJ1)));
        checkSizeN(set, 2);

        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        checkSizeN(set, 3);
        Assert.assertTrue(set.removeAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2)));
        checkSize1(set);
        Assert.assertFalse(set.removeAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2)));
        checkSize1(set);
        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        checkSizeN(set, 3);
        Assert.assertTrue(set.removeAll(Arrays.asList(TEST_OBJ1, TEST_OBJ3, TEST_OBJ2)));
        checkSize0(set);
        Assert.assertFalse(set.removeAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2)));
        checkSize0(set);
    }

    /** Test for {@link TinySet#retainAll(java.util.Collection)}. */
    @Test
    public void testRetainAll()
    {
        TinySet<Object> set = new TinySet<>();
        Assert.assertFalse(set.retainAll(Arrays.asList(TEST_OBJ1)));
        checkSize0(set);

        Assert.assertTrue(set.add(TEST_OBJ1));
        checkSize1(set);
        Assert.assertFalse(set.retainAll(Arrays.asList(TEST_OBJ1)));
        checkSize1(set);
        Assert.assertTrue(set.retainAll(Collections.emptySet()));
        checkSize0(set);

        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        checkSizeN(set, 3);
        Assert.assertTrue(set.retainAll(Collections.emptySet()));
        checkSize0(set);

        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        Assert.assertTrue(set.retainAll(Arrays.asList(TEST_OBJ1)));
        checkSize1(set);
        Assert.assertFalse(set.retainAll(Arrays.asList(TEST_OBJ1)));
        checkSize1(set);
        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        checkSizeN(set, 3);
        Assert.assertTrue(set.retainAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2)));
        checkSizeN(set, 2);
        Assert.assertFalse(set.retainAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2)));
        checkSizeN(set, 2);

        Assert.assertTrue(set.addAll(Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3)));
        checkSizeN(set, 3);
        Assert.assertFalse(set.retainAll(Arrays.asList(TEST_OBJ1, TEST_OBJ3, TEST_OBJ2)));
        checkSizeN(set, 3);
    }

    /** Test for {@link TinySet#add(Set, Object)}. */
    @Test
    public void testStaticAdd()
    {
        Set<Object> set = Collections.emptySet();
        checkSize0(set);
        set = TinySet.add(set, TEST_OBJ1);
        checkSize1(set);
        Assert.assertTrue(set.contains(TEST_OBJ1));
        set = TinySet.add(set, TEST_OBJ1);
        checkSize1(set);
        Assert.assertTrue(set.contains(TEST_OBJ1));
        set = TinySet.add(set, TEST_OBJ2);
        checkSizeN(set, 2);
        Assert.assertTrue(set.contains(TEST_OBJ2));
        set = TinySet.add(set, TEST_OBJ3);
        checkSizeN(set, 3);
        Assert.assertTrue(set.contains(TEST_OBJ3));
    }

    /** Test for {@link TinySet#addAll(Set, java.util.Collection)}. */
    @Test
    public void testStaticAddAll()
    {
        Set<Object> set = Collections.emptySet();
        checkSize0(set);
        set = TinySet.addAll(set, Collections.emptySet());
        checkSize0(set);
        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1));
        checkSize1(set);
        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2));
        checkSizeN(set, 2);
        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1));
        checkSizeN(set, 2);
        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2));
        checkSizeN(set, 2);
        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3));
        checkSizeN(set, 3);
    }

    /** Test for {@link TinySet#remove(Set, Object)}. */
    @Test
    public void testStaticRemove()
    {
        Set<Object> set = Collections.emptySet();
        set = TinySet.remove(set, TEST_OBJ2);
        checkSize0(set);
        set = TinySet.add(set, TEST_OBJ1);
        checkSize1(set);
        set = TinySet.remove(set, TEST_OBJ2);
        checkSize1(set);
        set = TinySet.remove(set, TEST_OBJ1);
        checkSize0(set);

        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3));
        checkSizeN(set, 3);
        set = TinySet.remove(set, TEST_OBJ1);
        checkSizeN(set, 2);
        set = TinySet.remove(set, TEST_OBJ1);
        checkSizeN(set, 2);
        set = TinySet.remove(set, TEST_OBJ3);
        checkSize1(set);
        set = TinySet.remove(set, TEST_OBJ2);
        checkSize0(set);
    }

    /**
     * Test for {@link TinySet#removeAll(java.util.Set, java.util.Collection)}.
     */
    @Test
    public void testStaticRemoveAll()
    {
        Set<Object> set = Collections.emptySet();
        set = TinySet.removeAll(set, Collections.emptySet());
        checkSize0(set);

        set = TinySet.add(set, TEST_OBJ1);
        checkSize1(set);
        set = TinySet.removeAll(set, Collections.emptySet());
        checkSize1(set);
        set = TinySet.removeAll(set, Arrays.asList(TEST_OBJ1));
        checkSize0(set);
        set = TinySet.removeAll(set, Arrays.asList(TEST_OBJ1));
        checkSize0(set);

        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3));
        checkSizeN(set, 3);
        set = TinySet.removeAll(set, Collections.emptySet());
        checkSizeN(set, 3);
        set = TinySet.removeAll(set, Arrays.asList(TEST_OBJ1));
        Assert.assertFalse(set.contains(TEST_OBJ1));
        checkSizeN(set, 2);
        set = TinySet.removeAll(set, Arrays.asList(TEST_OBJ1));
        checkSizeN(set, 2);

        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3));
        checkSizeN(set, 3);
        set = TinySet.removeAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2));
        Assert.assertTrue(set.contains(TEST_OBJ3));
        checkSize1(set);
        set = TinySet.removeAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2));
        Assert.assertTrue(set.contains(TEST_OBJ3));
        checkSize1(set);
        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3));
        checkSizeN(set, 3);
        set = TinySet.removeAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ3, TEST_OBJ2));
        checkSize0(set);
        set = TinySet.removeAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2));
        checkSize0(set);
    }

    /** Test for {@link TinySet#retainAll(Set, java.util.Collection)}. */
    @Test
    public void testStaticRetainAll()
    {
        Set<Object> set = Collections.emptySet();
        set = TinySet.retainAll(set, Arrays.asList(TEST_OBJ1));
        checkSize0(set);

        set = TinySet.add(set, TEST_OBJ1);
        checkSize1(set);
        set = TinySet.retainAll(set, Arrays.asList(TEST_OBJ1));
        checkSize1(set);
        set = TinySet.retainAll(set, Arrays.asList(TEST_OBJ2));
        checkSize0(set);

        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3));
        checkSizeN(set, 3);
        set = TinySet.retainAll(set, Collections.emptySet());
        checkSize0(set);

        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3));
        set = TinySet.retainAll(set, Arrays.asList(TEST_OBJ1));
        checkSize1(set);
        set = TinySet.retainAll(set, Arrays.asList(TEST_OBJ1));
        checkSize1(set);
        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3));
        checkSizeN(set, 3);
        set = TinySet.retainAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2));
        checkSizeN(set, 2);
        set = TinySet.retainAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2));
        checkSizeN(set, 2);

        set = TinySet.addAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ2, TEST_OBJ3));
        checkSizeN(set, 3);
        set = TinySet.retainAll(set, Arrays.asList(TEST_OBJ1, TEST_OBJ3, TEST_OBJ2));
        checkSizeN(set, 3);
    }

    /**
     * Check that the set is size 0 and is an {@code EmptySet}.
     *
     * @param set The set.
     */
    private void checkSize0(Set<Object> set)
    {
        Assert.assertEquals(0, set.size());
        Assert.assertEquals("java.util.Collections$EmptySet", set.getClass().getName());
    }

    /**
     * Check that the {@link TinySet} has a wrapped {@code EmptySet}.
     *
     * @param set The set.
     */
    private void checkSize0(TinySet<Object> set)
    {
        checkSize0(set.getSet());
    }

    /**
     * Check that the set is size 1 and is a {@code SingletonSet}.
     *
     * @param set The set.
     */
    private void checkSize1(Set<Object> set)
    {
        Assert.assertEquals(1, set.size());
        Assert.assertEquals("java.util.Collections$SingletonSet", set.getClass().getName());
    }

    /**
     * Check that the {@link TinySet} has a wrapped {@code SingletonSet}.
     *
     * @param set The set.
     */
    private void checkSize1(TinySet<Object> set)
    {
        Assert.assertEquals(1, set.size());
        Assert.assertEquals("java.util.Collections$SingletonSet", set.getSet().getClass().getName());
    }

    /**
     * Check that the set is the given size, which must be >= 2.
     *
     * @param set The set.
     * @param size The size.
     */
    private void checkSizeN(Set<Object> set, int size)
    {
        Assert.assertTrue(size >= 2);
        Assert.assertEquals(size, set.size());
    }
}
