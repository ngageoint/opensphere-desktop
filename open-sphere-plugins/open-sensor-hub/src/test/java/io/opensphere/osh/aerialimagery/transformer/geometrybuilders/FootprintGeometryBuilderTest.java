package io.opensphere.osh.aerialimagery.transformer.geometrybuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Unit test for {@link FootprintGeometryBuilder}.
 *
 */
public class FootprintGeometryBuilderTest
{
    /**
     * The test color.
     */
    private static final Color ourColor = Color.red;

    /**
     * The test opacity.
     */
    private static final int ourOpacity = 156;

    /**
     * The test type key.
     */
    private static final String ourTypeKey = "Iamatypekey";

    /**
     * The test zorder.
     */
    private static final int ourZOrder = 88;

    /**
     * Tests building a footprint.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        OrderParticipantKey key = createKey(support);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, key);
        DataTypeInfo dataType = createDataType(support, key, true);
        PlatformMetadata metadata = createMetadata();

        support.replayAll();

        FootprintGeometryBuilder builder = new FootprintGeometryBuilder(orderRegistry);
        List<Geometry> geometries = builder.buildGeometries(metadata, dataType, null).getFirstObject();

        assertEquals(1, geometries.size());

        PolygonGeometry geometry = (PolygonGeometry)geometries.get(0);

        assertEquals(metadata.getFootprint().getVertices(), geometry.getVertices());

        PolygonRenderProperties renderProps = geometry.getRenderProperties();
        assertEquals(ourZOrder, renderProps.getZOrder());
        assertTrue(renderProps.isPickable());
        assertTrue(renderProps.isDrawable());
        assertEquals(ColorUtilities.opacitizeColor(ourColor, ourOpacity / 255f), renderProps.getColor());
        assertFalse(renderProps.isHidden());

        TimeConstraint constraint = geometry.getConstraints().getTimeConstraint();

        assertEquals(ourTypeKey, constraint.getKey());
        assertEquals(TimeSpan.get(metadata.getTime()), constraint.getTimeSpan());

        support.verifyAll();
    }

    /**
     * Tests building an invisible footprint.
     */
    @Test
    public void testInvisible()
    {
        EasyMockSupport support = new EasyMockSupport();

        OrderParticipantKey key = createKey(support);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, key);
        DataTypeInfo dataType = createDataType(support, key, false);
        PlatformMetadata metadata = createMetadata();

        support.replayAll();

        FootprintGeometryBuilder builder = new FootprintGeometryBuilder(orderRegistry);

        assertTrue(builder.cachePublishedGeometries());

        Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(metadata, dataType, null);
        List<Geometry> geometries = addsAndRemoves.getFirstObject();

        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        assertEquals(1, geometries.size());

        PolygonGeometry geometry = (PolygonGeometry)geometries.get(0);

        assertEquals(metadata.getFootprint().getVertices(), geometry.getVertices());

        PolygonRenderProperties renderProps = geometry.getRenderProperties();
        assertEquals(ourZOrder, renderProps.getZOrder());
        assertTrue(renderProps.isPickable());
        assertTrue(renderProps.isDrawable());
        assertEquals(ColorUtilities.opacitizeColor(ourColor, ourOpacity / 255f), renderProps.getColor());
        assertTrue(renderProps.isHidden());

        TimeConstraint constraint = geometry.getConstraints().getTimeConstraint();

        assertEquals(ourTypeKey, constraint.getKey());
        assertEquals(TimeSpan.get(metadata.getTime()), constraint.getTimeSpan());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @param orderKey The order key.
     * @param isVisible Indicates if the type is visible or not.
     * @return The mocked data type.
     */
    private DataTypeInfo createDataType(EasyMockSupport support, OrderParticipantKey orderKey, boolean isVisible)
    {
        BasicVisualizationInfo visInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(visInfo.getTypeColor()).andReturn(ourColor);
        EasyMock.expect(Integer.valueOf(visInfo.getTypeOpacity())).andReturn(Integer.valueOf(ourOpacity));

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        EasyMock.expect(dataType.getOrderKey()).andReturn(orderKey);
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(visInfo).anyTimes();
        EasyMock.expect(dataType.getTypeKey()).andReturn(ourTypeKey);
        EasyMock.expect(Boolean.valueOf(dataType.isVisible())).andReturn(Boolean.valueOf(isVisible));

        return dataType;
    }

    /**
     * Creates an easy mocked {@link OrderParticipantKey}.
     *
     * @param support Used to create the mock.
     * @return The {@link OrderParticipantKey}.
     */
    private OrderParticipantKey createKey(EasyMockSupport support)
    {
        OrderParticipantKey key = support.createMock(OrderParticipantKey.class);

        return key;
    }

    /**
     * Creates the test metadata.
     *
     * @return The test metadata.
     */
    private PlatformMetadata createMetadata()
    {
        PlatformMetadata metadata = new PlatformMetadata();
        metadata.setTime(new Date(System.currentTimeMillis() - 1000));
        GeographicConvexQuadrilateral footprint = new GeographicConvexQuadrilateral(
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)));
        metadata.setFootprint(footprint);

        return metadata;
    }

    /**
     * Creates an easy mocked {@link OrderManagerRegistry}.
     *
     * @param support Used to create the mock.
     * @param expectedKey The expected order key to be passed to it.
     * @return The mocked {@link OrderManagerRegistry}.
     */
    private OrderManagerRegistry createOrderRegistry(EasyMockSupport support, OrderParticipantKey expectedKey)
    {
        OrderManager orderManager = support.createMock(OrderManager.class);
        EasyMock.expect(Integer.valueOf(orderManager.getOrder(EasyMock.eq(expectedKey)))).andReturn(Integer.valueOf(ourZOrder));

        OrderManagerRegistry orderRegistry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(expectedKey)).andReturn(orderManager);

        return orderRegistry;
    }
}
