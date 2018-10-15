package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.GeographicPositionArrayList;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTDoubleArrayList;
import io.opensphere.core.util.xml.MutableNamespaceContext;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;

/** Tests for {@link QueryRegionStateController}. */
public class QueryRegionStateControllerTest
{
    /**
     * Test for
     * {@link QueryRegionStateController#activateState(String, String, Collection, Node)}
     * and {@link QueryRegionStateController#deactivateState(String, Node)}.
     *
     * @throws ParserConfigurationException If the test fails.
     */
    @Test
    public void testActivateDeactivate() throws ParserConfigurationException
    {
        double[] vertices = getTestVertices1();

        String coordinates = getTestKmlCoordinates(vertices);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node queryAreasNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, "queryAreas"));
        Node queryAreaNode = queryAreasNode.appendChild(StateXML.createElement(doc, "queryArea"));
        queryAreaNode.appendChild(createKmlElement(doc, "Polygon")).appendChild(createKmlElement(doc, "outerBoundaryIs"))
                .appendChild(createKmlElement(doc, "LinearRing")).appendChild(createKmlElement(doc, "coordinates"))
                .setTextContent(coordinates);
        final String layer1 = "layer1";
        queryAreaNode.appendChild(StateXML.createElement(doc, "layer")).setTextContent(layer1);
        final String layer2 = "layer2";
        queryAreaNode.appendChild(StateXML.createElement(doc, "layer")).setTextContent(layer2);

        Collection<GeographicPosition> positions = getTestPositions(vertices);

        Collection<? extends TimeSpan> validTimes = Collections.singleton(TimeSpan.TIMELESS);

        QueryRegionManager queryRegionManager = EasyMock.createMock(QueryRegionManager.class);
        QueryRegion region = EasyMock.createMock(QueryRegion.class);
        Map<String, DataFilter> dataTypeToFilterMap = New.map();
        Arrays.asList(layer1 + "!!stateId", layer2 + "!!stateId").stream().forEach(k -> dataTypeToFilterMap.put(k, null));
        EasyMock.expect(
                queryRegionManager.addQueryRegion(matches(positions), EasyMock.eq(validTimes), EasyMock.eq(dataTypeToFilterMap)))
                .andReturn(region);
        EasyMock.replay(queryRegionManager, region);

        QueryRegionStateController controller = new QueryRegionStateController(queryRegionManager, null);
        String stateId = "stateId";
        controller.activateState(stateId, null, null, doc.getDocumentElement());

        EasyMock.verify(queryRegionManager, region);

        EasyMock.reset(queryRegionManager);
        queryRegionManager.removeQueryRegions(Collections.singletonList(region));
        EasyMock.replay(queryRegionManager);

        controller.deactivateState(stateId, queryAreaNode);

        EasyMock.verify(queryRegionManager, region);
    }

    /**
     * Test for {@link QueryRegionStateController#canActivateState(Node)}.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testCanActivateState() throws XPathExpressionException, ParserConfigurationException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        QueryRegionStateController controller = new QueryRegionStateController(null, null);
        Assert.assertFalse(controller.canActivateState(doc));

        doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, "queryAreas"));
        Assert.assertTrue(controller.canActivateState(doc));
    }

    /**
     * Test for {@link QueryRegionStateController#saveState(Node)}.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testSaveState() throws ParserConfigurationException, XPathExpressionException
    {
        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<>();

        double[] vertices1 = getTestVertices1();
        double[] vertices2 = getTestVertices2();

        PolygonRenderProperties renderProperties = new DefaultPolygonRenderProperties(ZOrderRenderProperties.TOP_Z, true, true);

        builder.setVertices(getTestPositions(vertices1));
        PolygonGeometry geom1 = new PolygonGeometry(builder, renderProperties, (Constraints)null);

        QueryRegion region1 = EasyMock.createMock(QueryRegion.class);
        region1.getGeometries();
        EasyMock.expectLastCall().andReturn(Collections.singletonList(geom1));
        region1.getValidTimes();
        EasyMock.expectLastCall().andReturn(Collections.singletonList(TimeSpan.TIMELESS)).anyTimes();
        List<String> typeKeys1 = Arrays.asList("type1", "type2");
        region1.getTypeKeys();
        EasyMock.expectLastCall().andReturn(typeKeys1);
        Map<String, DataFilter> filterMap = New.map();
        filterMap.put("type1", null);
        filterMap.put("type2", null);
        region1.getTypeKeyToFilterMap();
        EasyMock.expectLastCall().andReturn(filterMap);

        builder.setVertices(getTestPositions(vertices2));
        PolygonGeometry geom2 = new PolygonGeometry(builder, renderProperties, (Constraints)null);

        QueryRegion region2 = EasyMock.createMock(QueryRegion.class);
        region2.getGeometries();
        EasyMock.expectLastCall().andReturn(Collections.singletonList(geom2));
        region2.getValidTimes();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        TimeSpan validTime = TimeSpan.get(cal.getTime(), Days.ONE);
        EasyMock.expectLastCall().andReturn(Collections.singletonList(validTime)).anyTimes();
        region2.getTypeKeys();
        List<String> typeKeys2 = Arrays.asList("type1", "type2");
        EasyMock.expectLastCall().andReturn(typeKeys2);
        region2.getTypeKeyToFilterMap();
        EasyMock.expectLastCall().andReturn(filterMap);

        List<QueryRegion> regions = New.list();
        regions.add(region1);
        regions.add(region2);

        QueryRegionManager queryRegionManager = EasyMock.createMock(QueryRegionManager.class);
        queryRegionManager.getQueryRegions();
        EasyMock.expectLastCall().andReturn(regions);
        EasyMock.replay(region1, region2, queryRegionManager);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node stateNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        QueryRegionStateController controller = new QueryRegionStateController(queryRegionManager, null);
        controller.saveState(stateNode);

        EasyMock.verify(region1, region2, queryRegionManager);

        XPath xpath = StateXML.newXPath();
        ((MutableNamespaceContext)xpath.getNamespaceContext()).addNamespace("kml", "http://www.opengis.net/kml/2.2");
        Assert.assertEquals(getTestKmlCoordinates(vertices1),
                xpath.evaluate(
                        "/" + ModuleStateController.STATE_QNAME
                                + "/:queryAreas/:queryArea/kml:Polygon/kml:outerBoundaryIs/kml:LinearRing/kml:coordinates",
                        stateNode));

        Assert.assertEquals("",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:queryAreas/:queryArea[1]/:validTime", stateNode));
        Assert.assertEquals(validTime.toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:queryAreas/:queryArea[2]/:validTime", stateNode));

        Assert.assertEquals(typeKeys1.get(0),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:queryAreas/:queryArea[1]/:layer[1]", stateNode));
        Assert.assertEquals(typeKeys1.get(1),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:queryAreas/:queryArea[1]/:layer[2]", stateNode));
        Assert.assertEquals(typeKeys2.get(0),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:queryAreas/:queryArea[2]/:layer[1]", stateNode));
        Assert.assertEquals(typeKeys2.get(1),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:queryAreas/:queryArea[2]/:layer[2]", stateNode));
    }

    /**
     * Create a KML element with the proper namespace.
     *
     * @param doc The owner of the element.
     * @param name The name of the element.
     * @return The element.
     */
    private Element createKmlElement(Document doc, String name)
    {
        Element polygonElement = doc.createElementNS("http://www.opengis.net/kml/2.2", name);
        polygonElement.setPrefix("kml");
        return polygonElement;
    }

    /**
     * Convert the input array of vertices (lon/lat/alt) to a KML coordinate
     * string.
     *
     * @param vertices The vertices.
     * @return The KML coordinates.
     */
    private String getTestKmlCoordinates(double[] vertices)
    {
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < vertices.length;)
        {
            sb.append(vertices[index++]).append(',').append(vertices[index++]);
            double alt = vertices[index++];
            if (alt > 0.)
            {
                sb.append(',').append(vertices[index++]);
            }
            sb.append(' ');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Convert the input array of vertices (lon/lat/alt) to geographic
     * positions.
     *
     * @param vertices The vertices.
     * @return The positions.
     */
    private List<GeographicPosition> getTestPositions(double[] vertices)
    {
        // Remove the duplicate coordinate and switch lon/lat to lat/lon.
        PetrifyableTDoubleArrayList data = new PetrifyableTDoubleArrayList();
        for (int index = 0; index < vertices.length - 3; index += 3)
        {
            data.add(vertices[index + 1]);
            data.add(vertices[index]);
            data.add(vertices[index + 2]);
        }

        List<GeographicPosition> positions = GeographicPositionArrayList.createFromDegreesMeters(data, ReferenceLevel.TERRAIN);
        return positions;
    }

    /**
     * Get the test vertices in lon/lat/alt order.
     *
     * @return The vertices.
     */
    private double[] getTestVertices1()
    {
        return new double[] { 10., 10., 0., 10., 11., 0., 11., 11., 0., 11., 10., 0., 10., 10., 0. };
    }

    /**
     * Get the test vertices in lon/lat/alt order.
     *
     * @return The vertices.
     */
    private double[] getTestVertices2()
    {
        return new double[] { 15., 15., 1., 15., 16., 2., 16., 16., 2., 16., 15., 1., 15., 15., 1. };
    }

    /**
     * Set up an EasyMock matcher for the polygon vertices.
     *
     * @param positions The vertices.
     * @return {@code null}
     */
    private Collection<? extends PolygonGeometry> matches(final Collection<GeographicPosition> positions)
    {
        EasyMock.reportMatcher(new IArgumentMatcher()
        {
            @Override
            public void appendTo(StringBuffer buffer)
            {
                buffer.append("matches(Collection<GeographicPosition>)");
            }

            @Override
            public boolean matches(Object argument)
            {
                if (argument instanceof Iterable)
                {
                    for (Object obj : (Iterable<?>)argument)
                    {
                        if (obj instanceof PolygonGeometry)
                        {
                            return ((PolygonGeometry)obj).getVertices().equals(positions);
                        }
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });
        return null;
    }
}
