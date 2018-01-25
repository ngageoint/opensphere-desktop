package io.opensphere.core.util.rangeset;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A set of long values stored as a list of blocks composed of contiguous value
 * sets.
 *
 * This type of set does not conform to the {@link Set} interface, but it does
 * contain a set of only unique <code>long</code> values.
 *
 * Internally the list of blocks is stored in sorted ascending order. This set
 * is best used if there will be large contiguous blocks of values that can be
 * represented as range blocks. It will not work well for randomized or mostly
 * unique non-contiguous values.
 *
 * Provides many support functions for adding and removing sets of values, as
 * well as finding intersections of other sets of values with this set.
 *
 * Waring: Note that this set could represent more values than a standard array
 * or collection can contain because it could easily be set to represent the
 * range Long.MIN_VALUE to Long.MAX_VALUE values. This will cause problems with
 * some of the {@link Set} interface functions such as: size() toArray()
 * toArray(T[])
 *
 * Prefer valueCount() to size() and use of an Iterator to toArray(
 *
 * Should be Thread Safe for the most part.
 */
public interface RangedLongSet extends Set<Long>
{
    /**
     * Adds the value to the set.
     *
     * @param value the value to add
     * @return true if this set did not already contain the specified element
     */
    boolean add(long value);

    /**
     * Adds the ids from the other set to this set.
     *
     * @param otherSet the other set
     * @return true if the set was changed as a result of this call
     */
    boolean add(RangedLongSet otherSet);

    /**
     * Adds the values to the set.
     *
     * @param values the values to add to the set
     * @return true if the set was changed as a result of this call
     */
    @Override
    boolean addAll(Collection<? extends Long> values);

    /**
     * Adds the values to the set.
     *
     * @param values the values to add to the set
     * @return true if the set was changed as a result of this call
     */
    boolean addAll(long[] values);

    /**
     * Adds the values to the set.
     *
     * @param values the values to add to the set
     * @return true if the set was changed as a result of this call
     */
    boolean addAll(Long[] values);

    /**
     * Adds the block to the set and merges overlapping blocks where necessary.
     *
     * @param insertBlock the insert block
     * @return true if set was changed as a result of the addition.
     */
    boolean addBlock(RangeLongBlock insertBlock);

    /**
     * Adds the {@link Collection} of {@link RangeLongBlock} to the set.
     *
     * @param blocksToAdd the blocks to add to the set.
     * @return true if the set was changed as a result of this call
     */
    boolean addBlocks(Collection<RangeLongBlock> blocksToAdd);

    /**
     * Returns the number of RangeLongBlocks contained within this set.
     *
     * @return the int
     */

    int blockCount();

    /**
     * Checks for value in the set.
     *
     * @param value the value to check
     * @return true, if in set, false if not
     */
    boolean contains(Long value);

    /**
     * Gets the value blocks that back this RangedLongSet. Returns an
     * unmodifiableList that is a snapshot of the blocks at the time the call
     * was made. Subsequent changes to the values once the snapshot has been
     * take are not reflected in the result list from this call.
     *
     * @return the value blocks
     */
    List<RangeLongBlock> getBlocks();

    /**
     * Gets a RangedLongSet that represents the difference of this set A with
     * the provided set B. Where Result = A - B
     *
     * @param values the values for another set to difference
     * @return the new Difference set. Can be empty if A is a subset of B.
     */
    RangedLongSet getDifference(long[] values);

    /**
     * Gets a RangedLongSet that represents the difference of this set A with
     * the provided set B. Where Result = A - B
     *
     * @param other the other set to difference
     * @return the new Difference set. Can be empty if A is a subset of B.
     */
    RangedLongSet getDifference(RangedLongSet other);

    /**
     * Gets a RangedLongSet that represent the intersection of the provided set
     * of values with this set.
     *
     * @param values to check for intersection
     * @return the intersection list ( empty set if no intersection )
     */
    RangedLongSet getIntersection(Collection<Long> values);

    /**
     * Gets a RangedLongSet that represent the intersection of the provided set
     * of values with this set.
     *
     * @param values to check for intersection
     * @return the intersection list ( empty set if no intersection )
     */
    RangedLongSet getIntersection(long[] values);

    /**
     * Gets a RangedLongSet that represent the intersection of the provided set
     * of values with this set.
     *
     * @param values to check for intersection
     * @return the intersection list ( empty set if no intersection )
     */
    RangedLongSet getIntersection(Long[] values);

    /**
     * Returns a new RangedLongSet that contains the list of values that
     * intersect (are in common) between this set and the provided set.
     *
     * @param otherSet the other set to check for intersection.
     * @return the intersection set ( empty set if no intersection )
     */
    RangedLongSet getIntersection(RangedLongSet otherSet);

    /**
     * Gets a RangedLongSet that represent the intersection of the provided
     * block with this set.
     *
     * @param blockToIntersect to check for intersection
     * @return the intersection list ( empty set if no intersection )
     */
    RangedLongSet getIntersection(RangeLongBlock blockToIntersect);

    /**
     * Returns the highest value contained in the set, or null if the set is
     * empty.
     *
     * @return the highest value in the set.
     */
    Long getMaximum();

    /**
     * Returns the lowest value contained in the set, or null if the set is
     * empty.
     *
     * @return the lowest value in the set.
     */
    Long getMinimum();

    /**
     * Gets a new RangeLongSet that represents the union of this set and the
     * other set.
     *
     * @param other the other set to union with
     * @return the union {@link RangedLongSet}
     */
    RangedLongSet getUnion(RangedLongSet other);

    /**
     * Gets the array of values that are stored in this set. Constructs a new
     * array of the length returned by size() containing all individual ids.
     *
     * Note: Because of the way the range list stores the values ( as ranges )
     * it is possible that if the total number of values within the set exceeds
     * the {@link Integer}.MAX_VALUE ( the maximum number of elements allowed in
     * a list) that it will throw some kind of exception when it attempts to
     * build the array.
     *
     * Instead to iterate over all the values use iterator();
     *
     * @return the ids
     * @throws TooManyValuesToConstructArrayException if the number of values
     *             represented by the set exceeds {@link Integer}.MAX_VALUE
     */
    long[] getValues() throws TooManyValuesToConstructArrayException;

    /**
     * Checks for value in the set.
     *
     * @param value the value to check
     * @return true, if in set, false if not
     */
    boolean hasValue(long value);

    /**
     * Checks if the block intersects this set.
     *
     * @param block the block to test for intersection
     * @return true, if intersects
     */
    boolean intersects(RangeLongBlock block);

    /**
     * Removes all values from the set from valueList that occur within the set.
     *
     * @param valueList the list of values to remove.
     * @return true if this set changed as a result of the call
     */
    boolean remove(Collection<? extends Long> valueList);

    /**
     * Removes the value from the set, if it is in the set.
     *
     * @param value the value to remove
     * @return true if this set contained the specified element
     */
    boolean remove(long value);

    /**
     * Removes the value from the set, if it is in the set.
     *
     * @param value the value to remove
     * @return true if this set contained the specified element
     */
    boolean remove(Long value);

    /**
     * Removes values in the array that occur in this set from the set.
     *
     * @param values the values to remove.
     * @return true if this set changed as a result of the call
     */
    boolean remove(long[] values);

    /**
     * Removes values in the array that occur in this set from the set.
     *
     * @param values the values to remove.
     * @return true if this set changed as a result of the call
     */
    boolean remove(Long[] values);

    /**
     * Removes all ids in the other set that occur within this set.
     *
     * @param otherSet the other set
     * @return true if this set changed as a result of the call
     */
    boolean remove(RangedLongSet otherSet);

    /**
     * Removes the block of ids that occur in the set from the set.
     *
     * @param blockToRemove the block of ids to remove.
     * @return true if this set changed as a result of the call
     */
    boolean removeBlock(RangeLongBlock blockToRemove);

    /**
     * Removes longs that occur within the set of blocksToRemove from this set
     * of longs.
     *
     * @param blocksToRemove the blocks to remove
     * @return true if this set changed as a result of the call
     */
    boolean removeBlocks(Collection<RangeLongBlock> blocksToRemove);

    /**
     * The number of longs managed by this set.
     *
     * @return the number of values in the set
     */
    long valueCount();
}
