package io.opensphere.core.geometry;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultMeshScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;

/** Test for {@link TriangleScalableGeometry}. */
@SuppressWarnings("boxing")
public class TriangleScalableGeometryTest
{
    /**
     * Test for {@link TriangleScalableGeometry#clone()}.
     */
    @Test
    public void testClone()
    {
        TriangleScalableGeometry.Builder<GeographicPosition> builder = new TriangleScalableGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        ScalableMeshRenderProperties renderProperties1 = new DefaultMeshScalableRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        TriangleScalableGeometry geom = new TriangleScalableGeometry(builder, renderProperties1, constraints1);

        TriangleScalableGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getPolygonVertexCount(), clone.getPolygonVertexCount());
        Assert.assertEquals(geom.getPosition(), clone.getPosition());
        Assert.assertEquals(geom.getColors(), clone.getColors());
        Assert.assertEquals(geom.getNormals(), clone.getNormals());
        Assert.assertEquals(geom.getPositions(), clone.getPositions());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), clone.getConstraints());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
    }

    /**
     * Test for
     * {@link TriangleScalableGeometry#derive(BaseRenderProperties, Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        TriangleScalableGeometry.Builder<GeographicPosition> builder = new TriangleScalableGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        ScalableMeshRenderProperties renderProperties1 = new DefaultMeshScalableRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        TriangleScalableGeometry geom = new TriangleScalableGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        BaseRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.getRenderProperties(), ((TriangleScalableGeometry)derived).getRenderProperties());
        Assert.assertEquals(geom.getPolygonVertexCount(), ((TriangleScalableGeometry)derived).getPolygonVertexCount());
        Assert.assertEquals(geom.getPosition(), ((TriangleScalableGeometry)derived).getPosition());
        Assert.assertEquals(geom.getColors(), ((TriangleScalableGeometry)derived).getColors());
        Assert.assertEquals(geom.getNormals(), ((TriangleScalableGeometry)derived).getNormals());
        Assert.assertEquals(geom.getPositions(), ((TriangleScalableGeometry)derived).getPositions());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
