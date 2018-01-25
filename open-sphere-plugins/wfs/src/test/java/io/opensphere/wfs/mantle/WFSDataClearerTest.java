package io.opensphere.wfs.mantle;

import java.awt.Color;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportConverterRegistry;
import io.opensphere.mantle.data.geom.factory.impl.MapPointGeometryConverter;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;

/**
 * Unit test for {@link WFSDataClearer}.
 */
public class WFSDataClearerTest
{
    /**
     * Tests removing all features within a query region.
     *
     * @throws DataElementLookupException Bad lookup.
     */
    @Test
    public void testRemoveAll() throws DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        TimeSpan span1 = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis() - 5000);
        TimeSpan span2 = TimeSpan.get(System.currentTimeMillis() - 3000, System.currentTimeMillis());
        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1), Long.valueOf(2), Long.valueOf(3));
        DataTypeInfo layer = createLayer(support);
        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(span1, span2, geometrySupports);
        DataElementCache cache = createCache(support, layer, allIds, null);
        DataElementLookupUtils lookupUtils = createLookup(support, layer, allIds, elements);
        DataTypeController typeController = createTypeController(support, layer, new long[] { 1, 3 });

        MapGeometrySupportConverterRegistry converter = createConverter(support, geometrySupports);

        MantleToolbox mantle = createMantle(support, cache, lookupUtils, typeController, converter);
        QueryRegion queryArea = createRegion(support);

        support.replayAll();

        WFSDataClearer clearer = new WFSDataClearer(mantle, layer, queryArea);
        clearer.removeAll();

        support.verifyAll();
    }

    /**
     * Tests removing only features within a certain time span.
     *
     * @throws DataElementLookupException Bad lookup.
     */
    @Test
    public void testRemoveFeatures() throws DataElementLookupException
    {
        EasyMockSupport support = new EasyMockSupport();

        TimeSpan span1 = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis() - 5000);
        TimeSpan span2 = TimeSpan.get(System.currentTimeMillis() - 3000, System.currentTimeMillis());
        List<Long> allIds = New.list(Long.valueOf(0), Long.valueOf(1), Long.valueOf(2), Long.valueOf(3));
        DataTypeInfo layer = createLayer(support);
        List<MapGeometrySupport> geometrySupports = New.list();
        List<DataElement> elements = createElements(span1, span2, geometrySupports);
        DataElementCache cache = createCache(support, layer, allIds, New.list(span1, span1, span2, span2));
        DataElementLookupUtils lookupUtils = createLookup(support, layer, New.list(Long.valueOf(2), Long.valueOf(3)),
                New.list(elements.get(2), elements.get(3)));
        DataTypeController typeController = createTypeController(support, layer, new long[] { 3 });

        MapGeometrySupportConverterRegistry converter = createConverter(support,
                New.list(geometrySupports.get(2), geometrySupports.get(3)));

        MantleToolbox mantle = createMantle(support, cache, lookupUtils, typeController, converter);
        QueryRegion queryArea = createRegion(support);

        support.replayAll();

        WFSDataClearer clearer = new WFSDataClearer(mantle, layer, queryArea);
        clearer.removeFeatures(span2);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataElementCache}.
     *
     * @param support Used to create the mock.
     * @param layer The layer to expect.
     * @param elementIds The element ids to expect.
     * @param spans The time spans to return or null if not expected the time
     *            span call.
     * @return The {@link DataElementCache}.
     */
    private DataElementCache createCache(EasyMockSupport support, DataTypeInfo layer, List<Long> elementIds, List<TimeSpan> spans)
    {
        DataElementCache cache = support.createMock(DataElementCache.class);

        EasyMock.expect(cache.getElementIdsForTypeAsList(layer)).andReturn(elementIds);
        if (spans != null)
        {
            EasyMock.expect(cache.getTimeSpans(EasyMock.eq(elementIds))).andReturn(spans);
        }

        return cache;
    }

    /**
     * Creates a mocked {@link MapGeometrySupportConverterRegistry}.
     *
     * @param support Used to create the mock.
     * @param geometrySupports The test mantle geometries.
     * @return Returns a mocked {@link MapGeometrySupportConverterRegistry}.
     */
    private MapGeometrySupportConverterRegistry createConverter(EasyMockSupport support,
            List<MapGeometrySupport> geometrySupports)
    {
        MapGeometrySupportConverterRegistry converter = support.createMock(MapGeometrySupportConverterRegistry.class);

        Toolbox toolbox = support.createMock(Toolbox.class);
        MapPointGeometryConverter pointConverter = new MapPointGeometryConverter(toolbox);

        for (MapGeometrySupport geometrySupport : geometrySupports)
        {
            EasyMock.expect(converter.getConverter(geometrySupport)).andReturn(pointConverter);
        }

        return converter;
    }

    /**
     * Creates the test elements.
     *
     * @param firstSpan The first load span.
     * @param secondSpan The second load span.
     * @param supports Adds the map geometry support to that list.
     * @return Returns the test elements.
     */
    private List<DataElement> createElements(TimeSpan firstSpan, TimeSpan secondSpan, List<MapGeometrySupport> supports)
    {
        List<DataElement> elements = New.list();

        SimpleMapPointGeometrySupport point = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(7, 7));
        DefaultMapDataElement element = new DefaultMapDataElement(1000, firstSpan, point);
        elements.add(element);
        supports.add(point);

        point = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10.1, 10.1));
        element = new DefaultMapDataElement(1001, firstSpan, point);
        elements.add(element);
        supports.add(point);

        point = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(8, 8));
        element = new DefaultMapDataElement(1002, secondSpan, point);
        elements.add(element);
        supports.add(point);

        point = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10.8, 10.8));
        element = new DefaultMapDataElement(1003, secondSpan, point);
        elements.add(element);
        supports.add(point);

        return elements;
    }

    /**
     * Creates an easy mocked test layer.
     *
     * @param support Used to create the mock.
     * @return The mocked layer.
     */
    private DataTypeInfo createLayer(EasyMockSupport support)
    {
        DataTypeInfo layer = support.createMock(DataTypeInfo.class);

        DefaultMapFeatureVisualizationInfo visInfo = new DefaultMapFeatureVisualizationInfo(MapVisualizationType.POINT_ELEMENTS);
        EasyMock.expect(layer.getMapVisualizationInfo()).andReturn(visInfo).anyTimes();

        DefaultBasicVisualizationInfo basicVisInfo = new DefaultBasicVisualizationInfo(LoadsTo.TIMELINE, Color.RED, true);
        EasyMock.expect(layer.getBasicVisualizationInfo()).andReturn(basicVisInfo).anyTimes();

        return layer;
    }

    /**
     * Creates a mocked {@link DataElementLookupUtils}.
     *
     * @param support Used to create the mock.
     * @param layer The layer.
     * @param ids The ids to expect.
     * @param elements The elements to return.
     * @return The mocked {@link DataElementLookupUtils}.
     * @throws DataElementLookupException Bad lookup.
     */
    private DataElementLookupUtils createLookup(EasyMockSupport support, DataTypeInfo layer, List<Long> ids,
            List<DataElement> elements)
        throws DataElementLookupException
    {
        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);

        EasyMock.expect(lookup.getDataElements(ids, layer, null, false)).andReturn(elements);

        return lookup;
    }

    /**
     * Creates a {@link MantleToolbox}.
     *
     * @param support Used to create the mock.
     * @param cache The mocked cache.
     * @param lookupUtils The mocked lookup utils.
     * @param typeController The mocked type controller.
     * @param converter The mocked converter.
     * @return A mocked {@link MantleToolbox}.
     */
    private MantleToolbox createMantle(EasyMockSupport support, DataElementCache cache, DataElementLookupUtils lookupUtils,
            DataTypeController typeController, MapGeometrySupportConverterRegistry converter)
    {
        MantleToolbox mantle = support.createMock(MantleToolbox.class);

        EasyMock.expect(mantle.getDataElementCache()).andReturn(cache);
        EasyMock.expect(mantle.getDataElementLookupUtils()).andReturn(lookupUtils);
        EasyMock.expect(mantle.getDataTypeController()).andReturn(typeController);
        EasyMock.expect(mantle.getMapGeometrySupportConverterRegistry()).andReturn(converter);

        return mantle;
    }

    /**
     * Creates the region to test with.
     *
     * @param support Used to create the mock.
     * @return The mocked query region.
     */
    private QueryRegion createRegion(EasyMockSupport support)
    {
        QueryRegion region = support.createMock(QueryRegion.class);

        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<>();
        builder.setVertices(New.list(new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 11)),
                new GeographicPosition(LatLonAlt.createFromDegrees(11, 11)),
                new GeographicPosition(LatLonAlt.createFromDegrees(11, 10))));
        DefaultPolygonRenderProperties props = new DefaultPolygonRenderProperties(0, true, false);
        PolygonGeometry geometry = new PolygonGeometry(builder, props, null);

        region.getGeometries();
        EasyMock.expectLastCall().andReturn(New.list(geometry)).atLeastOnce();

        return region;
    }

    /**
     * Creates a mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @param layer The test layer.
     * @param ids The ids to expect to be removed.
     * @return The mocked {@link DataTypeController}.
     */
    private DataTypeController createTypeController(EasyMockSupport support, DataTypeInfo layer, long[] ids)
    {
        DataTypeController controller = support.createMock(DataTypeController.class);

        controller.removeDataElements(EasyMock.eq(layer), EasyMock.aryEq(ids));

        return controller;
    }
}
