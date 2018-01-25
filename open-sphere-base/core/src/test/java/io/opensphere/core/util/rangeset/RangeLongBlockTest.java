package io.opensphere.core.util.rangeset;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.model.RangeRelationType;
import org.junit.Assert;

/**
 * JUnit Tests for {@link RangeLongBlock}.
 */
public class RangeLongBlockTest
{
    /**
     * Test borders with range block.
     */
    @Test
    public void testBordersWithRangeBlock()
    {
        RangeLongBlock block = new RangeLongBlock(5, 10);
        RangeLongBlock before = new RangeLongBlock(1, 3);
        RangeLongBlock borderBefore = new RangeLongBlock(1, 4);
        RangeLongBlock overlapLeading = new RangeLongBlock(1, 5);
        RangeLongBlock equals = new RangeLongBlock(5, 10);
        RangeLongBlock subset = new RangeLongBlock(6, 9);
        RangeLongBlock superset = new RangeLongBlock(4, 11);
        RangeLongBlock overlapTrailing = new RangeLongBlock(9, 11);
        RangeLongBlock borderAfter = new RangeLongBlock(11, 12);
        RangeLongBlock after = new RangeLongBlock(12, 13);

        Assert.assertFalse(block.borders(before));
        Assert.assertTrue(block.borders(borderBefore));
        Assert.assertFalse(block.borders(overlapLeading));
        Assert.assertFalse(block.borders(equals));
        Assert.assertFalse(block.borders(subset));
        Assert.assertFalse(block.borders(superset));
        Assert.assertFalse(block.borders(overlapTrailing));
        Assert.assertTrue(block.borders(borderAfter));
        Assert.assertFalse(block.borders(after));
    }

    /**
     * Test borders with value.
     */
    @Test
    public void testBordersWithValue()
    {
        RangeLongBlock aBlock = new RangeLongBlock(3, 5);
        Assert.assertTrue(aBlock.borders(2));
        Assert.assertTrue(aBlock.borders(6));
        Assert.assertFalse(aBlock.borders(3));
        Assert.assertFalse(aBlock.borders(4));
        Assert.assertFalse(aBlock.borders(5));
        Assert.assertFalse(aBlock.borders(1));
        Assert.assertFalse(aBlock.borders(7));
    }

    /**
     * Test compare to.
     */
    @Test
    public void testCompareTo()
    {
        RangeLongBlock aBlock = new RangeLongBlock(1, 10);
        RangeLongBlock bBlock = new RangeLongBlock(1, 10);
        RangeLongBlock cBlock = new RangeLongBlock(2, 4);
        RangeLongBlock dBlock = new RangeLongBlock(1, 4);
        RangeLongBlock eBlock = new RangeLongBlock(2, 10);
        RangeLongBlock fBlock = new RangeLongBlock(-3, -1);
        RangeLongBlock gBlock = new RangeLongBlock(33, 34);

        Assert.assertEquals(0, aBlock.compareTo(bBlock));
        Assert.assertEquals(-1, aBlock.compareTo(cBlock));
        Assert.assertEquals(1, aBlock.compareTo(dBlock));
        Assert.assertEquals(-1, aBlock.compareTo(eBlock));
        Assert.assertEquals(1, aBlock.compareTo(fBlock));
        Assert.assertEquals(-1, aBlock.compareTo(gBlock));
    }

    /**
     * Test constructor.
     */
    @Test
    public void testConstructorAndSetRange()
    {
        RangeLongBlock aBlock = new RangeLongBlock(1, 2);
        Assert.assertEquals(1, aBlock.getStart());
        Assert.assertEquals(2, aBlock.getEnd());

        RangeLongBlock reversedValuesBlock = new RangeLongBlock(2, 1);
        Assert.assertEquals(1, reversedValuesBlock.getStart());
        Assert.assertEquals(2, reversedValuesBlock.getEnd());

        aBlock.setRange(5, 10);
        Assert.assertEquals(5, aBlock.getStart());
        Assert.assertEquals(10, aBlock.getEnd());

        // Reversed values should be corrected
        aBlock.setRange(30, 20);
        Assert.assertEquals(20, aBlock.getStart());
        Assert.assertEquals(30, aBlock.getEnd());
    }

    /**
     * Test contains value.
     */
    @Test
    public void testContainsValue()
    {
        RangeLongBlock aBlock = new RangeLongBlock(3, 7);
        Assert.assertTrue(aBlock.containsValue(3));
        Assert.assertTrue(aBlock.containsValue(4));
        Assert.assertTrue(aBlock.containsValue(5));
        Assert.assertTrue(aBlock.containsValue(6));
        Assert.assertTrue(aBlock.containsValue(7));
        Assert.assertFalse(aBlock.containsValue(2));
        Assert.assertFalse(aBlock.containsValue(8));
    }

    /**
     * Test clone.
     */
    @Test
    public void testCopyCtor()
    {
        RangeLongBlock aBlock = new RangeLongBlock(1, 10);
        Object copyObj = new RangeLongBlock(aBlock);
        Assert.assertTrue(aBlock.equals(copyObj));
    }

    /**
     * Test equals.
     */
    @Test
    public void testEquals()
    {
        RangeLongBlock aBlock = new RangeLongBlock(1, 10);
        RangeLongBlock bBlock = new RangeLongBlock(1, 10);
        RangeLongBlock cBlock = new RangeLongBlock(2, 4);
        RangeLongBlock dBlock = new RangeLongBlock(1, 4);
        RangeLongBlock eBlock = new RangeLongBlock(2, 10);
        RangeLongBlock fBlock = new RangeLongBlock(-3, -1);
        RangeLongBlock gBlock = new RangeLongBlock(33, 34);
        Assert.assertTrue(aBlock.equals(bBlock));
        Assert.assertTrue(!aBlock.equals(cBlock));
        Assert.assertTrue(!aBlock.equals(dBlock));
        Assert.assertTrue(!aBlock.equals(eBlock));
        Assert.assertTrue(!aBlock.equals(fBlock));
        Assert.assertTrue(!aBlock.equals(gBlock));
    }

    /**
     * Test expand.
     */
    @Test
    public void testExpand()
    {
        RangeLongBlock aBlock = new RangeLongBlock(3, 5);

        boolean gotIllegalArgumentException = false;
        try
        {
            aBlock.expand(1);
        }
        catch (IllegalArgumentException e)
        {
            gotIllegalArgumentException = true;
        }
        Assert.assertTrue(gotIllegalArgumentException);

        gotIllegalArgumentException = false;
        try
        {
            aBlock.expand(7);
        }
        catch (IllegalArgumentException e)
        {
            gotIllegalArgumentException = true;
        }
        Assert.assertTrue(gotIllegalArgumentException);

        aBlock.expand(2);
        Assert.assertEquals(2, aBlock.getStart());

        aBlock.expand(6);
        Assert.assertEquals(6, aBlock.getEnd());
    }

    /**
     * Test forms contiguous range.
     */
    @Test
    public void testFormsContiguousRange()
    {
        RangeLongBlock block = new RangeLongBlock(5, 10);
        RangeLongBlock before = new RangeLongBlock(1, 3);
        RangeLongBlock borderBefore = new RangeLongBlock(1, 4);
        RangeLongBlock overlapLeading = new RangeLongBlock(1, 5);
        RangeLongBlock equals = new RangeLongBlock(5, 10);
        RangeLongBlock subset = new RangeLongBlock(6, 9);
        RangeLongBlock superset = new RangeLongBlock(4, 11);
        RangeLongBlock overlapTrailing = new RangeLongBlock(9, 11);
        RangeLongBlock borderAfter = new RangeLongBlock(11, 12);
        RangeLongBlock after = new RangeLongBlock(12, 13);

        Assert.assertFalse(block.formsContiguousRange(before));
        Assert.assertTrue(block.formsContiguousRange(borderBefore));
        Assert.assertTrue(block.formsContiguousRange(overlapLeading));
        Assert.assertTrue(block.formsContiguousRange(equals));
        Assert.assertTrue(block.formsContiguousRange(subset));
        Assert.assertTrue(block.formsContiguousRange(superset));
        Assert.assertTrue(block.formsContiguousRange(overlapTrailing));
        Assert.assertTrue(block.formsContiguousRange(borderAfter));
        Assert.assertFalse(block.formsContiguousRange(after));
    }

    /**
     * Test get relation.
     */
    @Test
    public void testGetRelation()
    {
        RangeLongBlock block = new RangeLongBlock(5, 10);
        RangeLongBlock before = new RangeLongBlock(1, 3);
        RangeLongBlock borderBefore = new RangeLongBlock(1, 4);
        RangeLongBlock overlapLeading = new RangeLongBlock(1, 5);
        RangeLongBlock equals = new RangeLongBlock(5, 10);
        RangeLongBlock subset = new RangeLongBlock(6, 9);
        RangeLongBlock superset = new RangeLongBlock(4, 11);
        RangeLongBlock overlapTrailing = new RangeLongBlock(9, 11);
        RangeLongBlock borderAfter = new RangeLongBlock(11, 12);
        RangeLongBlock after = new RangeLongBlock(12, 13);

        Assert.assertEquals(RangeRelationType.BEFORE, block.getRelation(before));
        Assert.assertEquals(RangeRelationType.BORDERS_BEFORE, block.getRelation(borderBefore));
        Assert.assertEquals(RangeRelationType.OVERLAPS_FRONT_EDGE, block.getRelation(overlapLeading));
        Assert.assertEquals(RangeRelationType.EQUAL, block.getRelation(equals));
        Assert.assertEquals(RangeRelationType.SUBSET, block.getRelation(subset));
        Assert.assertEquals(RangeRelationType.SUPERSET, block.getRelation(superset));
        Assert.assertEquals(RangeRelationType.OVERLAPS_BACK_EDGE, block.getRelation(overlapTrailing));
        Assert.assertEquals(RangeRelationType.BORDERS_AFTER, block.getRelation(borderAfter));
        Assert.assertEquals(RangeRelationType.AFTER, block.getRelation(after));
    }

    /**
     * Test intersection.
     */
    @Test
    public void testIntersection()
    {
        RangeLongBlock block = new RangeLongBlock(5, 10);
        RangeLongBlock before = new RangeLongBlock(1, 3);

        RangeLongBlock intersect = block.intersection(before);
        Assert.assertNull(intersect);

        RangeLongBlock borderBefore = new RangeLongBlock(1, 4);
        intersect = block.intersection(borderBefore);
        Assert.assertNull(intersect);

        RangeLongBlock overlapLeading = new RangeLongBlock(1, 6);
        intersect = block.intersection(overlapLeading);
        Assert.assertEquals(5, intersect.getStart());
        Assert.assertEquals(6, intersect.getEnd());

        RangeLongBlock equals = new RangeLongBlock(5, 10);
        intersect = block.intersection(equals);
        Assert.assertEquals(5, intersect.getStart());
        Assert.assertEquals(10, intersect.getEnd());

        RangeLongBlock subset = new RangeLongBlock(6, 9);
        intersect = block.intersection(subset);
        Assert.assertEquals(6, intersect.getStart());
        Assert.assertEquals(9, intersect.getEnd());

        RangeLongBlock superset = new RangeLongBlock(4, 11);
        intersect = block.intersection(superset);
        Assert.assertEquals(5, intersect.getStart());
        Assert.assertEquals(10, intersect.getEnd());

        RangeLongBlock overlapTrailing = new RangeLongBlock(9, 11);
        intersect = block.intersection(overlapTrailing);
        Assert.assertEquals(9, intersect.getStart());
        Assert.assertEquals(10, intersect.getEnd());

        RangeLongBlock borderAfter = new RangeLongBlock(11, 12);
        intersect = block.intersection(borderAfter);
        Assert.assertNull(intersect);

        RangeLongBlock after = new RangeLongBlock(12, 13);
        intersect = block.intersection(after);
        Assert.assertNull(intersect);
    }

    /**
     * Test is after and is before.
     */
    @Test
    public void testIsAfterAndIsBefore()
    {
        RangeLongBlock block = new RangeLongBlock(5, 10);
        RangeLongBlock before = new RangeLongBlock(1, 3);
        RangeLongBlock borderBefore = new RangeLongBlock(1, 4);
        RangeLongBlock overlapLeading = new RangeLongBlock(1, 5);
        RangeLongBlock equals = new RangeLongBlock(5, 10);
        RangeLongBlock subset = new RangeLongBlock(6, 9);
        RangeLongBlock superset = new RangeLongBlock(4, 11);
        RangeLongBlock overlapTrailing = new RangeLongBlock(9, 11);
        RangeLongBlock borderAfter = new RangeLongBlock(11, 12);
        RangeLongBlock after = new RangeLongBlock(12, 13);

        Assert.assertFalse(block.isBefore(before));
        Assert.assertFalse(block.isBefore(borderBefore));
        Assert.assertFalse(block.isBefore(overlapLeading));
        Assert.assertFalse(block.isBefore(subset));
        Assert.assertFalse(block.isBefore(equals));
        Assert.assertFalse(block.isBefore(superset));
        Assert.assertFalse(block.isBefore(overlapTrailing));
        Assert.assertTrue(block.isBefore(borderAfter));
        Assert.assertTrue(block.isBefore(after));

        Assert.assertTrue(block.isAfter(before));
        Assert.assertTrue(block.isAfter(borderBefore));
        Assert.assertFalse(block.isAfter(overlapLeading));
        Assert.assertFalse(block.isAfter(subset));
        Assert.assertFalse(block.isAfter(equals));
        Assert.assertFalse(block.isAfter(superset));
        Assert.assertFalse(block.isAfter(overlapTrailing));
        Assert.assertFalse(block.isAfter(borderAfter));
        Assert.assertFalse(block.isAfter(after));
    }

    /**
     * Test iterator.
     */
    @Test
    public void testIterator()
    {
        RangeLongBlock aBlock = new RangeLongBlock(1, 5);
        Iterator<Long> itr = aBlock.iterator();
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(1, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(2, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(3, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(4, itr.next().longValue());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(5, itr.next().longValue());
        Assert.assertTrue(!itr.hasNext());

        TestHelper.testNoSuchElement(itr);

        TestHelper.testUnsupportedRemove(itr);
    }

    /**
     * Test merge.
     */
    @Test
    public void testMerge()
    {
        RangeLongBlock block = new RangeLongBlock(5, 10);
        RangeLongBlock before = new RangeLongBlock(1, 3);

        boolean hadIllegalArgumentException = false;
        try
        {
            block.merge(before);
        }
        catch (IllegalArgumentException e)
        {
            hadIllegalArgumentException = true;
        }
        Assert.assertTrue(hadIllegalArgumentException);

        RangeLongBlock borderBefore = new RangeLongBlock(1, 4);
        block = new RangeLongBlock(5, 10);
        block.merge(borderBefore);
        Assert.assertEquals(1, block.getStart());
        Assert.assertEquals(10, block.getEnd());

        RangeLongBlock overlapLeading = new RangeLongBlock(3, 5);
        block = new RangeLongBlock(5, 10);
        block.merge(overlapLeading);
        Assert.assertEquals(3, block.getStart());
        Assert.assertEquals(10, block.getEnd());

        RangeLongBlock equals = new RangeLongBlock(5, 10);
        block = new RangeLongBlock(5, 10);
        block.merge(equals);
        Assert.assertEquals(5, block.getStart());
        Assert.assertEquals(10, block.getEnd());

        RangeLongBlock subset = new RangeLongBlock(6, 9);
        block = new RangeLongBlock(5, 10);
        block.merge(subset);
        Assert.assertEquals(5, block.getStart());
        Assert.assertEquals(10, block.getEnd());

        RangeLongBlock superset = new RangeLongBlock(4, 11);
        block = new RangeLongBlock(5, 10);
        block.merge(superset);
        Assert.assertEquals(4, block.getStart());
        Assert.assertEquals(11, block.getEnd());

        RangeLongBlock overlapTrailing = new RangeLongBlock(9, 11);
        block = new RangeLongBlock(5, 10);
        block.merge(overlapTrailing);
        Assert.assertEquals(5, block.getStart());
        Assert.assertEquals(11, block.getEnd());

        RangeLongBlock borderAfter = new RangeLongBlock(11, 12);
        block = new RangeLongBlock(5, 10);
        block.merge(borderAfter);
        Assert.assertEquals(5, block.getStart());
        Assert.assertEquals(12, block.getEnd());

        block = new RangeLongBlock(5, 10);
        RangeLongBlock after = new RangeLongBlock(12, 13);
        hadIllegalArgumentException = false;
        try
        {
            block.merge(after);
        }
        catch (IllegalArgumentException e)
        {
            hadIllegalArgumentException = true;
        }
        Assert.assertTrue(hadIllegalArgumentException);
    }

    /**
     * Test overlaps.
     */
    @Test
    public void testOverlaps()
    {
        RangeLongBlock block = new RangeLongBlock(5, 10);
        RangeLongBlock before = new RangeLongBlock(1, 3);
        RangeLongBlock borderBefore = new RangeLongBlock(1, 4);
        RangeLongBlock overlapLeading = new RangeLongBlock(1, 5);
        RangeLongBlock equals = new RangeLongBlock(5, 10);
        RangeLongBlock subset = new RangeLongBlock(6, 9);
        RangeLongBlock superset = new RangeLongBlock(4, 11);
        RangeLongBlock overlapTrailing = new RangeLongBlock(9, 11);
        RangeLongBlock borderAfter = new RangeLongBlock(11, 12);
        RangeLongBlock after = new RangeLongBlock(12, 13);

        Assert.assertFalse(block.overlaps(before));
        Assert.assertFalse(block.overlaps(borderBefore));
        Assert.assertTrue(block.overlaps(overlapLeading));
        Assert.assertTrue(block.overlaps(subset));
        Assert.assertTrue(block.overlaps(equals));
        Assert.assertTrue(block.overlaps(superset));
        Assert.assertTrue(block.overlaps(overlapTrailing));
        Assert.assertFalse(block.overlaps(borderAfter));
        Assert.assertFalse(block.overlaps(after));
    }

    /**
     * Test size function.
     */
    @Test
    public void testSizeFunction()
    {
        RangeLongBlock aBlock = new RangeLongBlock(1, 10);
        Assert.assertTrue(aBlock.size() == 10);
    }

    /**
     * Test static create range long blocks.
     */
    @Test
    public void testStaticCreateRangeLongBlocks()
    {
        // Build the test sets.
        long[] primativeArray = { 1, 2, 2, 4, 6, 7, 8, -1, 99, 11, 98, 12, -2, 13 };
        Long[] objectArray = new Long[primativeArray.length];
        Collection<Long> collection = new LinkedList<>();
        for (int i = 0; i < primativeArray.length; i++)
        {
            objectArray[i] = Long.valueOf(primativeArray[i]);
            collection.add(Long.valueOf(primativeArray[i]));
        }

        // Build a collection of RangeLongBlock that represent what we
        // expect our answer to be.
        LinkedList<RangeLongBlock> blocks = new LinkedList<>();
        blocks.add(new RangeLongBlock(-1, -2));
        blocks.add(new RangeLongBlock(1, 2));
        blocks.add(new RangeLongBlock(4, 4));
        blocks.add(new RangeLongBlock(6, 8));
        blocks.add(new RangeLongBlock(11, 13));
        blocks.add(new RangeLongBlock(98, 99));

        List<RangeLongBlock> fromPrim = RangeLongBlock.createRangeLongBlocks(primativeArray);
        Assert.assertEquals(blocks, fromPrim);

        List<RangeLongBlock> fromObjectArray = RangeLongBlock.createRangeLongBlocks(objectArray);
        Assert.assertEquals(blocks, fromObjectArray);

        List<RangeLongBlock> fromCollection = RangeLongBlock.createRangeLongBlocks(collection);
        Assert.assertEquals(blocks, fromCollection);
    }

    /**
     * Test static create sub blocks by removing value.
     */
    @Test
    public void testStaticCreateSubBlocksByRemovingValue()
    {
        RangeLongBlock block = new RangeLongBlock(5, 7);
        List<RangeLongBlock> list = RangeLongBlock.createSubBlocksByRemovingValue(block, 4);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(block, list.get(0));

        list = RangeLongBlock.createSubBlocksByRemovingValue(block, 5);
        Assert.assertEquals(1, list.size());
        RangeLongBlock sub = list.get(0);
        Assert.assertEquals(6, sub.getStart());
        Assert.assertEquals(7, sub.getEnd());

        list = RangeLongBlock.createSubBlocksByRemovingValue(block, 6);
        Assert.assertEquals(2, list.size());
        sub = list.get(0);
        RangeLongBlock sub2 = list.get(1);
        Assert.assertEquals(5, sub.getStart());
        Assert.assertEquals(5, sub.getEnd());
        Assert.assertEquals(7, sub2.getStart());
        Assert.assertEquals(7, sub2.getEnd());

        list = RangeLongBlock.createSubBlocksByRemovingValue(block, 7);
        Assert.assertEquals(1, list.size());
        sub = list.get(0);
        Assert.assertEquals(5, sub.getStart());
        Assert.assertEquals(6, sub.getEnd());

        list = RangeLongBlock.createSubBlocksByRemovingValue(block, 8);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(block, list.get(0));
    }

    /**
     * Test static is contiguous.
     */
    @Test
    public void testStaticIsContiguous()
    {
        long[] contigSet = { 5, 6, 7, 8 };
        long[] contigSetWithDupes = { 5, 6, 7, 7, 8 };
        long[] nonContigSet = { 5, 6, 8, 9 };
        long[] unorderedButCongitIfSorted = { 7, 5, 6, 8 };
        long[] random = { 23, 88, 234, -12 };
        long[] oneValue = { 8 };
        long[] contigReverse = { 9, 8, 7, 6 };

        Assert.assertTrue(RangeLongBlock.isContiguous(contigSet));
        Assert.assertTrue(RangeLongBlock.isContiguous(contigSetWithDupes));
        Assert.assertFalse(RangeLongBlock.isContiguous(nonContigSet));

        Assert.assertFalse(RangeLongBlock.isContiguous(unorderedButCongitIfSorted));
        Arrays.sort(unorderedButCongitIfSorted);
        Assert.assertTrue(RangeLongBlock.isContiguous(unorderedButCongitIfSorted));

        Assert.assertFalse(RangeLongBlock.isContiguous(random));
        Arrays.sort(random);
        Assert.assertFalse(RangeLongBlock.isContiguous(random));

        Assert.assertTrue(RangeLongBlock.isContiguous(oneValue));

        Assert.assertFalse(RangeLongBlock.isContiguous(contigReverse));
        Arrays.sort(contigReverse);
        Assert.assertTrue(RangeLongBlock.isContiguous(contigReverse));
    }

    /**
     * Test static merge.
     */
    @Test
    public void testStaticMerge()
    {
        RangeLongBlock block = new RangeLongBlock(5, 10);
        RangeLongBlock before = new RangeLongBlock(1, 3);
        Assert.assertNull(RangeLongBlock.merge(block, before));

        RangeLongBlock borderBefore = new RangeLongBlock(1, 4);
        RangeLongBlock merged = RangeLongBlock.merge(block, borderBefore);
        Assert.assertEquals(1, merged.getStart());
        Assert.assertEquals(10, merged.getEnd());

        RangeLongBlock overlapLeading = new RangeLongBlock(3, 5);
        merged = RangeLongBlock.merge(block, overlapLeading);
        Assert.assertEquals(3, merged.getStart());
        Assert.assertEquals(10, merged.getEnd());

        RangeLongBlock equals = new RangeLongBlock(5, 10);
        merged = RangeLongBlock.merge(block, equals);
        Assert.assertEquals(5, merged.getStart());
        Assert.assertEquals(10, merged.getEnd());

        RangeLongBlock subset = new RangeLongBlock(6, 9);
        merged = RangeLongBlock.merge(block, subset);
        Assert.assertEquals(5, merged.getStart());
        Assert.assertEquals(10, merged.getEnd());

        RangeLongBlock superset = new RangeLongBlock(4, 11);
        merged = RangeLongBlock.merge(block, superset);
        Assert.assertEquals(4, merged.getStart());
        Assert.assertEquals(11, merged.getEnd());

        RangeLongBlock overlapTrailing = new RangeLongBlock(9, 11);
        merged = RangeLongBlock.merge(block, overlapTrailing);
        Assert.assertEquals(5, merged.getStart());
        Assert.assertEquals(11, merged.getEnd());

        RangeLongBlock borderAfter = new RangeLongBlock(11, 12);
        merged = RangeLongBlock.merge(block, borderAfter);
        Assert.assertEquals(5, merged.getStart());
        Assert.assertEquals(12, merged.getEnd());

        RangeLongBlock after = new RangeLongBlock(12, 13);
        Assert.assertNull(RangeLongBlock.merge(block, after));
    }
}
