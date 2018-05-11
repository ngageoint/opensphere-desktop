package io.opensphere.core.model.time;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.rangeset.RangeLongBlock;

/**
 * Test Cases TimeSpanSet.
 */
@SuppressWarnings("unlikely-arg-type")
public class DefaultTimeSpanSetTest
{
    // TODO: Fix this doc
    /**
     * Creates the smallest number of RangeLongBlocks that can represent the
     * values found in the provided array.
     *
     * @param values - the value list to use to create blocks.
     * @return the {@link List} of {@link RangeLongBlock}
     */
    public static List<TimeSpan> createTestTimeSpans(long[] values)
    {
        List<TimeSpan> resultBlocks = new LinkedList<>();

        if (values.length == 1)
        {
            resultBlocks.add(TimeSpan.get(values[0], values[0]));
        }
        else
        {
            long[] valuesToUse = Arrays.copyOf(values, values.length);
            Arrays.sort(valuesToUse);
            long lastStart = valuesToUse[0];
            long lastVal = valuesToUse[0];
            for (int i = 1; i < valuesToUse.length; i++)
            {
                if (valuesToUse[i] != lastVal && valuesToUse[i] != lastVal + 1)
                {
                    resultBlocks.add(TimeSpan.get(lastStart, lastVal + 1));
                    lastStart = valuesToUse[i];
                }

                if (i == valuesToUse.length - 1)
                {
                    resultBlocks.add(TimeSpan.get(lastStart, valuesToUse[i] + 1));
                }

                lastVal = valuesToUse[i];
            }
        }
        return resultBlocks;
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#intersects(TimeSpan)}.
     */
    @Test
    public void testIntersectsTimeSpan()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        TimeSpan timeSpan = new TimeSpanLongLong(5, 14);

        assertTrue(rlsA.intersects(timeSpan));
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#intersects(TimeSpanSet)}.
     */
    @Test
    public void testIntersectsTimeSpanSet()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        long[] setB = { 1, 2, 5, 6, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsB = new DefaultTimeSpanSet();
        rlsB.addAll(createTestTimeSpans(setB));

        assertTrue(rlsA.intersects(rlsB));
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#intersects(TimeSpanSet)}.
     */
    @Test
    public void testIntersectsTimeSpanSetEmpty()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        DefaultTimeSpanSet rlsB = new DefaultTimeSpanSet();

        assertFalse(rlsA.intersects(rlsB));
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#contains(java.util.Date)}.
     */
    @Test
    public void testContainsDate()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        Date date = new Date(5);

        assertTrue(rlsA.contains(date));
    }

    /**
     * Test some of the basic functions.
     */
    @Test
    public void testContains()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        // Test constructor with array of long.
        assertEquals(5, rlsA.size());

        DefaultTimeSpanSet rlsB = new DefaultTimeSpanSet();
        rlsB.addAll(createTestTimeSpans(setA));

        long[] setC = { 1, 2, 5, 6, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsC = new DefaultTimeSpanSet();
        rlsC.addAll(createTestTimeSpans(setC));

        // Test equals
        assertEquals(rlsA, rlsB);
        assertFalse(rlsA.equals(rlsC));

        // Test Hashcode
        assertEquals(rlsA.hashCode(), rlsB.hashCode());
        assertTrue(rlsA.hashCode() != rlsC.hashCode());

        // Test isEmpty
        DefaultTimeSpanSet emptySet = new DefaultTimeSpanSet();
        assertTrue(emptySet.isEmpty());
        assertFalse(rlsA.isEmpty());

        // Test clear
        assertFalse(rlsC.isEmpty());
        rlsC.clear();
        assertTrue(rlsC.isEmpty());

        // Test has value
        assertTrue(rlsA.contains(12));
        assertTrue(rlsA.contains(55));
        assertFalse(rlsA.contains(3));
        assertFalse(rlsA.contains(16));
        assertFalse(rlsA.contains(-1));
        assertFalse(rlsA.contains(5000));

        // Test contains
        assertTrue(rlsA.contains(Long.valueOf(12)));
        assertTrue(rlsA.contains(Long.valueOf(55)));
        assertFalse(rlsA.contains(Long.valueOf(3)));
        assertFalse(rlsA.contains(Long.valueOf(16)));
        assertFalse(rlsA.contains(Long.valueOf(-1)));
        assertFalse(rlsA.contains(Long.valueOf(5000)));
    }

    /**
     * Test method for
     * {@link DefaultTimeSpanSet#test(DefaultTimeSpanSet.MembershipTest, TimeSpan, boolean)}
     * .
     */
    @SuppressWarnings("javadoc")
    @Test
    public void testTest()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        TimelessTimeSpan timelessSpan = new TimelessTimeSpan();

        assertTrue(rlsA.test(null, timelessSpan, true));
        assertFalse(rlsA.test(null, timelessSpan, false));
    }

    /**
     * Test adds.
     */
    @Test
    public void testAdds()
    {
        // Note that at present all adds for the RangeLongSet eventually call
        // addBlock.

        DefaultTimeSpanSet rls = new DefaultTimeSpanSet();

        // Test single value add.
        rls.add(TimeSpan.get(5, 6));
        assertEquals(1, rls.size());
        assertTrue(rls.contains(5));
        assertEquals(1, rls.size());
        rls.clear();

        TimeSpan a = TimeSpan.get(2, 3);
        TimeSpan b = TimeSpan.get(5, 7);
        TimeSpan c = TimeSpan.get(12, 15);
        rls.add(a);
        rls.add(b);
        rls.add(c);
        assertEquals(3, rls.size());

        TimeSpan twoBlockMerge = TimeSpan.get(3, 5);
        /* Should merge together the 2-3 and 5-7 block in to a 2-7 block. */
        boolean addResult = rls.add(twoBlockMerge);
        assertEquals(2, rls.size());

        TimeSpan trailingEdgeMerge = TimeSpan.get(15, 16);
        addResult = rls.add(trailingEdgeMerge);
        assertEquals(2, rls.size());

        TimeSpan leadingEdgeMerge = TimeSpan.get(11, 12);
        addResult = rls.add(leadingEdgeMerge);
        assertTrue(addResult);
        assertEquals(2, rls.size());

        addResult = rls.add(TimeSpan.get(18, 30));
        assertTrue(addResult);
        assertEquals(3, rls.size());

        addResult = rls.add(TimeSpan.get(35, 55));
        assertTrue(addResult);
        assertEquals(4, rls.size());

        addResult = rls.add(TimeSpan.get(2, 32));
        assertTrue(addResult);
        assertEquals(2, rls.size());

        // No op
        addResult = rls.add(TimeSpan.get(10, 20));
        assertFalse(addResult);
        assertEquals(2, rls.size());
    }

    /**
     * Test intersect.
     */
    @Test
    public void testIntersection()
    {
        // All intersect functions use List<RangeLongBlock>
        // getIntersectionList(RangeLongBlock blockToIntersect)

        DefaultTimeSpanSet rls = new DefaultTimeSpanSet();
        rls.add(TimeSpan.get(2, 3));
        rls.add(TimeSpan.get(5, 6));
        rls.add(TimeSpan.get(8, 9));
        rls.add(TimeSpan.get(11, 15));
        rls.add(TimeSpan.get(55, 100));

        // Leading edge intersect
        TimeSpanSet leadingEdgeSet = new DefaultTimeSpanSet(TimeSpan.get(4, 6));
        TimeSpanSet intersect = rls.intersection(leadingEdgeSet);
        assertEquals(intersect.size(), 1);
        assertTrue(intersect.contains(5));

        // Trailing edge intersect
        TimeSpanSet trailingEdgeSet = new DefaultTimeSpanSet(TimeSpan.get(5, 6));
        intersect = rls.intersection(trailingEdgeSet);
        assertEquals(intersect.size(), 1);
        assertTrue(intersect.contains(5));

        // No intersect leading, trailing, interior
        TimeSpanSet noIntLeadingEdgeSet = new DefaultTimeSpanSet(TimeSpan.get(1, 1));
        intersect = rls.intersection(noIntLeadingEdgeSet);
        assertTrue(intersect.isEmpty());

        TimeSpanSet noIntTrailingEdgeSet = new DefaultTimeSpanSet(TimeSpan.get(100, 888));
        intersect = rls.intersection(noIntTrailingEdgeSet);
        assertTrue(intersect.isEmpty());

        TimeSpanSet noIntInteriorSet = new DefaultTimeSpanSet(TimeSpan.get(15, 54));
        intersect = rls.intersection(noIntInteriorSet);
        assertTrue(intersect.isEmpty());

        // Subset
        TimeSpanSet subSet = new DefaultTimeSpanSet(TimeSpan.get(60, 70));
        intersect = rls.intersection(subSet);
        assertEquals(intersect.size(), 1);
        assertEquals(subSet, intersect);

        // Superset
        DefaultTimeSpanSet superSet = new DefaultTimeSpanSet(TimeSpan.get(10, 16));
        intersect = rls.intersection(superSet);
        assertEquals(intersect.size(), 1);
        assertTrue(intersect.contains(11));
        assertTrue(intersect.contains(12));
        assertTrue(intersect.contains(13));
        assertTrue(intersect.contains(14));
        assertFalse(intersect.contains(15));

        // Superset spanning multiple
        DefaultTimeSpanSet superSetSpans2 = new DefaultTimeSpanSet(TimeSpan.get(7, 16));
        intersect = rls.intersection(superSetSpans2);
        assertEquals(intersect.size(), 2);
        assertTrue(intersect.contains(8));
        assertFalse(intersect.contains(9));
        assertTrue(intersect.contains(11));
        assertTrue(intersect.contains(12));
        assertTrue(intersect.contains(13));
        assertTrue(intersect.contains(14));
        assertFalse(intersect.contains(15));

        // Superset spanning all
        DefaultTimeSpanSet spansAll = new DefaultTimeSpanSet(TimeSpan.get(1, 1600));
        intersect = rls.intersection(spansAll);
        assertEquals(rls, intersect);
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
        DefaultTimeSpanSet rls = new DefaultTimeSpanSet();
        rls.addAll(createTestTimeSpans(setA));
        rls.add(TimeSpan.get(55, 100));
        assertEquals(5, rls.size());

        // Remove leading edge.
        boolean changed = rls.remove(TimeSpan.get(54, 57));
        assertTrue(changed);
        assertEquals(5, rls.size());
        changed = rls.remove(TimeSpan.get(10, 13));
        assertTrue(changed);
        assertEquals(5, rls.size());
        changed = rls.remove(TimeSpan.get(1, 2));
        assertTrue(changed);
        assertEquals(5, rls.size());

        // Remove trailing edge
        changed = rls.remove(TimeSpan.get(14, 21));
        assertTrue(changed);
        assertEquals(5, rls.size());
        changed = rls.remove(TimeSpan.get(98, 111));
        assertTrue(changed);
        assertEquals(5, rls.size());
        rls.add(TimeSpan.get(1, 2));
        changed = rls.remove(TimeSpan.get(2, 4));
        assertTrue(changed);
        assertEquals(5, rls.size());

        // remove interior ( split )
        changed = rls.remove(TimeSpan.get(60, 66));
        assertTrue(changed);
        assertEquals(6, rls.size());

        // Remove no op ( not in set )
        changed = rls.remove(TimeSpan.get(60, 66));
        assertFalse(changed);
        assertEquals(6, rls.size());

        // Remove exact
        changed = rls.remove(TimeSpan.get(57, 60));
        assertTrue(changed);
        assertEquals(5, rls.size());

        // Remove spanning
        changed = rls.remove(TimeSpan.get(3, 8));
        assertTrue(changed);
        assertEquals(4, rls.size());

        // Remove multi-spanning.
        changed = rls.remove(TimeSpan.get(7, 61));
        assertTrue(changed);
        assertEquals(2, rls.size());
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#union(TimeSpanSet)}.
     */
    @Test
    public void testUnion()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        long[] setB = { 1, 2, 5, 7, 15, 17, 50, 51, 52, 57 };
        DefaultTimeSpanSet rlsB = new DefaultTimeSpanSet();
        rlsB.addAll(createTestTimeSpans(setB));

        TimeSpanSet unionedSet = rlsA.union(rlsB);

        assertEquals(6, unionedSet.size());

        assertEquals(1, unionedSet.getTimeSpans().get(0).getStart());
        assertEquals(3, unionedSet.getTimeSpans().get(0).getEnd());

        assertEquals(5, unionedSet.getTimeSpans().get(1).getStart());
        assertEquals(10, unionedSet.getTimeSpans().get(1).getEnd());

        assertEquals(11, unionedSet.getTimeSpans().get(2).getStart());
        assertEquals(16, unionedSet.getTimeSpans().get(2).getEnd());

        assertEquals(17, unionedSet.getTimeSpans().get(3).getStart());
        assertEquals(18, unionedSet.getTimeSpans().get(3).getEnd());

        assertEquals(50, unionedSet.getTimeSpans().get(4).getStart());
        assertEquals(53, unionedSet.getTimeSpans().get(4).getEnd());

        assertEquals(55, unionedSet.getTimeSpans().get(5).getStart());
        assertEquals(58, unionedSet.getTimeSpans().get(5).getEnd());
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#toString()}.
     */
    @Test
    public void testToString()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        assertEquals("TimeSpanSet{TimeSpan[1970-01-01 00:00:00.001,1970-01-01 00:00:00.003],"
                + "TimeSpan[1970-01-01 00:00:00.005,1970-01-01 00:00:00.007],"
                + "TimeSpan[1970-01-01 00:00:00.008,1970-01-01 00:00:00.010],"
                + "TimeSpan[1970-01-01 00:00:00.011,1970-01-01 00:00:00.016],"
                + "TimeSpan[1970-01-01 00:00:00.055,1970-01-01 00:00:00.058]}", rlsA.toString());
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#toArray()}.
     */
    @Test
    public void testToArray()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        Object[] timeSpans = rlsA.toArray();

        assertEquals(5, timeSpans.length);
        assertTrue(timeSpans[0] instanceof TimeSpan);
        assertTrue(timeSpans[1] instanceof TimeSpan);
        assertTrue(timeSpans[2] instanceof TimeSpan);
        assertTrue(timeSpans[3] instanceof TimeSpan);
        assertTrue(timeSpans[4] instanceof TimeSpan);

        assertEquals(1, ((TimeSpan)timeSpans[0]).getStart());
        assertEquals(3, ((TimeSpan)timeSpans[0]).getEnd());

        assertEquals(5, ((TimeSpan)timeSpans[1]).getStart());
        assertEquals(7, ((TimeSpan)timeSpans[1]).getEnd());

        assertEquals(8, ((TimeSpan)timeSpans[2]).getStart());
        assertEquals(10, ((TimeSpan)timeSpans[2]).getEnd());

        assertEquals(11, ((TimeSpan)timeSpans[3]).getStart());
        assertEquals(16, ((TimeSpan)timeSpans[3]).getEnd());

        assertEquals(55, ((TimeSpan)timeSpans[4]).getStart());
        assertEquals(58, ((TimeSpan)timeSpans[4]).getEnd());
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#toArray(Object[])}.
     */
    @Test
    public void testToArrayParameter()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        TimeSpan[] timeSpans = rlsA.toArray(new TimeSpan[2]);

        assertEquals(5, timeSpans.length);

        assertEquals(1, timeSpans[0].getStart());
        assertEquals(3, timeSpans[0].getEnd());

        assertEquals(5, timeSpans[1].getStart());
        assertEquals(7, timeSpans[1].getEnd());

        assertEquals(8, timeSpans[2].getStart());
        assertEquals(10, timeSpans[2].getEnd());

        assertEquals(11, timeSpans[3].getStart());
        assertEquals(16, timeSpans[3].getEnd());

        assertEquals(55, timeSpans[4].getStart());
        assertEquals(58, timeSpans[4].getEnd());
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#getAsTimespan(Object)}.
     */
    @Test
    public void testGetAsTimeSpanTimeSpan()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        TimeSpan timeSpan = new TimelessTimeSpan();

        assertEquals(timeSpan, rlsA.getAsTimespan(timeSpan));
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#getAsTimespan(Object)}.
     */
    @Test
    public void testGetAsTimeSpanTimeSpanProvider()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        TimeSpanProvider provider = createStrictMock(TimeSpanProvider.class);
        TimeSpan timeSpan = new TimelessTimeSpan();
        expect(provider.getTimeSpan()).andReturn(timeSpan);
        replay(provider);

        assertEquals(timeSpan, rlsA.getAsTimespan(provider));

        verify(provider);
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#getAsTimespan(Object)}.
     */
    @Test
    public void testGetAsTimeSpanOther()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        assertNull(rlsA.getAsTimespan("foo"));
    }

    /**
     * Test method for
     * {@link DefaultTimeSpanSet#containsAll(java.util.Collection)}.
     */
    @Test
    public void testContainsAll()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        Collection<TimeSpan> testValues = Arrays.asList(new TimeSpanLongLong(1, 2), new TimeSpanLongLong(11, 16));

        assertTrue(rlsA.containsAll(testValues));
    }

    /**
     * Test method for
     * {@link DefaultTimeSpanSet#containsAll(java.util.Collection)}.
     */
    @Test
    public void testContainsAllWrongType()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));

        Collection<String> testValues = Arrays.asList(RandomStringUtils.randomAlphabetic(6),
                RandomStringUtils.randomAlphabetic(4));

        assertFalse(rlsA.containsAll(testValues));
    }

    /**
     * Test method for
     * {@link DefaultTimeSpanSet#containsAll(java.util.Collection)}.
     */
    @Test
    public void testContainsAllOutOfBounds()
    {
        long[] setA = { 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 15, 55, 56, 57 };
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        rlsA.addAll(createTestTimeSpans(setA));
        Collection<TimeSpan> testValues = Arrays.asList(new TimeSpanLongLong(1, 2), new TimeSpanLongLong(60, 72));

        assertFalse(rlsA.containsAll(testValues));
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#DefaultTimeSpanSet(TimeSpan)}.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testDefaultTimeSpanSetTimeSpan()
    {
        new DefaultTimeSpanSet(new TimelessTimeSpan());
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#add(TimeSpan)}.
     */
    @Test
    public void testAddNull()
    {
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        assertFalse(rlsA.add((TimeSpan)null));
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#add(TimeSpan)}.
     */
    @Test
    public void testAddTimeless()
    {
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        assertFalse(rlsA.add(new TimelessTimeSpan()));
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#equals(Object)}.
     */
    @Test
    public void testEqualsSameObject()
    {
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        assertEquals(rlsA, rlsA);
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#equals(Object)}.
     */
    @Test
    public void testEqualsNull()
    {
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        Assert.assertNotNull(rlsA);
    }

    /**
     * Test method for {@link DefaultTimeSpanSet#equals(Object)}.
     */
    @Test
    public void testEqualsWrongType()
    {
        DefaultTimeSpanSet rlsA = new DefaultTimeSpanSet();
        assertFalse(rlsA.equals(RandomStringUtils.randomAlphabetic(5)));
    }
}
