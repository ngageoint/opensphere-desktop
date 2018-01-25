package io.opensphere.analysis.export.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * Unit test for {@link ExtraColumnsMetaDataProvider}.
 */
public class ExtraColumnsMetaDataProviderTest
{
    /**
     * The test extra columns.
     */
    private static final String[] ourExtraColumns = new String[] { "col2", "col4", "col5" };

    /**
     * The test extra values.
     */
    private static final String[] ourExtraValues = new String[] { "val", "val4", "val5" };

    /**
     * The test original columns.
     */
    private static final String[] ourOriginalColumns = new String[] { "col1", "col2", "col3" };

    /**
     * The test original values.
     */
    private static final Object[] ourOriginalValues = new String[] { "val1", "val2", "val3" };

    /**
     * Tests getting keys, also verifies it doesn't return dups.
     */
    @Test
    public void testGetKeys()
    {
        EasyMockSupport support = new EasyMockSupport();

        MetaDataProvider original = createProvider(support);
        Map<String, Object> extras = createExtraColumns();

        support.replayAll();

        ExtraColumnsMetaDataProvider extraProvider = new ExtraColumnsMetaDataProvider(original, extras);
        List<String> keys = extraProvider.getKeys();

        assertEquals(ourOriginalColumns.length + ourExtraColumns.length - 1, keys.size());

        for (int i = 0; i < keys.size(); i++)
        {
            if (i < ourOriginalColumns.length)
            {
                assertEquals(ourOriginalColumns[i], keys.get(i));
            }
            else
            {
                assertEquals(ourExtraColumns[i - ourOriginalColumns.length + 1], keys.get(i));
            }
        }

        support.verifyAll();
    }

    /**
     * Tests getting values from original and extra columns. Also verifies that
     * if there is a duplicate column in extra values that it gets that value
     * instead of the original.
     */
    @Test
    public void testGetValue()
    {
        EasyMockSupport support = new EasyMockSupport();

        MetaDataProvider original = createProvider(support);
        Map<String, Object> extras = createExtraColumns();

        support.replayAll();

        ExtraColumnsMetaDataProvider provider = new ExtraColumnsMetaDataProvider(original, extras);

        assertEquals("val1", provider.getValue(ourOriginalColumns[0]));
        assertEquals("val4", provider.getValue("col4"));
        assertEquals("val", provider.getValue(ourOriginalColumns[1]));

        support.verifyAll();
    }

    /**
     * Tests getting values verifies keys and values are in the correct order,
     * also verifies that if there is a duplicate column in extra values that it
     * gets that value instead of the original.
     */
    @Test
    public void testGetValues()
    {
        EasyMockSupport support = new EasyMockSupport();

        MetaDataProvider original = createProvider(support);
        Map<String, Object> extras = createExtraColumns();

        support.replayAll();

        ExtraColumnsMetaDataProvider provider = new ExtraColumnsMetaDataProvider(original, extras);

        List<Object> values = provider.getValues();
        assertEquals(5, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("val", values.get(1));
        assertEquals("val3", values.get(2));
        assertEquals("val4", values.get(3));
        assertEquals("val5", values.get(4));

        support.verifyAll();
    }

    /**
     * Tests the has key, for both original and extra columns.
     */
    @Test
    public void testHasKey()
    {
        EasyMockSupport support = new EasyMockSupport();

        MetaDataProvider original = createProvider(support);
        Map<String, Object> extras = createExtraColumns();

        support.replayAll();

        ExtraColumnsMetaDataProvider provider = new ExtraColumnsMetaDataProvider(original, extras);

        assertTrue(provider.hasKey(ourOriginalColumns[0]));
        assertTrue(provider.hasKey(ourOriginalColumns[1]));
        assertTrue(provider.hasKey("col3"));
        assertTrue(provider.hasKey("col4"));
        assertTrue(provider.hasKey(ourExtraColumns[2]));
        assertFalse(provider.hasKey("col6"));

        support.verifyAll();
    }

    /**
     * Tests that it returns whatever original says.
     */
    @Test
    public void testKeysMutable()
    {
        EasyMockSupport support = new EasyMockSupport();

        MetaDataProvider original = support.createMock(MetaDataProvider.class);
        EasyMock.expect(original.keysMutable()).andReturn(true);
        Map<String, Object> extras = createExtraColumns();

        support.replayAll();

        ExtraColumnsMetaDataProvider provider = new ExtraColumnsMetaDataProvider(original, extras);

        assertTrue(provider.keysMutable());

        support.verifyAll();
    }

    /**
     * Tests removing keys from extra and original, and verifies that if there
     * are dup columns, that they are removed in both extra and original.
     */
    @Test
    public void testRemoveKey()
    {
        EasyMockSupport support = new EasyMockSupport();

        MetaDataProvider original = support.createMock(MetaDataProvider.class);
        EasyMock.expect(original.hasKey(EasyMock.cmpEq(ourOriginalColumns[0]))).andReturn(true);
        original.removeKey(EasyMock.cmpEq(ourOriginalColumns[0]));
        EasyMock.expect(original.hasKey(EasyMock.cmpEq(ourOriginalColumns[1]))).andReturn(true);
        original.removeKey(EasyMock.cmpEq(ourOriginalColumns[1]));
        EasyMock.expect(original.hasKey(EasyMock.cmpEq(ourOriginalColumns[0]))).andReturn(false);
        EasyMock.expect(original.hasKey(EasyMock.cmpEq(ourOriginalColumns[1]))).andReturn(false);
        EasyMock.expect(original.hasKey(EasyMock.cmpEq(ourExtraColumns[2]))).andReturn(false).times(2);

        Map<String, Object> extras = createExtraColumns();

        support.replayAll();

        ExtraColumnsMetaDataProvider provider = new ExtraColumnsMetaDataProvider(original, extras);

        provider.removeKey(ourOriginalColumns[0]);
        provider.removeKey(ourOriginalColumns[1]);
        provider.removeKey(ourExtraColumns[2]);

        assertFalse(provider.hasKey(ourOriginalColumns[0]));
        assertFalse(provider.hasKey(ourOriginalColumns[1]));
        assertFalse(provider.hasKey(ourExtraColumns[2]));

        support.verifyAll();
    }

    /**
     * Tests setting a value on both extra and original and verifies that if
     * there are dups in extras, that it is set in the extra portion.
     */
    @Test
    public void testSetValue()
    {
        EasyMockSupport support = new EasyMockSupport();

        MetaDataProvider original = support.createMock(MetaDataProvider.class);
        EasyMock.expect(original.setValue(EasyMock.cmpEq(ourOriginalColumns[0]), EasyMock.cmpEq("newVal1"))).andReturn(true);

        Map<String, Object> extras = createExtraColumns();

        support.replayAll();

        ExtraColumnsMetaDataProvider provider = new ExtraColumnsMetaDataProvider(original, extras);

        provider.setValue(ourOriginalColumns[0], "newVal1");
        provider.setValue(ourOriginalColumns[1], "newVal2");
        provider.setValue(ourExtraColumns[2], "newVal5");

        assertEquals("newVal2", provider.getValue(ourOriginalColumns[1]));
        assertEquals("newVal5", provider.getValue(ourExtraColumns[2]));

        support.verifyAll();
    }

    /**
     * Tests that it just delegates its call to original.
     */
    @Test
    public void testValuesMutable()
    {
        EasyMockSupport support = new EasyMockSupport();

        MetaDataProvider original = support.createMock(MetaDataProvider.class);
        EasyMock.expect(original.valuesMutable()).andReturn(true);
        Map<String, Object> extras = createExtraColumns();

        support.replayAll();

        ExtraColumnsMetaDataProvider provider = new ExtraColumnsMetaDataProvider(original, extras);

        assertTrue(provider.valuesMutable());

        support.verifyAll();
    }

    /**
     * Creates the map of extra columns and values.
     *
     * @return The extra cell values.
     */
    private Map<String, Object> createExtraColumns()
    {
        Map<String, Object> extra = New.map();

        for (int i = 0; i < ourExtraColumns.length; i++)
        {
            extra.put(ourExtraColumns[i], ourExtraValues[i]);
        }

        return extra;
    }

    /**
     * Creates the original columns to values map.
     *
     * @return The map of original columns to values.
     */
    private Map<String, Object> createOriginalColumns()
    {
        Map<String, Object> original = New.map();

        for (int i = 0; i < ourOriginalColumns.length; i++)
        {
            original.put(ourOriginalColumns[i], ourOriginalValues[i]);
        }

        return original;
    }

    /**
     * Creates an easy mocked {@link MetaDataProvider} that mocks having the
     * original columns and values.
     *
     * @param support Used to create the mock.
     * @return The {@link MetaDataProvider}.
     */
    private MetaDataProvider createProvider(EasyMockSupport support)
    {
        MetaDataProvider provider = support.createMock(MetaDataProvider.class);

        Map<String, Object> originalColumns = createOriginalColumns();

        EasyMock.expect(provider.getKeys()).andReturn(New.list(ourOriginalColumns)).anyTimes();
        EasyMock.expect(provider.getValue(EasyMock.isA(String.class))).andAnswer(() -> getValueAnswer(originalColumns))
                .anyTimes();
        EasyMock.expect(provider.getValues()).andReturn(New.list(ourOriginalValues)).anyTimes();
        EasyMock.expect(provider.hasKey(EasyMock.isA(String.class))).andAnswer(() -> hasKeyAnswer(originalColumns)).anyTimes();

        return provider;
    }

    /**
     * The answer for getValue.
     *
     * @param originalColumns The map of original columns to values.
     * @return The returns the value based of the column name passed in.
     */
    private Object getValueAnswer(Map<String, Object> originalColumns)
    {
        String column = EasyMock.getCurrentArguments()[0].toString();

        return originalColumns.get(column);
    }

    /**
     * The answer for hasKey.
     *
     * @param originalColumns The map of original columns to values.
     * @return The returns the value based of the column name passed in.
     */
    private boolean hasKeyAnswer(Map<String, Object> originalColumns)
    {
        return originalColumns.containsKey(EasyMock.getCurrentArguments()[0].toString());
    }
}
