package io.opensphere.osh.aerialimagery.transformer.geometrybuilders;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.model.Position;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.pipeline.processor.TextureReplacer;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.model.PlatformMetadataAndImage;

/**
 * Builds a geometry that will contain the georectified image to display on the
 * globe.
 */
public class ImageGeometryBuilder implements GeometryBuilder
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(ImageGeometryBuilder.class);

    /**
     * The currently displayed geometry mapped to the layer's key.
     */
    private final Map<String, TileGeometry> myGeometries = Collections.synchronizedMap(New.map());

    /**
     * The image managers.
     */
    private final Map<String, ImageManager> myImageManagers = Collections.synchronizedMap(New.map());

    /**
     * The order manager registry.
     */
    private final OrderManagerRegistry myOrderManager;

    /**
     * Constructs a new foot print geometry builder.
     *
     * @param orderRegistry The {@link OrderManagerRegistry}.
     */
    public ImageGeometryBuilder(OrderManagerRegistry orderRegistry)
    {
        myOrderManager = orderRegistry;
    }

    @Override
    public Pair<List<Geometry>, List<Geometry>> buildGeometries(PlatformMetadata model, DataTypeInfo uavDataType,
            DataTypeInfo videoLayer)
    {
        String typeKey = videoLayer.getTypeKey();
        if (!myImageManagers.containsKey(typeKey))
        {
            myImageManagers.put(typeKey, new ImageManager(typeKey, null));
        }

        ImageManager imageManager = myImageManagers.get(typeKey);

        boolean isNewGeom = false;
        if (!myGeometries.containsKey(typeKey))
        {
            isNewGeom = true;
            myGeometries.put(typeKey, createGeometry(model, imageManager, videoLayer));
        }

        TileGeometry geometry = myGeometries.get(typeKey);
        List<Geometry> removeGeoms = New.list();
        if (!geometry.getBounds().equals(model.getFootprint()))
        {
            removeGeoms.add(geometry);
            isNewGeom = true;
            geometry = createGeometry(model, imageManager, videoLayer);
            myGeometries.put(typeKey, geometry);
        }
        geometry.getRenderProperties().setOpacity(
                videoLayer.getBasicVisualizationInfo().getTypeOpacity() / (float)ColorUtilities.COLOR_COMPONENT_MAX_VALUE);

        List<Geometry> geometries = New.list();

        if (model instanceof PlatformMetadataAndImage)
        {           
            ByteArrayInputStream stream = new ByteArrayInputStream(((PlatformMetadataAndImage)model).getImageBytes().array());
            try
            {
                ImageIOImage image = ImageIOImage.read(stream);
                image = convertToArgb(image.getAWTImage());
                imageManager.addDirtyRegions(New.list(new ImageManager.DirtyRegion(0, image.getWidth(), 0, image.getHeight())));
                imageManager.setImageData(image);

                if (isNewGeom)
                {
                    geometries.add(geometry);
                }
            }
            catch (IOException e)
            {
                LOGGER.error(e, e);
            }
        }

        return new Pair<>(geometries, removeGeoms);
    }

    @Override
    public boolean cachePublishedGeometries()
    {
        return false;
    }

    /**
     * Closes any resources and returns the geometries that need to be removed.
     *
     * @return The geometries that need to be removed.
     */
    public List<Geometry> close()
    {
        return New.list(myGeometries.values());
    }

    /**
     * Converts the image to a an ARGB image for quicker drawing in the
     * {@link TextureReplacer}.
     *
     * @param imageToConvert The image to convert.
     * @return The converted image, created from the image pool.
     */
    private ImageIOImage convertToArgb(final BufferedImage imageToConvert)
    {
        BufferedImage image = new BufferedImage(imageToConvert.getWidth(), imageToConvert.getHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics graphics = image.getGraphics();
        graphics.drawImage(imageToConvert, 0, 0, null);

        ImageIOImage convertedImage = new ImageIOImage(image);

        convertedImage.setIsCacheByteBuffer(true);
        convertedImage.getByteBuffer();

        return convertedImage;
    }

    /**
     * Creates a new geometry to represent a new footprint.
     *
     * @param model The metadata of the uav.
     * @param imageManager The image manager for the layer.
     * @param videoLayer The video layer.
     * @return The created geometry.
     */
    private TileGeometry createGeometry(PlatformMetadata model, ImageManager imageManager, DataTypeInfo videoLayer)
    {
        TileGeometry.Builder<Position> tileBuilder = new TileGeometry.Builder<Position>();
        tileBuilder.setImageManager(imageManager);
        tileBuilder.setDivider(null);
        tileBuilder.setParent(null);
        tileBuilder.setRapidUpdate(true);
        tileBuilder.setBounds(model.getFootprint());

        OrderParticipantKey orderKey = videoLayer.getOrderKey();
        OrderManager orderManager = myOrderManager.getOrderManager(videoLayer.getOrderKey());
        TileRenderProperties props = new DefaultTileRenderProperties(orderManager.getOrder(orderKey), true, false);

        TileGeometry geometry = new TileGeometry(tileBuilder, props, null);

        return geometry;
    }
}
