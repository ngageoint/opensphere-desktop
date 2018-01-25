package io.opensphere.core.geometry;

import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import org.junit.Assert;

/** Test for {@link PointGeometry}. */
public class PointGeometryTest
{
    /**
     * Test for {@link PointGeometry#clone()} .
     */
    @Test
    public void testClone()
    {
        PointGeometry.Builder<GeographicPosition> builder = new PointGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(23., 56.));
        builder.setPosition(position);
        builder.setRapidUpdate(true);

        PointRenderProperties renderProperties1 = new DefaultPointRenderProperties(65763, false, true, false);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        PointGeometry geom = new PointGeometry(builder, renderProperties1, constraints1);

        PointGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.getPosition(), clone.getPosition());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), clone.getConstraints());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
    }

    /**
     * Test for
     * {@link PointGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties, Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        PointGeometry.Builder<GeographicPosition> builder = new PointGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(23., 56.));
        builder.setPosition(position);
        builder.setRapidUpdate(true);

        PointRenderProperties renderProperties1 = new DefaultPointRenderProperties(65763, false, true, false);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        PointGeometry geom = new PointGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        PointRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.getPosition(), ((PointGeometry)derived).getPosition());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
