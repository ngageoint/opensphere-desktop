package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;

/**
 * Compact emulation of TLongArrayList. This stores elements as integers when
 * possible. This is roughly half as fast as TLongArrayList but uses as little
 * as half the memory.
 */
public class CompactLongArrayList
{
    /** The blocks used to store elements. */
    private final List<Block> myBlocks = New.list();

    /** The size of the list. */
    private int mySize;

    /**
     * Gets the size of the list.
     *
     * @return the size
     */
    public int size()
    {
        return mySize;
    }

    /**
     * Tests whether this list contains any values.
     *
     * @return true if the list is empty.
     */
    public boolean isEmpty()
    {
        return mySize == 0;
    }

    /**
     * Adds a value to the list.
     *
     * @param value the value
     */
    public void add(long value)
    {
        Block block = getLastBlockIfFits(value);

        if (block == null)
        {
            myBlocks.add(new Block(value, mySize));
        }
        else
        {
            block.add(value);
        }

        ++mySize;
    }

    /**
     * Adds the values to the list.
     *
     * @param values the values
     */
    public void addAll(long[] values)
    {
        for (long value : values)
        {
            add(value);
        }
    }

    /**
     * Adds the values to the list.
     *
     * @param values the values
     */
    public void addAll(Collection<? extends Long> values)
    {
        for (Long value : values)
        {
            add(value.longValue());
        }
    }

    /**
     * Removes an index from the list.
     *
     * @param index the index
     */
    public void removeAt(int index)
    {
        int blockIndex = findBlockIndex(index);
        if (blockIndex == -1)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        Block block = myBlocks.get(blockIndex);

        block.removeAt(index);

        for (int i = blockIndex + 1; i < myBlocks.size(); ++i)
        {
            Block nextBlock = myBlocks.get(i);
            nextBlock.shiftIndexes(-1);
        }

        if (block.size() == 0)
        {
            myBlocks.remove(blockIndex);
        }

        --mySize;
    }

    /**
     * Gets the value at the given index.
     *
     * @param index the index
     * @return the value
     */
    public long get(int index)
    {
        Block block = findBlock(index);
        if (block == null)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        return block.get(index);
    }

//    /**
//     * Gets the index of the given value.
//     *
//     * @param value the value
//     * @return the index or -1
//     */
//    public int indexOf(long value)
//    {
//        int index = -1;
//        int blockIndex;
//        for (Block block : myBlocks)
//        {
//            if ((blockIndex = block.indexOf(value)) != -1)
//            {
//                index = blockIndex;
//                break;
//            }
//        }
//        return index;
//    }

    /**
     * Clears the list.
     */
    public void clear()
    {
        myBlocks.clear();
        mySize = 0;
    }

    /**
     * Gets the number of blocks.
     *
     * @return the block size
     */
    int getBlockSize()
    {
        return myBlocks.size();
    }

    /**
     * Gets the last block if the value fits in it, otherwise null.
     *
     * @param value the value
     * @return the last block, or null
     */
    private Block getLastBlockIfFits(long value)
    {
        Block lastBlock = myBlocks.isEmpty() ? null : myBlocks.get(myBlocks.size() - 1);
        return lastBlock != null && areClose(value, lastBlock.myFirst) ? lastBlock : null;
    }

    /**
     * Finds the block with the given index, or null.
     *
     * @param index the index
     * @return the block, or null
     */
    private Block findBlock(int index)
    {
        Block match = null;
        for (Block block : myBlocks)
        {
            if (block.containsIndex(index))
            {
                match = block;
                break;
            }
        }
        return match;
    }

    /**
     * Finds the index of block with the given index, or -1.
     *
     * @param index the index
     * @return the block index, or -1
     */
    private int findBlockIndex(int index)
    {
        int blockIndex = -1;
        for (int i = 0; i < myBlocks.size(); ++i)
        {
            Block block = myBlocks.get(i);
            if (block.containsIndex(index))
            {
                blockIndex = i;
                break;
            }
        }
        return blockIndex;
    }

    /**
     * Checks if two long values are within an integer distance of each other.
     *
     * @param value1 one value
     * @param value2 the other value
     * @return whether the values are close
     */
    private static boolean areClose(long value1, long value2)
    {
        return Math.abs(value1 - value2) < Integer.MAX_VALUE;
    }

    /** Block of values. */
    private static class Block
    {
        /** The first value in the block. */
        private final long myFirst;

        /** The offsets for the second through n values. */
        private final TIntArrayList myOffsets = new TIntArrayList();

        /** The minimum index in the block. */
        private int myMinIndex;

        /** The maximum index in the block. */
        private int myMaxIndex;

        /**
         * Constructor.
         *
         * @param firstValue the first value in the block
         * @param index the index of the first value
         */
        public Block(long firstValue, int index)
        {
            myFirst = firstValue;
            myMinIndex = index;
            myMaxIndex = index;
        }

        /**
         * Gets the size of the block.
         *
         * @return the size
         */
        public int size()
        {
            return myMaxIndex - myMinIndex + 1;
        }

        /**
         * Adds a value to the block.
         *
         * @param value the value
         */
        public void add(long value)
        {
            int offset = (int)Math.subtractExact(value, myFirst);
            myOffsets.add(offset);
            ++myMaxIndex;
        }

        /**
         * Removes an index from the block.
         *
         * @param index the index
         */
        public void removeAt(int index)
        {
            if (index != myMinIndex)
            {
                myOffsets.removeAt(toOffsetIndex(index));
            }
            // Else this becomes a dead block
            --myMaxIndex;
        }

        /**
         * Gets the value at the given index.
         *
         * @param index the index
         * @return the value
         */
        public long get(int index)
        {
            return index == myMinIndex ? myFirst : myOffsets.get(toOffsetIndex(index)) + myFirst;
        }

        /**
         * Returns whether the given index is contained in the block.
         *
         * @param index the index
         * @return whether the given index is contained in the block
         */
        public boolean containsIndex(int index)
        {
            return myMinIndex <= index && index <= myMaxIndex;
        }

//        /**
//         * Gets the index of the given value.
//         *
//         * @param value the value
//         * @return the index or -1
//         */
//        public int indexOf(long value)
//        {
//            int index = -1;
//            for (int i = 0, n = size(); i < n; ++i)
//            {
//                if (get(i) == value)
//                {
//                    index = i;
//                    break;
//                }
//            }
//            return index;
//        }

        /**
         * Shifts the indexes by the amount.
         *
         * @param amount the amount to shift
         */
        public void shiftIndexes(int amount)
        {
            myMinIndex += amount;
            myMaxIndex += amount;
        }

        /**
         * Converts an index to an offset index.
         *
         * @param index the index
         * @return the offset index
         */
        private int toOffsetIndex(int index)
        {
            return index - myMinIndex - 1;
        }
    }
}
