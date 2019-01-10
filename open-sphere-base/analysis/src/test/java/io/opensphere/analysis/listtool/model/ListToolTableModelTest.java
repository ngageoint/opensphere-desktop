package io.opensphere.analysis.listtool.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.analysis.table.model.MetaColumn;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.test.SwingJunitTestRunner;

/** Tests for {@link ListToolTableModel}. */
@RunWith(SwingJunitTestRunner.class)
public class ListToolTableModelTest
{
    /**
     * Tests it.
     */
    @Test
    public void testIt()
    {
        // Create data type
        final DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        metaData.addKey("Field1", String.class, this);
        metaData.addKey("Field2", String.class, this);
        metaData.copyKeysToOriginalKeys();
        final DataTypeInfo dataType = new DefaultDataTypeInfo(null, "sourcePrefix", "typeKey", "typeName", "displayName", true,
                metaData);

        final Toolbox toolbox = EasyMock.createNiceMock(Toolbox.class);
        final PluginToolboxRegistry pluginToolboxRegistry = EasyMock.createNiceMock(PluginToolboxRegistry.class);
        final MantleToolbox mantleToolbox = EasyMock.createNiceMock(MantleToolbox.class);

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginToolboxRegistry).anyTimes();
        EasyMock.expect(pluginToolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantleToolbox).anyTimes();

        EasyMock.replay(toolbox, pluginToolboxRegistry, mantleToolbox);

        // Create table model
        final ListToolTableModel model = new ListToolTableModel(toolbox, dataType);
        final TestDataElementProvider rowDataProvider = new TestDataElementProvider(toolbox, model, dataType,
                model.getMetaColumns());
        model.setRowDataProvider(rowDataProvider);
        final TestTableModelListener listener = new TestTableModelListener();
        model.addTableModelListener(listener);

        // Seed "mantle" with data
        long id = 1;
        // 0
        rowDataProvider.getMap().put(id, newDataElement(id++, dataType, "Papyrus", "Containing"));
        // 1 delete
        rowDataProvider.getMap().put(id, newDataElement(id++, dataType, "The", "Spell"));
        // 2 delete
        rowDataProvider.getMap().put(id, newDataElement(id++, dataType, "To", "Preserve"));
        // 3
        rowDataProvider.getMap().put(id, newDataElement(id++, dataType, "Its", "Possessor"));
        // 4 delete
        rowDataProvider.getMap().put(id, newDataElement(id++, dataType, "Against", "Attacks"));
        // 5
        rowDataProvider.getMap().put(id, newDataElement(id++, dataType, "From", "He"));
        // 6
        rowDataProvider.getMap().put(id, newDataElement(id++, dataType, "Who", "Is"));

        final int metaSize = model.getMetaColumns().size();

        // Test columns
        Assert.assertEquals(metaSize + 2, model.getColumnCount());
        Assert.assertEquals("Field1", model.getColumnName(metaSize));
        Assert.assertEquals("Field2", model.getColumnName(metaSize + 1));

        // Test adding data
        model.addIds(CollectionUtilities.listViewLong(1, 2, 3, 4, 5, 6, 7), true);
        Assert.assertEquals(7, model.getRowCount());
        Assert.assertEquals("Papyrus", model.getValueAt(0, metaSize));
        Assert.assertEquals("The", model.getValueAt(1, metaSize));
        Assert.assertEquals("To", model.getValueAt(2, metaSize));
        Assert.assertEquals("Its", model.getValueAt(3, metaSize));
        Assert.assertEquals("Against", model.getValueAt(4, metaSize));
        Assert.assertEquals("From", model.getValueAt(5, metaSize));
        Assert.assertEquals("Who", model.getValueAt(6, metaSize));
        Assert.assertEquals("Containing", model.getValueAt(0, metaSize + 1));
        Assert.assertEquals(CollectionUtilities.listViewInt(0, 1, 2, 3, 4, 5, 6),
                listener.getMap().get(Integer.valueOf(TableModelEvent.INSERT)));

        // Test removing data
        model.removeIds(CollectionUtilities.listViewLong(2, 3, 5));
        Assert.assertEquals(4, model.getRowCount());
        Assert.assertEquals("Papyrus", model.getValueAt(0, metaSize));
        Assert.assertEquals("Its", model.getValueAt(1, metaSize));
        Assert.assertEquals("From", model.getValueAt(2, metaSize));
        Assert.assertEquals("Who", model.getValueAt(3, metaSize));
        Assert.assertEquals(7, model.getDataAt(3).getId());
        Assert.assertEquals(CollectionUtilities.listViewInt(4, 1, 2),
                listener.getMap().get(Integer.valueOf(TableModelEvent.DELETE)));

        // Test re-adding data to make sure the cache was cleared correctly
        model.addIds(CollectionUtilities.listViewLong(2, 3, 5), true);
        Assert.assertEquals("The", model.getValueAt(4, metaSize));
        Assert.assertEquals("To", model.getValueAt(5, metaSize));
        Assert.assertEquals("Against", model.getValueAt(6, metaSize));

        // Test filtering duplicates
        Assert.assertEquals(7, model.getRowCount());
        model.addIds(CollectionUtilities.listViewLong(2), true);
        Assert.assertEquals(7, model.getRowCount());
        model.addIds(CollectionUtilities.listViewLong(2), false);
        Assert.assertEquals(8, model.getRowCount());
        Assert.assertEquals("The", model.getValueAt(7, metaSize));
    }

    /**
     * Creates a new data element for testing.
     *
     * @param id the id
     * @param dataType the data type
     * @param values the values
     * @return the data element
     */
    private static DataElement newDataElement(final long id, final DataTypeInfo dataType, final String... values)
    {
        final Map<String, Serializable> initialMap = New.insertionOrderMap();
        initialMap.put("Field1", values[0]);
        initialMap.put("Field2", values[1]);
        return new DefaultDataElement(id, TimeSpan.TIMELESS, dataType, new SimpleMetaDataProvider(initialMap));
    }

    /**
     * Test TableModelListener.
     */
    private static class TestTableModelListener implements TableModelListener
    {
        /** The map. */
        private final LazyMap<Integer, Collection<Integer>> myMap = LazyMap.create(New.<Integer, Collection<Integer>>map(),
                Integer.class, New.<Integer>listFactory());

        @Override
        public void tableChanged(final TableModelEvent e)
        {
            for (int row = e.getFirstRow(); row <= e.getLastRow(); row++)
            {
                myMap.get(Integer.valueOf(e.getType())).add(Integer.valueOf(row));
            }
        }

        /**
         * Gets the map.
         *
         * @return the map
         */
        public LazyMap<Integer, Collection<Integer>> getMap()
        {
            return myMap;
        }
    }

    /**
     * Test DataElementProvider.
     */
    private static class TestDataElementProvider extends DataElementProvider
    {
        /** The map. */
        private final TLongObjectMap<DataElement> myMap = new TLongObjectHashMap<>();

        /**
         * Constructor.
         *
         * @param model the table model
         * @param dataType the data type
         * @param metaColumns the meta columns
         */
        public TestDataElementProvider(final Toolbox toolbox, final TableModel model, final DataTypeInfo dataType,
                final List<MetaColumn<?>> metaColumns)
        {
            super(model, toolbox, dataType, metaColumns, -1);
        }

        @Override
        public DataElement lookupDataElement(final long id)
        {
            return myMap.get(id);
        }

        /**
         * Gets the map.
         *
         * @return the map
         */
        public TLongObjectMap<DataElement> getMap()
        {
            return myMap;
        }
    }
}
