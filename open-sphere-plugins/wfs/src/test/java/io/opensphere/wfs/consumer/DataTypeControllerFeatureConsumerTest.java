package io.opensphere.wfs.consumer;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.TimeManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;

/**
 * Unit test for {@link DataTypeControllerFeatureConsumer}.
 */
public class DataTypeControllerFeatureConsumerTest
{
    /**
     * The test type key.
     */
    private static final String ourTypeKey = "I am key";

    /**
     * Tests flushing the added data elements and the consumer doesn't have to
     * be aware of load times.
     */
    @Test
    public void testFlushNoTime()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = createDataType(support);
        List<TimeSpan> loadSpans = New.list(TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 15000),
                TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis()));
        List<MapDataElement> elements = createFeatures(support, loadSpans, false);
        TimeManager timeManager = null;
        DataTypeController typeController = createTypeController(support, dataType, elements);

        support.replayAll();

        DataTypeControllerFeatureConsumer consumer = new DataTypeControllerFeatureConsumer(typeController, timeManager, dataType,
                1);
        consumer.addFeatures(elements);
        consumer.flush();

        support.verifyAll();
    }

    /**
     * Tests flushing the added data elements and the consumer is aware of load
     * times.
     */
    @Test
    public void testFlushTime()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = createDataType(support);
        List<TimeSpan> loadSpans = New.list(TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 15000),
                TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis()));
        List<MapDataElement> elements = createFeatures(support, loadSpans, true);
        TimeManager timeManager = createTimeManager(support, loadSpans);
        DataTypeController typeController = createTypeController(support, dataType,
                New.list(elements.get(0), elements.get(2), elements.get(3)));

        support.replayAll();

        DataTypeControllerFeatureConsumer consumer = new DataTypeControllerFeatureConsumer(typeController, timeManager, dataType,
                1);
        consumer.addFeatures(elements);
        consumer.flush();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @return The {@link DataTypeInfo}.
     */
    private DataTypeInfo createDataType(EasyMockSupport support)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        EasyMock.expect(dataType.getTypeKey()).andReturn(ourTypeKey);

        return dataType;
    }

    /**
     * Creates test data elements. The first elements are a timeless element and
     * one that is outside of the load spans. The rest of the elements are
     * within the load spans.
     *
     * @param support Used to create the mock.
     * @param loadSpans The test load spans.
     * @param isTime True if the consumer is time aware, false it all elements
     *            will be added.
     * @return The mocked elements.
     */
    private List<MapDataElement> createFeatures(EasyMockSupport support, List<TimeSpan> loadSpans, boolean isTime)
    {
        List<MapDataElement> elements = New.list();

        MapDataElement element = support.createMock(MapDataElement.class);
        if (isTime)
        {
            EasyMock.expect(element.getTimeSpan()).andReturn(TimeSpan.TIMELESS);
        }
        elements.add(element);

        element = support.createMock(MapDataElement.class);
        if (isTime)
        {
            EasyMock.expect(element.getTimeSpan()).andReturn(TimeSpan.get(5000));
        }
        elements.add(element);

        for (TimeSpan loadSpan : loadSpans)
        {
            element = support.createMock(MapDataElement.class);
            if (isTime)
            {
                EasyMock.expect(element.getTimeSpan()).andReturn(TimeSpan.get(loadSpan.getMidpoint()));
            }
            elements.add(element);
        }

        return elements;
    }

    /**
     * Creates an easy mocked {@link TimeManager}.
     *
     * @param support Used to create the mock.
     * @param loadSpans The load spans the time manager should return.
     * @return The mocked {@link TimeManager}.
     */
    private TimeManager createTimeManager(EasyMockSupport support, List<TimeSpan> loadSpans)
    {
        TimeManager timeManager = support.createMock(TimeManager.class);

        ObservableList<TimeSpan> list = new ObservableList<>();
        list.addAll(loadSpans);
        EasyMock.expect(timeManager.getLoadTimeSpans()).andReturn(list);

        return timeManager;
    }

    /**
     * Creates an easy mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @param dataType The mocked {@link DataTypeInfo} to expect in the add
     *            elements call.
     * @param expected The expected elements to be added.
     * @return The mocked {@link DataTypeController}.
     */
    private DataTypeController createTypeController(EasyMockSupport support, DataTypeInfo dataType, List<MapDataElement> expected)
    {
        DataTypeController controller = support.createMock(DataTypeController.class);

        EasyMock.expect(Boolean.valueOf(controller.hasDataTypeInfoForTypeKey(ourTypeKey))).andReturn(Boolean.TRUE);

        long[] ids = new long[expected.size()];
        for (int i = 0; i < ids.length; i++)
        {
            ids[i] = i;
        }

        EasyMock.expect(controller.addMapDataElements(EasyMock.eq(dataType), EasyMock.isNull(), EasyMock.isNull(),
                EasyMock.eq(expected), EasyMock.isA(DataTypeControllerFeatureConsumer.class))).andReturn(ids);

        return controller;
    }
}
