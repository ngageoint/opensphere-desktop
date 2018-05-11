package io.opensphere.core.geometry;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;

/** Test for {@link CircularMeshGeometry}. */
@SuppressWarnings("boxing")
public class CircularMeshGeometryTest
{
    /**
     * Test for {@link CircularMeshGeometry#clone()}.
     */
    @Test
    public void testClone()
    {
        CircularMeshGeometry.Builder<GeographicPosition> builder = new CircularMeshGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setNumVertices(13);
        builder.setRadius(.123f);
        builder.setWarpFactor(.0321);
        builder.setCenter(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        PolygonMeshRenderProperties renderProperties1 = new DefaultPolygonMeshRenderProperties(65763, true, true, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        CircularMeshGeometry geom = new CircularMeshGeometry(builder, renderProperties1, constraints1);

        CircularMeshGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.getRadius(), clone.getRadius(), 0.f);
        Assert.assertEquals(geom.getCenter(), clone.getCenter());
        Assert.assertEquals(geom.getVertexCount(), clone.getVertexCount());
        Assert.assertEquals(geom.getPolygonVertexCount(), clone.getPolygonVertexCount());
        Assert.assertEquals(geom.getColors(), clone.getColors());
        Assert.assertEquals(geom.getNormals(), clone.getNormals());
        Assert.assertEquals(geom.getPositions(), clone.getPositions());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
    }

    /**
     * Test for
     * {@link CircularMeshGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties, Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        CircularMeshGeometry.Builder<GeographicPosition> builder = new CircularMeshGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setNumVertices(13);
        builder.setRadius(.123f);
        builder.setWarpFactor(.0321);
        builder.setCenter(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));

        PolygonMeshRenderProperties renderProperties1 = new DefaultPolygonMeshRenderProperties(65763, true, true, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        CircularMeshGeometry geom = new CircularMeshGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        ColorRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.getRadius(), ((CircularMeshGeometry)derived).getRadius(), 0.f);
        Assert.assertEquals(geom.getCenter(), ((CircularMeshGeometry)derived).getCenter());
        Assert.assertEquals(geom.getVertexCount(), ((CircularMeshGeometry)derived).getVertexCount());
        Assert.assertEquals(geom.getPolygonVertexCount(), ((CircularMeshGeometry)derived).getPolygonVertexCount());
        Assert.assertEquals(geom.getColors(), ((CircularMeshGeometry)derived).getColors());
        Assert.assertEquals(geom.getNormals(), ((CircularMeshGeometry)derived).getNormals());
        Assert.assertEquals(geom.getPositions(), ((CircularMeshGeometry)derived).getPositions());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
