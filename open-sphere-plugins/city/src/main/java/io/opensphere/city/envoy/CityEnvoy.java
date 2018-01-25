package io.opensphere.city.envoy;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.city.model.json.Response;
import io.opensphere.city.model.json.Result;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.SimpleEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.server.util.JsonUtils;

/** CyberCity 3D envoy. */
public class CityEnvoy extends SimpleEnvoy<Result>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CityEnvoy.class);

    /** The {@link PropertyDescriptor} for the object results. */
    private static final PropertyDescriptor<Result> OBJECTS_DESCRIPTOR = new PropertyDescriptor<>("Objects", Result.class);

    /** The objects data model category family. */
    private static final String OBJECTS_FAMILY = "City.Objects";

    /** The URL cache path. */
    private final Path myCachePath;

    /** The executor. */
    private final ExecutorService myExecutor = ThreadUtilities
            .newTerminatingFixedThreadPool(new NamedThreadFactory("Collada-Downloader"), 2);

    /**
     * Helper method for a client to query this envoy.
     *
     * @param dataRegistry the data registry
     * @param baseUrl the base URL to query
     * @param bbox the bounding box
     * @return the layer info
     * @throws QueryException if something goes wrong with the query
     */
    public static Collection<Result> query(DataRegistry dataRegistry, String baseUrl, GeographicBoundingBox bbox)
        throws QueryException
    {
        final DataModelCategory category = new DataModelCategory(null, OBJECTS_FAMILY, baseUrl);
        final Polygon polygon = JTSUtilities.createPolygonFromBounds(new GeometryFactory(), bbox.getMinLonD(), bbox.getMaxLonD(),
                bbox.getMinLatD(), bbox.getMaxLatD());
        final GeometryMatcher geomMatcher = new GeometryMatcher(GeometryAccessor.GEOMETRY_PROPERTY_NAME,
                GeometryMatcher.OperatorType.EQUALS, polygon);

        final SimpleQuery<Result> query = new SimpleQuery<>(category, OBJECTS_DESCRIPTOR, New.list(geomMatcher));
        final QueryTracker tracker = dataRegistry.performQuery(query);
        if (query.getResults() != null && !query.getResults().isEmpty())
        {
            int index = 0;
            final List<Result> results = New.list();
            for (final Result result : query.getResults())
            {
                if (index < tracker.getIds().length)
                {
                    result.setDataRegistryId(tracker.getIds()[index]);
                    results.add(result);
                    index++;
                }
            }
            return results;
        }
        else if (tracker.getException() != null)
        {
            throw new QueryException(tracker.getException().getMessage(), tracker.getException());
        }
        else
        {
            return New.collection();
        }
    }

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public CityEnvoy(Toolbox toolbox)
    {
        super(toolbox);
        final String runtimeDir = StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"), System.getProperties());
        myCachePath = Paths.get(runtimeDir, "urlCache");
    }

    /**
     * Gets the file system path for the URL.
     *
     * @param url the URL
     * @return the path
     */
    private Path getPath(URL url)
    {
        final String server = StringUtilities.replaceSpecialCharacters(url.getHost());
        String file = StringUtilities.replaceSpecialCharacters(url.getFile());
        file = file.replace("key_a5014122-a04a-52ba-be0d-8a06c85b5fab_token_63fcdf1fdc4800e8d423eedf8e0cfb57e3fd1b85_", "");
        if (file.length() > 250)
        {
            file = file.substring(0, 250);
        }
        return Paths.get(myCachePath.toString(), server, file + ".dat");
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return OBJECTS_FAMILY.equals(category.getFamily());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws InterruptedException, QueryException
    {
        for (final Satisfaction sat : satisfactions)
        {
            final IntervalPropertyValueSet valueSet = sat.getIntervalPropertyValueSet();
            final Collection<? extends Geometry> geometries = valueSet.getValues(GeometryAccessor.PROPERTY_DESCRIPTOR);
            for (final Geometry geometry : geometries)
            {
                try
                {
                    final URL url = getUrl(category, geometry);
                    try (CancellableInputStream inputStream = sendGet(url))
                    {
                        final Collection<Result> items = parseDepositItems(inputStream);
                        if (!items.isEmpty())
                        {
                            final CacheDeposit<Result> deposit = createDeposit(category, items);
                            queryReceiver.receive(deposit);
                        }
                    }
                }
                catch (IOException | CacheException e)
                {
                    throw new QueryException(e);
                }
            }
        }
    }

    @Override
    protected URL getUrl(DataModelCategory category) throws MalformedURLException
    {
        // Not used
        return null;
    }

    @Override
    protected Collection<Result> parseDepositItems(CancellableInputStream inputStream) throws IOException
    {
        final List<Result> results = New.list();
        if (inputStream != null && inputStream.available() > 0)
        {
            final Response response = JsonUtils.createMapper().readValue(inputStream, Response.class);
            results.addAll(response.getResults());
            final List<Callable<Void>> tasks = New.list(results.size());
            for (final Result result : results)
            {
                tasks.add(new Callable<Void>()
                {
                    @Override
                    public Void call()
                    {
                        try
                        {
                            queryAndPopulateColladaStream(result);
                        }
                        catch (MalformedURLException | QueryException e)
                        {
                            LOGGER.error(e, e);
                        }
                        return null;
                    }
                });
            }
            try
            {
                myExecutor.invokeAll(tasks);
            }
            catch (final InterruptedException e)
            {
                LOGGER.error(e, e);
            }
        }
        return results;
    }

    @Override
    protected CacheDeposit<Result> createDeposit(DataModelCategory category, Collection<? extends Result> items)
    {
        return new DefaultCacheDeposit<>(category.withSource(getClass().getName()),
                Collections.singleton(UnserializableAccessor.getHomogeneousAccessor(OBJECTS_DESCRIPTOR)), items, true,
                CacheDeposit.SESSION_END, false);
    }

    /**
     * Gets the URL.
     *
     * @param category the data model category
     * @param geometry the geometry
     * @return the URL
     * @throws MalformedURLException if the URL is malformed
     */
    private URL getUrl(DataModelCategory category, Geometry geometry) throws MalformedURLException
    {
        final Point centroid = geometry.getCentroid();
        final StringBuilder sb = new StringBuilder(category.getCategory());
        sb.append("&latitude=").append(centroid.getY());
        sb.append("&longitude=").append(centroid.getX());
        sb.append("&radius=").append(getRadiusKm(geometry));
        return new URL(sb.toString());
    }

    /* (non-Javadoc)
     *
     * @see io.opensphere.core.api.adapter.SimpleEnvoy#sendGet(java.net.URL) */
    @Override
    protected CancellableInputStream sendGet(URL url) throws QueryException
    {
        CancellableInputStream responseStream = null;
        final Path path = getPath(url);
        try
        {
            if (Files.exists(path))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.info("Playback " + path);
                }
                responseStream = new CancellableInputStream(new BufferedInputStream(Files.newInputStream(path)), () ->
                {
                });
            }
//            else
//            {
//                responseStream = super.sendGet(url);
//
//                LOGGER.info("Record " + path);
//                Path parent = path.getParent();
//                if (parent != null)
//                {
//                    Files.createDirectories(parent);
//                }
//                try (OutputStream fileStream = new BufferedOutputStream(Files.newOutputStream(path)))
//                {
//                    InputStream copyStream = new StreamReader(responseStream).copyStream(fileStream);
//                    responseStream = new CancellableInputStream(copyStream, () ->
//                    {
//                    });
//                }
//            }
        }
        catch (final ClosedByInterruptException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
        }
        catch (final IOException e)
        {
            throw new QueryException(e);
        }

        return responseStream;
    }

    /**
     * Queries the result's COLLADA URL and populates the result with the input
     * stream.
     *
     * @param result the result
     * @throws MalformedURLException if the URL is bad
     * @throws QueryException if there was a problem reading from the URL
     */
    private void queryAndPopulateColladaStream(Result result) throws MalformedURLException, QueryException
    {
        try (CancellableInputStream colladaContent = sendGet(new URL(result.getDaeDownloadUrl())))
        {
            final StreamReader reader = new StreamReader(colladaContent);
            final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            reader.copyStream(outstream);
            result.setColladaContent(new ByteArrayInputStream(outstream.toByteArray()));
        }
        catch (final ClosedByInterruptException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
        }
        catch (final IOException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Calculates the radius of the circle within the bounding box of the
     * geometry.
     *
     * @param geometry the geometry
     * @return the distance in kilometers
     */
    private static double getRadiusKm(Geometry geometry)
    {
        final Coordinate[] coordinates = geometry.getCoordinates();
        final double maxLat = Arrays.stream(coordinates).mapToDouble(c -> c.y).max().orElse(0);
        final double minLat = Arrays.stream(coordinates).mapToDouble(c -> c.y).min().orElse(0);
        final LatLonAlt p1 = LatLonAlt.createFromDegrees(maxLat, 0);
        final LatLonAlt p2 = LatLonAlt.createFromDegrees(minLat, 0);
        final double radiusKm = GeographicBody3D.greatCircleDistanceM(p1, p2, WGS84EarthConstants.RADIUS_MEAN_M)
                / (Constants.UNIT_PER_KILO * 2);
        return radiusKm;
    }
}
