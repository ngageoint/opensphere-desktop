package io.opensphere.mantle.data.cache.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * A test designed to exercise the functionality of the
 * {@link CacheEntryLUWrapper} class.
 */
public class CacheEntryLUWrapperTest
{
    /**
     * The object on which tests are performed.
     */
    private CacheEntryLUWrapper myTestObject;

    /**
     * The entry contained within the wrapper.
     */
    private CacheEntry myTestEntry;

    /**
     * Creates the resources needed to execute the tests.
     */
    @Before
    public void setUp()
    {
        DynamicEnumerationRegistry registry = createStrictMock(DynamicEnumerationRegistry.class);
        DataElement element = createStrictMock(DataElement.class);
        VisualizationState state = new VisualizationState(false);

        expect(element.getVisualizationState()).andReturn(state);
        TimeSpan span = TimeSpan.get(0, 1);
        expect(element.getTimeSpan()).andReturn(span).anyTimes();

        DataTypeInfo mockDataType = createStrictMock(DataTypeInfo.class);
        expect(element.getDataTypeInfo()).andReturn(mockDataType);

        expect(mockDataType.getTypeKey()).andReturn("TYPE_KEY").anyTimes();

        MetaDataProvider mockMetaDataProvider = createStrictMock(MetaDataProvider.class);
        expect(element.getMetaData()).andReturn(mockMetaDataProvider);

        expect(element.getId()).andReturn(Long.MAX_VALUE);

        replay(element, mockDataType);
        boolean useDynamicClasses = false;
        myTestEntry = new CacheEntry(registry, element, useDynamicClasses);

        myTestObject = new CacheEntryLUWrapper(myTestEntry);
    }

    /**
     * Test method for {@link CacheEntryLUWrapper#hashCode()}.
     */
    @Test
    public void testHashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myTestEntry.hashCode();

        result = prime * result + (int)(myTestEntry.getLastUsedTime() ^ myTestEntry.getLastUsedTime() >>> 32);
        assertEquals(result, myTestObject.hashCode());
    }

    /**
     * Test method for
     * {@link CacheEntryLUWrapper#CacheEntryLUWrapper(CacheEntry)} .
     */
    @Test
    public void testCacheEntryLUWrapper()
    {
        assertNotNull(myTestObject);
    }

    /**
     * Test method for
     * {@link CacheEntryLUWrapper#compareTo(CacheEntryLUWrapper)} .
     */
    @Test
    public void testCompareTo()
    {
        assertEquals(0, myTestObject.compareTo(myTestObject));

        DynamicEnumerationRegistry registry = createStrictMock(DynamicEnumerationRegistry.class);
        DataElement element = createStrictMock(DataElement.class);
        VisualizationState state = new VisualizationState(false);

        expect(element.getVisualizationState()).andReturn(state);
        TimeSpan span = TimeSpan.get(0, 1);
        expect(element.getTimeSpan()).andReturn(span).anyTimes();

        DataTypeInfo mockDataType = createStrictMock(DataTypeInfo.class);
        expect(element.getDataTypeInfo()).andReturn(mockDataType);

        expect(mockDataType.getTypeKey()).andReturn("TYPE_KEY").anyTimes();

        MetaDataProvider mockMetaDataProvider = createStrictMock(MetaDataProvider.class);
        expect(element.getMetaData()).andReturn(mockMetaDataProvider);

        expect(element.getId()).andReturn(Long.MAX_VALUE);

        replay(element, mockDataType);
        boolean useDynamicClasses = false;

        try
        {
            Thread.sleep(25);
        }
        catch (InterruptedException e)
        {
            /* safe to ignore */
        }
        CacheEntry otherEntry = new CacheEntry(registry, element, useDynamicClasses);

        CacheEntryLUWrapper otherObject = new CacheEntryLUWrapper(myTestEntry);
        assertEquals(0, myTestObject.compareTo(otherObject));

        CacheEntryLUWrapper yetAnotherObject = new CacheEntryLUWrapper(otherEntry);
        assertEquals(-1, myTestObject.compareTo(yetAnotherObject));
    }

    /**
     * Test method for {@link CacheEntryLUWrapper#equals(Object)}.
     */
    @Test
    public void testEqualsObject()
    {
        assertNotNull(myTestObject);
        // Is this a valid test?
        assertEquals(myTestObject, myTestObject);
        assertNotSame(myTestObject, new Object());

        CacheEntryLUWrapper otherObject = new CacheEntryLUWrapper(myTestEntry);
        assertEquals(otherObject, myTestObject);
    }

    /**
     * Test method for {@link CacheEntryLUWrapper#getEntry()}.
     */
    @Test
    public void testGetEntry()
    {
        assertEquals(myTestEntry, myTestObject.getEntry());
    }
}
