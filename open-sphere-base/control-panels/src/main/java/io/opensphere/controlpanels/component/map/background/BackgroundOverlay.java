package io.opensphere.controlpanels.component.map.background;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.opensphere.controlpanels.component.map.model.MapModel;
import io.opensphere.controlpanels.component.map.overlay.Overlay;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.image.Image;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.New;

/**
 * The overlay that displays the map background.
 */
public class BackgroundOverlay implements Observer, Overlay
{
    /**
     * The background controller.
     */
    private final BackgroundController myController;

    /**
     * The list of images to draw on the background.
     */
    private final List<java.awt.Image> myImages = New.list();

    /**
     * The map model.
     */
    private final MapModel myMapModel;

    /**
     * The model.
     */
    private final BackgroundModel myModel;

    /**
     * Constructs a new background overlay.
     *
     * @param mapModel The model used by the map component.
     */
    public BackgroundOverlay(MapModel mapModel)
    {
        myMapModel = mapModel;
        myModel = new BackgroundModel();
        myModel.addObserver(this);
        myController = new BackgroundController(mapModel, myModel);
    }

    @Override
    public void close()
    {
        myController.close();
        myModel.deleteObserver(this);
    }

    @Override
    public void draw(Graphics graphics)
    {
        int index = 0;
        for (TileGeometry geometry : myModel.getGeometries())
        {
            BoundingBox<? extends Position> bounds = (BoundingBox<? extends Position>)geometry.getBounds();

            Vector3d upperLeft = bounds.getUpperLeft().asVector3d();

            java.awt.Image image = myImages.get(index);

            double scaleFactor = myModel.getGeometryScaleFactors().get(index).doubleValue();
            graphics.drawImage(image, (int)(upperLeft.getX() * scaleFactor), (int)(upperLeft.getY() * scaleFactor), null);

            index++;
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (o instanceof BackgroundModel && BackgroundModel.GEOMETRIES_PROP.equals(arg))
        {
            createNewImages();
        }
    }

    /**
     * Creates new images to cache because the background geometries have
     * changed.
     */
    private void createNewImages()
    {
        myImages.clear();

        int index = 0;
        for (TileGeometry geometry : myModel.getGeometries())
        {
            Image image = geometry.getImageManager().getImageProvider().getImage(null);

            byte[] imageBytes = image.getByteBuffer().array();
            DataBuffer dataBuffer = new DataBufferByte(imageBytes, imageBytes.length);
            BufferedImage buff = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            ComponentSampleModel sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, image.getWidth(), image.getHeight(),
                    3, image.getWidth() * 3, new int[] { 2, 1, 0 });
            Raster raster = Raster.createRaster(sampleModel, dataBuffer, null);
            buff.setData(raster);

            double scaleFactor = myModel.getGeometryScaleFactors().get(index).doubleValue();
            myImages.add(buff.getScaledInstance((int)(scaleFactor * myMapModel.getWidth()),
                    (int)(scaleFactor * myMapModel.getHeight()), java.awt.Image.SCALE_SMOOTH));
            index++;
        }
    }
}
