package io.opensphere.core.geometry;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;

/** Test for {@link LabelGeometry}. */
@SuppressWarnings("boxing")
public class LabelGeometryTest
{
    /**
     * Test for {@link LabelGeometry#clone()} .
     */
    @Test
    public void testClone()
    {
        LabelGeometry.Builder<GeographicPosition> builder = new LabelGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(23., 56.));
        builder.setPosition(position);
        builder.setRapidUpdate(true);
        builder.setFont("font");
        builder.setText("text");
        builder.setHorizontalAlignment(.5f);
        builder.setVerticalAlignment(.5f);

        LabelRenderProperties renderProperties1 = new DefaultLabelRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        LabelGeometry geom = new LabelGeometry(builder, renderProperties1, constraints1);

        LabelGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.getPosition(), clone.getPosition());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertEquals(geom.getText(), clone.getText());
        Assert.assertEquals(geom.getHorizontalAlignment(), clone.getHorizontalAlignment(), 0f);
        Assert.assertEquals(geom.getVerticalAlignment(), clone.getVerticalAlignment(), 0f);
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), clone.getConstraints());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
    }

    /**
     * Test for
     * {@link LabelGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties, io.opensphere.core.geometry.constraint.Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        LabelGeometry.Builder<GeographicPosition> builder = new LabelGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(23., 56.));
        builder.setPosition(position);
        builder.setRapidUpdate(true);
        builder.setFont("font");
        builder.setText("text");
        builder.setHorizontalAlignment(.5f);
        builder.setVerticalAlignment(.5f);

        LabelRenderProperties renderProperties1 = new DefaultLabelRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        LabelGeometry geom = new LabelGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        LabelRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.getPosition(), ((LabelGeometry)derived).getPosition());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertEquals(geom.getText(), ((LabelGeometry)derived).getText());
        Assert.assertEquals(geom.getHorizontalAlignment(), ((LabelGeometry)derived).getHorizontalAlignment(), 0f);
        Assert.assertEquals(geom.getVerticalAlignment(), ((LabelGeometry)derived).getVerticalAlignment(), 0f);
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
