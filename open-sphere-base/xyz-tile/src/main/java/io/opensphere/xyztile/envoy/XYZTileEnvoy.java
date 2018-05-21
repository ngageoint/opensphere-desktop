package io.opensphere.xyztile.envoy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.accessor.InputStreamAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageFormatUnknownException;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Envoy that goes out and gets tile images from a Mapbox server.
 */
public abstract class XYZTileEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(XYZTileEnvoy.class);

    /**
     * The accessor for getting the input stream for the raw image bytes. The
     * raw bytes for the image may be used directly from this stream.
     */
    private static final InputStreamAccessor<InputStream> IMAGE_STREAM_ACCESSOR = InputStreamAccessor
            .getHomogeneousAccessor(XYZTileUtils.IMAGE_PROPERTY_DESCRIPTOR);

    /**
     * Constructs a new tile envoy.
     *
     * @param toolbox The system toolbox.
     */
    public XYZTileEnvoy(Toolbox toolbox)
    {
        super(toolbox);
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfaction(DataModelCategory dataModelCategory,
            Collection<? extends IntervalPropertyValueSet> intervalSets)
    {
        return SingleSatisfaction.generateSatisfactions(intervalSets);
    }

    @Override
    public String getThreadPoolName()
    {
        return getClass().getSimpleName();
    }

    @Override
    public void open()
    {
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return XYZTileUtils.TILES_FAMILY.equals(category.getFamily()) && StringUtils.isNotEmpty(category.getSource())
                && StringUtils.isNotEmpty(category.getCategory());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws InterruptedException, QueryException
    {
        if (parameters.size() != 1 || !(parameters.get(0) instanceof ZYXKeyPropertyMatcher))
        {
            throw new IllegalArgumentException(ZYXKeyPropertyMatcher.class.getSimpleName() + " was not found in parameters.");
        }

        ZYXKeyPropertyMatcher param = (ZYXKeyPropertyMatcher)parameters.get(0);
        ZYXImageKey key = param.getImageKey();

        String urlString = buildImageUrlString(category, key);
        URL url = UrlUtilities.toURL(urlString);
        if (url == null)
        {
            throw new QueryException("Invalid tile URL: " + urlString);
        }

        try
        {
            HttpServer server = getToolbox().getServerProviderRegistry().getProvider(HttpServer.class).getServer(url);

            ResponseValues response = new ResponseValues();

            try (CancellableInputStream stream = server.sendGet(url, response))
            {
                if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    StreamReader reader = new StreamReader(stream);
                    ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
                    reader.copyStream(imageOut);
                    Image image = ImageIOImage.read(new ByteArrayInputStream(imageOut.toByteArray()), false);
                    DDSImage ddsImage = ((ImageIOImage)image).asDDSImage();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ObjectOutputStream output = new ObjectOutputStream(out);
                    output.writeObject(ddsImage);
                    InputStream ddsStream = new ByteArrayInputStream(out.toByteArray());

                    Collection<PropertyAccessor<InputStream, ?>> imageAccessors = New.collection();
                    imageAccessors.add(SerializableAccessor
                            .<InputStream, String>getSingletonAccessor(XYZTileUtils.KEY_PROPERTY_DESCRIPTOR, param.getOperand()));
                    imageAccessors.add(IMAGE_STREAM_ACCESSOR);

                    DefaultCacheDeposit<InputStream> imageDeposit = new DefaultCacheDeposit<>(category, imageAccessors,
                            New.list(ddsStream), true, getExpirationTime(category), false);

                    queryReceiver.receive(imageDeposit);
                }
                else if (response.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND)
                {
                    StreamReader errorMessageReader = new StreamReader(stream);
                    throw new QueryException(
                            url.toString() + " returned code " + response.getResponseCode() + " " + response.getResponseMessage()
                                    + " message " + errorMessageReader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
                }
                else if (LOGGER.isDebugEnabled())
                {
                    StreamReader errorMessageReader = new StreamReader(stream);
                    LOGGER.debug(
                            url.toString() + " returned code " + response.getResponseCode() + " " + response.getResponseMessage()
                                    + " message " + errorMessageReader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
                }
            }
        }
        catch (CacheException | ImageFormatUnknownException | IOException | URISyntaxException e)
        {
            throw new QueryException(e);
        }
    }

    /**
     * Tests the envoy connection by sending a ping (i.e., a single get request)
     * to the category's server.
     *
     * @param category Contains the server url information.
     * @return The result of the envoy ping
     */
    public boolean ping(DataModelCategory category)
    {
        boolean pingSuccess = false;

        ZYXImageKey key = new ZYXImageKey(0, 0, 0,
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(0., 45.), LatLonAlt.createFromDegrees(45., 90.)));
        String urlString = buildImageUrlString(category, key);
        URL url = UrlUtilities.toURL(urlString);

        HttpServer server = getToolbox().getServerProviderRegistry().getProvider(HttpServer.class).getServer(url);
        ResponseValues response = new ResponseValues();
        try (CancellableInputStream stream = server.sendGet(url, response))
        {
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                pingSuccess = true;
            }
        }
        catch (IOException | URISyntaxException e)
        {
            Notify.error("Failed to query " + category.getCategory() + " server: Could not establish connection during ping",
                    Method.TOAST);
        }

        return pingSuccess;
    }

    /**
     * Builds the url string in order to get the image for the given key.
     *
     * @param category Contains the server url information.
     * @param key The specific tile to get the image for.
     * @return The url string that will get the tile image.
     */
    protected abstract String buildImageUrlString(DataModelCategory category, ZYXImageKey key);

    /**
     * Gets the desired expiration time of the image deposits.
     *
     * @param category The layer we are depositing images for.
     * @return The expiration time of the image deposits.
     */
    protected Date getExpirationTime(DataModelCategory category)
    {
        return TimeInstant.get().plus(XYZTileUtils.TILE_EXPIRATION).toDate();
    }
}
