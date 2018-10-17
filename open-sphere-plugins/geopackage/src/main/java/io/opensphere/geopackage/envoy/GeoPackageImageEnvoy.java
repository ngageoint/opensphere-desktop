package io.opensphere.geopackage.envoy;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
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
import io.opensphere.core.image.Image;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.util.ImageEncoder;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Envoy that queries a specified geopackage file for tile images.
 */
public class GeoPackageImageEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /**
     * The accessor for getting the input stream for the raw image bytes. The
     * raw bytes for the image may be used directly from this stream.
     */
    private static final InputStreamAccessor<InputStream> IMAGE_STREAM_ACCESSOR = InputStreamAccessor
            .getHomogeneousAccessor(GeoPackagePropertyDescriptors.IMAGE_PROPERTY_DESCRIPTOR);

    /**
     * Used to encode the images for faster drawing.
     */
    private final ImageEncoder myEncoder = new ImageEncoder();

    /**
     * The geopackage file we are querying.
     */
    private final GeoPackage myGeoPackage;

    /**
     * Constructs a new GeoPackageImageEnvoy that queries for images in the
     * specified geopackage file.
     *
     * @param toolbox The system toolbox.
     * @param geoPackage The geopackage file to query.
     */
    public GeoPackageImageEnvoy(Toolbox toolbox, GeoPackage geoPackage)
    {
        super(toolbox);
        myGeoPackage = geoPackage;
    }

    @Override
    public void close()
    {
        myGeoPackage.close();
        super.close();
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
        return myGeoPackage.getPath().equals(category.getSource()) && StringUtils.isNotEmpty(category.getFamily())
                && Image.class.getName().equals(category.getCategory());
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

        String layer = category.getFamily();

        ZYXKeyPropertyMatcher param = (ZYXKeyPropertyMatcher)parameters.get(0);
        ZYXImageKey key = param.getImageKey();

        // Now figure out the x, y values for this tile by getting the layer's
        // total bounding box
        // and figuring out the matrix width and height at the tile's zoom
        // level.

        long zoomLevel = key.getZ();
        TileDao dao;
        synchronized (myGeoPackage)
        {
            dao = myGeoPackage.getTileDao(layer);
        }

        long column = key.getX();
        long row = key.getY();

        TileRow tileRow = dao.queryForTile(column, row, zoomLevel);

        if (tileRow != null && tileRow.getTileData() != null)
        {
            InputStream imageData = myEncoder.encodeImage(tileRow.getTileData());

            DataModelCategory imageCategory = new DataModelCategory(category.getSource(), layer, Image.class.getName());
            Collection<PropertyAccessor<InputStream, ?>> imageAccessors = New.collection();
            imageAccessors.add(SerializableAccessor.<InputStream, String>getSingletonAccessor(
                    GeoPackagePropertyDescriptors.KEY_PROPERTY_DESCRIPTOR, param.getOperand()));
            imageAccessors.add(IMAGE_STREAM_ACCESSOR);

            DefaultCacheDeposit<InputStream> imageDeposit = new DefaultCacheDeposit<>(imageCategory, imageAccessors,
                    New.list(imageData), true, CacheDeposit.SESSION_END, false);

            try
            {
                queryReceiver.receive(imageDeposit);
            }
            catch (CacheException e)
            {
                throw new QueryException(e);
            }
        }
    }
}
