package io.opensphere.analysis.export.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JTable;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;

import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.table.model.MetaColumnsTableModel;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.export.Exporter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;

/**
 * Tests the {@link ExportExecutor}.
 */
public class ExportExecutorTest
{
    /**
     * The test file extension.
     */
    private static final String ourTestExtension = ".kml";

    /**
     * Tests exporting to a file.
     *
     * @throws IOException Bad IO.
     * @throws ExportException Bad export.
     * @throws InterruptedException Don't interrupt.
     */
//    @Test
    public void testExecuteImport() throws IOException, ExportException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TaskActivity> activities = New.list();

        List<DataElement> dataElements = createTestData(support);

        DataElementCache cache = createCache(support, dataElements.get(0).getDataTypeInfo());
        UIRegistry uiRegistry = createUIRegistry(support, activities);
        EventManager eventManager = support.createMock(EventManager.class);

        File testFile = File.createTempFile("test", ourTestExtension);
        assertTrue(testFile.delete());
        Component parent = new Container();
        MockJFileChooser dialog = new MockJFileChooser(testFile, parent, false);

        CountDownLatch latch = new CountDownLatch(1);
        Exporter exporter = createExporter(support, dataElements, testFile, latch, null);
        EasyMock.expect(Boolean.valueOf(exporter.preExport())).andReturn(Boolean.TRUE);
        MetaColumnsTableModel elements = createTableModel(support, dataElements);

        ExportOptionsModel optionsModel = new ExportOptionsModel();

        PreferencesRegistry prefsRegistry = createPrefsRegistry(support);

        ExportCompleteListener listener = createListener(support, parent, testFile, dataElements.size());

        support.replayAll();

        ExportExecutor executor = new ExportExecutor(cache, uiRegistry, eventManager, prefsRegistry);
        executor.executeExport(dialog, parent, exporter, elements, new JTable(), optionsModel, listener);

        assertEquals(1, activities.size());

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        Thread.sleep(100);

        assertTrue(activities.get(0).isComplete());

        support.verifyAll();
    }

    /**
     * Tests what happens when user cancels the export.
     *
     * @throws IOException Bad IO.
     * @throws ExportException Bad export.
     * @throws InterruptedException Don't interrupt.
     */
//    @Test
    public void testExecuteImportCancel() throws IOException, ExportException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TaskActivity> activities = New.list();

        List<DataElement> dataElements = createTestData(support);

        DataElementCache cache = support.createMock(DataElementCache.class);
        UIRegistry uiRegistry = EasyMock.createMock(UIRegistry.class);
        EventManager eventManager = support.createMock(EventManager.class);

        File testFile = File.createTempFile("test", ourTestExtension);
        testFile.deleteOnExit();
        Component parent = new Container();
        MockJFileChooser dialog = new MockJFileChooser(testFile, parent, true);

        Exporter exporter = support.createMock(Exporter.class);
        EasyMock.expect(exporter.setObjects(EasyMock.anyObject())).andReturn(null);
        EasyMock.expect(Boolean.valueOf(exporter.preExport())).andReturn(Boolean.TRUE);
        MetaColumnsTableModel elements = createTableModel(support, dataElements);

        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(Integer.valueOf(prefs.getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0)))
                .andReturn(Integer.valueOf(0));
        EasyMock.expect(prefsRegistry.getPreferences(ListToolPreferences.class)).andReturn(prefs);

        ExportOptionsModel optionsModel = new ExportOptionsModel();

        ExportCompleteListener listener = support.createMock(ExportCompleteListener.class);

        support.replayAll();

        ExportExecutor executor = new ExportExecutor(cache, uiRegistry, eventManager, prefsRegistry);
        executor.executeExport(dialog, parent, exporter, elements, new JTable(), optionsModel, listener);

        assertEquals(0, activities.size());

        support.verifyAll();
    }

    /**
     * Tests what happens when an exception occurs during export.
     *
     * @throws IOException Bad IO.
     * @throws ExportException Bad Export.
     * @throws InterruptedException Don't interrupt.
     */
//    @Test
    public void testExecuteImportException() throws IOException, ExportException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TaskActivity> activities = New.list();

        List<DataElement> dataElements = createTestData(support);

        DataElementCache cache = createCache(support, dataElements.get(0).getDataTypeInfo());
        UIRegistry uiRegistry = createUIRegistry(support, activities);

        File testFile = File.createTempFile("test", ourTestExtension);
        assertTrue(testFile.delete());
        Component parent = new Container();
        MockJFileChooser dialog = new MockJFileChooser(testFile, parent, false);

        CountDownLatch latch = new CountDownLatch(1);
        ExportException testException = new ExportException("Test exception");
        Exporter exporter = createExporter(support, dataElements, testFile, latch, testException);
        EasyMock.expect(Boolean.valueOf(exporter.preExport())).andReturn(Boolean.TRUE);

        EventManager eventManager = createEventManager(support, testException, testFile, latch);
        MetaColumnsTableModel elements = createTableModel(support, dataElements);
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support);

        ExportOptionsModel optionsModel = new ExportOptionsModel();

        ExportCompleteListener listener = support.createMock(ExportCompleteListener.class);

        support.replayAll();

        ExportExecutor executor = new ExportExecutor(cache, uiRegistry, eventManager, prefsRegistry);
        executor.executeExport(dialog, parent, exporter, elements, new JTable(), optionsModel, listener);

        assertEquals(1, activities.size());

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        Thread.sleep(100);

        assertTrue(activities.get(0).isComplete());

        support.verifyAll();
    }

    /**
     * Tests exporting to a file and the file specified by the user does not
     * have an extension.
     *
     * @throws IOException Bad IO.
     * @throws ExportException Bad export.
     * @throws InterruptedException Don't interrupt.
     */
//    @Test
    public void testExecuteImportNoExtension() throws IOException, ExportException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TaskActivity> activities = New.list();

        List<DataElement> dataElements = createTestData(support);

        DataElementCache cache = createCache(support, dataElements.get(0).getDataTypeInfo());
        UIRegistry uiRegistry = createUIRegistry(support, activities);
        EventManager eventManager = support.createMock(EventManager.class);

        File testFile = File.createTempFile("test", ourTestExtension);
        testFile.deleteOnExit();
        Component parent = new Container();
        MockJFileChooser dialog = new MockJFileChooser(new File(testFile.getAbsolutePath().replace(".kml", "")), parent, false);

        CountDownLatch latch = new CountDownLatch(1);
        Exporter exporter = createExporter(support, dataElements, testFile, latch, null);
        EasyMock.expect(Boolean.valueOf(exporter.preExport())).andReturn(Boolean.TRUE);

        MetaColumnsTableModel elements = createTableModel(support, dataElements);

        ExportOptionsModel optionsModel = new ExportOptionsModel();

        PreferencesRegistry prefsRegistry = createPrefsRegistry(support);

        ExportCompleteListener listener = createListener(support, parent, testFile, dataElements.size());
        listener = createUserAskListener(listener, parent, testFile, Boolean.TRUE);

        support.replayAll();

        ExportExecutor executor = new ExportExecutor(cache, uiRegistry, eventManager, prefsRegistry);
        executor.executeExport(dialog, parent, exporter, elements, new JTable(), optionsModel, listener);

        assertEquals(1, activities.size());

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        Thread.sleep(100);

        assertTrue(activities.get(0).isComplete());

        support.verifyAll();
    }

    /**
     * Tests what happens when user does not want to overwrite a file.
     *
     * @throws IOException Bad IO.
     * @throws ExportException Bad export.
     * @throws InterruptedException Don't interrupt.
     */
//    @Test
    public void testExecuteImportNoOverwrite() throws IOException, ExportException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        List<TaskActivity> activities = New.list();

        List<DataElement> dataElements = createTestData(support);

        DataElementCache cache = support.createMock(DataElementCache.class);
        UIRegistry uiRegistry = EasyMock.createMock(UIRegistry.class);
        EventManager eventManager = support.createMock(EventManager.class);

        File testFile = File.createTempFile("test", ourTestExtension);
        testFile.deleteOnExit();
        Component parent = new Container();
        MockJFileChooser dialog = new MockJFileChooser(testFile, parent, false);

        Exporter exporter = support.createMock(Exporter.class);
        EasyMock.expect(exporter.getMimeType()).andReturn(MimeType.KML).atLeastOnce();
        EasyMock.expect(exporter.setObjects(EasyMock.anyObject())).andReturn(null).times(2);
        EasyMock.expect(Boolean.valueOf(exporter.preExport())).andReturn(Boolean.TRUE);

        MetaColumnsTableModel elements = createTableModel(support, dataElements);

        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(Integer.valueOf(prefs.getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0)))
                .andReturn(Integer.valueOf(0));
        EasyMock.expect(prefsRegistry.getPreferences(ListToolPreferences.class)).andReturn(prefs);

        ExportOptionsModel optionsModel = new ExportOptionsModel();

        ExportCompleteListener listener = support.createMock(ExportCompleteListener.class);
        listener = createUserAskListener(listener, parent, testFile, Boolean.FALSE);

        support.replayAll();

        ExportExecutor executor = new ExportExecutor(cache, uiRegistry, eventManager, prefsRegistry);
        executor.executeExport(dialog, parent, exporter, elements, new JTable(), optionsModel, listener);

        assertEquals(0, activities.size());

        support.verifyAll();
    }

    /**
     * Answer for the addTaskActivity call on the mocked {@link MenuBarRegistry}
     * .
     *
     * @param activities The list to add the added {@link TaskActivity} to.
     * @return Null.
     */
    private Void addTaskActivityAnswer(List<TaskActivity> activities)
    {
        TaskActivity activity = (TaskActivity)EasyMock.getCurrentArguments()[0];
        assertTrue(activity.isActive());
        activities.add(activity);

        return null;
    }

    /**
     * Creates an easy mocked {@link DataElementCache}.
     *
     * @param support Used to create the mock.
     * @param expectedDataType The expected data to be passed to the cache.
     * @return The mocked {@link DataElementCache}.
     */
    private DataElementCache createCache(EasyMockSupport support, DataTypeInfo expectedDataType)
    {
        DirectAccessRetriever retriever = support.createMock(DirectAccessRetriever.class);

        DataElementCache cache = support.createMock(DataElementCache.class);

        EasyMock.expect(cache.getDirectAccessRetriever(EasyMock.eq(expectedDataType))).andReturn(retriever).anyTimes();

        return cache;
    }

    /**
     * Creates an easy mocked {@link EventManager}.
     *
     * @param support Used to create the mock.
     * @param expectedException The expected exception.
     * @param expectedFile The test export file.
     * @param latch The count down latch.
     * @return The mocked {@link EventManager}.
     */
    private EventManager createEventManager(EasyMockSupport support, Exception expectedException, File expectedFile,
            CountDownLatch latch)
    {
        EventManager eventManager = support.createMock(EventManager.class);

        eventManager.publishEvent(EasyMock.isA(UserMessageEvent.class));
        EasyMock.expectLastCall().andAnswer(() -> publishEventAnswer(expectedException, expectedFile, latch));

        return eventManager;
    }

    /**
     * Creates an easy mocked {@link Exporter}.
     *
     * @param support Used to create the mock.
     * @param elements The expected elements to be passed to the exporter.
     * @param expectedFile The expected file to be passed to the exporter.
     * @param latch The count down latch.
     * @param expectedException The expected exception.
     * @return The mocked {@link Exporter}.
     * @throws IOException Bad IO.
     * @throws ExportException Bad export.
     */
    private Exporter createExporter(EasyMockSupport support, List<DataElement> elements, File expectedFile, CountDownLatch latch,
            ExportException expectedException)
        throws IOException, ExportException
    {
        Exporter exporter = support.createMock(Exporter.class);
        EasyMock.expect(exporter.getMimeType()).andReturn(MimeType.KML).atLeastOnce();

        EasyMock.expect(exporter.setObjects(EasyMock.eq(elements))).andReturn(exporter).times(2);
        exporter.export(EasyMock.eq(expectedFile));
        if (expectedException == null)
        {
            EasyMock.expectLastCall().andAnswer(() ->
            {
                latch.countDown();
                return expectedFile;
            });
        }
        else
        {
            EasyMock.expectLastCall().andThrow(expectedException);
        }

        return exporter;
    }

    /**
     * Creates an easy mocked {@link ExportCompleteListener}.
     *
     * @param support Used to create the listener.
     * @param expectedParent The expected parent.
     * @param expectedFile The expected file.
     * @param expectedCount The expected count.
     * @return The mocked {@link ExportCompleteListener}.
     */
    private ExportCompleteListener createListener(EasyMockSupport support, Component expectedParent, File expectedFile,
            int expectedCount)
    {
        ExportCompleteListener listener = support.createMock(ExportCompleteListener.class);

        listener.exportComplete(EasyMock.eq(expectedParent), EasyMock.eq(expectedFile), EasyMock.eq(expectedCount));

        return listener;
    }

    /**
     * Creates an easy mocked {@link PreferencesRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link PreferencesRegistry}.
     */
    private PreferencesRegistry createPrefsRegistry(EasyMockSupport support)
    {
        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(Integer
                .valueOf(prefs.getInt(EasyMock.cmpEq(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS), EasyMock.eq(0))))
                .andReturn(Integer.valueOf(2));

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(registry.getPreferences(EasyMock.eq(ListToolPreferences.class))).andReturn(prefs);

        return registry;
    }

    /**
     * Creates a mocked {@link MetaColumnsTableModel}.
     *
     * @param support Used to create the mock.
     * @param data The test data.
     * @return The mocked {@link MetaColumnsTableModel}.
     */
    private MetaColumnsTableModel createTableModel(EasyMockSupport support, List<DataElement> data)
    {
        MetaColumnsTableModel model = support.createMock(MetaColumnsTableModel.class);

        EasyMock.expect(Integer.valueOf(model.getRowCount())).andReturn(Integer.valueOf(data.size())).anyTimes();
        EasyMock.expect(model.getDataAt(EasyMock.anyInt())).andAnswer(() -> getDataAtAnswer(data)).anyTimes();

        return model;
    }

    /**
     * Creates the test data to work with.
     *
     * @param support Used to mock the {@link DataTypeInfo} contained in the
     *            {@link DataElement}.
     * @return The test data to work with.
     */
    private List<DataElement> createTestData(EasyMockSupport support)
    {
        MetaDataInfo metadataInfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metadataInfo.getLatitudeKey()).andReturn("LAT").anyTimes();
        EasyMock.expect(metadataInfo.getLongitudeKey()).andReturn("LON").anyTimes();

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getMetaDataInfo()).andReturn(metadataInfo).anyTimes();

        // DATE_TIME, ROOM, MESSAGE, LAT, LON,
        List<DataElement> elements = New.list();

        for (int i = 0; i < 4; i++)
        {
            Map<String, Serializable> row = New.map();
            TimeSpan timeSpan = TimeSpan.get(1000 + 1000 * i);
            row.put("DATE_TIME", timeSpan);
            row.put("ROOM", "room" + i);
            row.put("MESSAGE", "message" + i);
            row.put("LAT", Double.valueOf(10.1 * i));
            row.put("LON", Double.valueOf(11.1 * i));

            SimpleMetaDataProvider provider = new SimpleMetaDataProvider(row);
            DataElement element = new DefaultDataElement(i, timeSpan, dataType, provider);
            element.getVisualizationState().setSelected(i % 2 == 0);
            elements.add(element);
        }

        return elements;
    }

    /**
     * Creates an easy mocked {@link UIRegistry}.
     *
     * @param support Used to create the mock.
     * @param activities The list to add the added {@link TaskActivity} to.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support, List<TaskActivity> activities)
    {
        MenuBarRegistry menuBarRegistry = support.createMock(MenuBarRegistry.class);
        menuBarRegistry.addTaskActivity(EasyMock.isA(TaskActivity.class));
        EasyMock.expectLastCall().andAnswer(() -> addTaskActivityAnswer(activities));

        UIRegistry registry = support.createMock(UIRegistry.class);

        EasyMock.expect(registry.getMenuBarRegistry()).andReturn(menuBarRegistry);

        return registry;
    }

    /**
     * Creates an easy mocked {@link ExportCompleteListener} expected
     * askUserForOverwrite to be called.
     *
     * @param listener The already mocked {@link ExportCompleteListener}.
     * @param expectedParent The expected parent.
     * @param expectedFile The expected file.
     * @param canOverwrite The return value for the askUserForOverwrite function
     *            call.
     * @return The mocked {@link ExportCompleteListener}.
     */
    private ExportCompleteListener createUserAskListener(ExportCompleteListener listener, Component expectedParent,
            File expectedFile, Boolean canOverwrite)
    {
        EasyMock.expect(Boolean.valueOf(listener.askUserForOverwrite(EasyMock.eq(expectedParent), EasyMock.eq(expectedFile))))
                .andReturn(canOverwrite);

        return listener;
    }

    /**
     * The answer for the getDataAt call on {@link MetaColumnsTableModel}.
     *
     * @param data The test data.
     * @return The element at the passed in index.
     */
    private DataElement getDataAtAnswer(List<DataElement> data)
    {
        int rowIndex = ((Integer)EasyMock.getCurrentArguments()[0]).intValue();

        return data.get(rowIndex);
    }

    /**
     * The answer to publishEvent.
     *
     * @param expectedException The expected exception.
     * @param expectedFile The test export file.
     * @param latch The count down latch.
     * @return Null.
     */
    private Void publishEventAnswer(Exception expectedException, File expectedFile, CountDownLatch latch)
    {
        UserMessageEvent event = (UserMessageEvent)EasyMock.getCurrentArguments()[0];

        assertEquals(expectedException, event.getException());
        assertEquals("Failed to export data to " + expectedFile, event.getMessage());
        assertEquals(Type.ERROR, event.getType());
        assertFalse(event.isMakeVisible());
        assertTrue(event.isShowToast());

        latch.countDown();

        return null;
    }
}
