package io.opensphere.analysis.util.collections;

/** Interface for a block of long values. */
public interface LongBlock extends Iterable<Long>
{
    /**
     * Returns the size of the block.
     *
     * @return the size
     */
    int size();

    /**
     * Returns whether the block is empty.
     *
     * @return whether the block is empty
     */
    boolean isEmpty();

    /**
     * Returns whether the block contains the value.
     *
     * @param value the value
     * @return whether the block contains the value
     */
    boolean contains(long value);

    /**
     * Adds the value to the block.
     *
     * @param value the value
     * @return whether the value was added
     */
    boolean add(long value);

    /**
     * Removes the value from the block.
     *
     * @param value the value
     * @return whether the value was removed
     */
    boolean remove(long value);

    /**
     * Clears the block.
     */
    void clear();

    /**
     * Gets the value at the index.
     *
     * @param index the index
     * @return the value
     */
    long get(int index);

    /**
     * Removes the value at the index.
     *
     * @param index the index
     * @return the value that was removed
     */
    long remove(int index);

    /**
     * Gets the index of the value, or -1.
     *
     * @param value the value
     * @return the index of the value, or -1
     */
    int indexOf(long value);

    /**
     * Returns whether the value can be added to the block.
     *
     * @param value the value
     * @return whether the value can be added to the block
     */
    boolean canAdd(long value);

    /**
     * Returns whether the value can be removed from the block.
     *
     * @param value the value
     * @return whether the value can be removed from the block
     */
    boolean canRemove(long value);
}
