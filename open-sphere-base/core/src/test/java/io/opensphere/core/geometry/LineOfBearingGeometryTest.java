package io.opensphere.core.geometry;

import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultLOBRenderProperties;
import io.opensphere.core.geometry.renderproperties.LOBRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import org.junit.Assert;

/** Test for {@link LineOfBearingGeometry}. */
public class LineOfBearingGeometryTest
{
    /**
     * Test for {@link LineOfBearingGeometry#clone()}.
     */
    @Test
    public void testClone()
    {
        LineOfBearingGeometry.Builder builder = new LineOfBearingGeometry.Builder();
        builder.setDataModelId(3423L);
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(23., 56.));
        builder.setPosition(position);
        builder.setRapidUpdate(true);
        builder.setLineOrientation(15f);

        LOBRenderProperties renderProperties1 = new DefaultLOBRenderProperties(65763, false, true);
        renderProperties1.setWidth(5f);
        renderProperties1.setLineLength(200f);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        LineOfBearingGeometry geom = new LineOfBearingGeometry(builder, renderProperties1, constraints1);

        LineOfBearingGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.getPosition(), clone.getPosition());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertEquals(geom.getRenderProperties().getWidth(), clone.getRenderProperties().getWidth(), 0f);
        Assert.assertEquals(geom.getRenderProperties().getLineLength(), clone.getRenderProperties().getLineLength(), 0f);
        Assert.assertEquals(geom.getRenderProperties().getDirectionalArrowLength(),
                clone.getRenderProperties().getDirectionalArrowLength(), 0f);
        Assert.assertEquals(geom.getLineOrientation(), clone.getLineOrientation(), 0f);
        Assert.assertEquals(geom.isDisplayArrow(), clone.isDisplayArrow());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), clone.getConstraints());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
    }

    /**
     * Test for
     * {@link LineOfBearingGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties, Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        LineOfBearingGeometry.Builder builder = new LineOfBearingGeometry.Builder();
        builder.setDataModelId(3423L);
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(23., 56.));
        builder.setPosition(position);
        builder.setRapidUpdate(true);
        builder.setLineOrientation(15f);

        LOBRenderProperties renderProperties1 = new DefaultLOBRenderProperties(65763, false, true);
        renderProperties1.setWidth(5f);
        renderProperties1.setLineLength(200f);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        LineOfBearingGeometry geom = new LineOfBearingGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        PolylineRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.getPosition(), ((LineOfBearingGeometry)derived).getPosition());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertEquals(geom.getRenderProperties().getWidth(),
                ((LineOfBearingGeometry)derived).getRenderProperties().getWidth(), 0f);
        Assert.assertEquals(geom.getRenderProperties().getLineLength(),
                ((LineOfBearingGeometry)derived).getRenderProperties().getLineLength(), 0f);
        Assert.assertEquals(geom.getRenderProperties().getDirectionalArrowLength(),
                ((LineOfBearingGeometry)derived).getRenderProperties().getDirectionalArrowLength(), 0f);
        Assert.assertEquals(geom.getLineOrientation(), ((LineOfBearingGeometry)derived).getLineOrientation(), 0f);
        Assert.assertEquals(geom.isDisplayArrow(), ((LineOfBearingGeometry)derived).isDisplayArrow());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
