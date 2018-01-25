package io.opensphere.wms;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.MimeType;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.layer.WMSDataTypeInfo;
import io.opensphere.wms.layer.WMSLayer;

/**
 * Envoy that retrieves WMS layers from the file system.
 */
public class FileWMSEnvoy extends AbstractEnvoy
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FileWMSEnvoy.class);

    /** The base directory containing the WMS layers. */
    private final String myBaseDir;

    /** The file extension for the image files. */
    private final String myFileExtension;

    /** An image provider implementation. */
    private final ImageProvider<LevelRowCol> myFileSystemImageProvider = new ImageProvider<LevelRowCol>()
    {
        @Override
        public Image getImage(LevelRowCol coords)
        {
            try
            {
                URL url = getImageURL(coords);
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Loading image: " + url);
                }
                if (url.getProtocol().equals("file") && url.getPath().endsWith(".dds"))
                {
                    return new DDSImage(new File(url.toURI()));
                }
                else
                {
                    ImageIOImage img = new ImageIOImage(ImageIO.read(url));
                    return img;
                }
            }
            catch (IOException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Failed to read image for coords [" + coords + "]: " + e);
                }
                return null;
            }
            catch (URISyntaxException e)
            {
                LOGGER.error("Failed to get URI for image: " + e, e);
                return null;
            }
        }
    };

    /**
     * Construct the envoy.
     *
     * @param toolbox The toolbox.
     * @param baseDir The base directory containing the image files.
     * @param fileExtension The file extension of the image files.
     */
    public FileWMSEnvoy(Toolbox toolbox, String baseDir, String fileExtension)
    {
        super(toolbox);
        myBaseDir = baseDir;
        myFileExtension = fileExtension;
    }

    /**
     * Construct the envoy.
     *
     * @param toolbox The toolbox.
     */
    protected FileWMSEnvoy(Toolbox toolbox)
    {
        super(toolbox);
        myBaseDir = "";
        myFileExtension = "";
    }

    @Override
    public void open()
    {
        // TODO read from configuration
        final int numLayers = 5;
        final float initialSize = 45f;

        // TODO configuration should be read, not created
        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerTitle("BlueMarble");
        String layerName = FileWMSLayer.class.getName() + ":" + getBaseDir();
        conf.setLayerName(layerName);
        conf.setLayerKey(layerName);
        conf.setCacheImageFormat(MimeType.DDS.getMimeType());

        WMSDataTypeInfo info = new WMSDataTypeInfo(getToolbox(), (Preferences)null, "FileWMS",
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());
        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLRCImageProvider(myFileSystemImageProvider);
        builder.setLayerCellDimensions(createLayerCellDimensions(numLayers, initialSize));
        WMSLayer blueMarbleLayer = new FileWMSLayer(builder);

        String source = FileWMSEnvoy.class.getName() + ":" + myBaseDir;
        String family = WMSLayer.class.getName();
        String category = "";
        Date expiration = CacheDeposit.SESSION_END;

        Collection<? extends PropertyAccessor<WMSLayer, ?>> accessors = Collections
                .singleton(UnserializableAccessor.getHomogeneousAccessor(WMSLayer.PROPERTY_DESCRIPTOR));
        CacheDeposit<? extends WMSLayer> deposit = new DefaultCacheDeposit<WMSLayer>(
                new DataModelCategory(source, family, category), accessors, Collections.singleton(blueMarbleLayer), true,
                expiration, true);
        getDataRegistry().addModels(deposit);
    }

    @Override
    public void setFilter(Object filter)
    {
    }

    /**
     * Get the base directory containing the level directories for the layer.
     *
     * @return The base directory as a string.
     */
    protected String getBaseDir()
    {
        return myBaseDir;
    }

    /**
     * Get the file extension for the image files.
     *
     * @return The file extension.
     */
    protected String getFileExtension()
    {
        return myFileExtension;
    }

    /**
     * Accessor for the file system image provider.
     *
     * @return The image provider.
     */
    protected ImageProvider<LevelRowCol> getFileSystemImageProvider()
    {
        return myFileSystemImageProvider;
    }

    /**
     * Get the image provider.
     *
     * @return The image provider.
     */
    protected ImageProvider<LevelRowCol> getImageProvider()
    {
        return myFileSystemImageProvider;
    }

    /**
     * Get the URL for the image matching some level/row/col coordinates.
     *
     * @param coords The coordinates.
     * @return The path as a string.
     * @throws MalformedURLException If there is a problem constructing the URL.
     */
    protected URL getImageURL(LevelRowCol coords) throws MalformedURLException
    {
        StringBuilder pathname = new StringBuilder(getBaseDir());
        pathname.append(File.separator).append(coords.getLevel()).append(File.separator).append(coords.getRow())
                .append(File.separator).append(coords.getRow()).append('_').append(coords.getCol()).append(getFileExtension());
        return new File(pathname.toString()).toURI().toURL();
    }

    /**
     * Create a list of lat/lon coordinates that define how large the cells are
     * in each layer's grid.
     *
     * @param numLayers The number of layers.
     * @param levelZeroSizeD The size of the zero-level cells.
     * @return A list of sizes, starting with level 0.
     */
    private List<Vector2d> createLayerCellDimensions(int numLayers, float levelZeroSizeD)
    {
        List<Vector2d> layerCellDimensions = new ArrayList<>(numLayers);
        for (int i = 0; i < numLayers; i++)
        {
            float cellSize = levelZeroSizeD / (1 << i);
            layerCellDimensions.add(new Vector2d(cellSize, cellSize));
        }
        return layerCellDimensions;
    }
}
