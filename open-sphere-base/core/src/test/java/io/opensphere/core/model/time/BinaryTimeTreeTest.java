package io.opensphere.core.model.time;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gnu.trove.list.TIntList;

/**
 * BinaryTimeTreeTest.
 */
public class BinaryTimeTreeTest
{
    /**
     * The object on which tests are performed.
     */
    private BinaryTimeTree<TimeSpanProvider> myTestObject;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws java.lang.Exception if the resources cannot be initialized.
     */
    @Before
    public void setUp() throws Exception
    {
        myTestObject = new BinaryTimeTree<>();
    }

    /**
     * Test method to verify that there are no private methods in the
     * {@link BinaryTimeTree} class.
     */
    @Test
    public void testNonPrivateMethods()
    {
        Method[] declaredMethods = BinaryTimeTree.class.getDeclaredMethods();

        for (Method method : declaredMethods)
        {
            if (!method.getName().startsWith("$"))
            {
                assertFalse(method.getName() + " is private. No private methods are permitted.",
                        Modifier.isPrivate(method.getModifiers()));
            }
        }
    }

    /**
     * Test method for {@link BinaryTimeTree#BinaryTimeTree()}.
     */
    @Test
    public void testBinaryTimeTree()
    {
        assertNotNull(myTestObject);
    }

    /**
     * Test method for {@link BinaryTimeTree#clear()}.
     */
    @Test
    public void testClear()
    {
        myTestObject.clear();
        assertEquals(0, myTestObject.size());
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#countInRange(io.opensphere.core.model.time.TimeSpan)}
     * .
     */
    @Test(expected = NullPointerException.class)
    public void testCountInRangeEmptyNode()
    {
        assertEquals(0, myTestObject.countInRange(TimeSpan.ZERO));
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#countInRange(io.opensphere.core.model.time.TimeSpan)}
     * .
     */
    @Test
    public void testCountInRange()
    {
        TimeSpan span1 = new TimeSpanLongLong(0, 1);
        TimeSpan span2 = new TimeSpanLongLong(1, 2);

        TimeSpanProvider mock1 = createNiceMock(TimeSpanProvider.class);
        TimeSpanProvider mock2 = createNiceMock(TimeSpanProvider.class);

        expect(mock1.getTimeSpan()).andReturn(span1).anyTimes();
        expect(mock2.getTimeSpan()).andReturn(span2).anyTimes();

        replay(mock1, mock2);

        myTestObject.insert(Arrays.asList(mock1, mock2));

        assertEquals(1, myTestObject.countInRange(span1));
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#countsInBins(io.opensphere.core.model.time.TimeSpan, int)}
     * .
     */
    @Test
    public void testCountsInBins()
    {
        TimeSpan span1 = new TimeSpanLongLong(0, 100);
        TimeSpan span2 = new TimeSpanLongLong(0, 50);

        TimeSpanProvider mock1 = createNiceMock(TimeSpanProvider.class);
        TimeSpanProvider mock2 = createNiceMock(TimeSpanProvider.class);

        expect(mock1.getTimeSpan()).andReturn(span1).anyTimes();
        expect(mock2.getTimeSpan()).andReturn(span2).anyTimes();

        replay(mock1, mock2);

        myTestObject.insert(Arrays.asList(mock1, mock2));

        CountReport report = myTestObject.countsInBins(span1, 50);
        assertEquals(2, report.getMaxBinCount());
        assertEquals(1, report.getMinBinCount());
        assertEquals(76, report.getTotalCount());
    }

    /**
     * Test method for {@link BinaryTimeTree#countsInRanges(java.util.List)}.
     */
    @Test
    public void testCountsInRanges()
    {
        TimeSpan span1 = new TimeSpanLongLong(0, 100);
        TimeSpan span2 = new TimeSpanLongLong(0, 50);

        TimeSpanProvider mock1 = createNiceMock(TimeSpanProvider.class);
        TimeSpanProvider mock2 = createNiceMock(TimeSpanProvider.class);

        expect(mock1.getTimeSpan()).andReturn(span1).anyTimes();
        expect(mock2.getTimeSpan()).andReturn(span2).anyTimes();

        replay(mock1, mock2);

        myTestObject.insert(Arrays.asList(mock1, mock2));
        TIntList list = myTestObject.countsInRanges(Arrays.asList(span1, span2));

        assertEquals(2, list.size());
    }

    /**
     * Test method for {@link BinaryTimeTree#countsInRanges(java.util.List)}.
     */
    @Test
    public void testCountsInRangesEmpty()
    {
        TIntList list = myTestObject.countsInRanges(new ArrayList<>());
        assertTrue(list.isEmpty());
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#insert(io.opensphere.core.model.time.TimeSpanProvider)}
     * .
     */
    @Test
    public void testInsertE()
    {
        TimeSpan span1 = new TimeSpanLongLong(0, 1);
        TimeSpanProvider mock1 = createNiceMock(TimeSpanProvider.class);
        expect(mock1.getTimeSpan()).andReturn(span1).anyTimes();
        replay(mock1);

        myTestObject.insert(mock1);

        assertEquals(1, myTestObject.size());
    }

    /**
     * Test method for {@link BinaryTimeTree#insert(java.util.List)}.
     */
    @Test
    public void testInsertListOfE()
    {
        TimeSpan span1 = new TimeSpanLongLong(0, 1);
        TimeSpan span2 = new TimeSpanLongLong(1, 2);

        TimeSpanProvider mock1 = createNiceMock(TimeSpanProvider.class);
        TimeSpanProvider mock2 = createNiceMock(TimeSpanProvider.class);

        expect(mock1.getTimeSpan()).andReturn(span1).anyTimes();
        expect(mock2.getTimeSpan()).andReturn(span2).anyTimes();

        replay(mock1, mock2);

        myTestObject.insert(Arrays.asList(mock1, mock2));

        assertEquals(2, myTestObject.size());
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#internalSize(io.opensphere.core.model.time.BTreeNode)}
     * .
     */
    @Test
    public void testInternalSize()
    {
        BTreeNode<TimeSpanProvider> node = new BTreeNode<>();

        BTreeNode<TimeSpanProvider> subNode1 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode2 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode3 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode4 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode5 = new BTreeNode<>();

        subNode1.getValues().add(createMock(TimeSpanProvider.class));
        subNode1.getValues().add(createMock(TimeSpanProvider.class));

        subNode2.getValues().add(createMock(TimeSpanProvider.class));
        subNode2.getValues().add(createMock(TimeSpanProvider.class));
        subNode2.getValues().add(createMock(TimeSpanProvider.class));

        subNode3.getValues().add(createMock(TimeSpanProvider.class));
        subNode3.getValues().add(createMock(TimeSpanProvider.class));
        node.setSubNodes(new ArrayList<>(Arrays.asList(subNode1, subNode2, subNode3, subNode4, subNode5)));

        node.setRange(new TimeSpanLongLong(0, 1000));

        TimeSpanProvider provider1 = createMock(TimeSpanProvider.class);
        TimeSpanProvider provider2 = createMock(TimeSpanProvider.class);

        TimeSpanLongLong span1 = new TimeSpanLongLong(10, 20);
        expect(provider1.getTimeSpan()).andReturn(span1).anyTimes();
        TimeSpanLongLong span2 = new TimeSpanLongLong(20, 200);
        expect(provider2.getTimeSpan()).andReturn(span2).anyTimes();

        replay(provider1, provider2);

        assertEquals(7, myTestObject.internalSize(node));
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#maxValuesPerNodeInteral(io.opensphere.core.model.time.BTreeNode, int)}
     * .
     */
    @Test
    public void testMaxValuesPerNodeInteral()
    {
        TimeSpan span1 = new TimeSpanLongLong(0, 1);
        TimeSpan span2 = new TimeSpanLongLong(1, 2);

        TimeSpanProvider mock1 = createNiceMock(TimeSpanProvider.class);
        TimeSpanProvider mock2 = createNiceMock(TimeSpanProvider.class);
        TimeSpanProvider mock3 = createNiceMock(TimeSpanProvider.class);
        TimeSpanProvider mock4 = createNiceMock(TimeSpanProvider.class);
        TimeSpanProvider mock5 = createNiceMock(TimeSpanProvider.class);

        expect(mock1.getTimeSpan()).andReturn(span1).anyTimes();
        expect(mock2.getTimeSpan()).andReturn(span2).anyTimes();

        replay(mock1, mock2);

        BTreeNode<TimeSpanProvider> node = new BTreeNode<>();
        node.getValues().add(mock1);
        node.getValues().add(mock2);
        BTreeNode<TimeSpanProvider> subNode = new BTreeNode<>();
        subNode.getValues().add(mock3);
        subNode.getValues().add(mock4);
        subNode.getValues().add(mock5);
        node.getSubNodes().add(subNode);

        assertEquals(3, myTestObject.maxValuesPerNodeInteral(node, 1));
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#maxValuesPerNodeInteral(io.opensphere.core.model.time.BTreeNode, int)}
     * .
     */
    @Test
    public void testMaxValuesPerNodeInteralNull()
    {
        assertEquals(0, myTestObject.maxValuesPerNodeInteral(null, 5));
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#maxValuesPerNodeInteral(io.opensphere.core.model.time.BTreeNode, int)}
     * .
     */
    @Test
    public void testMaxValuesPerNodeInteralEmpty()
    {
        assertEquals(5, myTestObject.maxValuesPerNodeInteral(new BTreeNode<>(), 5));
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#enlargeTimeRange(java.util.Collection, io.opensphere.core.model.time.TimeSpan)}
     * .
     */
    @Test
    public void testEnlargeTimeRange()
    {
        TimeSpan span = new TimeSpanLongLong(10, 20);

        TimeSpan span1 = new TimeSpanLongLong(0, 8);
        TimeSpan span2 = new TimeSpanLongLong(26, 50);

        TimeSpan result = myTestObject.enlargeTimeRange(Arrays.asList(span1, span2), span);

        assertEquals(0, result.getStart());
        assertEquals(50, result.getEnd());
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#nodeClear(io.opensphere.core.model.time.BTreeNode)}
     * .
     */
    @Test
    public void testNodeClearWithSubnodes()
    {
        BTreeNode<TimeSpanProvider> node = new BTreeNode<>();

        BTreeNode<TimeSpanProvider> subNode1 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode2 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode3 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode4 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode5 = new BTreeNode<>();

        subNode1.getValues().add(createMock(TimeSpanProvider.class));
        subNode1.getValues().add(createMock(TimeSpanProvider.class));

        subNode2.getValues().add(createMock(TimeSpanProvider.class));
        subNode2.getValues().add(createMock(TimeSpanProvider.class));
        subNode2.getValues().add(createMock(TimeSpanProvider.class));

        subNode3.getValues().add(createMock(TimeSpanProvider.class));
        subNode3.getValues().add(createMock(TimeSpanProvider.class));

        subNode5.setValues(null);

        node.setSubNodes(new ArrayList<>(Arrays.asList(subNode1, subNode2, subNode3, subNode4, subNode5)));

        node.setValues(new ArrayList<>(Arrays.asList(createMock(TimeSpanProvider.class), createMock(TimeSpanProvider.class))));

        myTestObject.nodeClear(node);

        assertTrue(node.getValues().isEmpty());
        assertTrue(node.getSubNodes().isEmpty());
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#nodeClear(io.opensphere.core.model.time.BTreeNode)}
     * .
     */
    @Test
    public void testNodeClearWithNullValues()
    {
        BTreeNode<TimeSpanProvider> node = new BTreeNode<>();

        node.setValues(null);
        node.setSubNodes(null);

        myTestObject.nodeClear(node);

        assertNull(node.getValues());
        assertNull(node.getSubNodes());
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#nodeClear(io.opensphere.core.model.time.BTreeNode)}
     * .
     */
    @Test
    public void testNodeClearNull()
    {
        // no exceptions is passing
        myTestObject.nodeClear(null);
    }

    /**
     * Test method for
     * {@link BinaryTimeTree#subDivide(java.util.List, io.opensphere.core.model.time.BTreeNode)}
     * .
     */
    @Test
    public void testSubDivide()
    {
        myTestObject = new BinaryTimeTree<>(1, 1);

        BTreeNode<TimeSpanProvider> node = new BTreeNode<>();

        BTreeNode<TimeSpanProvider> subNode1 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode2 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode3 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode4 = new BTreeNode<>();
        BTreeNode<TimeSpanProvider> subNode5 = new BTreeNode<>();

        subNode1.getValues().add(createMock(TimeSpanProvider.class));
        subNode1.getValues().add(createMock(TimeSpanProvider.class));

        subNode2.getValues().add(createMock(TimeSpanProvider.class));
        subNode2.getValues().add(createMock(TimeSpanProvider.class));
        subNode2.getValues().add(createMock(TimeSpanProvider.class));

        subNode3.getValues().add(createMock(TimeSpanProvider.class));
        subNode3.getValues().add(createMock(TimeSpanProvider.class));
        node.setSubNodes(new ArrayList<>(Arrays.asList(subNode1, subNode2, subNode3, subNode4, subNode5)));

        node.setRange(new TimeSpanLongLong(0, 1000));

        TimeSpanProvider provider1 = createMock(TimeSpanProvider.class);
        TimeSpanProvider provider2 = createMock(TimeSpanProvider.class);

        TimeSpanLongLong span1 = new TimeSpanLongLong(10, 20);
        expect(provider1.getTimeSpan()).andReturn(span1).anyTimes();
        TimeSpanLongLong span2 = new TimeSpanLongLong(20, 200);
        expect(provider2.getTimeSpan()).andReturn(span2).anyTimes();

        replay(provider1, provider2);

        List<TimeSpanProvider> values = new ArrayList<>(Arrays.asList(provider1, provider2));

        node.setValues(values);

        myTestObject.subDivide(values, node);

        assertEquals(7, node.getSubNodes().size());
    }
}
