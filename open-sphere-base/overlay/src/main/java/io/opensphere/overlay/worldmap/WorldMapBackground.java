package io.opensphere.overlay.worldmap;

import java.awt.Point;
import java.awt.event.MouseEvent;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.viewer.impl.ViewerAnimator;

/** Background for the mini map. */
public class WorldMapBackground extends AbstractWorldMapRenderable
{
    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /**
     * Construct me.
     *
     * @param parent parent component.
     */
    public WorldMapBackground(Component parent)
    {
        super(parent);
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    @Override
    public void handleCleanupListeners()
    {
        myMouseSupport.cleanupListeners();
    }

    @Override
    public void init()
    {
        super.init();

        ScreenBoundingBox bbox = getDrawBounds();

        TileGeometry.Builder<ScreenPosition> tileBuilder = getGenericTileBuilder();
        // TODO make configurable and create a new image.
        tileBuilder.setImageManager(new ImageManager(null, new SingletonImageProvider(
                "/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg", Image.CompressionType.D3DFMT_DXT1)));

        final float opacityValue = .9f;
        tileBuilder.setBounds(bbox);
        TileRenderProperties props = new DefaultTileRenderProperties(1, true, true);
        props.setHighlightColorARGB(0);
        props.setOpacity(opacityValue);
        TileGeometry worldMapTile = new TileGeometry(tileBuilder, props, null);

        getGeometries().add(worldMapTile);

        myMouseSupport.setActionGeometry(worldMapTile);
    }

    @Override
    public void mouseClicked(Geometry geom, MouseEvent event)
    {
        super.mouseClicked(geom, event);

        if (event.getClickCount() == 2)
        {
            Point end = event.getPoint();
            GeographicPosition pos = convertToLatLon(end);

            ViewerAnimator animator = new ViewerAnimator(getTransformer().getToolbox().getMapManager().getStandardViewer(), pos);
            animator.start(500);
        }
    }
}
