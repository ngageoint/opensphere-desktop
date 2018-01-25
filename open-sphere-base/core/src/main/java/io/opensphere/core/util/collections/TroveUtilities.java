package io.opensphere.core.util.collections;

import java.util.List;
import java.util.Set;

import gnu.trove.TByteCollection;
import gnu.trove.TCharCollection;
import gnu.trove.TCollections;
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
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.set.TByteSet;
import gnu.trove.set.TCharSet;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.TFloatSet;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.TShortSet;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * Utilities for working with Trove collections.
 */
@SuppressWarnings("PMD.GodClass")
public final class TroveUtilities
{
    /** Base size of a {@link TByteArrayList}. */
    private static final int TBYTEARRAYLIST_BASE_SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES + 1,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Base size of a {@link TCharArrayList}. */
    private static final int TCHARARRAYLIST_BASE_SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES * Constants.CHAR_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Base size of a {@link TDoubleArrayList}. */
    private static final int TDOUBLEARRAYLIST_BASE_SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Base size of a {@link TFloatArrayList}. */
    private static final int TFLOATARRAYLIST_BASE_SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Base size of a {@link TIntArrayList}. */
    private static final int TINTARRAYLIST_BASE_SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES * 2,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Base size of a {@link TLongArrayList}. */
    private static final int TLONGARRAYLIST_BASE_SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Base size of a {@link TObjectDoubleHashMap}. */
    private static final int TOBJECTDOUBLEHASHMAP_BASE_SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES
                    + Constants.BOOLEAN_SIZE_BYTES * 2 + Constants.INT_SIZE_BYTES * 4 + Constants.FLOAT_SIZE_BYTES * 2,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Base size of a {@link TShortArrayList}. */
    private static final int TSHORTARRAYLIST_BASE_SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES);

    /**
     * Get the size of a {@link TByteArrayList}.
     *
     * @param capacity The capacity of the array list.
     * @return The size in bytes.
     */
    public static int sizeOfTByteArrayList(int capacity)
    {
        return TBYTEARRAYLIST_BASE_SIZE_BYTES
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + capacity * 1, Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    /**
     * Get the size of a {@link TCharArrayList}.
     *
     * @param capacity The capacity of the array list.
     * @return The size in bytes.
     */
    public static int sizeOfTCharArrayList(int capacity)
    {
        return TCHARARRAYLIST_BASE_SIZE_BYTES + MathUtil
                .roundUpTo(Constants.ARRAY_SIZE_BYTES + capacity * Constants.CHAR_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    /**
     * Get the size of a {@link TDoubleArrayList}.
     *
     * @param capacity The capacity of the array list.
     * @return The size in bytes.
     */
    public static int sizeOfTDoubleArrayList(int capacity)
    {
        return TDOUBLEARRAYLIST_BASE_SIZE_BYTES + MathUtil.roundUpTo(
                Constants.ARRAY_SIZE_BYTES + capacity * Constants.DOUBLE_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    /**
     * Get the size of a {@link TFloatArrayList}.
     *
     * @param capacity The capacity of the array list.
     * @return The size in bytes.
     */
    public static int sizeOfTFloatArrayList(int capacity)
    {
        return TFLOATARRAYLIST_BASE_SIZE_BYTES + MathUtil
                .roundUpTo(Constants.ARRAY_SIZE_BYTES + capacity * Constants.FLOAT_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    /**
     * Get the size of a {@link TIntArrayList}.
     *
     * @param capacity The capacity of the array list.
     * @return The size in bytes.
     */
    public static int sizeOfTIntArrayList(int capacity)
    {
        return TINTARRAYLIST_BASE_SIZE_BYTES + MathUtil
                .roundUpTo(Constants.ARRAY_SIZE_BYTES + capacity * Constants.INT_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    /**
     * Get the size of a {@link TLongArrayList}.
     *
     * @param capacity The capacity of the array list.
     * @return The size in bytes.
     */
    public static int sizeOfTLongArrayList(int capacity)
    {
        return TLONGARRAYLIST_BASE_SIZE_BYTES + MathUtil
                .roundUpTo(Constants.ARRAY_SIZE_BYTES + capacity * Constants.LONG_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    /**
     * Get the size of a {@link TObjectDoubleHashMap}.
     *
     * @param capacity The capacity of the map.
     * @param keySize The size of the objects which are the map keys.
     * @return The size in bytes.
     */
    public static int sizeOfTObjectDoubleHashMap(int capacity, int keySize)
    {
        return TOBJECTDOUBLEHASHMAP_BASE_SIZE_BYTES
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + capacity * Constants.DOUBLE_SIZE_BYTES,
                        Constants.MEMORY_BLOCK_SIZE_BYTES)
                + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + capacity * keySize, Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    /**
     * Get the size of a {@link TShortArrayList}.
     *
     * @param capacity The capacity of the array list.
     * @return The size in bytes.
     */
    public static int sizeOfTShortArrayList(int capacity)
    {
        return TSHORTARRAYLIST_BASE_SIZE_BYTES + MathUtil
                .roundUpTo(Constants.ARRAY_SIZE_BYTES + capacity * Constants.SHORT_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES);
    }

    /**
     * Converts a trove long list to regular java list.
     *
     * @param tList the trove list
     * @return the java list
     */
    public static List<Long> toLongList(TLongCollection tList)
    {
        List<Long> list = New.list(tList.size());
        tList.forEach(value -> list.add(value));
        return list;
    }

    /**
     * Converts a trove long list to regular java set.
     *
     * @param tList the trove list
     * @return the java set
     */
    public static Set<Long> toLongSet(TLongCollection tList)
    {
        final float loadFactor = .75f;
        Set<Long> list = New.set(Math.max((int)(tList.size() / loadFactor) + 1, 16));
        tList.forEach(value -> list.add(value));
        return list;
    }

    /**
     * Get an unmodifiable version of a collection.
     *
     * @param input The input collection.
     * @return The unmodifiable collection.
     */
    public static TByteCollection unmodifiableCollection(TByteCollection input)
    {
        return input instanceof TUnmodifiableByteCollection ? input : TCollections.unmodifiableCollection(input);
    }

    /**
     * Get an unmodifiable version of a collection.
     *
     * @param input The input collection.
     * @return The unmodifiable collection.
     */
    public static TCharCollection unmodifiableCollection(TCharCollection input)
    {
        return input instanceof TUnmodifiableCharCollection ? input : TCollections.unmodifiableCollection(input);
    }

    /**
     * Get an unmodifiable version of a collection.
     *
     * @param input The input collection.
     * @return The unmodifiable collection.
     */
    public static TDoubleCollection unmodifiableCollection(TDoubleCollection input)
    {
        return input instanceof TUnmodifiableDoubleCollection ? input : TCollections.unmodifiableCollection(input);
    }

    /**
     * Get an unmodifiable version of a collection.
     *
     * @param input The input collection.
     * @return The unmodifiable collection.
     */
    public static TFloatCollection unmodifiableCollection(TFloatCollection input)
    {
        return input instanceof TUnmodifiableFloatCollection ? input : TCollections.unmodifiableCollection(input);
    }

    /**
     * Get an unmodifiable version of a collection.
     *
     * @param input The input collection.
     * @return The unmodifiable collection.
     */
    public static TIntCollection unmodifiableCollection(TIntCollection input)
    {
        return input instanceof TUnmodifiableIntCollection ? input : TCollections.unmodifiableCollection(input);
    }

    /**
     * Get an unmodifiable version of a collection.
     *
     * @param input The input collection.
     * @return The unmodifiable collection.
     */
    public static TLongCollection unmodifiableCollection(TLongCollection input)
    {
        return input instanceof TUnmodifiableLongCollection ? input : TCollections.unmodifiableCollection(input);
    }

    /**
     * Get an unmodifiable version of a collection.
     *
     * @param input The input collection.
     * @return The unmodifiable collection.
     */
    public static TShortCollection unmodifiableCollection(TShortCollection input)
    {
        return input instanceof TUnmodifiableShortCollection ? input : TCollections.unmodifiableCollection(input);
    }

    /**
     * Get an unmodifiable version of a list.
     *
     * @param input The input list.
     * @return The unmodifiable list.
     */
    public static TByteList unmodifiableList(TByteList input)
    {
        return input instanceof TUnmodifiableByteList ? input : TCollections.unmodifiableList(input);
    }

    /**
     * Get an unmodifiable version of a list.
     *
     * @param input The input list.
     * @return The unmodifiable list.
     */
    public static TCharList unmodifiableList(TCharList input)
    {
        return input instanceof TUnmodifiableCharList ? input : TCollections.unmodifiableList(input);
    }

    /**
     * Get an unmodifiable version of a list.
     *
     * @param input The input list.
     * @return The unmodifiable list.
     */
    public static TDoubleList unmodifiableList(TDoubleList input)
    {
        return input instanceof TUnmodifiableDoubleList ? input : TCollections.unmodifiableList(input);
    }

    /**
     * Get an unmodifiable version of a list.
     *
     * @param input The input list.
     * @return The unmodifiable list.
     */
    public static TFloatList unmodifiableList(TFloatList input)
    {
        return input instanceof TUnmodifiableFloatList ? input : TCollections.unmodifiableList(input);
    }

    /**
     * Get an unmodifiable version of a list.
     *
     * @param input The input list.
     * @return The unmodifiable list.
     */
    public static TIntList unmodifiableList(TIntList input)
    {
        return input instanceof TUnmodifiableIntList ? input : TCollections.unmodifiableList(input);
    }

    /**
     * Get an unmodifiable version of a list.
     *
     * @param input The input list.
     * @return The unmodifiable list.
     */
    public static TLongList unmodifiableList(TLongList input)
    {
        return input instanceof TUnmodifiableLongList ? input : TCollections.unmodifiableList(input);
    }

    /**
     * Get an unmodifiable version of a list.
     *
     * @param input The input list.
     * @return The unmodifiable list.
     */
    public static TShortList unmodifiableList(TShortList input)
    {
        return input instanceof TUnmodifiableShortList ? input : TCollections.unmodifiableList(input);
    }

    /**
     * Get an unmodifiable version of a set.
     *
     * @param input The input set.
     * @return The unmodifiable set.
     */
    public static TByteSet unmodifiableSet(TByteSet input)
    {
        return input instanceof TUnmodifiableByteSet ? input : TCollections.unmodifiableSet(input);
    }

    /**
     * Get an unmodifiable version of a set.
     *
     * @param input The input set.
     * @return The unmodifiable set.
     */
    public static TCharSet unmodifiableSet(TCharSet input)
    {
        return input instanceof TUnmodifiableCharSet ? input : TCollections.unmodifiableSet(input);
    }

    /**
     * Get an unmodifiable version of a set.
     *
     * @param input The input set.
     * @return The unmodifiable set.
     */
    public static TDoubleSet unmodifiableSet(TDoubleSet input)
    {
        return input instanceof TUnmodifiableDoubleSet ? input : TCollections.unmodifiableSet(input);
    }

    /**
     * Get an unmodifiable version of a set.
     *
     * @param input The input set.
     * @return The unmodifiable set.
     */
    public static TFloatSet unmodifiableSet(TFloatSet input)
    {
        return input instanceof TUnmodifiableFloatSet ? input : TCollections.unmodifiableSet(input);
    }

    /**
     * Get an unmodifiable version of a set.
     *
     * @param input The input set.
     * @return The unmodifiable set.
     */
    public static TIntSet unmodifiableSet(TIntSet input)
    {
        return input instanceof TUnmodifiableIntSet ? input : TCollections.unmodifiableSet(input);
    }

    /**
     * Get an unmodifiable version of a set.
     *
     * @param input The input set.
     * @return The unmodifiable set.
     */
    public static TLongSet unmodifiableSet(TLongSet input)
    {
        return input instanceof TUnmodifiableLongSet ? input : TCollections.unmodifiableSet(input);
    }

    /**
     * Get an unmodifiable version of a set.
     *
     * @param input The input set.
     * @return The unmodifiable set.
     */
    public static TShortSet unmodifiableSet(TShortSet input)
    {
        return input instanceof TUnmodifiableShortSet ? input : TCollections.unmodifiableSet(input);
    }

    /** Disallow instantiation. */
    private TroveUtilities()
    {
    }
}
