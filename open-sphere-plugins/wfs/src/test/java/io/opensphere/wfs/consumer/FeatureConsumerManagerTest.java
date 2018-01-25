package io.opensphere.wfs.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.TimeManager;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DataElementCache;

/**
 * Unit test for {@link FeatureConsumerManager}.
 */
public class FeatureConsumerManagerTest
{
    /**
     * The test type key.
     */
    private static final String ourTypeKey = "I am key";

    /**
     * Tests getting a consumer both time aware and not time aware.
     */
    @Test
    public void testRequestConsumer()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = createDataType(support);
        TimeManager timeManager = createTimeManager(support);
        MantleToolbox mantle = createMantle(support);

        support.replayAll();

        FeatureConsumerManager manager = new FeatureConsumerManager(mantle, timeManager);

        DataTypeControllerFeatureConsumer timeAware = (DataTypeControllerFeatureConsumer)manager.requestConsumer(dataType, true);
        assertEquals(timeManager, timeAware.getTimeManager());

        DataTypeControllerFeatureConsumer timeUnaware = (DataTypeControllerFeatureConsumer)manager.requestConsumer(dataType,
                false);
        assertNull(timeUnaware.getTimeManager());

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

        EasyMock.expect(dataType.getTypeKey()).andReturn(ourTypeKey).atLeastOnce();

        return dataType;
    }

    /**
     * Creates the easy mocked mantle toolbox.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link MantleToolbox}.
     */
    private MantleToolbox createMantle(EasyMockSupport support)
    {
        DataTypeController typeController = support.createMock(DataTypeController.class);

        DataElementCache cache = support.createMock(DataElementCache.class);
        EasyMock.expect(Integer.valueOf(cache.getPreferredInsertBlockSize())).andReturn(Integer.valueOf(50000)).times(2);

        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantle.getDataElementCache()).andReturn(cache).times(2);
        EasyMock.expect(mantle.getDataTypeController()).andReturn(typeController).times(2);

        return mantle;
    }

    /**
     * Creates the easy mocked {@link TimeManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link TimeManager}.
     */
    private TimeManager createTimeManager(EasyMockSupport support)
    {
        TimeManager timeManager = support.createMock(TimeManager.class);

        return timeManager;
    }
}
