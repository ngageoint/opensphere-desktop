package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.concurrent.Phaser;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.LayerType;
import io.opensphere.mantle.controller.event.impl.DataTypeRemovedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;

/**
 * Unit test for the {@link GeoPackageLayerActivationHandler}.
 */
public class GeoPackageLayerActivationHandlerTest
{
    /**
     * The registered color change listener.
     */
    private EventListener<DataTypeInfoColorChangeEvent> myColorChangedListener;

    /**
     * The removed listener to test.
     */
    private EventListener<DataTypeRemovedEvent> myRemovedListener;

    /**
     * The visibility listener to test.
     */
    private EventListener<DataTypeVisibilityChangeEvent> myVisibilityListener;

    /**
     * Test for when a feature layer becomes active.
     */
    @Test
    public void testFeatureActive()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo featureLayer = createFeatureLayer();
        DataGroupInfo dataGroup = createDataGroup(support, featureLayer);
        LayerActivationListener featureListener = createListener(support, featureLayer);
        LayerActivationListener tileListener = support.createMock(LayerActivationListener.class);
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, tileListener,
                featureListener);
        provider.handleCommit(true, dataGroup, new PhasedTaskCanceller(null, new Phaser()));

        support.verifyAll();
    }

    /**
     * Tests for when feature layers become inactive.
     */
    @Test
    public void testFeatureInactive()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo featureLayer = createFeatureLayer();
        DataGroupInfo dataGroup = createDataGroup(support, featureLayer);
        LayerActivationListener featureListener = support.createMock(LayerActivationListener.class);
        featureListener.layerDeactivated(EasyMock.same(featureLayer));
        LayerActivationListener tileListener = support.createMock(LayerActivationListener.class);
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, tileListener,
                featureListener);
        provider.handleCommit(false, dataGroup, new PhasedTaskCanceller(null, new Phaser()));

        support.verifyAll();
    }

    /**
     * Test for when a feature layer is invisible.
     */
    @Test
    public void testFeatureInvisible()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo featureLayer = createFeatureLayer();
        DataGroupInfo dataGroup = createDataGroup(support, featureLayer);
        featureLayer.setParent(dataGroup);
        LayerActivationListener featureListener = support.createMock(LayerActivationListener.class);
        LayerActivationListener tileListener = support.createMock(LayerActivationListener.class);
        EventManager eventManager = createEventManagerForClose(support);

        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, tileListener,
                featureListener);
        myVisibilityListener.notify(new DataTypeVisibilityChangeEvent(featureLayer, false, true, this));
        provider.close();

        support.verifyAll();
    }

    /**
     * Test for when a feature layer becomes visible.
     */
    @Test
    public void testFeatureVisible()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo featureLayer = createFeatureLayer();
        DataGroupInfo dataGroup = createDataGroup(support, featureLayer);
        featureLayer.setParent(dataGroup);
        LayerActivationListener featureListener = support.createMock(LayerActivationListener.class);
        LayerActivationListener tileListener = support.createMock(LayerActivationListener.class);
        EventManager eventManager = createEventManagerForClose(support);

        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, tileListener,
                featureListener);
        myVisibilityListener.notify(new DataTypeVisibilityChangeEvent(featureLayer, true, true, this));
        provider.close();

        support.verifyAll();
    }

    /**
     * Tests handling color changes.
     */
    @Test
    public void testHandleColorChange()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo tileLayer = createTileLayer();
        GeoPackageDataTypeInfo featureLayer = createFeatureLayer();
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);
        LayerActivationListener featureListener = support.createMock(LayerActivationListener.class);
        EventManager eventManager = createEventManagerForClose(support);

        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, listener, featureListener);
        Color newColor = new Color(255, 255, 255, 100);
        myColorChangedListener.notify(new DataTypeInfoColorChangeEvent(tileLayer, newColor, true, this));
        myColorChangedListener.notify(new DataTypeInfoColorChangeEvent(featureLayer, newColor, true, this));

        assertEquals(100f, tileLayer.getMapVisualizationInfo().getTileRenderProperties().getOpacity(), 0f);

        provider.close();

        support.verifyAll();
    }

    /**
     * Tests when a geopackage layer is activated.
     */
    @Test
    public void testHandleCommit()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo tileLayer = createTileLayer();
        DataGroupInfo dataGroup = createDataGroup(support, tileLayer);
        LayerActivationListener listener = createListener(support, tileLayer);
        LayerActivationListener featureListener = support.createMock(LayerActivationListener.class);
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, listener, featureListener);
        provider.handleCommit(true, dataGroup, null);

        support.verifyAll();
    }

    /**
     * Tests when a geopackage layer is deactivated.
     */
    @Test
    public void testHandleCommitInactive()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo tileLayer = createTileLayer();
        DataGroupInfo dataGroup = createDataGroup(support, tileLayer);
        LayerActivationListener featureListener = support.createMock(LayerActivationListener.class);
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);
        listener.layerDeactivated(EasyMock.eq(tileLayer));
        EventManager eventManager = createEventManager(support);

        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, listener, featureListener);
        provider.handleCommit(false, dataGroup, null);

        support.verifyAll();
    }

    /**
     * Tests when a geopackage layer is deactivated.
     */
    @Test
    public void testInvisible()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo tileLayer = createTileLayer();
        LayerActivationListener featureListener = support.createMock(LayerActivationListener.class);
        DataGroupInfo dataGroup = createDataGroup(support, tileLayer);
        DataTypeInfo dti = createDataType(support, dataGroup);
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);
        listener.layerDeactivated(EasyMock.eq(tileLayer));
        EventManager eventManager = createEventManagerForClose(support);

        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, listener, featureListener);
        myVisibilityListener.notify(new DataTypeVisibilityChangeEvent(dti, false, true, this));
        provider.close();

        support.verifyAll();
    }

    /**
     * Tests when a geopackage layer is deactivated.
     */
    @Test
    public void testRemoved()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo tileLayer = createTileLayer();
        LayerActivationListener featureListener = support.createMock(LayerActivationListener.class);
        DataGroupInfo dataGroup = createDataGroup(support, tileLayer);
        DataTypeInfo dti = createDataType(support, dataGroup);
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);
        listener.layerDeactivated(EasyMock.eq(tileLayer));
        EventManager eventManager = createEventManagerForClose(support);

        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, listener, featureListener);
        myRemovedListener.notify(new DataTypeRemovedEvent(dti, this));
        provider.close();

        support.verifyAll();
    }

    /**
     * Tests when a geopackage layer is activated.
     */
    @Test
    public void testVisible()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeoPackageDataTypeInfo tileLayer = createTileLayer();
        DataGroupInfo dataGroup = createDataGroup(support, tileLayer);
        DataTypeInfo dti = createDataType(support, dataGroup);
        LayerActivationListener listener = createListener(support, tileLayer);
        LayerActivationListener featureListener = support.createMock(LayerActivationListener.class);
        EventManager eventManager = createEventManagerForClose(support);
        support.replayAll();

        GeoPackageLayerActivationHandler provider = new GeoPackageLayerActivationHandler(eventManager, listener, featureListener);
        myVisibilityListener.notify(new DataTypeVisibilityChangeEvent(dti, true, true, this));
        provider.close();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataGroupInfo}.
     *
     * @param support Used to create the mock.
     * @param theTileLayer The tile layer data type.
     * @return The mocked {@link DataGroupInfo}.
     */
    private DataGroupInfo createDataGroup(EasyMockSupport support, GeoPackageDataTypeInfo theTileLayer)
    {
        DefaultDataTypeInfo dataType = new DefaultDataTypeInfo(null, "prefix", "key1", "key1", "name1", false);

        DataGroupInfo group = support.createMock(DataGroupInfo.class);

        EasyMock.expect(group.getMembers(EasyMock.eq(false))).andReturn(New.set(dataType, theTileLayer));

        return group;
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @param group The parent.
     * @return The mocked {@link DataTypeInfo}.
     */
    private DataTypeInfo createDataType(EasyMockSupport support, DataGroupInfo group)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        EasyMock.expect(dataType.getParent()).andReturn(group);

        return dataType;
    }

    /**
     * Creates an easy mocked {@link EventManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link EventManager}.
     */
    @SuppressWarnings("unchecked")
    private EventManager createEventManager(EasyMockSupport support)
    {
        EventManager eventManager = support.createMock(EventManager.class);
        eventManager.subscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myVisibilityListener = (EventListener<DataTypeVisibilityChangeEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.subscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myColorChangedListener = (EventListener<DataTypeInfoColorChangeEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });
        eventManager.subscribe(EasyMock.eq(DataTypeRemovedEvent.class), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myRemovedListener = (EventListener<DataTypeRemovedEvent>)EasyMock.getCurrentArguments()[1];
            return null;
        });

        return eventManager;
    }

    /**
     * Creates an easy mocked {@link EventManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link EventManager}.
     */
    private EventManager createEventManagerForClose(EasyMockSupport support)
    {
        EventManager eventManager = createEventManager(support);
        eventManager.unsubscribe(EasyMock.eq(DataTypeVisibilityChangeEvent.class), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myVisibilityListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });
        eventManager.unsubscribe(EasyMock.eq(DataTypeInfoColorChangeEvent.class), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myColorChangedListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });
        eventManager.unsubscribe(EasyMock.eq(DataTypeRemovedEvent.class), EasyMock.notNull());
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(myRemovedListener, EasyMock.getCurrentArguments()[1]);
            return null;
        });

        return eventManager;
    }

    /**
     * Creates a test geopackage feature data type.
     *
     * @return The test data type.
     */
    private GeoPackageDataTypeInfo createFeatureLayer()
    {
        GeoPackageDataTypeInfo featureLayer = new GeoPackageDataTypeInfo(null,
                new GeoPackageLayer("package", "c:\\somefile.gpkg", "tile", LayerType.FEATURE, 100), "key3");

        return featureLayer;
    }

    /**
     * Creates an easy mocked {@link LayerActivationListener}.
     *
     * @param support Used to create the mock.
     * @param expected The expected tile layer to be passed.
     * @return The mocked layer listener.
     */
    private LayerActivationListener createListener(EasyMockSupport support, GeoPackageDataTypeInfo expected)
    {
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);

        listener.layerActivated(EasyMock.same(expected));

        return listener;
    }

    /**
     * Creates a test geopackage tile data type.
     *
     * @return The test data type.
     */
    private GeoPackageDataTypeInfo createTileLayer()
    {
        GeoPackageDataTypeInfo tileLayer = new GeoPackageDataTypeInfo(null,
                new GeoPackageLayer("package", "c:\\somefile.gpkg", "tile", LayerType.TILE, 100), "key3");
        TileRenderProperties props = new DefaultTileRenderProperties(0, true, false);
        props.setOpacity(255);
        DefaultMapTileVisualizationInfo mapVisInfo = new DefaultMapTileVisualizationInfo(MapVisualizationType.IMAGE_TILE, props,
                true);

        tileLayer.setMapVisualizationInfo(mapVisInfo);

        return tileLayer;
    }
}
