package io.opensphere.core.geometry;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/** Test for {@link PointSetGeometry}. */
@SuppressWarnings("boxing")
public class PointSetGeometryTest
{
    /**
     * Test for {@link PointSetGeometry#clone()}.
     */
    @Test
    public void testClone()
    {
        PointSetGeometry.Builder<GeographicPosition> builder = new PointSetGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);

        List<GeographicPosition> positions = new ArrayList<>();
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(35., 55.)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(36., 54.)));

        List<Color> colors = new ArrayList<>();
        colors.add(Color.BLACK);
        colors.add(Color.BLUE);
        colors.add(Color.GRAY);

        builder.setPositions(positions);
        builder.setColors(colors);

        PointRenderProperties renderProperties1 = new DefaultPointRenderProperties(65763, false, true, true);

        PointSetGeometry geom = new PointSetGeometry(builder, renderProperties1, null);

        PointSetGeometry clone = geom.clone();

        Assert.assertNotSame(geom, clone);
        Assert.assertEquals(geom.getDataModelId(), clone.getDataModelId());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getPositions(), clone.getPositions());
        Assert.assertEquals(geom.getColors(), clone.getColors());
        Assert.assertEquals(geom.getPositionType(), clone.getPositionType());
        Assert.assertEquals(geom.isRapidUpdate(), clone.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), clone.getRenderProperties());
        Assert.assertEquals(geom.getRenderProperties(), clone.getRenderProperties());
    }

    /**
     * Test for
     * {@link PointSetGeometry#derive(BaseRenderProperties, io.opensphere.core.geometry.constraint.Constraints)}
     * .
     */
    @Test
    public void testDerive()
    {
        PointSetGeometry.Builder<GeographicPosition> builder = new PointSetGeometry.Builder<GeographicPosition>();
        builder.setDataModelId(3423L);
        builder.setRapidUpdate(true);

        List<GeographicPosition> positions = new ArrayList<>();
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(34., 56.)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(35., 55.)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(36., 54.)));

        List<Color> colors = new ArrayList<>();
        colors.add(Color.BLACK);
        colors.add(Color.BLUE);
        colors.add(Color.GRAY);

        builder.setPositions(positions);
        builder.setColors(colors);

        PointRenderProperties renderProperties1 = new DefaultPointRenderProperties(65763, false, true, true);

        PointSetGeometry geom = new PointSetGeometry(builder, renderProperties1, null);
        AbstractRenderableGeometry absGeom = geom;

        BaseRenderProperties renderProperties2 = renderProperties1.clone();
        PointSetGeometry derived = (PointSetGeometry)absGeom.derive(renderProperties2, null);

        Assert.assertNotSame(geom, derived);
        Assert.assertEquals(geom.getDataModelId(), derived.getDataModelId());
        Assert.assertEquals(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertEquals(geom.getPositions(), derived.getPositions());
        Assert.assertEquals(geom.getColors(), derived.getColors());
        Assert.assertEquals(geom.getPositionType(), derived.getPositionType());
        Assert.assertEquals(geom.isRapidUpdate(), derived.isRapidUpdate());
        Assert.assertNotSame(geom.getRenderProperties(), derived.getRenderProperties());
        Assert.assertSame(renderProperties2, derived.getRenderProperties());
    }
}
