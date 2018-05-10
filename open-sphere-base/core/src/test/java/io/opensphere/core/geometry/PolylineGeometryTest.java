package io.opensphere.core.geometry;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Test for {@link PolylineGeometry}.
 */
@SuppressWarnings("boxing")
public class PolylineGeometryTest
{
    /** The builder. */
    private final PolylineGeometry.Builder<GeographicPosition> myBuilder = new PolylineGeometry.Builder<GeographicPosition>();

    /** Positions used for testing. */
    private final List<GeographicPosition> myPositions = new ArrayList<>();

    /** Render properties. */
    private final PolylineRenderProperties myRenderProperties = new DefaultPolylineRenderProperties(65763, true, false);

    /**
     * Constructor.
     */
    public PolylineGeometryTest()
    {
        myRenderProperties.setColor(Color.BLACK);
        myPositions.add(new GeographicPosition(LatLonAlt.createFromDegrees(0., 0.)));
        myPositions.add(new GeographicPosition(LatLonAlt.createFromDegrees(10., 10.)));
        myBuilder.setVertices(myPositions);
    }

    /**
     * Test for {@link PolylineGeometry#clone()} .
     */
    @Test
    public void testClone()
    {
        PolylineGeometry.Builder<GeographicPosition> builder = new PolylineGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setLineSmoothing(true);
        builder.setLineType(LineType.GREAT_CIRCLE);
        builder.setVertices(Arrays.asList(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(35., 57.))));

        PolylineRenderProperties renderProperties1 = new DefaultPolylineRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        PolylineGeometry geom = new PolylineGeometry(builder, renderProperties1, constraints1);

        PolylineGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertEquals(geom.isLineSmoothing(), clone.isLineSmoothing());
        Assert.assertEquals(geom.getLineType(), clone.getLineType());
        Assert.assertEquals(geom.getVertices(), clone.getVertices());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), clone.getConstraints());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
    }

    /** Test normal construction. */
    @SuppressWarnings("unused")
    @Test
    public void testConstruction()
    {
        List<GeographicPosition> positions = new ArrayList<>();

        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., 0., Altitude.ReferenceLevel.TERRAIN)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., 0., Altitude.ReferenceLevel.TERRAIN)));
        myBuilder.setVertices(positions);

        new PolylineGeometry(myBuilder, myRenderProperties, null);

        positions.clear();

        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., 0., Altitude.ReferenceLevel.ELLIPSOID)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., 0., Altitude.ReferenceLevel.ELLIPSOID)));
        myBuilder.setVertices(positions);

        new PolylineGeometry(myBuilder, myRenderProperties, null);
    }

    /** Test construction with negative width. */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructionBadWidthNegative()
    {
        myRenderProperties.setWidth(-1f);
        new PolylineGeometry(myBuilder, myRenderProperties, null);
    }

    /**
     * Test construction with terrain altitude references with different
     * altitudes.
     */
    @SuppressWarnings("unused")
    @Test
    public void testConstructionDifferentTerrainAltitudes()
    {
        List<GeographicPosition> positions = new ArrayList<>();

        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., 0., Altitude.ReferenceLevel.TERRAIN)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., 1., Altitude.ReferenceLevel.TERRAIN)));
        myBuilder.setVertices(positions);

        new PolylineGeometry(myBuilder, myRenderProperties, null);
    }

    /** Test construction with empty vertices. */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructionEmptyVertices()
    {
        myBuilder.setVertices(Collections.<GeographicPosition>emptyList());
        new PolylineGeometry(myBuilder, myRenderProperties, null);
    }

    /** Test construction with heterogeneous altitude references. */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructionMixedAltitudeReferences()
    {
        List<GeographicPosition> positions = new ArrayList<>();

        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., 0., Altitude.ReferenceLevel.TERRAIN)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegreesMeters(0., 0., 0., Altitude.ReferenceLevel.ELLIPSOID)));
        myBuilder.setVertices(positions);

        new PolylineGeometry(myBuilder, myRenderProperties, null);
    }

    /**
     * Test for
     * {@link PolylineGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties, Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        PolylineGeometry.Builder<GeographicPosition> builder = new PolylineGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);
        builder.setLineSmoothing(true);
        builder.setLineType(LineType.GREAT_CIRCLE);
        builder.setVertices(Arrays.asList(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)),
                new GeographicPosition(LatLonAlt.createFromDegrees(35., 57.))));

        PolylineRenderProperties renderProperties1 = new DefaultPolylineRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        PolylineGeometry geom = new PolylineGeometry(builder, renderProperties1, constraints1);
        AbstractRenderableGeometry absGeom = geom;

        PolylineRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        AbstractRenderableGeometry derived = absGeom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertEquals(geom.isLineSmoothing(), ((PolylineGeometry)derived).isLineSmoothing());
        Assert.assertEquals(geom.getLineType(), ((PolylineGeometry)derived).getLineType());
        Assert.assertEquals(geom.getVertices(), ((PolylineGeometry)derived).getVertices());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
