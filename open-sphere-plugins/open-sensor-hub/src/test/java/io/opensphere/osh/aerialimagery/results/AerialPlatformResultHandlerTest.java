package io.opensphere.osh.aerialimagery.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.MapManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animationhelper.TimeRefreshNotifier;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.LinkedLayer;
import io.opensphere.osh.aerialimagery.model.LinkedLayers;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.transformer.AerialImageryTransformer;
import io.opensphere.osh.aerialimagery.util.Constants;
import io.opensphere.osh.model.Field;
import io.opensphere.osh.model.MutableOffering;
import io.opensphere.osh.model.OSHDataTypeInfo;
import io.opensphere.osh.model.Offering;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.util.OSHImageQuerier;

/**
 * Unit test for {@link AerialPlatformResultHandler}.
 */
public class AerialPlatformResultHandlerTest
{
    /**
     * The test data type key.
     */
    private static final String ourDataTypeKey = "http://somehost//platformLoc";

    /**
     * The test server url.
     */
    private static final String ourServerUrl = "http://somehost";

    /**
     * Tests the canHandle method.
     */
    @Test
    public void testCanHandle()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        PreferencesRegistry prefsRegistry = createPrefsRegistryBasic(support);
        Toolbox toolbox = createToolbox(support, dataRegistry, uiRegistry, prefsRegistry);
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);

        List<Output> validOuts = createValidOutputs();
        List<Output> invalidOuts = createInvalidOutputs();

        support.replayAll();

        AerialPlatformResultHandler handler = new AerialPlatformResultHandler(toolbox, querier);

        List<Output> totalOutputs = New.list(invalidOuts);
        totalOutputs.addAll(validOuts);

        List<Output> actualOuts = handler.canHandle(totalOutputs);

        assertEquals(validOuts, actualOuts);

        actualOuts = handler.canHandle(invalidOuts);

        assertTrue(actualOuts.isEmpty());

        support.verifyAll();
    }

    /**
     * Tests the getQueryProperty method.
     */
    @Test
    public void testGetQueryProperty()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        PreferencesRegistry prefsRegistry = createPrefsRegistryBasic(support);
        Toolbox toolbox = createToolbox(support, dataRegistry, uiRegistry, prefsRegistry);
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);

        List<Output> validOuts = createValidOutputs();
        Offering offering = createOffering();

        support.replayAll();

        AerialPlatformResultHandler handler = new AerialPlatformResultHandler(toolbox, querier);

        assertEquals("http://www.opengis.net/def/property/OGC/0/PlatformLocation",
                handler.getQueryProperty(offering, validOuts.get(0)));
        assertEquals("http://www.opengis.net/def/property/OGC/0/PlatformOrientation",
                handler.getQueryProperty(offering, validOuts.get(1)));
        assertEquals("http://sensorml.com/ont/swe/property/OSH/0/GimbalOrientation",
                handler.getQueryProperty(offering, validOuts.get(2)));

        support.verifyAll();
    }

    /**
     * Tests group activation.
     */
    @Test
    public void testHandleGroupActivation()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);
        PreferencesRegistry prefsRegistry = createPrefsRegistryBasic(support);
        Toolbox toolbox = createToolbox(support, dataRegistry, uiRegistry, prefsRegistry);

        TimeManager timeManager = support.createMock(TimeManager.class);
        timeManager.addActiveTimeSpanChangeListener(EasyMock.isA(TimeRefreshNotifier.class));
        EasyMock.expect(toolbox.getTimeManager()).andReturn(timeManager);

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);

        AnimationManager animationManager = support.createMock(AnimationManager.class);
        animationManager.addAnimationChangeListener(EasyMock.isA(TimeRefreshNotifier.class));
        EasyMock.expect(toolbox.getAnimationManager()).andReturn(animationManager);

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(toolbox.getMapManager()).andReturn(mapManager);

        List<Output> validOuts = createValidOutputs();
        Offering offering = createOffering();
        OSHDataTypeInfo dataType = createDataType(offering, validOuts);

        support.replayAll();

        AerialPlatformResultHandler handler = new AerialPlatformResultHandler(toolbox, querier);

        handler.handleGroupActivation(dataType, ActivationState.ACTIVE);
        assertTrue(toolbox.getTransformerRegistry().getObjectsForSource(dataType).iterator()
                .next() instanceof AerialImageryTransformer);

        handler.handleGroupActivation(dataType, ActivationState.DEACTIVATING);
        assertTrue(toolbox.getTransformerRegistry().getObjectsForSource(dataType).isEmpty());

        support.verifyAll();
    }

    /**
     * Tests group activation.
     */
    @Test
    public void testHandleGroupActivationWithVideoLayer()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support);
        Toolbox toolbox = createToolbox(support, dataRegistry, uiRegistry, prefsRegistry);

        TimeManager timeManager = support.createMock(TimeManager.class);
        timeManager.addActiveTimeSpanChangeListener(EasyMock.isA(TimeRefreshNotifier.class));
        EasyMock.expect(toolbox.getTimeManager()).andReturn(timeManager);

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry);

        AnimationManager animationManager = support.createMock(AnimationManager.class);
        animationManager.addAnimationChangeListener(EasyMock.isA(TimeRefreshNotifier.class));
        EasyMock.expect(toolbox.getAnimationManager()).andReturn(animationManager);

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(toolbox.getMapManager()).andReturn(mapManager);

        List<Output> validOuts = createValidOutputs();
        Offering offering = createOffering();
        OSHDataTypeInfo dataType = createDataType(offering, validOuts);

        DataTypeInfo videoLayer = support.createMock(DataTypeInfo.class);
        EasyMock.expect(videoLayer.getTypeKey()).andReturn("videolayerid");
        DataGroupInfo videoGroup = support.createMock(DataGroupInfo.class);
        EasyMock.expect(Boolean.valueOf(videoGroup.hasMembers(EasyMock.eq(false)))).andReturn(Boolean.TRUE);
        EasyMock.expect(videoGroup.getMembers(EasyMock.eq(false))).andReturn(New.set(videoLayer));

        support.replayAll();

        AerialPlatformResultHandler handler = new AerialPlatformResultHandler(toolbox, querier);

        handler.handleGroupActivation(dataType, ActivationState.ACTIVE);
        AerialImageryTransformer transformer = (AerialImageryTransformer)toolbox.getTransformerRegistry()
                .getObjectsForSource(dataType).iterator().next();
        ActiveDataGroupsChangedEvent event = new ActiveDataGroupsChangedEvent(this, New.set(videoGroup), New.set());
        handler.notify(event);
        assertEquals(videoLayer, transformer.getLinkedLayer().get(0));

        handler.handleGroupActivation(dataType, ActivationState.DEACTIVATING);
        assertTrue(toolbox.getTransformerRegistry().getObjectsForSource(dataType).isEmpty());

        support.verifyAll();
    }

    /**
     * Tests handling results.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testHandleResults() throws IOException
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        EasyMockSupport support = new EasyMockSupport();

        DataRegistry dataRegistry = createRegistry(support);
        UIRegistry uiRegistry = createUIRegistry(support);
        OSHImageQuerier querier = support.createMock(OSHImageQuerier.class);
        PreferencesRegistry prefsRegistry = createPrefsRegistryBasic(support);

        Toolbox toolbox = createToolbox(support, dataRegistry, uiRegistry, prefsRegistry);

        List<Output> validOuts = createValidOutputs();
        Offering offering = createOffering();
        OSHDataTypeInfo dataType = createDataType(offering, validOuts);

        List<CancellableInputStream> testData = createTestData();

        support.replayAll();

        AerialPlatformResultHandler handler = new AerialPlatformResultHandler(toolbox, querier);

        handler.handleResults(dataType, validOuts, testData);

        support.verifyAll();
    }

    /**
     * Creates a test data type.
     *
     * @param offering The test offering.
     * @param outputs The test outputs.
     * @return The test data type.
     */
    private OSHDataTypeInfo createDataType(Offering offering, List<Output> outputs)
    {
        OSHDataTypeInfo dataType = new OSHDataTypeInfo(null, null, ourServerUrl, offering, outputs);

        return dataType;
    }

    /**
     * Creates outputs unrecognized by this handler.
     *
     * @return Unrecognized outputs.
     */
    private List<Output> createInvalidOutputs()
    {
        Output output = new Output("phoneLocation");

        Field timeField = new Field("time");
        timeField.setProperty("http://www.opengis.net/def/property/OGC/0/SamplingTime");

        Field latField = new Field("lat");
        Field lonField = new Field("lon");
        Field alt = new Field("alt");

        output.getFields().add(timeField);
        output.getFields().add(latField);
        output.getFields().add(lonField);
        output.getFields().add(alt);

        return New.list(output);
    }

    /**
     * Creates a test offering.
     *
     * @return The test offering.
     */
    private Offering createOffering()
    {
        MutableOffering mut = new MutableOffering();
        mut.getObservableProperties()
                .addAll(New.list("http://www.opengis.net/def/property/OGC/0/PlatformLocation",
                        "http://www.opengis.net/def/property/OGC/0/PlatformOrientation",
                        "http://sensorml.com/ont/swe/property/OSH/0/GimbalOrientation"));
        return new Offering(mut);
    }

    /**
     * Creates the easy mocked preferences registry.
     *
     * @param support Used to create the mock.
     * @return The preferences registry.
     */
    private PreferencesRegistry createPrefsRegistry(EasyMockSupport support)
    {
        LinkedLayers linkedLayers = new LinkedLayers();

        LinkedLayer linkedLayer = new LinkedLayer();
        linkedLayer.setLinkedLayersTypeKey("linkedTypeKey1");
        linkedLayer.setOtherLinkedLayersTypeKey("otherLinkedTypeKey1");
        linkedLayers.getLinkedLayers().add(linkedLayer);

        linkedLayer = new LinkedLayer();
        linkedLayer.setLinkedLayersTypeKey(ourDataTypeKey);
        linkedLayer.setOtherLinkedLayersTypeKey("videolayerid");
        linkedLayers.getLinkedLayers().add(linkedLayer);

        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(prefs.getJAXBObject(EasyMock.eq(LinkedLayers.class), EasyMock.cmpEq("linkedlayers"), EasyMock.isNull()))
                .andReturn(linkedLayers);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(registry.getPreferences(EasyMock.eq(LayerLinker.class))).andReturn(prefs);

        return registry;
    }

    /**
     * Creates the easy mocked preferences registry.
     *
     * @param support Used to create the mock.
     * @return The preferences registry.
     */
    private PreferencesRegistry createPrefsRegistryBasic(EasyMockSupport support)
    {
        Preferences prefs = support.createMock(Preferences.class);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(registry.getPreferences(EasyMock.eq(LayerLinker.class))).andReturn(prefs);

        return registry;
    }

    /**
     * Creates an easy mocked registry for adding models.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataRegistry}.
     */
    @SuppressWarnings("unchecked")
    private DataRegistry createRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        EasyMock.expect(registry.addModels(EasyMock.isA(DefaultCacheDeposit.class))).andAnswer(this::depositAnswer);

        return registry;
    }

    /**
     * Creates test data.
     *
     * @return The test data.
     */
    private List<CancellableInputStream> createTestData()
    {
        String location = "2015-12-19T21:01:29.231Z,34.6905037,-86.5819168,183.99";
        String platAtt = "2015-12-19T21:01:29.231Z,70.96,-58.82,0.1";
        String gimbal = "2015-12-19T21:01:29.231Z,70.96,-58.82,0.1";

        ByteArrayInputStream locationStream = new ByteArrayInputStream(location.getBytes(StringUtilities.DEFAULT_CHARSET));
        ByteArrayInputStream platAttStream = new ByteArrayInputStream(platAtt.getBytes(StringUtilities.DEFAULT_CHARSET));
        ByteArrayInputStream gimbalStream = new ByteArrayInputStream(gimbal.getBytes(StringUtilities.DEFAULT_CHARSET));

        return New.list(new CancellableInputStream(locationStream, null), new CancellableInputStream(platAttStream, null),
                new CancellableInputStream(gimbalStream, null));
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param dataRegistry data registry.
     * @param uiRegistry ui registry.
     * @param prefsRegistry The mocked {@link PreferencesRegistry}.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataRegistry dataRegistry, UIRegistry uiRegistry,
            PreferencesRegistry prefsRegistry)
    {
        EventManager eventManager = support.createMock(EventManager.class);
        eventManager.subscribe(EasyMock.eq(ActiveDataGroupsChangedEvent.class), EasyMock.isA(AerialPlatformResultHandler.class));

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).atLeastOnce();

        GenericRegistry<Transformer> transformerRegistry = new GenericRegistry<>();
        EasyMock.expect(toolbox.getTransformerRegistry()).andReturn(transformerRegistry).anyTimes();

        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry);

        return toolbox;
    }

    /**
     * Creates an easy mocked {@link UIRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support)
    {
        MenuBarRegistry menuRegistry = support.createMock(MenuBarRegistry.class);
        menuRegistry.addTaskActivity(EasyMock.isA(CancellableTaskActivity.class));
        EasyMock.expectLastCall().times(3);

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        EasyMock.expect(uiRegistry.getMenuBarRegistry()).andReturn(menuRegistry).times(3);

        return uiRegistry;
    }

    /**
     * Creates outputs recognized by this handler.
     *
     * @return Valid outputs.
     */
    private List<Output> createValidOutputs()
    {
        Output location = new Output("platformLoc");

        Field timeField = new Field("time");
        timeField.setProperty("http://www.opengis.net/def/property/OGC/0/SamplingTime");

        Field latField = new Field("lat");
        Field lonField = new Field("lon");
        Field alt = new Field("alt");

        location.getFields().add(timeField);
        location.getFields().add(latField);
        location.getFields().add(lonField);
        location.getFields().add(alt);

        Output platformAtt = new Output("platformAtt");

        Field yaw = new Field("yaw");
        Field pitch = new Field("pitch");
        Field roll = new Field("roll");

        platformAtt.getFields().add(timeField);
        platformAtt.getFields().add(yaw);
        platformAtt.getFields().add(pitch);
        platformAtt.getFields().add(roll);

        Output gimbalAtt = new Output("gimbalAtt");

        gimbalAtt.getFields().add(timeField);
        gimbalAtt.getFields().add(yaw);
        gimbalAtt.getFields().add(pitch);
        gimbalAtt.getFields().add(roll);

        return New.list(location, platformAtt, gimbalAtt);
    }

    /**
     * The answer to the addModels mock call.
     *
     * @return The model ids.
     * @throws ParseException Bad parse.
     */
    @SuppressWarnings("unchecked")
    private long[] depositAnswer() throws ParseException
    {
        DefaultCacheDeposit<PlatformMetadata> deposit = (DefaultCacheDeposit<PlatformMetadata>)EasyMock.getCurrentArguments()[0];

        DataModelCategory expected = new DataModelCategory(ourServerUrl, Constants.PLATFORM_METADATA_FAMILY, ourDataTypeKey);

        assertEquals(expected, deposit.getCategory());
        Iterator<? extends PropertyAccessor<? super PlatformMetadata, ?>> iter = deposit.getAccessors().iterator();

        SerializableAccessor<PlatformMetadata, PlatformMetadata> metadataAccessor = (SerializableAccessor<PlatformMetadata, PlatformMetadata>)iter
                .next();
        SerializableAccessor<PlatformMetadata, Long> timestampAccessor = (SerializableAccessor<PlatformMetadata, Long>)iter
                .next();

        assertEquals(DateTimeUtilities.parseISO8601Date("2015-12-19T21:01:29.231Z").getTime(),
                timestampAccessor.access(null).longValue());
        assertEquals(Constants.PLATFORM_METADATA_DESCRIPTOR, metadataAccessor.getPropertyDescriptor());

        assertEquals(1, deposit.getInput().size());

        PlatformMetadata metadata = deposit.getInput().iterator().next();

        assertEquals("Sat Dec 19 21:01:29 GMT 2015", metadata.getTime().toString());
        assertEquals(231, metadata.getTime().getTime() % 1000);
        assertEquals(LatLonAlt.createFromDegrees(34.6905037, -86.5819168), metadata.getLocation());
        assertEquals(70.96, metadata.getCameraYawAngle(), 0d);
        assertEquals(-58.82, metadata.getCameraPitchAngle(), 0d);
        assertEquals(0.1, metadata.getCameraRollAngle(), 0d);
        assertEquals(70.96, metadata.getYawAngle(), 0d);
        assertEquals(-58.82, metadata.getPitchAngle(), 0d);
        assertEquals(0.1, metadata.getRollAngle(), 0d);
        assertNotNull(metadata.getFootprint());

        return new long[] { 1 };
    }
}
