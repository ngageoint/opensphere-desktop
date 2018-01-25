package io.opensphere.kml.common.model;

import org.junit.Assert;
import org.junit.Test;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;

/** Tests {@link KMLFeature}. */
public class KMLFeatureTest
{
    /** Tests the set/get tile methods. */
    @Test
    public void testSetTile()
    {
        KMLFeature feature = new KMLFeature(new Placemark(), new KMLDataSource());
        TileGeometry.Builder<Position> builder = new TileGeometry.Builder<>();
        builder.setBounds(new GeographicConvexQuadrilateral(new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0))));
        DefaultTileRenderProperties renderProperties = new DefaultTileRenderProperties(0, false, false);
        TileGeometry geom1 = new TileGeometry(builder, renderProperties, null);
        TileGeometry geom2 = new TileGeometry(builder, renderProperties, null);

        Assert.assertNull(feature.getTile());

        feature.setTile(geom1);
        Assert.assertEquals(geom1, feature.getTile());

        feature.setTile(geom2);
        Assert.assertEquals(geom2, feature.getTile());

        feature.setTile(null);
        Assert.assertNull(feature.getTile());
    }

    /** Tests the set/get label methods. */
    @Test
    public void testSetLabel()
    {
        KMLFeature feature = new KMLFeature(new Placemark(), new KMLDataSource());
        LabelGeometry.Builder<Position> builder = new LabelGeometry.Builder<>();
        builder.setText("hi");
        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)));
        DefaultLabelRenderProperties renderProperties = new DefaultLabelRenderProperties(0, false, false);
        LabelGeometry geom1 = new LabelGeometry(builder, renderProperties, null);
        LabelGeometry geom2 = new LabelGeometry(builder, renderProperties, null);

        Assert.assertNull(feature.getLabel());

        feature.setLabel(geom1);
        Assert.assertEquals(geom1, feature.getLabel());

        feature.setLabel(geom2);
        Assert.assertEquals(geom2, feature.getLabel());

        feature.setLabel(null);
        Assert.assertNull(feature.getLabel());
    }
}
