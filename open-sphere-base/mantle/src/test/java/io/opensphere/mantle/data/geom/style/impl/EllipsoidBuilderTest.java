package io.opensphere.mantle.data.geom.style.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.EllipsoidGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.units.length.Yards;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;

/**
 * Unit test for {@link EllipsoidBuilder} class.
 */
public class EllipsoidBuilderTest
{
    /**
     * Tests building an ellipsoid.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);
        MapManager mapManager = EllipsoidTestUtils.createMapManager(support);
        DataTypeInfo dataType = createDataType(support);
        VisualizationState visState = new VisualizationState(true, true, false);
        FeatureIndividualGeometryBuilderData builderData = createBuilderData(support, dataType, visState);

        support.replayAll();

        MockFeatureStyle style = new MockFeatureStyle(toolbox, Color.RED, Yards.class);
        EllipsoidBuilder builder = new EllipsoidBuilder(mapManager);

        LatLonAlt location = LatLonAlt.createFromDegreesMeters(10, 11, 5280, ReferenceLevel.TERRAIN);
        GeographicPosition position = new GeographicPosition(location);
        Constraints constraints = Constraints.createTimeOnlyConstraint(TimeSpan.get());
        Geometry geom = builder.createEllipsoid(builderData,
                new double[] { EllipsoidTestUtils.AXIS_A / 2, EllipsoidTestUtils.AXIS_B / 2, 14 }, position, constraints,
                EllipsoidTestUtils.QUALITY, style);

        EllipsoidGeometry ellipsoid = (EllipsoidGeometry)geom;

        List<List<Position>> expectedPositions = EllipsoidTestUtils.getExpectedPositions();
        List<Position> combinedExpected = New.list();
        for (List<Position> strip : expectedPositions)
        {
            combinedExpected.addAll(strip);
        }

        List<? extends Color> colors = ellipsoid.getColors();
        assertEquals(combinedExpected.size(), colors.size());
        for (Color color : colors)
        {
            assertEquals(Color.RED, color);
        }

        assertEquals(AbstractRenderer.TRIANGLE_STRIP_VERTEX_COUNT, ellipsoid.getPolygonVertexCount());

        List<? extends Position> positions = ellipsoid.getPositions();
        assertEquals(combinedExpected.size(), positions.size());
        int index = 0;
        for (Position aPosition : positions)
        {
            assertEquals(combinedExpected.get(index).asVector3d(), aPosition.asVector3d());
            index++;
        }

        List<Vector3d> expectedNormals = EllipsoidTestUtils.calculateExpectedNormals(expectedPositions);
        assertEquals(combinedExpected.size(), expectedNormals.size());
        List<? extends Vector3d> actualNormals = ellipsoid.getNormals();
        assertEquals(combinedExpected.size(), actualNormals.size());
        assertEquals(expectedNormals, actualNormals);

        Matrix4d transform = ellipsoid.getRenderProperties().getTransform();
        Matrix4d expected = new Matrix4d(new double[] { -0.4736845415289106, 0.8806943233417152, -0.0006811388781438329, 0.0,
            -0.8806921226360405, -0.473681244990376, 0.002731898825549281, 0.0, 0.002083325075794714, 0.0018939318870861037,
            0.9999960363814627, 0.0, 11.0, 10.0, 5280.0, 1.0 });

        assertEquals(expected, transform);

        assertEquals(constraints, ellipsoid.getConstraints());
        assertEquals(1001, ellipsoid.getRenderProperties().getZOrder());
        assertTrue(ellipsoid.getRenderProperties().isPickable());
        assertTrue(ellipsoid.getRenderProperties().isDrawable());
        assertEquals(10, ellipsoid.getDataModelId());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link FeatureIndividualGeometryBuilderData}.
     *
     * @param support Used to create the mock.
     * @param dataType The mocked layer.
     * @param visState The mocked {@link VisualizationState}.
     * @return The mocked class.
     */
    private FeatureIndividualGeometryBuilderData createBuilderData(EasyMockSupport support, DataTypeInfo dataType,
            VisualizationState visState)
    {
        FeatureIndividualGeometryBuilderData builderData = support.createMock(FeatureIndividualGeometryBuilderData.class);

        EasyMock.expect(builderData.getDataType()).andReturn(dataType);
        EasyMock.expect(Long.valueOf(builderData.getGeomId())).andReturn(Long.valueOf(10));
        EasyMock.expect(builderData.getVS()).andReturn(visState);

        return builderData;
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataTypeInfo}.
     */
    private DataTypeInfo createDataType(EasyMockSupport support)
    {
        MapVisualizationInfo mapVisInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(Integer.valueOf(mapVisInfo.getZOrder())).andReturn(Integer.valueOf(1001));

        BasicVisualizationInfo basic = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(basic.getLoadsTo()).andReturn(LoadsTo.TIMELINE);

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getMapVisualizationInfo()).andReturn(mapVisInfo);
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(basic);

        return dataType;
    }
}
