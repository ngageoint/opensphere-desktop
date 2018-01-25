package io.opensphere.osh.aerialimagery.transformer.geometrybuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Unit test for {@link PlatformGeometryBuilder}.
 */
public class PlatformGeometryBuilderTest
{
    /**
     * The color.
     */
    private static final Color ourColor = Color.red;

    /**
     * The test type key.
     */
    private static final String ourTypeKey = "Iamtypekey";

    /**
     * Tests building geometries.
     */
    @Test
    public void testBuildGeometries()
    {
        EasyMockSupport support = new EasyMockSupport();

        MapContext<? extends Viewer> mapManager = createMapManager(support);
        DataTypeInfo dataType = createDataType(support);
        DataTypeInfo videoLayer = support.createMock(DataTypeInfo.class);
        PlatformMetadata metadata = createMetadata();

        support.replayAll();

        PlatformGeometryBuilder builder = new PlatformGeometryBuilder(mapManager);
        Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(metadata, dataType, videoLayer);

        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        List<Geometry> geometries = addsAndRemoves.getFirstObject();
        List<PolygonMeshGeometry> meshes = getMeshes(geometries);
        List<PolylineGeometry> lines = getLines(geometries);
        Matrix4d expected = new Matrix4d(new double[] { -.0008035867145991338, -0.002768272986857213, -0.009575537220561378, 0,
            -0.0037781292850447104, -0.00880517522006317, 0.0028626261456000334, 0, -0.009223881368361542, 0.003847798593250691,
            -0.0003383171414083064, 0, -86.5819168, 34.6905037, 0, 1 });
        assertMeshes(meshes, expected);
        assertLines(lines);

        support.verifyAll();
    }

    /**
     * Tests just updating the transform.
     */
    @Test
    public void testBuildGeometriesUpdateTransform()
    {
        EasyMockSupport support = new EasyMockSupport();

        MapContext<? extends Viewer> mapManager = createMapManager(support);
        DataTypeInfo dataType = createDataType(support);
        DataTypeInfo videoLayer = support.createMock(DataTypeInfo.class);
        PlatformMetadata metadata = createMetadata();

        support.replayAll();

        PlatformGeometryBuilder builder = new PlatformGeometryBuilder(mapManager);
        Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(metadata, dataType, videoLayer);

        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        List<Geometry> geometries = addsAndRemoves.getFirstObject();
        List<PolygonMeshGeometry> meshes = getMeshes(geometries);
        List<PolylineGeometry> lines = getLines(geometries);
        Matrix4d expected = new Matrix4d(new double[] { -.0008035867145991338, -0.002768272986857213, -0.009575537220561378, 0,
            -0.0037781292850447104, -0.00880517522006317, 0.0028626261456000334, 0, -0.009223881368361542, 0.003847798593250691,
            -0.0003383171414083064, 0, -86.5819168, 34.6905037, 0, 1 });
        assertMeshes(meshes, expected);
        assertLines(lines);

        metadata = createMetadata2();
        addsAndRemoves = builder.buildGeometries(metadata, dataType, videoLayer);
        assertTrue(addsAndRemoves.getFirstObject().isEmpty());
        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        assertLines(lines);
        expected = new Matrix4d(new double[] { -.0008047818378674965, -0.002768051717940191, -0.00957550081615824, 0,
            -0.00377800261819908, -0.008805145158241899, 0.002862885774733315, 0, -0.009223829053615405, 0.0038480265629407813,
            -0.0003371485734548596, 0, -86.5819167, 34.6905036, 0, 1 });
        assertMeshes(meshes, expected);

        support.verifyAll();
    }

    /**
     * Verifies its cache policy.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCachePublishedGeometries()
    {
        EasyMockSupport support = new EasyMockSupport();

        MapContext<? extends Viewer> mapManager = support.createMock(MapContext.class);

        support.replayAll();

        PlatformGeometryBuilder builder = new PlatformGeometryBuilder(mapManager);
        assertFalse(builder.cachePublishedGeometries());

        support.verifyAll();
    }

    /**
     * Tests the close method.
     */
    @Test
    public void testClose()
    {
        EasyMockSupport support = new EasyMockSupport();

        MapContext<? extends Viewer> mapManager = createMapManager(support);
        DataTypeInfo dataType = createDataType(support);
        DataTypeInfo videoLayer = support.createMock(DataTypeInfo.class);
        PlatformMetadata metadata = createMetadata();

        support.replayAll();

        PlatformGeometryBuilder builder = new PlatformGeometryBuilder(mapManager);
        Pair<List<Geometry>, List<Geometry>> addsAndRemoves = builder.buildGeometries(metadata, dataType, videoLayer);

        assertTrue(addsAndRemoves.getSecondObject().isEmpty());

        List<Geometry> geometries = addsAndRemoves.getFirstObject();
        List<PolygonMeshGeometry> meshes = getMeshes(geometries);
        List<PolylineGeometry> lines = getLines(geometries);
        Matrix4d expected = new Matrix4d(new double[] { -.0008035867145991338, -0.002768272986857213, -0.009575537220561378, 0,
            -0.0037781292850447104, -0.00880517522006317, 0.0028626261456000334, 0, -0.009223881368361542, 0.003847798593250691,
            -0.0003383171414083064, 0, -86.5819168, 34.6905037, 0, 1 });
        assertMeshes(meshes, expected);
        assertLines(lines);

        List<Geometry> toRemove = builder.close();
        assertEquals(geometries, toRemove);

        support.verifyAll();
    }

    /**
     * Asserts the lines.
     *
     * @param lines The lines.
     */
    private void assertLines(List<PolylineGeometry> lines)
    {
        assertEquals(31, lines.size());
        for (PolylineGeometry line : lines)
        {
            assertEquals(Color.black, line.getRenderProperties().getColor());
        }
    }

    /**
     * Asserts the meshes.
     *
     * @param meshes The meshes.
     * @param expected The expected transform.
     */
    private void assertMeshes(List<PolygonMeshGeometry> meshes, Matrix4d expected)
    {
        assertEquals(107, meshes.size());
        for (PolygonMeshGeometry mesh : meshes)
        {
            assertTrue(mesh.getPolygonMesh().getPolygonVertexCount() > 0);
            assertTrue(mesh.getDataModelId() > 0);
            assertFalse(mesh.isRapidUpdate());
            assertEquals(ourColor, mesh.getRenderProperties().getColor());

            Matrix4d actual = mesh.getRenderProperties().getTransform();
            assertEquals(expected, actual);
        }
    }

    /**
     * The answer to the mocked convertToModel call.
     *
     * @return The position as a vector.
     */
    private Vector3d convertToModelAnswer()
    {
        GeographicPosition pos = (GeographicPosition)EasyMock.getCurrentArguments()[0];

        return pos.asVector3d();
    }

    /**
     * Creates an easy mocked data type.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataTypeInfo}.
     */
    private DataTypeInfo createDataType(EasyMockSupport support)
    {
        BasicVisualizationInfo visInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(visInfo.getTypeColor()).andReturn(ourColor).atLeastOnce();

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        EasyMock.expect(dataType.getTypeKey()).andReturn(ourTypeKey).atLeastOnce();
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(visInfo).atLeastOnce();

        return dataType;
    }

    /**
     * Creates an easy mocked map manager.
     *
     * @param support Used to create the mock.
     * @return The mocked map manager.
     */
    @SuppressWarnings("unchecked")
    private MapContext<? extends Viewer> createMapManager(EasyMockSupport support)
    {
        Projection projection = support.createMock(Projection.class);
        EasyMock.expect(projection.convertToModel(EasyMock.isA(GeographicPosition.class), EasyMock.eq(Vector3d.ORIGIN)))
                .andAnswer(this::convertToModelAnswer).atLeastOnce();

        MapContext<? extends Viewer> mapManager = support.createMock(MapContext.class);
        EasyMock.expect(mapManager.getProjection()).andReturn(projection).atLeastOnce();

        return mapManager;
    }

    /**
     * Creates test metadata.
     *
     * @return The test metadata.
     */
    private PlatformMetadata createMetadata()
    {
        PlatformMetadata metadata = new PlatformMetadata();

        metadata.setCameraPitchAngle(-15.443460464477539);
        metadata.setCameraRollAngle(-2.876760482788086);
        metadata.setCameraYawAngle(0.10008019953966141);
        metadata.setPitchAngle(1.3307557106018066);
        metadata.setRollAngle(1.6258397102355957);
        metadata.setYawAngle(73.36104583740234);
        metadata.setLocation(LatLonAlt.createFromDegrees(34.6905037, -86.5819168));

        return metadata;
    }

    /**
     * Creates test metadata.
     *
     * @return The test metadata.
     */
    private PlatformMetadata createMetadata2()
    {
        PlatformMetadata metadata = new PlatformMetadata();

        metadata.setCameraPitchAngle(-14.831459999084473);
        metadata.setCameraRollAngle(-2.8407604694366455);
        metadata.setCameraYawAngle(0.17208021879196167);
        metadata.setPitchAngle(1.3301459550857544);
        metadata.setRollAngle(1.619006872177124);
        metadata.setYawAngle(73.3594970703125);
        metadata.setLocation(LatLonAlt.createFromDegrees(34.6905036, -86.5819167));

        return metadata;
    }

    /**
     * Gets the lines.
     *
     * @param geometries The geometries containing the lines.
     * @return The lines.
     */
    private List<PolylineGeometry> getLines(List<Geometry> geometries)
    {
        List<PolylineGeometry> meshes = New.list();

        for (Geometry geom : geometries)
        {
            if (geom instanceof PolylineGeometry)
            {
                meshes.add((PolylineGeometry)geom);
            }
        }

        return meshes;
    }

    /**
     * Gets the meshes.
     *
     * @param geometries The geometries containing meshes.
     * @return The meshes.
     */
    private List<PolygonMeshGeometry> getMeshes(List<Geometry> geometries)
    {
        List<PolygonMeshGeometry> meshes = New.list();

        for (Geometry geom : geometries)
        {
            if (geom instanceof PolygonMeshGeometry)
            {
                meshes.add((PolygonMeshGeometry)geom);
            }
        }

        return meshes;
    }
}
