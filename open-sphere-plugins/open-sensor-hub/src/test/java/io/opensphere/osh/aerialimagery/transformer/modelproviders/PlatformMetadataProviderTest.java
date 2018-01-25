package io.opensphere.osh.aerialimagery.transformer.modelproviders;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.cache.matcher.NumberPropertyMatcher;
import io.opensphere.core.cache.matcher.NumberPropertyMatcher.OperatorType;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.util.Constants;

/**
 * Unit test for {@link PlatformMetadataProvider}.
 */
public class PlatformMetadataProviderTest
{
    /**
     * The test server url.
     */
    private static final String ourServer = "http://somehost";

    /**
     * The type key.
     */
    private static final String ourTypeKey = "iamtypekey";

    /**
     * The query time.
     */
    private static final long ourQueryTime = System.currentTimeMillis();

    /**
     * Tests getting the model.
     */
    @Test
    public void testGetModel()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<PlatformMetadata> testData = createTestData();

        DataRegistry dataRegistry = createRegistry(support, testData);
        DataTypeInfo dataType = createDataType(support);
        DataTypeInfo videoLayer = support.createMock(DataTypeInfo.class);

        support.replayAll();

        PlatformMetadataProvider provider = new PlatformMetadataProvider(dataRegistry);

        PlatformMetadata metadata = provider.getModel(dataType, videoLayer, ourQueryTime, null);

        assertEquals(testData.get(testData.size() - 1), metadata);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataTypeInfo}.
     */
    private DataTypeInfo createDataType(EasyMockSupport support)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        EasyMock.expect(dataType.getTypeKey()).andReturn(ourTypeKey);
        EasyMock.expect(dataType.getUrl()).andReturn(ourServer);

        return dataType;
    }

    /**
     * Creates the test data.
     *
     * @return The test data.
     */
    private List<PlatformMetadata> createTestData()
    {
        List<PlatformMetadata> testData = New.list();

        PlatformMetadata metadata = new PlatformMetadata();
        metadata.setTime(new Date(ourQueryTime - 1000));
        testData.add(metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(new Date(ourQueryTime - 500));
        testData.add(metadata);

        metadata = new PlatformMetadata();
        metadata.setTime(new Date(ourQueryTime - 200));
        testData.add(metadata);

        return testData;
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param testData The test data to return.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createRegistry(EasyMockSupport support, List<PlatformMetadata> testData)
    {
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        EasyMock.expect(dataRegistry.performQuery(EasyMock.isA(SimpleQuery.class))).andAnswer(() -> queryAnswer(testData));

        return dataRegistry;
    }

    /**
     * The answer to the mocked performQuery call.
     *
     * @param testData The test data to return.
     * @return Null.
     */
    @SuppressWarnings("unchecked")
    private QueryTracker queryAnswer(List<PlatformMetadata> testData)
    {
        SimpleQuery<PlatformMetadata> query = (SimpleQuery<PlatformMetadata>)EasyMock.getCurrentArguments()[0];

        DataModelCategory expected = new DataModelCategory(ourServer, Constants.PLATFORM_METADATA_FAMILY, ourTypeKey);
        assertEquals(expected, query.getDataModelCategory());
        assertEquals(Constants.PLATFORM_METADATA_DESCRIPTOR, query.getPropertyValueReceivers().get(0).getPropertyDescriptor());

        List<? extends PropertyMatcher<?>> matchers = query.getParameters();

        assertEquals(2, matchers.size());

        NumberPropertyMatcher<Long> lte = (NumberPropertyMatcher<Long>)matchers.get(0);
        NumberPropertyMatcher<Long> gte = (NumberPropertyMatcher<Long>)matchers.get(1);

        assertEquals(Constants.METADATA_TIMESTAMP_PROPERTY_DESCRIPTOR, lte.getPropertyDescriptor());
        assertEquals(OperatorType.LTE, lte.getOperator());
        assertEquals(ourQueryTime, lte.getOperand().longValue());

        assertEquals(Constants.METADATA_TIMESTAMP_PROPERTY_DESCRIPTOR, gte.getPropertyDescriptor());
        assertEquals(OperatorType.GTE, gte.getOperator());
        assertEquals(ourQueryTime - 1000, gte.getOperand().longValue());

        ((PropertyValueReceiver<PlatformMetadata>)query.getPropertyValueReceivers().get(0)).receive(testData);

        return null;
    }
}
