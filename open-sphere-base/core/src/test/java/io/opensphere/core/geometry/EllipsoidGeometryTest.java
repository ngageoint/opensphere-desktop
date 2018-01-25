package io.opensphere.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.awt.Color;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for the {@link EllipsoidGeometry} class.
 */
public class EllipsoidGeometryTest
{
    /**
     * Tests building an {@link EllipsoidGeometry}.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        MapManager mapManager = EllipsoidTestUtils.createMapManager(support);

        support.replayAll();

        EllipsoidGeometryBuilder<GeographicPosition> builder = new EllipsoidGeometryBuilder<>(mapManager);
        builder.setAxisAMeters(EllipsoidTestUtils.AXIS_A);
        builder.setAxisBMeters(EllipsoidTestUtils.AXIS_B);
        builder.setAxisCMeters(EllipsoidTestUtils.AXIS_C);
        builder.setQuality(EllipsoidTestUtils.QUALITY);
        builder.setColor(Color.RED);
        LatLonAlt location = LatLonAlt.createFromDegreesMeters(10, 11, 5280, ReferenceLevel.TERRAIN);
        builder.setLocation(new GeographicPosition(location));
        builder.setHeading(14);
        builder.setPitch(15);
        builder.setRoll(16);
        builder.setDataModelId(76);

        List<List<Position>> expectedPositions = EllipsoidTestUtils.getExpectedPositions();
        List<Position> combinedExpected = New.list();
        for (List<Position> strip : expectedPositions)
        {
            combinedExpected.addAll(strip);
        }

        DefaultPolygonMeshRenderProperties renderProperties = new DefaultPolygonMeshRenderProperties(0, true, true, false);
        Constraints constraints = Constraints.createTimeOnlyConstraint(TimeSpan.get());

        EllipsoidGeometry geometry = new EllipsoidGeometry(builder, renderProperties, constraints);
        List<? extends Color> colors = geometry.getColors();
        assertEquals(combinedExpected.size(), colors.size());
        for (Color color : colors)
        {
            assertEquals(Color.RED, color);
        }

        assertEquals(AbstractRenderer.TRIANGLE_STRIP_VERTEX_COUNT, geometry.getPolygonVertexCount());

        List<? extends Position> positions = geometry.getPositions();
        assertEquals(combinedExpected.size(), positions.size());
        int index = 0;
        for (Position position : positions)
        {
            assertEquals(combinedExpected.get(index).asVector3d(), position.asVector3d());
            index++;
        }

        List<Vector3d> expectedNormals = EllipsoidTestUtils.calculateExpectedNormals(expectedPositions);
        assertEquals(combinedExpected.size(), expectedNormals.size());
        List<? extends Vector3d> actualNormals = geometry.getNormals();
        assertEquals(combinedExpected.size(), actualNormals.size());
        assertEquals(expectedNormals, actualNormals);

        Matrix4d transform = geometry.getRenderProperties().getTransform();
        Matrix4d expected = new Matrix4d(new double[] { -0.5187182308412952, 0.8122809550672292, -.26670404389125235, 0.0,
            -0.8501440620567355, -0.45705076232245867, 0.2614568308729626, 0.0, 0.0904791177153618, 0.36235928399246203,
            0.9276363935087497, 0.0, 11.0, 10.0, 5280.0, 1.0 });

        assertEquals(expected, transform);
        assertEquals(76, geometry.getDataModelId());

        support.verifyAll();
    }

    /**
     * Tests building an {@link EllipsoidGeometry}.
     */
    @Test
    public void testDerive()
    {
        EasyMockSupport support = new EasyMockSupport();

        MapManager mapManager = EllipsoidTestUtils.createMapManager(support);
        EasyMock.expectLastCall().times(2);

        support.replayAll();

        EllipsoidGeometryBuilder<GeographicPosition> builder = new EllipsoidGeometryBuilder<>(mapManager);
        builder.setAxisAMeters(EllipsoidTestUtils.AXIS_A);
        builder.setAxisBMeters(EllipsoidTestUtils.AXIS_B);
        builder.setAxisCMeters(EllipsoidTestUtils.AXIS_C);
        builder.setQuality(EllipsoidTestUtils.QUALITY);
        builder.setColor(Color.RED);
        LatLonAlt location = LatLonAlt.createFromDegreesMeters(10, 11, 5280, ReferenceLevel.TERRAIN);
        builder.setLocation(new GeographicPosition(location));
        builder.setHeading(14);
        builder.setPitch(15);
        builder.setRoll(16);
        builder.setDataModelId(76);

        List<List<Position>> expectedPositions = EllipsoidTestUtils.getExpectedPositions();
        List<Position> combinedExpected = New.list();
        for (List<Position> strip : expectedPositions)
        {
            combinedExpected.addAll(strip);
        }

        DefaultPolygonMeshRenderProperties renderProperties = new DefaultPolygonMeshRenderProperties(0, true, true, false);
        Constraints constraints = Constraints.createTimeOnlyConstraint(TimeSpan.get());

        EllipsoidGeometry first = new EllipsoidGeometry(builder, renderProperties, constraints);
        EllipsoidGeometry geometry = first.derive(renderProperties, constraints);
        assertNotSame(first, geometry);
        List<? extends Color> colors = geometry.getColors();
        assertEquals(combinedExpected.size(), colors.size());
        for (Color color : colors)
        {
            assertEquals(Color.RED, color);
        }

        assertEquals(AbstractRenderer.TRIANGLE_STRIP_VERTEX_COUNT, geometry.getPolygonVertexCount());

        List<? extends Position> positions = geometry.getPositions();
        assertEquals(combinedExpected.size(), positions.size());
        int index = 0;
        for (Position position : positions)
        {
            assertEquals(combinedExpected.get(index).asVector3d(), position.asVector3d());
            index++;
        }

        List<Vector3d> expectedNormals = EllipsoidTestUtils.calculateExpectedNormals(expectedPositions);
        assertEquals(combinedExpected.size(), expectedNormals.size());
        List<? extends Vector3d> actualNormals = geometry.getNormals();
        assertEquals(combinedExpected.size(), actualNormals.size());
        assertEquals(expectedNormals, actualNormals);

        Matrix4d transform = geometry.getRenderProperties().getTransform();
        Matrix4d expected = new Matrix4d(new double[] { -0.5187182308412952, 0.8122809550672292, -.26670404389125235, 0.0,
            -0.8501440620567355, -0.45705076232245867, 0.2614568308729626, 0.0, 0.0904791177153618, 0.36235928399246203,
            0.9276363935087497, 0.0, 11.0, 10.0, 5280.0, 1.0 });

        assertEquals(expected, transform);
        assertEquals(76, geometry.getDataModelId());

        support.verifyAll();
    }
}
