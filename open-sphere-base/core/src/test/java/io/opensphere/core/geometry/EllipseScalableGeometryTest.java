package io.opensphere.core.geometry;

import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.ScalableRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import org.junit.Assert;

/** Test for {@link EllipseScalableGeometry}. */
public class EllipseScalableGeometryTest
{
    /**
     * Test for {@link EllipseScalableGeometry#clone()}.
     */
    @Test
    public void testClone()
    {
        EllipseScalableGeometry.Builder builder = new EllipseScalableGeometry.Builder();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setAngleDegrees(.8f);
        builder.setPointsNumber(21);
        builder.setSemiMajorAxis(.02f);
        builder.setSemiMinorAxis(.01f);
        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        ScalableRenderProperties renderProperties1 = new DefaultScalableRenderProperties(65763, false, true, false);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        EllipseScalableGeometry geom = new EllipseScalableGeometry(builder, renderProperties1, constraints1);

        EllipseScalableGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.getAngle(), clone.getAngle(), 0.f);
        Assert.assertEquals(geom.getSemiMajorAxis(), clone.getSemiMajorAxis(), 0.f);
        Assert.assertEquals(geom.getSemiMinorAxis(), clone.getSemiMinorAxis(), 0.f);
        Assert.assertEquals(geom.getPointsNumber(), clone.getPointsNumber());
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
     * {@link EllipseScalableGeometry#derive(BaseRenderProperties, Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        EllipseScalableGeometry.Builder builder = new EllipseScalableGeometry.Builder();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setAngleDegrees(.8f);
        builder.setPointsNumber(21);
        builder.setSemiMajorAxis(.02f);
        builder.setSemiMinorAxis(.01f);
        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        ScalableRenderProperties renderProperties1 = new DefaultScalableRenderProperties(65763, false, true, false);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        EllipseScalableGeometry geom = new EllipseScalableGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        BaseRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.getAngle(), ((EllipseScalableGeometry)derived).getAngle(), 0.f);
        Assert.assertEquals(geom.getSemiMajorAxis(), ((EllipseScalableGeometry)derived).getSemiMajorAxis(), 0.f);
        Assert.assertEquals(geom.getSemiMinorAxis(), ((EllipseScalableGeometry)derived).getSemiMinorAxis(), 0.f);
        Assert.assertEquals(geom.getPointsNumber(), ((EllipseScalableGeometry)derived).getPointsNumber());
        Assert.assertEquals(geom.getPolygonVertexCount(), ((EllipseScalableGeometry)derived).getPolygonVertexCount());
        Assert.assertEquals(geom.getPosition(), ((EllipseScalableGeometry)derived).getPosition());
        Assert.assertEquals(geom.getColors(), ((EllipseScalableGeometry)derived).getColors());
        Assert.assertEquals(geom.getNormals(), ((EllipseScalableGeometry)derived).getNormals());
        Assert.assertEquals(geom.getPositions(), ((EllipseScalableGeometry)derived).getPositions());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
