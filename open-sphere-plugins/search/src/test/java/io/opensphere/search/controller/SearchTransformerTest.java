package io.opensphere.search.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.collections.New;
import io.opensphere.search.model.SearchModel;

/**
 * Unit test for {@link SearchTransformer}.
 *
 */
public class SearchTransformerTest
{
    /**
     * The geometries published during the test.
     */
    private final List<Geometry> myPublishedGeometries = New.list();

    /**
     * Tests the {@link SearchTransformer} ability to add and remove geometries.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeometryRegistry registry = createGeometryRegistry(support);

        SearchResult noLocations = new SearchResult();
        noLocations.setText("no locations");

        SearchResult pointResult = new SearchResult();
        pointResult.getLocations().add(LatLonAlt.createFromDegrees(10, 11));
        pointResult.setText("point");

        SearchResult box = new SearchResult();
        box.getLocations().addAll(New.list(LatLonAlt.createFromDegrees(10, 10), LatLonAlt.createFromDegrees(10, 11),
                LatLonAlt.createFromDegrees(11, 11), LatLonAlt.createFromDegrees(11, 10), LatLonAlt.createFromDegrees(10, 10)));
        box.setText("box");

        SearchModel model = new SearchModel();

        support.replayAll();

        SearchTransformer transformer = new SearchTransformer(model, registry, Maps.synchronizedBiMap(HashBiMap.create()),
                Maps.synchronizedBiMap(HashBiMap.create()));

        model.getShownResults().addAll(noLocations, pointResult, box);

        assertEquals(4, myPublishedGeometries.size());

        PointGeometry point = (PointGeometry)myPublishedGeometries.get(0);

        assertEquals(Color.CYAN, point.getRenderProperties().getColor());
        assertEquals(PointRenderProperties.TOP_Z, point.getRenderProperties().getZOrder());
        assertTrue(point.getRenderProperties().isDrawable());
        assertTrue(point.getRenderProperties().isPickable());
        assertFalse(point.getRenderProperties().isObscurant());
        assertFalse(point.getRenderProperties().isHidden());
        assertTrue(point.getRenderProperties().isRound());
        assertEquals(5f, point.getRenderProperties().getSize(), 0f);
        assertEquals(7f, point.getRenderProperties().getHighlightSize(), 0f);
        assertEquals(-2130706433, point.getRenderProperties().getHighlightColorARGB());
        assertEquals(LatLonAlt.createFromDegrees(10, 11), ((GeographicPosition)point.getPosition()).getLatLonAlt());

        PolylineGeometry boxGeom = (PolylineGeometry)myPublishedGeometries.get(2);

        assertEquals(Color.CYAN, boxGeom.getRenderProperties().getColor());
        assertEquals(PointRenderProperties.TOP_Z, boxGeom.getRenderProperties().getZOrder());
        assertTrue(boxGeom.getRenderProperties().isDrawable());
        assertTrue(boxGeom.getRenderProperties().isPickable());
        assertFalse(boxGeom.getRenderProperties().isObscurant());
        assertFalse(boxGeom.getRenderProperties().isHidden());
        assertEquals(4f, boxGeom.getRenderProperties().getWidth(), 0f);
        assertEquals(-2130706433, boxGeom.getRenderProperties().getHighlightColorARGB());
        assertEquals(LatLonAlt.createFromDegrees(10, 10), ((GeographicPosition)boxGeom.getVertices().get(0)).getLatLonAlt());
        assertEquals(LatLonAlt.createFromDegrees(10, 11), ((GeographicPosition)boxGeom.getVertices().get(1)).getLatLonAlt());
        assertEquals(LatLonAlt.createFromDegrees(11, 11), ((GeographicPosition)boxGeom.getVertices().get(2)).getLatLonAlt());
        assertEquals(LatLonAlt.createFromDegrees(11, 10), ((GeographicPosition)boxGeom.getVertices().get(3)).getLatLonAlt());
        assertEquals(LatLonAlt.createFromDegrees(10, 10), ((GeographicPosition)boxGeom.getVertices().get(4)).getLatLonAlt());

        model.getShownResults().clear();

        assertTrue(myPublishedGeometries.isEmpty());

        transformer.close();

        model.getShownResults().add(pointResult);

        assertTrue(myPublishedGeometries.isEmpty());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link GeometryRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link GeometryRegistry}.
     */
    @SuppressWarnings("unchecked")
    private GeometryRegistry createGeometryRegistry(EasyMockSupport support)
    {
        GeometryRegistry registry = support.createMock(GeometryRegistry.class);

        registry.addGeometriesForSource(EasyMock.isA(SearchTransformer.class), EasyMock.isA(Collection.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myPublishedGeometries.addAll((Collection<? extends Geometry>)EasyMock.getCurrentArguments()[1]);
            return null;
        }).anyTimes();
        registry.removeGeometriesForSource(EasyMock.isA(SearchTransformer.class), EasyMock.isA(Collection.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myPublishedGeometries.removeAll((Collection<? extends Geometry>)EasyMock.getCurrentArguments()[1]);
            return null;
        }).anyTimes();

        return registry;
    }
}
