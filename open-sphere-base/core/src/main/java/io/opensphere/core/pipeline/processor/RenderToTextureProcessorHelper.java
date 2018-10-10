package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.RenderToTextureGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultBaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRoundnessRenderProperty;
import io.opensphere.core.geometry.renderproperties.DefaultPointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.util.RenderToTextureImageProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.core.viewer.impl.SimpleMapContext;
import io.opensphere.core.viewer.impl.Viewer3D;

/**
 * Helper class for the render to texture processor.
 */
public final class RenderToTextureProcessorHelper
{
    /**
     * Generate dry-run geometries.
     *
     * @return The geometries.
     */
    public static Set<RenderToTextureGeometry> generateDryRunGeometries()
    {
        RenderToTextureGeometry.Builder rttgBuilder = new RenderToTextureGeometry.Builder();
        rttgBuilder.setRenderBox(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(100, 100)));

        TileGeometry.Builder<GeographicPosition> tileBuilder = new TileGeometry.Builder<>();
        tileBuilder.setBounds(new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0), LatLonAlt.createFromDegrees(10, 10)));
        RenderToTextureImageProvider imageProvider = new RenderToTextureImageProvider();
        tileBuilder.setImageManager(new ImageManager(null, imageProvider));
        TileGeometry tileGeometry = new TileGeometry(tileBuilder, new DefaultTileRenderProperties(0, true, true), null);
        rttgBuilder.setTileGeometry(tileGeometry);

        Viewer3D viewer = new Viewer3D(new Viewer3D.Builder());
        MapContext<?> mapContext = new SimpleMapContext<>(new ScreenViewer(), viewer);
        mapContext.reshape(100, 100);
        rttgBuilder.setMapContext(mapContext);
        rttgBuilder.setBackgroundColor(Color.RED);

        Collection<Geometry> subGeometries = New.collection();

        PointGeometry.Builder<ScreenPosition> builder = new PointGeometry.Builder<>();
        builder.setPosition(new ScreenPosition(10, 10));

        PointRenderProperties renderProperties = new DefaultPointRenderProperties(
                new DefaultBaseAltitudeRenderProperties(0, false, true, true), new DefaultPointSizeRenderProperty(),
                new DefaultPointRoundnessRenderProperty());

        subGeometries.add(new PointGeometry(builder, renderProperties, null));
        renderProperties.getRoundnessRenderProperty().setRound(true);
        renderProperties.setSize(3);
        subGeometries.add(new PointGeometry(builder, renderProperties, null));

        rttgBuilder.setInitialGeometries(subGeometries);
        return Collections.singleton(new RenderToTextureGeometry(rttgBuilder));
    }

    /** Disallow instantiation. */
    private RenderToTextureProcessorHelper()
    {
    }
}
