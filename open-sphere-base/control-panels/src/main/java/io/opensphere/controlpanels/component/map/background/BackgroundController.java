package io.opensphere.controlpanels.component.map.background;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;

/**
 * Controls the background overlay and supplies the background overlay with Tile
 * Geometries.
 */
public class BackgroundController implements Observer
{
    /**
     * The main map model.
     */
    private final MapModel myMapModel;

    /**
     * The model that contains.
     */
    private final BackgroundModel myModel;

    /** The tile builder. */
    private final TileGeometry.Builder<ScreenPosition> myTileBuilder;

    /**
     * Constructs a new background controller.
     *
     * @param mapModel The main map used to get the map's screen size.
     * @param model The background model to populate with background geometries.
     */
    public BackgroundController(MapModel mapModel, BackgroundModel model)
    {
        myMapModel = mapModel;
        myModel = model;
        myTileBuilder = new TileGeometry.Builder<ScreenPosition>();
        myTileBuilder.setDivider(null);
        myTileBuilder.setParent(null);

        myTileBuilder.setImageManager(new ImageManager(null, new SingletonImageProvider(
                "/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg", Image.CompressionType.D3DFMT_DXT1)));
        getBackgroundGeometries();
        myMapModel.addObserver(this);
    }

    /**
     * Removes itself as an observer to the models.
     */
    public void close()
    {
        myMapModel.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (o instanceof MapModel && (MapModel.SIZE_PROP.equals(arg) || MapModel.REGION_PROP.equals(arg)))
        {
            getBackgroundGeometries();
        }
    }

    /**
     * Gets the background geometries.
     */
    private void getBackgroundGeometries()
    {
        Collection<TileGeometry> currentGeometries = myModel.getGeometries();
        myModel.remove(currentGeometries);

        ScreenBoundingBox viewport = myMapModel.getViewport();

        final float opacityValue = .9f;
        myTileBuilder.setBounds(
                new ScreenBoundingBox(new ScreenPosition(0 - viewport.getUpperLeft().getX(), 0 - viewport.getUpperLeft().getY()),
                        new ScreenPosition(viewport.getLowerRight().getX(), viewport.getLowerRight().getY())));
        TileRenderProperties props = new DefaultTileRenderProperties(1, true, true);
        props.setHighlightColorARGB(0);
        props.setOpacity(opacityValue);
        TileGeometry worldMapTile = new TileGeometry(myTileBuilder, props, null);

        myModel.getGeometryScaleFactors().clear();
        myModel.getGeometryScaleFactors().add(myMapModel.getWidth() / viewport.getWidth());

        myModel.add(New.list(worldMapTile));
    }
}
