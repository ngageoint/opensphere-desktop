package io.opensphere.core.geometry;

import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultMeshScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableMeshRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import org.junit.Assert;

/** Test for {@link FrustumGeometry}. */
public class FrustumGeometryTest
{
    /**
     * Test for {@link FrustumGeometry#clone()}.
     */
    @Test
    public void testClone()
    {
        FrustumGeometry.Builder<GeographicPosition> builder = new FrustumGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setBaseRadius(.123f);
        builder.setTopRadius(.0321f);
        builder.setCircularPoints(13);
        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        ScalableMeshRenderProperties renderProperties1 = new DefaultMeshScalableRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        FrustumGeometry geom = new FrustumGeometry(builder, renderProperties1, constraints1);

        FrustumGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.getBaseRadius(), clone.getBaseRadius(), 0.f);
        Assert.assertEquals(geom.getTopRadius(), clone.getTopRadius(), 0.f);
        Assert.assertEquals(geom.getCircularPoints(), clone.getCircularPoints());
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
     * {@link FrustumGeometry#derive(BaseRenderProperties, Constraints)}.
     */
    @Test
    public void testDerive()
    {
        FrustumGeometry.Builder<GeographicPosition> builder = new FrustumGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setBaseRadius(.123f);
        builder.setTopRadius(.0321f);
        builder.setCircularPoints(13);
        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        ScalableMeshRenderProperties renderProperties1 = new DefaultMeshScalableRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        FrustumGeometry geom = new FrustumGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        BaseRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.getBaseRadius(), ((FrustumGeometry)derived).getBaseRadius(), 0.f);
        Assert.assertEquals(geom.getTopRadius(), ((FrustumGeometry)derived).getTopRadius(), 0.f);
        Assert.assertEquals(geom.getCircularPoints(), ((FrustumGeometry)derived).getCircularPoints());
        Assert.assertEquals(geom.getPolygonVertexCount(), ((FrustumGeometry)derived).getPolygonVertexCount());
        Assert.assertEquals(geom.getPosition(), ((FrustumGeometry)derived).getPosition());
        Assert.assertEquals(geom.getColors(), ((FrustumGeometry)derived).getColors());
        Assert.assertEquals(geom.getNormals(), ((FrustumGeometry)derived).getNormals());
        Assert.assertEquals(geom.getPositions(), ((FrustumGeometry)derived).getPositions());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
