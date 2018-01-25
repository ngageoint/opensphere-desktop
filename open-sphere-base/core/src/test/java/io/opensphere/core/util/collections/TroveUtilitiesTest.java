package io.opensphere.core.util.collections;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.TByteCollection;
import gnu.trove.TCharCollection;
import gnu.trove.TDoubleCollection;
import gnu.trove.TFloatCollection;
import gnu.trove.TIntCollection;
import gnu.trove.TLongCollection;
import gnu.trove.TShortCollection;
import gnu.trove.impl.unmodifiable.TUnmodifiableByteCollection;
import gnu.trove.impl.unmodifiable.TUnmodifiableByteList;
import gnu.trove.impl.unmodifiable.TUnmodifiableByteSet;
import gnu.trove.impl.unmodifiable.TUnmodifiableCharCollection;
import gnu.trove.impl.unmodifiable.TUnmodifiableCharList;
import gnu.trove.impl.unmodifiable.TUnmodifiableCharSet;
import gnu.trove.impl.unmodifiable.TUnmodifiableDoubleCollection;
import gnu.trove.impl.unmodifiable.TUnmodifiableDoubleList;
import gnu.trove.impl.unmodifiable.TUnmodifiableDoubleSet;
import gnu.trove.impl.unmodifiable.TUnmodifiableFloatCollection;
import gnu.trove.impl.unmodifiable.TUnmodifiableFloatList;
import gnu.trove.impl.unmodifiable.TUnmodifiableFloatSet;
import gnu.trove.impl.unmodifiable.TUnmodifiableIntCollection;
import gnu.trove.impl.unmodifiable.TUnmodifiableIntList;
import gnu.trove.impl.unmodifiable.TUnmodifiableIntSet;
import gnu.trove.impl.unmodifiable.TUnmodifiableLongCollection;
import gnu.trove.impl.unmodifiable.TUnmodifiableLongList;
import gnu.trove.impl.unmodifiable.TUnmodifiableLongSet;
import gnu.trove.impl.unmodifiable.TUnmodifiableShortCollection;
import gnu.trove.impl.unmodifiable.TUnmodifiableShortList;
import gnu.trove.impl.unmodifiable.TUnmodifiableShortSet;
import gnu.trove.list.TByteList;
import gnu.trove.list.TCharList;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TCharArrayList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.set.TByteSet;
import gnu.trove.set.TCharSet;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.TFloatSet;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.TShortSet;
import gnu.trove.set.hash.TByteHashSet;
import gnu.trove.set.hash.TCharHashSet;
import gnu.trove.set.hash.TDoubleHashSet;
import gnu.trove.set.hash.TFloatHashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.set.hash.TShortHashSet;

/** Test for {@link TroveUtilities}. */
public class TroveUtilitiesTest
{
    /**
     * Test for {@link TroveUtilities#unmodifiableCollection(TByteCollection)}.
     */
    @Test
    public void testUnmodifiableCollectionTByteCollection()
    {
        TByteArrayList input = new TByteArrayList();
        TByteCollection result = TroveUtilities.unmodifiableCollection(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableByteCollection);
        Assert.assertSame(result, TroveUtilities.unmodifiableCollection(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableCollection(TCharCollection)}.
     */
    @Test
    public void testUnmodifiableCollectionTCharCollection()
    {
        TCharArrayList input = new TCharArrayList();
        TCharCollection result = TroveUtilities.unmodifiableCollection(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableCharCollection);
        Assert.assertSame(result, TroveUtilities.unmodifiableCollection(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableCollection(TDoubleCollection)}
     * .
     */
    @Test
    public void testUnmodifiableCollectionTDoubleCollection()
    {
        TDoubleArrayList input = new TDoubleArrayList();
        TDoubleCollection result = TroveUtilities.unmodifiableCollection(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableDoubleCollection);
        Assert.assertSame(result, TroveUtilities.unmodifiableCollection(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableCollection(TFloatCollection)}.
     */
    @Test
    public void testUnmodifiableCollectionTFloatCollection()
    {
        TFloatArrayList input = new TFloatArrayList();
        TFloatCollection result = TroveUtilities.unmodifiableCollection(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableFloatCollection);
        Assert.assertSame(result, TroveUtilities.unmodifiableCollection(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableCollection(TIntCollection)}.
     */
    @Test
    public void testUnmodifiableCollectionTIntCollection()
    {
        TIntArrayList input = new TIntArrayList();
        TIntCollection result = TroveUtilities.unmodifiableCollection(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableIntCollection);
        Assert.assertSame(result, TroveUtilities.unmodifiableCollection(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableCollection(TLongCollection)}.
     */
    @Test
    public void testUnmodifiableCollectionTLongCollection()
    {
        TLongArrayList input = new TLongArrayList();
        TLongCollection result = TroveUtilities.unmodifiableCollection(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableLongCollection);
        Assert.assertSame(result, TroveUtilities.unmodifiableCollection(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableCollection(TShortCollection)}.
     */
    @Test
    public void testUnmodifiableCollectionTShortCollection()
    {
        TShortArrayList input = new TShortArrayList();
        TShortCollection result = TroveUtilities.unmodifiableCollection(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableShortCollection);
        Assert.assertSame(result, TroveUtilities.unmodifiableCollection(result));
    }

    /** Test for {@link TroveUtilities#unmodifiableList(TByteList)}. */
    @Test
    public void testUnmodifiableListTByteList()
    {
        TByteArrayList input = new TByteArrayList();
        TByteList result = TroveUtilities.unmodifiableList(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableByteList);
        Assert.assertSame(result, TroveUtilities.unmodifiableList(result));
    }

    /** Test for {@link TroveUtilities#unmodifiableList(TCharList)}. */
    @Test
    public void testUnmodifiableListTCharList()
    {
        TCharArrayList input = new TCharArrayList();
        TCharList result = TroveUtilities.unmodifiableList(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableCharList);
        Assert.assertSame(result, TroveUtilities.unmodifiableList(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableList(TDoubleList)} .
     */
    @Test
    public void testUnmodifiableListTDoubleList()
    {
        TDoubleArrayList input = new TDoubleArrayList();
        TDoubleList result = TroveUtilities.unmodifiableList(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableDoubleList);
        Assert.assertSame(result, TroveUtilities.unmodifiableList(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableList(TFloatList)}.
     */
    @Test
    public void testUnmodifiableListTFloatList()
    {
        TFloatArrayList input = new TFloatArrayList();
        TFloatList result = TroveUtilities.unmodifiableList(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableFloatList);
        Assert.assertSame(result, TroveUtilities.unmodifiableList(result));
    }

    /** Test for {@link TroveUtilities#unmodifiableList(TIntList)}. */
    @Test
    public void testUnmodifiableListTIntList()
    {
        TIntArrayList input = new TIntArrayList();
        TIntList result = TroveUtilities.unmodifiableList(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableIntList);
        Assert.assertSame(result, TroveUtilities.unmodifiableList(result));
    }

    /** Test for {@link TroveUtilities#unmodifiableList(TLongList)}. */
    @Test
    public void testUnmodifiableListTLongList()
    {
        TLongArrayList input = new TLongArrayList();
        TLongList result = TroveUtilities.unmodifiableList(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableLongList);
        Assert.assertSame(result, TroveUtilities.unmodifiableList(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableList(TShortList)}.
     */
    @Test
    public void testUnmodifiableListTShortList()
    {
        TShortArrayList input = new TShortArrayList();
        TShortList result = TroveUtilities.unmodifiableList(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableShortList);
        Assert.assertSame(result, TroveUtilities.unmodifiableList(result));
    }

    /** Test for {@link TroveUtilities#unmodifiableSet(TByteSet)}. */
    @Test
    public void testUnmodifiableSetTByteSet()
    {
        TByteHashSet input = new TByteHashSet();
        TByteSet result = TroveUtilities.unmodifiableSet(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableByteSet);
        Assert.assertSame(result, TroveUtilities.unmodifiableSet(result));
    }

    /** Test for {@link TroveUtilities#unmodifiableSet(TCharSet)}. */
    @Test
    public void testUnmodifiableSetTCharSet()
    {
        TCharHashSet input = new TCharHashSet();
        TCharSet result = TroveUtilities.unmodifiableSet(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableCharSet);
        Assert.assertSame(result, TroveUtilities.unmodifiableSet(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableSet(TDoubleSet)} .
     */
    @Test
    public void testUnmodifiableSetTDoubleSet()
    {
        TDoubleHashSet input = new TDoubleHashSet();
        TDoubleSet result = TroveUtilities.unmodifiableSet(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableDoubleSet);
        Assert.assertSame(result, TroveUtilities.unmodifiableSet(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableSet(TFloatSet)}.
     */
    @Test
    public void testUnmodifiableSetTFloatSet()
    {
        TFloatHashSet input = new TFloatHashSet();
        TFloatSet result = TroveUtilities.unmodifiableSet(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableFloatSet);
        Assert.assertSame(result, TroveUtilities.unmodifiableSet(result));
    }

    /** Test for {@link TroveUtilities#unmodifiableSet(TIntSet)}. */
    @Test
    public void testUnmodifiableSetTIntSet()
    {
        TIntHashSet input = new TIntHashSet();
        TIntSet result = TroveUtilities.unmodifiableSet(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableIntSet);
        Assert.assertSame(result, TroveUtilities.unmodifiableSet(result));
    }

    /** Test for {@link TroveUtilities#unmodifiableSet(TLongSet)}. */
    @Test
    public void testUnmodifiableSetTLongSet()
    {
        TLongHashSet input = new TLongHashSet();
        TLongSet result = TroveUtilities.unmodifiableSet(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableLongSet);
        Assert.assertSame(result, TroveUtilities.unmodifiableSet(result));
    }

    /**
     * Test for {@link TroveUtilities#unmodifiableSet(TShortSet)}.
     */
    @Test
    public void testUnmodifiableSetTShortSet()
    {
        TShortHashSet input = new TShortHashSet();
        TShortSet result = TroveUtilities.unmodifiableSet(input);
        Assert.assertNotSame(input, result);
        Assert.assertTrue(result instanceof TUnmodifiableShortSet);
        Assert.assertSame(result, TroveUtilities.unmodifiableSet(result));
    }
}
