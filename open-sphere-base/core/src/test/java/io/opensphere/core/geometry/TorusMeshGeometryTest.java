package io.opensphere.core.geometry;

import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.math.Matrix3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import org.junit.Assert;

/** Test for {@link TorusMeshGeometry}. */
public class TorusMeshGeometryTest
{
    /**
     * Test for {@link TorusMeshGeometry#clone()}.
     */
    @Test
    public void testClone()
    {
        TorusMeshGeometry.Builder<GeographicPosition> builder = new TorusMeshGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setCircularPoints(13);
        builder.setRadius(.123f);
        builder.setTubeRadius(.0543);
        builder.setTubeSections(20);
        builder.setPositionTransform(new Matrix3d());
        builder.setCenter(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        PolygonMeshRenderProperties renderProperties1 = new DefaultPolygonMeshRenderProperties(65763, false, true, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        TorusMeshGeometry geom = new TorusMeshGeometry(builder, renderProperties1, constraints1);

        TorusMeshGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.getRadius(), clone.getRadius(), 0.f);
        Assert.assertEquals(geom.getTubeRadius(), clone.getTubeRadius(), 0.f);
        Assert.assertEquals(geom.getTubeSections(), clone.getTubeSections());
        Assert.assertEquals(geom.getCenter(), clone.getCenter());
        Assert.assertEquals(geom.getCirPoints(), clone.getCirPoints());
        Assert.assertEquals(geom.getPositionTransform(), clone.getPositionTransform());
        Assert.assertNotSame(geom.getPositionTransform(), clone.getPositionTransform());
        Assert.assertEquals(geom.getPolygonVertexCount(), clone.getPolygonVertexCount());
        Assert.assertEquals(geom.getColors(), clone.getColors());
        Assert.assertEquals(geom.getNormals(), clone.getNormals());
        Assert.assertEquals(geom.getPositions(), clone.getPositions());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
        Assert.assertNotSame(geom.getConstraints(), clone.getConstraints());
    }

    /**
     * Test for
     * {@link TorusMeshGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties, Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        TorusMeshGeometry.Builder<GeographicPosition> builder = new TorusMeshGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setCircularPoints(13);
        builder.setRadius(.123f);
        builder.setTubeRadius(.0543);
        builder.setTubeSections(20);
        builder.setPositionTransform(new Matrix3d());
        builder.setCenter(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        PolygonMeshRenderProperties renderProperties1 = new DefaultPolygonMeshRenderProperties(65763, false, true, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        TorusMeshGeometry geom = new TorusMeshGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        ColorRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.getRadius(), ((TorusMeshGeometry)derived).getRadius(), 0.f);
        Assert.assertEquals(geom.getTubeRadius(), ((TorusMeshGeometry)derived).getTubeRadius(), 0.f);
        Assert.assertEquals(geom.getTubeSections(), ((TorusMeshGeometry)derived).getTubeSections());
        Assert.assertEquals(geom.getCenter(), ((TorusMeshGeometry)derived).getCenter());
        Assert.assertEquals(geom.getCirPoints(), ((TorusMeshGeometry)derived).getCirPoints());
        Assert.assertEquals(geom.getPositionTransform(), ((TorusMeshGeometry)derived).getPositionTransform());
        Assert.assertNotSame(geom.getPositionTransform(), ((TorusMeshGeometry)derived).getPositionTransform());
        Assert.assertEquals(geom.getPolygonVertexCount(), ((TorusMeshGeometry)derived).getPolygonVertexCount());
        Assert.assertEquals(geom.getColors(), ((TorusMeshGeometry)derived).getColors());
        Assert.assertEquals(geom.getNormals(), ((TorusMeshGeometry)derived).getNormals());
        Assert.assertEquals(geom.getPositions(), ((TorusMeshGeometry)derived).getPositions());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
