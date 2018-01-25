package io.opensphere.core.geometry;

import java.util.Arrays;

import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.time.TimeSpan;
import org.junit.Assert;

/** Test for {@link PolygonGeometry}. */
public class PolygonGeometryTest
{
    /**
     * Test for {@link PolygonGeometry#clone()} .
     */
    @Test
    public void testClone()
    {
        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setLineSmoothing(true);
        builder.setLineType(LineType.GREAT_CIRCLE);
        builder.setRapidUpdate(true);
        builder.setVertices(Arrays.asList(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(35., 57.))));

        PolygonRenderProperties renderProperties1 = new DefaultPolygonRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        PolygonGeometry geom = new PolygonGeometry(builder, renderProperties1, constraints1);

        PolygonGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.isLineSmoothing(), ((PolylineGeometry)clone).isLineSmoothing());
        Assert.assertEquals(geom.getLineType(), ((PolylineGeometry)clone).getLineType());
        Assert.assertEquals(geom.getVertices(), ((PolylineGeometry)clone).getVertices());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), clone.getConstraints());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
    }

    /**
     * Test for
     * {@link PolygonGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties, io.opensphere.core.geometry.constraint.Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setLineSmoothing(true);
        builder.setLineType(LineType.GREAT_CIRCLE);
        builder.setRapidUpdate(true);
        builder.setVertices(Arrays.asList(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(35., 57.))));

        PolygonRenderProperties renderProperties1 = new DefaultPolygonRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        PolygonGeometry geom = new PolygonGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        ColorRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.isLineSmoothing(), ((PolylineGeometry)derived).isLineSmoothing());
        Assert.assertEquals(geom.getLineType(), ((PolylineGeometry)derived).getLineType());
        Assert.assertEquals(geom.getVertices(), ((PolylineGeometry)derived).getVertices());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
