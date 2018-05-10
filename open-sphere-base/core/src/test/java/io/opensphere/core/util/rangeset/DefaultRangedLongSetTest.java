package io.opensphere.core.util.rangeset;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test Cases for DefaultRangedLongSet.
 */
public class DefaultRangedLongSetTest
{
    /**
     * Test some of the basic functions.
     */
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultRangedLongSet rlsA = new DefaultRangedLongSet(setA);

        // Test constructor with array of long.
        Assert.assertEquals(setA.length, rlsA.valueCount());
        Assert.assertEquals(5, rlsA.blockCount());

        DefaultRangedLongSet rlsB = new DefaultRangedLongSet(setA);

        long[] setC = { 1, 2, 5, 6, 14, 15, 55, 56, 57 };
        DefaultRangedLongSet rlsC = new DefaultRangedLongSet(setC);

        // Test equals
        Assert.assertTrue(rlsA.equals(rlsB));
        Assert.assertFalse(rlsA.equals(rlsC));

        // Test Hashcode
        Assert.assertEquals(rlsA.hashCode(), rlsB.hashCode());
        Assert.assertTrue(rlsA.hashCode() != rlsC.hashCode());

        // Test isEmpty
        DefaultRangedLongSet emptySet = new DefaultRangedLongSet();
        Assert.assertTrue(emptySet.isEmpty());
        Assert.assertFalse(rlsA.isEmpty());

        // Test clear
        Assert.assertFalse(rlsC.isEmpty());
        rlsC.clear();
        Assert.assertTrue(rlsC.isEmpty());

        // Test has value
        Assert.assertTrue(rlsA.hasValue(12));
        Assert.assertTrue(rlsA.hasValue(55));
        Assert.assertFalse(rlsA.hasValue(3));
        Assert.assertFalse(rlsA.hasValue(16));
        Assert.assertFalse(rlsA.hasValue(-1));
        Assert.assertFalse(rlsA.hasValue(5000));

        // Test contains
        Assert.assertTrue(rlsA.contains(Integer.valueOf(12)));
        Assert.assertTrue(rlsA.contains(Integer.valueOf(55)));
        Assert.assertFalse(rlsA.contains(Integer.valueOf(3)));
        Assert.assertFalse(rlsA.contains(Integer.valueOf(16)));
        Assert.assertFalse(rlsA.contains(Integer.valueOf(-1)));
        Assert.assertFalse(rlsA.contains(Integer.valueOf(5000)));
    }

    /**
     * Test adds.
     */
    @Test
    public void testAdds()
    {
        // Note that at present all adds for the RangeLongSet
        // eventually call addBlock.

        DefaultRangedLongSet rls = new DefaultRangedLongSet();

        // Test single value add.
        rls.addBlock(new RangeLongBlock(5));
        Assert.assertEquals(1, rls.size());
        Assert.assertTrue(rls.hasValue(5));
        Assert.assertEquals(1, rls.blockCount());
        rls.clear();

        RangeLongBlock a = new RangeLongBlock(2, 3);
        RangeLongBlock b = new RangeLongBlock(5, 7);
        RangeLongBlock c = new RangeLongBlock(12, 15);
        rls.addBlock(a);
        rls.addBlock(b);
        rls.addBlock(c);
        Assert.assertEquals(3, rls.blockCount());

        RangeLongBlock twoBlockMerge = new RangeLongBlock(4);
        /* Should merge together the 1-3 and 5-7 block in to a 1-7 block. */
        boolean addResult = rls.addBlock(twoBlockMerge);
        Assert.assertEquals(2, rls.blockCount());

        RangeLongBlock trailingEdgeMerge = new RangeLongBlock(15, 16);
        addResult = rls.addBlock(trailingEdgeMerge);
        Assert.assertEquals(2, rls.blockCount());

        RangeLongBlock leadingEdgeMerge = new RangeLongBlock(11, 12);
        addResult = rls.addBlock(leadingEdgeMerge);
        Assert.assertTrue(addResult);
        Assert.assertEquals(2, rls.blockCount());

        addResult = rls.addBlock(new RangeLongBlock(18, 30));
        Assert.assertTrue(addResult);
        Assert.assertEquals(3, rls.blockCount());

        addResult = rls.addBlock(new RangeLongBlock(35, 55));
        Assert.assertTrue(addResult);
        Assert.assertEquals(4, rls.blockCount());

        addResult = rls.addBlock(new RangeLongBlock(0, -10));
        Assert.assertTrue(addResult);
        Assert.assertEquals(5, rls.blockCount());

        addResult = rls.addBlock(new RangeLongBlock(2, 32));
        Assert.assertTrue(addResult);
        Assert.assertEquals(3, rls.blockCount());

        addResult = rls.addBlock(new RangeLongBlock(10, 20));
        Assert.assertFalse(addResult);
        Assert.assertEquals(3, rls.blockCount());
    }

    /**
     * Test intersect.
     */
    @Test
    public void testIntersect()
    {
        // All intersect functions use
        // List<RangeLongBlock> getIntersectionList(RangeLongBlock
        // blockToIntersect)

        DefaultRangedLongSet rls = new DefaultRangedLongSet();
        rls.addBlock(new RangeLongBlock(1, 3));
        rls.addBlock(new RangeLongBlock(5, 6));
        rls.addBlock(new RangeLongBlock(8, 9));
        rls.addBlock(new RangeLongBlock(11, 15));
        rls.addBlock(new RangeLongBlock(55, 100));

        // Leading edge intersect
        RangedLongSet leadingEdgeSet = new DefaultRangedLongSet(new RangeLongBlock(4, 5));
        RangedLongSet intersect = rls.getIntersection(leadingEdgeSet);
        Assert.assertEquals(intersect.blockCount(), 1);
        Assert.assertEquals(intersect.size(), 1);
        Assert.assertTrue(intersect.hasValue(5));

        // Trailing edge intersect
        RangedLongSet trailingEdgeSet = new DefaultRangedLongSet(new RangeLongBlock(6, 7));
        intersect = rls.getIntersection(trailingEdgeSet);
        Assert.assertEquals(intersect.blockCount(), 1);
        Assert.assertEquals(intersect.size(), 1);
        Assert.assertTrue(intersect.hasValue(6));

        // No intersect leading, trailing, interior
        RangedLongSet noIntLeadingEdgeSet = new DefaultRangedLongSet(new RangeLongBlock(-5, 0));
        intersect = rls.getIntersection(noIntLeadingEdgeSet);
        Assert.assertTrue(intersect.isEmpty());

        RangedLongSet noIntTrailingEdgeSet = new DefaultRangedLongSet(new RangeLongBlock(101, 888));
        intersect = rls.getIntersection(noIntTrailingEdgeSet);
        Assert.assertTrue(intersect.isEmpty());

        RangedLongSet noIntInteriorSet = new DefaultRangedLongSet(new RangeLongBlock(16, 54));
        intersect = rls.getIntersection(noIntInteriorSet);
        Assert.assertTrue(intersect.isEmpty());

        // Subset
        RangedLongSet subSet = new DefaultRangedLongSet(new RangeLongBlock(60, 70));
        intersect = rls.getIntersection(subSet);
        Assert.assertEquals(intersect.blockCount(), 1);
        Assert.assertEquals(intersect.size(), 11);
        Assert.assertTrue(subSet.equals(intersect));

        // Superset
        DefaultRangedLongSet superSet = new DefaultRangedLongSet(new RangeLongBlock(10, 16));
        intersect = rls.getIntersection(superSet);
        Assert.assertEquals(intersect.blockCount(), 1);
        Assert.assertEquals(intersect.size(), 5);
        Assert.assertTrue(intersect.hasValue(11));
        Assert.assertTrue(intersect.hasValue(12));
        Assert.assertTrue(intersect.hasValue(13));
        Assert.assertTrue(intersect.hasValue(14));
        Assert.assertTrue(intersect.hasValue(15));

        // Superset spanning multiple
        DefaultRangedLongSet superSetSpans2 = new DefaultRangedLongSet(new RangeLongBlock(7, 16));
        intersect = rls.getIntersection(superSetSpans2);
        Assert.assertEquals(intersect.blockCount(), 2);
        Assert.assertEquals(intersect.size(), 7);
        Assert.assertTrue(intersect.hasValue(8));
        Assert.assertTrue(intersect.hasValue(9));
        Assert.assertTrue(intersect.hasValue(11));
        Assert.assertTrue(intersect.hasValue(12));
        Assert.assertTrue(intersect.hasValue(13));
        Assert.assertTrue(intersect.hasValue(14));
        Assert.assertTrue(intersect.hasValue(15));

        // Superset spanning all
        DefaultRangedLongSet spansAll = new DefaultRangedLongSet(new RangeLongBlock(-50, 1600));
        intersect = rls.getIntersection(spansAll);
        Assert.assertTrue(rls.equals(intersect));
    }

    /**
     * Test iterator.
     */
    @Test
    public void testIterator()
    {
        long[] set = { 1, 2, 3, 5, 6, 7, 9, 10, 11 };
        DefaultRangedLongSet rls = new DefaultRangedLongSet(set);
        Iterator<Long> itr = rls.iterator();
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(1, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(2, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(3, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(5, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(6, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(7, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(9, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(10, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(11, itr.next().longValue());
        Assert.assertTrue(!itr.hasNext());

        TestHelper.testNoSuchElement(itr);

        TestHelper.testUnsupportedRemove(itr);
    }

    /**
     * Test remove.
     */
    @Test
    public void testRemove()
    {
        // Note that all remove functions eventually call
        // removeBlock

        long[] setA = { 1, 2, 3, 5, 6, 8, 9, 11, 12, 13, 14, 15 };
        DefaultRangedLongSet rls = new DefaultRangedLongSet(setA);
        rls.addBlock(new RangeLongBlock(55, 100));
        Assert.assertTrue(rls.blockCount() == 5);

        // Remove leading edge.
        boolean changed = rls.removeBlock(new RangeLongBlock(54, 56));
        Assert.assertTrue(changed);
        Assert.assertEquals(5, rls.blockCount());
        changed = rls.removeBlock(new RangeLongBlock(10, 11));
        Assert.assertTrue(changed);
        Assert.assertEquals(5, rls.blockCount());
        changed = rls.removeBlock(new RangeLongBlock(0, 1));
        Assert.assertTrue(changed);
        Assert.assertEquals(5, rls.blockCount());

        // Remove trailing edge
        changed = rls.removeBlock(new RangeLongBlock(15, 20));
        Assert.assertTrue(changed);
        Assert.assertEquals(5, rls.blockCount());
        changed = rls.removeBlock(new RangeLongBlock(98, 110));
        Assert.assertTrue(changed);
        Assert.assertEquals(5, rls.blockCount());
        changed = rls.removeBlock(new RangeLongBlock(3, 4));
        Assert.assertTrue(changed);
        Assert.assertEquals(5, rls.blockCount());

        // remove interior ( split )
        changed = rls.removeBlock(new RangeLongBlock(60, 65));
        Assert.assertTrue(changed);
        Assert.assertEquals(6, rls.blockCount());

        // Remove no op ( not in set )
        changed = rls.removeBlock(new RangeLongBlock(60, 65));
        Assert.assertFalse(changed);
        Assert.assertEquals(6, rls.blockCount());

        // Remove exact
        changed = rls.removeBlock(new RangeLongBlock(12, 14));
        Assert.assertTrue(changed);
        Assert.assertEquals(5, rls.blockCount());

        // Remove spanning
        changed = rls.removeBlock(new RangeLongBlock(3, 7));
        Assert.assertTrue(changed);
        Assert.assertEquals(4, rls.blockCount());

        // Remove multi-spanning.
        changed = rls.removeBlock(new RangeLongBlock(7, 60));
        Assert.assertTrue(changed);
        Assert.assertEquals(2, rls.blockCount());
    }
}
