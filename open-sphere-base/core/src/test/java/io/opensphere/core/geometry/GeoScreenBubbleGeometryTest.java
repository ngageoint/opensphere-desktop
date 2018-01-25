package io.opensphere.core.geometry;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.geometry.GeoScreenBubbleGeometry.Builder;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoxAnchor;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.time.TimeSpan;

/** Test for {@link GeoScreenBubbleGeometry}. */
public class GeoScreenBubbleGeometryTest
{
    /**
     * Test for creating a builder and generating a new geometry from it.
     */
    @Test
    public void testBuilder()
    {
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(23., 56.));
        GeographicBoxAnchor gba = new GeographicBoxAnchor(position, null, 0, 0);

        ScreenPosition sPos1 = new ScreenPosition(0, 0);
        ScreenPosition sPos2 = new ScreenPosition(15, 15);

        GeoScreenBoundingBox gsbb = new GeoScreenBoundingBox(sPos1, sPos2, gba);
        GeoScreenBubbleGeometry.Builder builder = new GeoScreenBubbleGeometry.Builder();
        builder.setBoundingBox(gsbb);
        builder.setDataModelId(3423L);
        builder.setAttachment(position);
        builder.setBorderBuffer(3.);
        builder.setCornerRadius(.75);
        builder.setCornerVertexCount(5);
        builder.setGapPercent(15);

        builder.setLineSmoothing(true);
        builder.setLineType(LineType.GREAT_CIRCLE);
        builder.setRapidUpdate(true);

        PolygonRenderProperties renderProperties1 = new DefaultPolygonRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        GeoScreenBubbleGeometry geom = new GeoScreenBubbleGeometry(builder, renderProperties1, constraints1);

        Builder builder2 = geom.createBuilder();

        GeoScreenBubbleGeometry geom2 = new GeoScreenBubbleGeometry(builder2, renderProperties1, constraints1);

        Assert.assertNotSame(geom, geom2);
        Assert.assertEquals(geom.getBoundingBox(), geom2.getBoundingBox());
        Assert.assertEquals(geom.getDataModelId(), geom2.getDataModelId());
        Assert.assertEquals(geom.isLineSmoothing(), geom2.isLineSmoothing());
        Assert.assertEquals(geom.getLineType(), geom2.getLineType());
        Assert.assertEquals(geom.getAttachment(), geom2.getAttachment());
        Assert.assertEquals(geom.getBorderBuffer(), geom2.getBorderBuffer(), 0.);
        Assert.assertEquals(geom.getCornerRadius(), geom2.getCornerRadius(), 0.);
        Assert.assertEquals(geom.getCornerVertexCount(), geom2.getCornerVertexCount());
        Assert.assertEquals(geom.getGapPercent(), geom2.getGapPercent(), 0.);
        Assert.assertEquals(geom.isRapidUpdate(), geom2.isRapidUpdate());
        Assert.assertSame(geom.getRenderProperties(), geom2.getRenderProperties());
        Assert.assertSame(geom.getConstraints(), geom2.getConstraints());
    }

    /**
     * Test for {@link GeoScreenBubbleGeometry#clone()} .
     */
    @Test
    public void testClone()
    {
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(23., 56.));
        GeographicBoxAnchor gba = new GeographicBoxAnchor(position, null, 0, 0);

        ScreenPosition sPos1 = new ScreenPosition(0, 0);
        ScreenPosition sPos2 = new ScreenPosition(15, 15);

        GeoScreenBoundingBox gsbb = new GeoScreenBoundingBox(sPos1, sPos2, gba);
        GeoScreenBubbleGeometry.Builder builder = new GeoScreenBubbleGeometry.Builder();
        builder.setBoundingBox(gsbb);
        builder.setDataModelId(3423L);
        builder.setAttachment(position);
        builder.setBorderBuffer(3.);
        builder.setCornerRadius(.75);
        builder.setCornerVertexCount(5);
        builder.setGapPercent(15);

        builder.setLineSmoothing(true);
        builder.setLineType(LineType.GREAT_CIRCLE);
        builder.setRapidUpdate(true);

        PolygonRenderProperties renderProperties1 = new DefaultPolygonRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        GeoScreenBubbleGeometry geom = new GeoScreenBubbleGeometry(builder, renderProperties1, constraints1);

        GeoScreenBubbleGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getBoundingBox(), clone.getBoundingBox());
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.isLineSmoothing(), clone.isLineSmoothing());
        Assert.assertEquals(geom.getLineType(), clone.getLineType());
        Assert.assertEquals(geom.getAttachment(), clone.getAttachment());
        Assert.assertEquals(geom.getBorderBuffer(), clone.getBorderBuffer(), 0.);
        Assert.assertEquals(geom.getCornerRadius(), clone.getCornerRadius(), 0.);
        Assert.assertEquals(geom.getCornerVertexCount(), clone.getCornerVertexCount());
        Assert.assertEquals(geom.getGapPercent(), clone.getGapPercent(), 0.);
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), clone.getConstraints());
        Assert.assertEquals(geom.getConstraints(), clone.getConstraints());
    }

    /**
     * Test for
     * {@link GeoScreenBubbleGeometry#derive(io.opensphere.core.geometry.renderproperties.BaseRenderProperties, Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        GeographicPosition position = new GeographicPosition(LatLonAlt.createFromDegrees(23., 56.));
        GeographicBoxAnchor gba = new GeographicBoxAnchor(position, null, 0, 0);

        ScreenPosition sPos1 = new ScreenPosition(0, 0);
        ScreenPosition sPos2 = new ScreenPosition(15, 15);

        GeoScreenBoundingBox gsbb = new GeoScreenBoundingBox(sPos1, sPos2, gba);
        GeoScreenBubbleGeometry.Builder builder = new GeoScreenBubbleGeometry.Builder();
        builder.setBoundingBox(gsbb);
        builder.setDataModelId(3423L);
        builder.setAttachment(position);
        builder.setBorderBuffer(3.);
        builder.setCornerRadius(.75);
        builder.setCornerVertexCount(5);
        builder.setGapPercent(15);

        builder.setLineSmoothing(true);
        builder.setLineType(LineType.GREAT_CIRCLE);
        builder.setRapidUpdate(true);

        PolygonRenderProperties renderProperties1 = new DefaultPolygonRenderProperties(65763, false, true);

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(TimeSpan.get(10L, 20L));
        Constraints constraints1 = new Constraints(timeConstraint);

        GeoScreenBubbleGeometry geom = new GeoScreenBubbleGeometry(builder, renderProperties1, constraints1);

        ColorRenderProperties renderProperties2 = renderProperties1.clone();
        Constraints constraints2 = new Constraints(timeConstraint);
        GeoScreenBubbleGeometry derived = geom.derive(renderProperties2, constraints2);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getBoundingBox(), derived.getBoundingBox());
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.isLineSmoothing(), derived.isLineSmoothing());
        Assert.assertEquals(geom.getLineType(), derived.getLineType());
        Assert.assertEquals(geom.getAttachment(), derived.getAttachment());
        Assert.assertEquals(geom.getBorderBuffer(), derived.getBorderBuffer(), 0.);
        Assert.assertEquals(geom.getCornerRadius(), derived.getCornerRadius(), 0.);
        Assert.assertEquals(geom.getCornerVertexCount(), derived.getCornerVertexCount());
        Assert.assertEquals(geom.getGapPercent(), derived.getGapPercent(), 0.);
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
        Assert.assertNotSame(geom.getConstraints(), derived.getConstraints());
        Assert.assertSame(constraints2, derived.getConstraints());
    }
}
