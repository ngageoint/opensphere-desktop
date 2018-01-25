package io.opensphere.analysis.util.collections;

/**
 * List of longs that stores contiguous values efficiently.
 */
public class RangedLongList extends AbstractBlockLongList
{
    @Override
    protected LongBlock newBlock()
    {
        return new RangeLongBlock();
    }

    @Override
    protected void handleCantRemove(long value, int blockIndex)
    {
        RangeLongBlock rangeBlock = (RangeLongBlock)getBlocks().get(blockIndex);
        RangeLongBlock[] blocks = rangeBlock.split(value);
        getBlocks().set(blockIndex, blocks[0]);
        getBlocks().add(blockIndex + 1, blocks[1]);
    }
}
